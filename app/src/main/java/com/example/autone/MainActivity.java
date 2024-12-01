package com.example.autone;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

// 서버에 이미 저장된 와이파이 - X
// 여기 목록에서 클릭하면 와이파이 추가?
// 추가한 후에 목록에 계속 보임
// 중복된 경우 중복이라고 토스트

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    //private WifiScanAdapter adapter;
    private WifiManager wifiManager;
    private List<ScanResult> scanDatas; // ScanResult List
    private List<WifiData> wifiList;
    private ListView listView;
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    HashSet<String> mset = new HashSet<>();
    static HashMap<String, Main_screen.User> WIFIName;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1000);
                    return;
                }
                scanDatas = wifiManager.getScanResults();
                Toast.makeText(getApplicationContext(), scanDatas.get(0).SSID, Toast.LENGTH_SHORT).show();

                wifiList = new ArrayList<>();
                for(ScanResult select : scanDatas){
                    String BSSID = select.BSSID;
                    String SSID = select.SSID;
                    WifiData wifiData = new WifiData(BSSID, SSID, false);
                    wifiList.add(wifiData);
                }

                // 어댑터뷰(리스트 뷰)
                listView = (ListView)findViewById(R.id.listView);
                // 어댑터
                ArrayAdapter adapter = new WifiAdapter(getApplicationContext(), R.layout.wifi_item, wifiList); // 안드로이드에서 기본적으로 제공되는 레이아웃
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(getApplicationContext(), wifiList.get(i).getSSID(), Toast.LENGTH_SHORT).show();
                        WifiData item  = wifiList.get(i);
                        if(mset.contains(item.getBSSID())){ //WIFI ssid & bssid 이름 firebase에 추가하기
                            Toast.makeText(getApplicationContext(), "Existing WIFI Tag. Select new WIFI.", Toast.LENGTH_SHORT).show();
                        } else{
                            myRef.child("WIFI List").child(user.getUid()).child("WIFI Name").child(item.getSSID()).setValue(item.getBSSID());
                            //myRef.child("WIFI List").child(user.getUid()).child("WIFI Name").child("bssid").setValue(item.getBSSID());
                        }
                    }
                });
                // listview 갱신
                adapter.notifyDataSetChanged();

            }else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for(DataSnapshot ds: snapshot.getChildren()){
                String bssid = ds.getKey();
                mset.add(bssid);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        listView = (ListView) findViewById(R.id.listView);

        ImageView previousbtn = findViewById(R.id.imageView500);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
}
/*
    @Override
    protected void onStart() {
        super.onStart();
        updateData();
    }

    String wifiName;

    public void updateData(){
        WIFIName = new HashMap<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("WIFI List").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    wifiName = ds.child(user.getUid()).getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }*/

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentFilter);
        wifiManager.startScan();

        listView.setFocusable(true);
        myRef.child("WIFI List").child(user.getUid()).addValueEventListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        myRef.child("WIFI List").child(user.getUid()).removeEventListener(listener);
        mset.clear();
    }

    public void showWifiList(View v){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
    }
}