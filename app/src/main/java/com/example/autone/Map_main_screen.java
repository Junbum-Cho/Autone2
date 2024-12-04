package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Map_main_screen extends AppCompatActivity implements mCurrentLocationEventListener, mMapViewEventListener {

    private ViewGroup mapViewContainer;
    public MapView mapView;
    BottomSheetDialog dialog;
    Button show;
    Button emergency;
    String dialog_name="empty";

    private ArrayList<Map_main_screen_Adapter.Item> emergencyCentersList = new ArrayList<>();
    private ArrayList<Map_main_screen_Adapter.Item> crimeList = new ArrayList<>();
    private Map_main_screen_Adapter emergencyAdapter;
    private Map_main_screen_Adapter crimeAdapter;
    private Location userLocation;
    private OkHttpClient client = new OkHttpClient();
    private static final String KAKAO_API_KEY = "API_KEY"; // Use your Kakao API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main_screen);

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

        setupButtons();
        checkPermissions();

        mapView = new MapView(this);
        mapViewContainer = findViewById(R.id.map_view);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationEventListener(this);

        dialog = new BottomSheetDialog(this);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Initialize the adapter
        emergencyAdapter = new Map_main_screen_Adapter();
        crimeAdapter = new Map_main_screen_Adapter();

        fetchCrimeData();
    }

    private void setupButtons() {
        ImageView controlBarMain = findViewById(R.id.imageView19);
        controlBarMain.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Main_screen.class);
            startActivity(intent);
            finish();
        });

        ImageView controlBarWifi = findViewById(R.id.imageView29);
        controlBarWifi.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), WiFi_Tag_Screen.class);
            startActivity(intent);
        });

        ImageView controlBarIdentity = findViewById(R.id.imageView35);
        controlBarIdentity.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), User_Identity_view.class);
            startActivity(intent);
        });

        Button WIFITagScreen = findViewById(R.id.button200);
        WIFITagScreen.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        show = findViewById(R.id.button2);
        emergency = findViewById(R.id.button14);

        show.setOnClickListener(v -> {
            if (userLocation == null) {
                Toast.makeText(this, "위치를 특정하는 중.. 다시 한번 더 시도해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                createCrime();
                dialog.show();
            }
        });

        emergency.setOnClickListener(v -> {
            if (userLocation == null) {
                Toast.makeText(this, "위치를 특정하는 중.. 다시 한번 더 시도해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                createEmergency();
                dialog.show();
            }
        });
    }

    private void checkPermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
        }
    }

    private void createCrime() {
        View view = getLayoutInflater().inflate(R.layout.map_bottom_sheet, null, false);
        TextView name = view.findViewById(R.id.name);
        name.setText("발생 범죄");
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_mbs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(crimeAdapter);

        // Fetch and display crime data from Firebase
        fetchCrimeData();

        dialog.setContentView(view);
    }

    private void createEmergency() {
        View view = getLayoutInflater().inflate(R.layout.map_bottom_sheet, null, false);
        TextView name = view.findViewById(R.id.name);
        name.setText("파출소");
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_mbs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(emergencyAdapter);

        // Fetch and display emergency center data
        fetchEmergencyCenters(userLocation.getLatitude(), userLocation.getLongitude());

        dialog.setContentView(view);
    }

    private void fetchEmergencyCenters(double latitude, double longitude) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=파출소&x=" + longitude + "&y=" + latitude + "&radius=5000";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "KakaoAK " + KAKAO_API_KEY)
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

                        emergencyCentersList.clear();
                        for (int i = 0; i < documents.length(); i++) {
                            JSONObject document = documents.getJSONObject(i);
                            String placeName = document.getString("place_name");
                            String distance = document.getString("distance");
                            String addressName = document.getString("address_name");

                            // Format distance without decimal places
                            int distanceMeters = (int) Double.parseDouble(distance);

                            emergencyCentersList.add(new Map_main_screen_Adapter.Item(
                                    addressName,
                                    Double.parseDouble(document.getString("y")),
                                    Double.parseDouble(document.getString("x")),
                                    placeName,
                                    ""
                            ));
                        }

                        runOnUiThread(() -> {
                            emergencyAdapter.localDataSet.clear();
                            emergencyAdapter.localDataSet.addAll(emergencyCentersList);
                            emergencyAdapter.setUserLocation(userLocation);
                            emergencyAdapter.notifyDataSetChanged();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fetchCrimeData() {
        if (userLocation == null) {
            //Toast.makeText(this, "현재 위치를 찾을 수 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("RealTime Crime")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        crimeList.clear();
                        for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                            String cityName = citySnapshot.getKey();
                            Log.d("Map_main_screen", "Processing city: " + cityName);

                            for (DataSnapshot crimeSnapshot : citySnapshot.getChildren()) {
                                Log.d("Map_main_screen", "Processing crime: " + crimeSnapshot.getKey());

                                // Print the entire structure of the current crimeSnapshot
                                for (DataSnapshot field : crimeSnapshot.getChildren()) {
                                    Log.d("Map_main_screen", "Field: " + field.getKey() + ", Value: " + field.getValue());
                                }

                                String crimeType = crimeSnapshot.child("Crime Type").getValue(String.class);
                                String location = crimeSnapshot.child("Location").getValue(String.class);
                                String timestamp = crimeSnapshot.child("timestamp").getValue(String.class);

                                // Log retrieved values
                                Log.d("Map_main_screen", "City: " + cityName + ", Crime Type: " + crimeType + ", Location: " + location + ", Timestamp: " + timestamp);

                                if (crimeType == null || location == null || timestamp == null) {
                                    Log.e("Map_main_screen", "Missing data for crime snapshot: " + crimeSnapshot.getKey());
                                    continue; // Skip entries with missing data
                                }

                                double crimeLat = 0;
                                double crimeLon = 0;
                                String[] locationParts = location.replace("Lat: ", "").replace("Lon: ", "").split(",");
                                if (locationParts.length == 2) {
                                    try {
                                        crimeLat = Double.parseDouble(locationParts[0].trim());
                                        crimeLon = Double.parseDouble(locationParts[1].trim());
                                    } catch (NumberFormatException e) {
                                        Log.e("Map_main_screen", "Error parsing location: " + e.getMessage());
                                        continue; // Skip this entry if location parsing fails
                                    }
                                } else {
                                    Log.e("Map_main_screen", "Invalid location format: " + location);
                                    continue; // Skip this entry if location format is invalid
                                }

                                final String finalCrimeType = crimeType;
                                final double finalCrimeLat = crimeLat;
                                final double finalCrimeLon = crimeLon;

                                fetchAddress(finalCrimeLat, finalCrimeLon, address -> {
                                    String distance = calculateDistance(finalCrimeLat, finalCrimeLon);
                                    synchronized (crimeList) {
                                        crimeList.add(new Map_main_screen_Adapter.Item(address, finalCrimeLat, finalCrimeLon, finalCrimeType, distance));
                                        Log.d("Map_main_screen", "Added crime: " + address + " - " + distance + "m");
                                    }
                                    runOnUiThread(() -> crimeAdapter.notifyDataSetChanged());
                                });
                            }
                        }

                        // Log the size of the crime list after processing all snapshots
                        Log.d("Map_main_screen", "Crime list size after processing: " + crimeList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Map_main_screen.this, "Failed to fetch crime data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAddress(double lat, double lon, AddressCallback callback) {
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        mapViewContainer.addView(mapView);
        System.out.println("Map_main_screen: mapView add" + mapView.isActivated());
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (!check_result) {
                finish();
            }
        }
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        double latitude = mapPointGeo.latitude;
        double longitude = mapPointGeo.longitude;
        userLocation = new Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
        fetchEmergencyCenters(latitude, longitude);
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

    interface AddressCallback {
        void onAddressFetched(String address);
    }
}
