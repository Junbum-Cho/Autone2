package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class WiFi_Tag_Screen extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WIFI_Tag_Screen_Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ArrayList<User_WIFI> arrayList;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_tag);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // Initialize Firebase database

        ImageButton previousbtn = findViewById(R.id.imageButton2);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.recyclerView5);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        arrayList = new ArrayList<>();

        adapter = new WIFI_Tag_Screen_Adapter(arrayList, this);
        recyclerView.setAdapter(adapter);

        databaseReference = database.getReference("WIFI List").child(firebaseAuth.getCurrentUser().getUid()).child("WIFI Name");

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for (DataSnapshot wifiSnapshot : snapshot.getChildren()) {
                    String wifiName = wifiSnapshot.getKey();
                    String wifiDetails = wifiSnapshot.getValue(String.class);
                    User_WIFI userWiFi = new User_WIFI(wifiName, wifiDetails);
                    arrayList.add(userWiFi);
                    Log.d("WiFi_Tag_Screen", "Fetched Wi-Fi: " + wifiName + " - " + wifiDetails);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WiFi_Tag_Screen", error.toException().toString());
            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
    }
}