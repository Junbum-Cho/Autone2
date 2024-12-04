package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.os.Build.VERSION;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Notification_Center_Crime extends AppCompatActivity implements mCurrentLocationEventListener, mMapViewEventListener {
    private ViewGroup mapViewContainer;
    public MapView mapView;
    private ArrayList<Notification_Crime_Adapter.Item> crimeList = new ArrayList<>();
    private Notification_Crime_Adapter adapter;
    private Location userLocation;
    private OkHttpClient client = new OkHttpClient();
    private static final String KAKAO_API_KEY = "7cb5034d3170de7caea7f9eb98a29b8b"; // Add your Kakao API key here

    ArrayList<Notification_Crime_Adapter.Item> mDataSet = new ArrayList<>();
    private HashSet<String> familyUids = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center_crime);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한을 물어본다
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return;
        }
        mapView = new MapView(this);

        mapViewContainer = (ViewGroup) findViewById(R.id.mapView);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        ImageButton previousbtn = findViewById(R.id.imageButton9);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Button notificationrequest = findViewById(R.id.button11);
        notificationrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Notificaitons_center_requests.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView2);
        adapter = new Notification_Crime_Adapter(crimeList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchFamilyData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapViewContainer.addView(mapView);
        mapView.onResume();
        System.out.println("Notification_Center_Crime: mapView add"+mapView.isActivated());
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationEventListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewContainer.removeView(mapView);
        mapView.onPause();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        MapPoint Seoul = MapPoint.mapPointWithGeoCoord(35.1501, 126.8559);
        mapView.setMapCenterPointAndZoomLevel(Seoul, 8, true);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        double latitude = mapPointGeo.latitude;
        double longitude = mapPointGeo.longitude;
        userLocation = new Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
        fetchCrimeData(); // Fetch crimes when the location is updated
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

    private long lastFetchTime = 0;
    private static final long MIN_FETCH_INTERVAL = 30000; // 30 seconds

    private void fetchCrimeData() {
        if (userLocation == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchTime < MIN_FETCH_INTERVAL) {
            // Do not fetch if the last fetch was too recent
            return;
        }
        lastFetchTime = currentTime;

        FirebaseDatabase.getInstance().getReference("RealTime Crime")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Notification_Crime_Adapter.Item> newCrimeList = new ArrayList<>();
                        for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                            for (DataSnapshot crimeSnapshot : citySnapshot.getChildren()) {
                                String uid = crimeSnapshot.child("userUid").getValue(String.class);
                                String crimeType = crimeSnapshot.child("Crime Type").getValue(String.class);
                                String location = crimeSnapshot.child("Location").getValue(String.class);
                                String timestamp = crimeSnapshot.child("timestamp").getValue(String.class);

                                if (location != null) {
                                    double crimeLat = 0;
                                    double crimeLon = 0;
                                    String[] locationParts = location.replace("Lat: ", "").replace("Lon: ", "").split(",");
                                    if (locationParts.length == 2) {
                                        try {
                                            crimeLat = Double.parseDouble(locationParts[0].trim());
                                            crimeLon = Double.parseDouble(locationParts[1].trim());
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                            continue; // Skip this entry if location parsing fails
                                        }
                                    }

                                    final String finalCrimeType = crimeType;
                                    final double finalCrimeLat = crimeLat;
                                    final double finalCrimeLon = crimeLon;
                                    final String finalUid = uid;
                                    fetchAddress(finalCrimeLat, finalCrimeLon, address -> {
                                        String distance = calculateDistance(finalCrimeLat, finalCrimeLon);
                                        Notification_Crime_Adapter.Item newItem = new Notification_Crime_Adapter.Item(address, distance, finalCrimeType, timestamp);
                                        if (familyUids.contains(finalUid)) {
                                            newItem.setIsFamily(true); // Custom method to mark the item as family-related
                                        }
                                        synchronized (newCrimeList) {
                                            newCrimeList.add(newItem);
                                        }
                                        runOnUiThread(() -> {
                                            updateCrimeList(newCrimeList);
                                        });
                                    });
                                } else {
                                    Log.e("Notification_Center_Crime", "Location is null for crime snapshot: " + crimeSnapshot.getKey());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Notification_Center_Crime.this, "Failed to fetch crime data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchFamilyData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser.getUid();
        FirebaseDatabase.getInstance().getReference("Friends list").child(currentUserUid).child("Existing Family List")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot familySnapshot : snapshot.getChildren()) {
                            familyUids.add(familySnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Notification_Center_Crime.this, "Failed to fetch family data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCrimeList(ArrayList<Notification_Crime_Adapter.Item> newCrimeList) {
        crimeList.clear();
        crimeList.addAll(newCrimeList);
        adapter.notifyDataSetChanged();
    }

    private void fetchAddress(double lat, double lon, AddressCallback callback) {
        String apiKey = "API_KEY";
        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=" + lon + "&y=" + lat;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "KakaoAK " + KAKAO_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray documents = jsonObject.getJSONArray("documents");
                        if (documents.length() > 0) {
                            String address = documents.getJSONObject(0).getJSONObject("address").getString("address_name");
                            callback.onAddressFetched(address);
                        } else {
                            callback.onAddressFetched("Address not found");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onAddressFetched("Error fetching address");
                    }
                } else {
                    callback.onAddressFetched("Error fetching address");
                }
            }
        });
    }

    private String calculateDistance(double crimeLat, double crimeLon) {
        if (userLocation == null) return "N/A";
        Location crimeLocation = new Location("");
        crimeLocation.setLatitude(crimeLat);
        crimeLocation.setLongitude(crimeLon);
        float distance = userLocation.distanceTo(crimeLocation);
        return String.format("%.0f", distance); // Format without decimal places
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if (check_result == false) {
                finish();
            }
        }
    }

    interface AddressCallback {
        void onAddressFetched(String address);
    }
}
