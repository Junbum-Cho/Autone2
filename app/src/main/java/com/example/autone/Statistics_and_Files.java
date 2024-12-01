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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Statistics_and_Files extends AppCompatActivity {

    private static final String TAG = "Statistics_and_Files";

    ArrayList<Statistics_and_Files_Adapter.Item> mDataSet = new ArrayList<>();
    DatabaseReference databaseReference;
    Statistics_and_Files_Adapter adapter;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_and_files);

        ImageButton previousbtn = findViewById(R.id.imageButton3);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView7);
        adapter = new Statistics_and_Files_Adapter(mDataSet);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Click Listener for touchable recyclerView
        adapter.setOnItemClickListener(new Statistics_and_Files_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Statistics_and_Files_Adapter.Item selectedItem = mDataSet.get(position);
                Intent intent = new Intent(getApplicationContext(), Autone_Final_Report_Screen_1.class);
                intent.putExtra("reportID", selectedItem.reportID);
                startActivity(intent);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Case Reports");
        fetchDataFromFirebase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
    }

    public void fetchDataFromFirebase() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDataSet.clear();
                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) { // Iterates over ReportID nodes
                    Log.d(TAG, "ReportSnapshot: " + reportSnapshot.toString()); // Log the entire snapshot to see its structure
                    if (reportSnapshot.hasChild("timestamp") && reportSnapshot.hasChild("location") && reportSnapshot.hasChild("prompt")) {
                        String reportID = reportSnapshot.getKey();
                        String timestamp = reportSnapshot.child("timestamp").getValue(String.class);
                        String location = reportSnapshot.child("location").getValue(String.class);
                        String prompt = reportSnapshot.child("prompt").getValue(String.class);
                        Statistics_and_Files_Adapter.Item item = new Statistics_and_Files_Adapter.Item(reportID, timestamp, location, prompt);
                        mDataSet.add(item);
                    }
                }
                adapter.notifyDataSetChanged(); // Ensure adapter is not null
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "실패했습니다.", Toast.LENGTH_SHORT);
            }
        });
    }
}