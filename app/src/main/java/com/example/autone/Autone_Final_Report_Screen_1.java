package com.example.autone;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

public class Autone_Final_Report_Screen_1 extends AppCompatActivity {

    private static final String TAG = "Autone_Final_Report_Screen_1";
    private static final String KAKAO_API_KEY = "API_KEY";

    private DatabaseReference databaseReference;
    private TextView userLocationTextView;
    private String reportID;
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private MediaPlayer mediaPlayer;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autone_final_report_screen1);
        firebaseAuth = FirebaseAuth.getInstance();

        userLocationTextView = findViewById(R.id.textView73);
        reportID = getIntent().getStringExtra("reportID");

        if (reportID == null) {
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Case Reports");

        fetchReportDetails();

        Button finalReportContinue = findViewById(R.id.button22);
        finalReportContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), autone_final_report_screen_2.class);
                intent.putExtra("reportID", reportID);
                startActivity(intent);
            }
        });

        View playAudioButton = findViewById(R.id.view5);
        playAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playReportAudio();
            }
        });

        mapView = new MapView(this);
        mapViewContainer = findViewById(R.id.mapView2);
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
                            String location = reportSnapshot.child("location").getValue(String.class);
                            if (location != null) {
                                userLocationTextView.setText(location);
                                showUserLocationOnMap(location);
                            } else {
                                userLocationTextView.setText("No location available");
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

    private void showUserLocationOnMap(String location) {
        // String 형태로 위도 및 경도를 불러옴
        String[] latLon = location.replace("Lat: ", "").replace("Lon: ", "").split(", ");
        double latitude = Double.parseDouble(latLon[0]);
        double longitude = Double.parseDouble(latLon[1]);

        runOnUiThread(() -> {
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
            mapView.setMapCenterPointAndZoomLevel(mapPoint, 2, true);
            mapView.addPOIItem(createMarker(mapPoint, "사건 장소"));
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
    private void playReportAudio() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(uid).child(reportID);
            reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String audioUrl = dataSnapshot.child("recording").getValue(String.class);
                        if (audioUrl != null && !audioUrl.isEmpty()) {
                            startPlayingAudio(audioUrl);
                        } else {
                            Toast.makeText(Autone_Final_Report_Screen_1.this, "Audio URL not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Autone_Final_Report_Screen_1.this, "Report details not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Autone_Final_Report_Screen_1.this, "Error fetching audio URL", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlayingAudio(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mapViewContainer != null && mapView != null) {
            mapViewContainer.removeView(mapView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapViewContainer != null && mapView != null) {
            mapViewContainer.removeView(mapView);
            mapViewContainer = null;
            mapView = null;
        }
    }
}
