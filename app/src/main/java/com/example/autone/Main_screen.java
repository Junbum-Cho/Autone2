package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// STT
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.widget.Toast;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioFormat;

import java.util.concurrent.atomic.AtomicBoolean; // Added import

public class Main_screen extends AppCompatActivity implements mCurrentLocationEventListener, mMapViewEventListener, SoundClassifierHelper.SoundClassifierListener{
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private CrimeDataCleaner crimeDataCleaner; // Add this line
    private Vibrator vibrator;
    static HashMap<String, User> userList;

    public static class User {
        String name, key, birth, gender, address, email;
    }

    private short[] buffer = new short[SAMPLE_RATE * AUDIO_DURATION_SECONDS];
    private List<float[]> inputBatch = new ArrayList<>(); // List to accumulate batches for 32 inputs
    private static final String TAG = "MainActivity";
    private static final int SAMPLE_RATE = 16000;
    private AudioRecord audioRecord;
    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int AUDIO_DURATION_SECONDS = 7;
    private SoundClassifierHelper soundClassifierHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        crimeDataCleaner = new CrimeDataCleaner(); // Initialize the CrimeDataCleaner

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 200);
        }

        // Initialize SoundClassifierHelper
        soundClassifierHelper = SoundClassifierHelper.getInstance(this);
        soundClassifierHelper.setListener(this);
        //soundClassifierHelper.startRecordingWithSlidingWindow();

        // Vibrator Initialize
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Start the shake detection service
        Intent shakeServiceIntent = new Intent(this, ShakeDetectionService.class);
        ContextCompat.startForegroundService(this, shakeServiceIntent);

        // map 키해시
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한을 물어본다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return;
        }

        mapViewContainer = findViewById(R.id.map_view_main);
        mapView = new MapView(this);
        mapViewContainer.addView(mapView);

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationEventListener(this);

        setupButtons();
    }
    // End of onCreate method

    @Override
    public void onSoundDetected(String detectedClassName, float confidence) {
        // Your implementation here
        runOnUiThread(() -> {
            //startVibration();
            Intent intent = new Intent(getApplicationContext(), Report_progress_screen.class);
            intent.putExtra("detectedClassName", detectedClassName);
            startActivity(intent);
            soundClassifierHelper.stopRecording();
            Log.d(TAG, "Crime Detected " + detectedClassName + " with confidence: " + confidence);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRecording", soundClassifierHelper.isRecording());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean wasRecording = savedInstanceState.getBoolean("isRecording", true);
        if (wasRecording) {
            soundClassifierHelper.startRecordingWithSlidingWindow();
        }
    }

    // Stop recording audio when necessary
    private void stopRecording() {
        if (audioRecord != null && isRecording.get()) { // Use AtomicBoolean's get()
            isRecording.set(false); // Setting to false to stop the thread
            // The thread will handle stopping and releasing audioRecord
        }
    }

    private void startVibration(){
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null && mapView.getParent() != null) {
            ((ViewGroup) mapView.getParent()).removeView(mapView);
            mapViewContainer.addView(mapView);
            mapView.removeAllPOIItems();
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null && mapViewContainer != null && mapView.getParent() != null) {
            mapView.onPause();
            ((ViewGroup) mapView.getParent()).removeView(mapView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapViewContainer != null && mapView.getParent() != null) {
            ((ViewGroup) mapView.getParent()).removeView(mapView);
        }
        mapViewContainer = null;
        mapView = null;
        soundClassifierHelper.stopRecording();
    }

    private boolean timerFlag = false;
    private ProgressDialog mDialog;

    public void updateData() {
        Log.d("Main_screen", "updateData");
        mDialog = new ProgressDialog(Main_screen.this);
        mDialog.setMessage("Processing...");
        mDialog.show();

        userList = new HashMap<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    User user = new User();
                    user.key = ds.getKey();
                    user.birth = ds.child("Date_of_Birth").getValue().toString();
                    user.name = ds.child("Name").getValue().toString();
                    user.gender = ds.child("Gender").getValue().toString();
                    user.address = ds.child("Address").getValue().toString();
                    user.email = ds.child("Email").getValue().toString();
                    userList.put(user.key, user);
                }
                mDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean allGranted = true;

            // Check if all permissions are granted
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            // If any permission is denied, close the activity
            if (!allGranted) {
                Toast.makeText(this, "Required permissions are denied. Closing the app.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setupButtons(){
        ImageView reportBtn = findViewById(R.id.imageView83);
        reportBtn.setOnTouchListener(new View.OnTouchListener() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private static final int LONG_PRESS_DURATION = 3000; // 3 seconds
            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    // Trigger the long press action for reporting
                    Intent intent = new Intent(getApplicationContext(), Report_progress_screen.class);
                    startActivity(intent);
                    //startVibration();
                    soundClassifierHelper.stopRecording();  // Stop recording when long press triggers report
                }
            };
            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressStartTime = System.currentTimeMillis();
                        // Schedule the long press action after 3 seconds
                        handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                        return true; // Indicate that we are handling the touch

                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        // Remove the long press action if touch is released
                        handler.removeCallbacks(longPressRunnable);

                        if (pressDuration < LONG_PRESS_DURATION) {
                            // Short press detected, perform toggle action for audio recording
                            if (soundClassifierHelper.isRecording()) {
                                soundClassifierHelper.stopRecording();
                                Toast.makeText(Main_screen.this, "소리 탐지 기능 비활성화됨.", Toast.LENGTH_SHORT).show();
                            } else {
                                // If not recording, start it
                                soundClassifierHelper.startRecordingWithSlidingWindow();
                                Toast.makeText(Main_screen.this, "소리 탐지 기능 활성화됨.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        return true; // Indicate that we handled the touch

                    case MotionEvent.ACTION_CANCEL:
                        // Touch event was canceled, remove any pending actions
                        handler.removeCallbacks(longPressRunnable);
                        return false;
                }
                return false;
            }
        });

        ImageView profilebtn = findViewById(R.id.imageView76);
        profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), User_profile.class);
                startActivity(intent);
            }
        });

        ImageView ringbtn = findViewById(R.id.imageView81);
        ringbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Notification_Center_Crime.class);
                startActivity(intent);
            }
        });

        Button nearbycrime = findViewById(R.id.button22222);
        nearbycrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Map_main_screen.class);
                startActivity(intent);
            }
        });

        Button nearbyemergency = findViewById(R.id.button14);
        nearbyemergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Map_main_screen.class);
                startActivity(intent);
            }
        });

        Button addwifitag = findViewById(R.id.button16);
        addwifitag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent); // 그냥 여기서 바로 태그 추가 화면으로 넘어가기
            }
        });

        ImageView changesignal = findViewById(R.id.imageView82);
        changesignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), voice_recognition_settings.class);
                startActivity(intent);
            }
        });

        ImageView adjustvolume = findViewById(R.id.imageView84);
        adjustvolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Adjust_voice_volume.class);
                startActivity(intent);
            }
        });

        ImageView openfile = findViewById(R.id.imageView85);
        openfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Statistics_and_Files.class);
                startActivity(intent);
            }
        });

        ImageView openfriendslist = findViewById(R.id.imageView86);
        openfriendslist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Friends_List.class);
                startActivity(intent);
            }
        });

        ImageView searchlocation = findViewById(R.id.imageView87);
        searchlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Map_main_screen.class);
                startActivity(intent);
            }
        });

        ImageView controlBarWifi = findViewById(R.id.imageView30);
        controlBarWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WiFi_Tag_Screen.class);
                startActivity(intent);
            }
        });

        ImageView controlBarIdentity = findViewById(R.id.imageView36);
        controlBarIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), User_Identity_view.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        MapPoint Seoul = MapPoint.mapPointWithGeoCoord(35.1501, 126.8559);
        mapView.setMapCenterPointAndZoomLevel(Seoul, 8, true);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        // Only handle current location updates here, not any saved locations.
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }
}