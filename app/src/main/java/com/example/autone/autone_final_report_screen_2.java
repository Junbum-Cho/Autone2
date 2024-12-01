package com.example.autone;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class autone_final_report_screen_2 extends AppCompatActivity {

    private static final String TAG = "autone_final_report_screen_2";
    private static final String KAKAO_API_KEY = "7cb5034d3170de7caea7f9eb98a29b8b";

    private DatabaseReference databaseReference;
    private TextView gptPromptTextView;
    private TextView emergencyCenterTextView;
    private String reportID;
    private MapView mapView;
    private ViewGroup mapViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autone_final_report_screen2);

        gptPromptTextView = findViewById(R.id.textView9);
        gptPromptTextView.setMovementMethod(new ScrollingMovementMethod());
        emergencyCenterTextView = findViewById(R.id.textView112);
        reportID = getIntent().getStringExtra("reportID");

        if (reportID == null) {
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Case Reports");

        fetchReportDetails();

        Button finalReportContinue = findViewById(R.id.button23);
        finalReportContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Statistics_and_Files.class);
                startActivity(intent);
            }
        });

        mapView = new MapView(this);
        ViewGroup mapViewContainer = findViewById(R.id.mapView3);
        mapViewContainer.addView(mapView);
        // Disable current location tracking
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
    }

    private void fetchReportDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean reportFound = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { // Iterates over UserUID nodes
                    for (DataSnapshot reportSnapshot : userSnapshot.getChildren()) { // Iterates over ReportID nodes
                        if (reportSnapshot.getKey().equals(reportID)) {
                            reportFound = true;
                            String prompt = reportSnapshot.child("prompt").getValue(String.class);
                            String emergencyCenter = reportSnapshot.child("emergency_center_location").getValue(String.class);
                            if (prompt != null) {
                                gptPromptTextView.setText(prompt);
                            } else {
                                gptPromptTextView.setText("불러올 수 있는 신고 리포트가 없습니다.");
                            }

                            if (emergencyCenter != null) {
                                emergencyCenterTextView.setText(emergencyCenter);
                                showEmergencyCenterOnMap(emergencyCenter);
                            } else {
                                emergencyCenterTextView.setText("파출소 위치를 불러올 수 없습니다.");
                            }
                            break;
                        }
                    }
                    if (reportFound) break;
                }
                if (!reportFound) {
                    Toast.makeText(getApplicationContext(), "Report not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "DataBase Error.", Toast.LENGTH_SHORT);
            }
        });
    }

    private void showEmergencyCenterOnMap(String address) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + address;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "KakaoAK " + KAKAO_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to get coordinates for address: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray documents = jsonObject.getJSONArray("documents");
                        if (documents.length() > 0) {
                            JSONObject addressInfo = documents.getJSONObject(0);
                            double latitude = addressInfo.getDouble("y");
                            double longitude = addressInfo.getDouble("x");

                            runOnUiThread(() -> {
                                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                                mapView.setMapCenterPointAndZoomLevel(mapPoint, 2, true);
                                mapView.addPOIItem(createMarker(mapPoint, "파출소 주소"));
                            });
                        } else {
                            Log.e(TAG, "해당 주소를 식별할 수 없음.: " + address);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse JSON response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Failed to get coordinates for address: " + response.message());
                }
            }
        });
    }

    private MapPOIItem createMarker(MapPoint mapPoint, String name) {
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(name);
        marker.setTag(0);
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // Specify a marker type
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        return marker;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapViewContainer != null && mapView.getParent() != null) {
            ((ViewGroup) mapView.getParent()).removeView(mapView);
        }
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapViewContainer != null && mapView.getParent() != null) {
            ((ViewGroup) mapView.getParent()).removeView(mapView);
        }
        mapViewContainer = null;
        mapView = null;
    }
}