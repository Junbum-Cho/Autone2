package com.example.autone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class Report_progress_screen extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder;
    private String fileName = null;
    private String audioFileUrl = "";
    private Vibrator vibrator;
    private String caseId = null;
    private boolean isCancelled = false;
    private boolean promptReceived = false;
    private String gptPrompt = null;
    private Location userLocation = null;
    private String emergencyCenterAddress = "";
    private String gptPromptForSMS = "";
    private String keyword;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();
    FirebaseAuth firebaseAuth;

    Timer timer;
    TextView textView;
    TextView nearestCenterTextView;

    public static  final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client;

    private static final String MY_SECRET_KEY = "API_KEY";
    private static final String KAKAO_API_KEY = "API_KEY"; // Add your Kakao API key here

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void storeDataToFirebase(String prompt, String keyword, double latitude, double longitude, String audioFileUrl) {
        if (isCancelled || !promptReceived || userLocation == null || audioFileUrl.isEmpty()) return; // Skip storing data if cancelled

        String currentDateTime = getCurrentDateTime();

        // Round latitude and longitude to 3 decimal places
        double roundedLatitude = Math.round(latitude * 1000.0) / 1000.0;
        double roundedLongitude = Math.round(longitude * 1000.0) / 1000.0;

        // Format the location string
        String locationString = "Lat: " + roundedLatitude + ", Lon: " + roundedLongitude;

        // Using Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("prompt", prompt);
        data.put("timestamp", currentDateTime);
        data.put("location", locationString);
        data.put("recording", audioFileUrl);
        data.put("emergency_center_location", emergencyCenterAddress); // Add emergency center address
        db.collection("prompts").add(data)
                .addOnSuccessListener(documentReference -> {
                    if (!isCancelled) {
                        Toast.makeText(getApplicationContext(), "Success to save today's date", Toast.LENGTH_LONG).show();
                        System.out.print("Success!");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isCancelled) {
                        Toast.makeText(getApplicationContext(), "Failed to save today's date", Toast.LENGTH_LONG).show();
                        System.out.print("Fail..");
                    }
                });

        // Using Firebase Realtime Database
        String uid = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "unknown_user";
        DatabaseReference myRef = database.getReference("Case Reports").child(uid).child(caseId);
        myRef.child("prompt").setValue(prompt);
        myRef.child("timestamp").setValue(currentDateTime);
        myRef.child("location").setValue(locationString);
        myRef.child("recording").setValue(audioFileUrl);
        myRef.child("emergency_center_location").setValue(emergencyCenterAddress); // Add emergency center address

        // Get city name and save to RealTime Crime branch
        getCityNameAndStoreData(keyword, roundedLatitude, roundedLongitude, locationString, currentDateTime);
    }

    private void getCityNameAndStoreData(String keyword, double latitude, double longitude, String locationString, String timestamp) {
        String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + longitude + "&y=" + latitude;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "KakaoAK " + KAKAO_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (!isCancelled) {
                        Toast.makeText(getApplicationContext(), "Failed to get city name: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && !isCancelled) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray documents = jsonObject.getJSONArray("documents");
                        if (documents.length() > 0) {
                            JSONObject regionInfo = documents.getJSONObject(0);
                            String cityName = regionInfo.getString("region_2depth_name");

                            firebaseAuth = FirebaseAuth.getInstance();
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            // Save to RealTime Crime branch
                            DatabaseReference realTimeCrimeRef = database.getReference("RealTime Crime").child(cityName).push();
                            realTimeCrimeRef.child("userUid").setValue(uid);
                            realTimeCrimeRef.child("Crime Type").setValue(keyword);
                            realTimeCrimeRef.child("Location").setValue(locationString);
                            realTimeCrimeRef.child("timestamp").setValue(timestamp);
                        } else {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "No city name found for location.", Toast.LENGTH_LONG).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to parse city name response.", Toast.LENGTH_LONG).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to get city name.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_progress_screen);

        Intent intent = getIntent();
        keyword = intent.getStringExtra("detectedClassName");

        if (keyword == null) {
            keyword = "알 수 없는 범죄 유형";
        }

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        //Vibrator Initialize
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        nearestCenterTextView = findViewById(R.id.textView69);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
        } else {
            vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Run this part on the UI thread since it interacts with UI components
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isCancelled && !isFinishing() && !isDestroyed() && hasWindowFocus()){
                            Intent intent = new Intent (Report_progress_screen.this, Main_screen.class);
                            startActivity(intent);
                            vibrator.cancel();
                            finish();
                        }
                    }
                });
            }
        }, 15000);

        Button previousbtn = findViewById(R.id.button12);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                isCancelled = true; // Mark the task as cancelled
                stopRecording();
                timer.cancel(); // Cancel the timer
                vibrator.cancel();

                Intent intent = new Intent(getApplicationContext(), Main_screen.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });

        textView = findViewById(R.id.textView72);

        client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // Set the file path for the audio recording
        fileName = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/audiorecordtest.3gp";
        getCurrentLocation();
    }

    private static final int PERMISSIONS_REQUEST_SEND_SMS = 1;
    public void SmsSend(String strPhoneNumber, String strMsg) {
        // Check if the SEND_SMS permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // If not granted, request the SEND_SMS permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // Permission is already granted, proceed with sending the SMS
            sendSMSMessage(strPhoneNumber, strMsg);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the SMS
                sendSMSMessage("911", gptPromptForSMS);
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSMSReport() {
        // Ensure both the GPT prompt and the emergency center address are available
        if (gptPromptForSMS == null || gptPromptForSMS.isEmpty()) {
            Toast.makeText(this, "GPT prompt not generated yet", Toast.LENGTH_LONG).show();
            return;
        }
        if (emergencyCenterAddress == null || emergencyCenterAddress.isEmpty()) {
            Toast.makeText(this, "Emergency center not found yet", Toast.LENGTH_LONG).show();
            return;
        }

        // Construct the report message with the GPT prompt
        String reportMessage = gptPromptForSMS + "\nNearest Emergency Center: " + emergencyCenterAddress;

        // Replace with a test number or real emergency number
        String testPhoneNumber = "911";

        // Check if the permission is granted or request it before sending
        System.out.println(testPhoneNumber);
        sendSMSMessage(testPhoneNumber, reportMessage);
    }
    private static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    private static final int RESULT_ERROR_NO_SERVICE = 4;
    private static final int RESULT_ERROR_NULL_PDU = 3;
    private static final int RESULT_ERROR_RADIO_OFF = 2;

    private void sendSMSMessage(String phoneNumber, String message) {
        // 브로드캐스트 리시버 등록
        BroadcastReceiver sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d("SMS", "SMS 전송 성공");
                        break;
                    case RESULT_ERROR_GENERIC_FAILURE:
                        Log.e("SMS", "SMS 전송 실패: 일반적인 오류");
                        break;
                    case RESULT_ERROR_NO_SERVICE:
                        Log.e("SMS", "SMS 전송 실패: 서비스 없음");
                        break;
                    case RESULT_ERROR_NULL_PDU:
                        Log.e("SMS", "SMS 전송 실패: PDU 실패");
                        break;
                    case RESULT_ERROR_RADIO_OFF:
                        Log.e("SMS", "SMS 전송 실패: 라디오 꺼짐");
                        break;
                    default:
                        Log.e("SMS", "SMS 전송 실패: 알 수 없는 오류");
                        break;
                }
                unregisterReceiver(this);
            }
        };

        BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("SMS", "SMS 전달 완료");
                unregisterReceiver(this);
            }
        };

        // 리시버 등록
        registerReceiver(sentReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredReceiver, new IntentFilter("SMS_DELIVERED"));

        try {
            SmsManager smsManager = SmsManager.getDefault();

            ArrayList<String> parts = smsManager.divideMessage(message);

            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();

            for (int i = 0; i < parts.size(); i++) {
                PendingIntent sentIntent = PendingIntent.getBroadcast(this, i,
                        new Intent("SMS_SENT_" + i), PendingIntent.FLAG_IMMUTABLE);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, i,
                        new Intent("SMS_DELIVERED_" + i), PendingIntent.FLAG_IMMUTABLE);

                sentIntents.add(sentIntent);
                deliveredIntents.add(deliveredIntent);
            }

            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveredIntents);

            Log.d("SMS", "SMS 전송 시도 중...");
        } catch (Exception ex) {
            Log.e("SMS", "SMS 전송 실패: " + ex.getMessage());
        }
    }
    private void checkAndSendSMSReport() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!gptPromptForSMS.isEmpty() && emergencyCenterAddress != null) {
                    System.out.println("1111222");
                    sendSMSReport();
                }
                else{
                    System.out.println("1111222333333");
                }
            }
        }, 10000); // 10초 후 실행

    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if (isCancelled) return; // Skip if cancelled

                    userLocation = location; // Save the location
                    createCaseId();
                    startRecording();
                    fetchUserDataAndSendRequest(keyword, location);
                    locationManager.removeUpdates(this);  // Stop updates after getting the location

                    // Find the nearest emergency center
                    findNearestEmergencyCenter(location);
                }
            });
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void findNearestEmergencyCenter(Location userLocation) {
        double latitude = userLocation.getLatitude();
        double longitude = userLocation.getLongitude();
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?y=" + latitude
                + "&x=" + longitude + "&radius=" + 5000 + "&size=1&query=파출소";


        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "KakaoAK " + KAKAO_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (!isCancelled) {
                        Toast.makeText(getApplicationContext(), "가까운 파출소 탐색 실패.. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && !isCancelled) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray documents = jsonObject.getJSONArray("documents");
                        if (documents.length() > 0) {

                            JSONObject nearestCenter = documents.getJSONObject(0);
                            emergencyCenterAddress = nearestCenter.getString("address_name");
                            runOnUiThread(() -> nearestCenterTextView.setText(emergencyCenterAddress));

                            checkAndSendSMSReport();

                        } else {
                            runOnUiThread(() -> nearestCenterTextView.setText("No emergency centers found nearby."));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to parse response.", Toast.LENGTH_LONG).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to get nearest center.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void createCaseId() {
        String uid = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "unknown_user";
        DatabaseReference myRef = database.getReference("Case Reports").child(uid);
        caseId = myRef.push().getKey();  // Generate a unique case ID
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        // Stop recording after 10 seconds
        new Timer().schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (!isCancelled) {
                    stopRecording();
                }
            }
        }, 10000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;

                // Verify the file exists and has content
                File audioFile = new File(fileName);
                if (audioFile.exists() && audioFile.length() > 0) {
                    if (!isCancelled) {
                        uploadAudioToFirebase(fileName);
                    }
                } else {
                    if (!isCancelled) {
                        Toast.makeText(getApplicationContext(), "녹음 실패.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadAudioToFirebase(String filePath) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference audioRef = storageRef.child("audio/" + file.getLastPathSegment());
        UploadTask uploadTask = audioRef.putFile(file);

        uploadTask.addOnSuccessListener(taskSnapshot -> audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (!isCancelled) {
                audioFileUrl = uri.toString();
                // Use the audio file URL to store in Firebase Database
                storeDataToFirebase(gptPrompt, keyword, userLocation.getLatitude(), userLocation.getLongitude(), audioFileUrl);
            }
        })).addOnFailureListener(e -> {
            if (!isCancelled) {
                Toast.makeText(getApplicationContext(), "녹음 업로딩 실패.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDataAndSendRequest(String keyword, Location location) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("User").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isCancelled) return; // Skip if cancelled
                String name = dataSnapshot.child("Name").
                        getValue(String.class);
                String dob = dataSnapshot.child("Date_of_Birth").
                        getValue(String.class);
                String address = dataSnapshot.child("Address").
                        getValue(String.class);
                sendGPTRequest(keyword, location, name, dob, address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isCancelled) {
                    popupToast("유저 데이터 불러오기 실패함.");
                }
            }
        });
    }

    void editView(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isCancelled) {
                    textView.setText(": " + message);
                }
            }
        });
    }

    void popupToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isCancelled) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void sendGPTRequest(String keyword, Location location, String name, String dob, String address) {
        if (isCancelled) return; // Skip if cancelled

        double roundedLatitude = Math.round(location.getLatitude() * 1000.0) / 1000.0;
        double roundedLongitude = Math.round(location.getLongitude() * 1000.0) / 1000.0;

        JSONArray arr = new JSONArray();
        JSONObject baseAi = new JSONObject();
        JSONObject userMsg = new JSONObject();
        try {
            baseAi.put("role", "system");
            baseAi.put("content", "당신의 역할은 범죄 긴급 신고앱(Autone)의 프롬트 작성자입니다. 당신은 사용자의 안전을 지킬 의무가 있으며, 이를 근처의 " +
                    "가까운 파출소에 전달할 신고 프롬트를 작성하여 성취해야 합니다. 지금 앱의 사용자가 범죄에 노출되었습니다. 해당 범죄는 " + keyword + "을(를) 동반했으며 대처가 필요합니다." +
                    " 앱의 사용자의 안전을 지키기 위해 사용자의 신원과 위치와 지금 일어나는 범죄 특성을 포함하여 빠른 대응이 이루어지기를 촉구하는 효과적이고 명료한 신고 프롬트 (메시지 형태로) 를 작성하십시오. " +
                    "사용자 신원: " + name + ", 사용자 생년월일: " + dob + ", 사용자 현재 위치 GPS (=범죄가 일어나고 있는 위치): " + location + "중요: 문자 앞에 이러한 문구를 포합하시오:" +
                    "'이는 범죄 신고앱 Autone에서 제작한 긴급 신고앱입니다:' 더 나아가, 잎서 명시한 사용자 개인정보와 모든 범죄정보를 포함하시오.");

            userMsg.put("role", "user");
            userMsg.put("content", keyword);

            arr.put(baseAi);
            arr.put(userMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-4o");
            object.put("messages", arr);

        } catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer "+MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isCancelled) {
                            Toast.makeText(getApplicationContext(), "Failed to load response due to " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && !isCancelled) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");

                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        editView(result);
                        gptPrompt = result;
                        gptPromptForSMS = result;
                        promptReceived = true;

                        // Store the prompt and location data to Firebase after the audio file URL is available
                        storeDataToFirebase(result, keyword, roundedLatitude, roundedLongitude, audioFileUrl);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!isCancelled) {
                        popupToast("GPT-4o 에러");
                    }
                }
            }
        });
    }
}
