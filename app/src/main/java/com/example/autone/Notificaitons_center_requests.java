package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Notificaitons_center_requests extends AppCompatActivity {

    ArrayList<Notification_Center_Requests_Adapter.Item> mDataSet = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    Notification_Center_Requests_Adapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_notificaitons_requests);
        ImageButton previousbtn = findViewById(R.id.imageButton10);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Button notificationcrime = findViewById(R.id.button20);
        notificationcrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Notification_Center_Crime.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView8);
        adapter = new Notification_Center_Requests_Adapter(mDataSet);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(new Notification_Center_Requests_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                Notification_Center_Requests_Adapter.Item item = adapter.localDataSet.get(position);
                String key = item.userkey;
                if (item.friend_or_family.equals("family")){
                    myRef1.child("Friends list").child(uid).child("Existing Family List").child(item.userkey).setValue(true);
                    myRef1.child("Friends list").child(item.userkey).child("Existing Family List").child(uid).setValue(item.identity);
                    myRef1.child("Friends list").child(uid).child("Friend Family Request List").child(item.userkey).removeValue();
                }
                else if (item.friend_or_family.equals("friend")) {
                    // Add to current user's friends list
                    myRef1.child("Friends list").child(uid).child("Existing Friends List").child(key).setValue(true);
                    // Add the current user to the friends list of the user who sent the request
                    myRef1.child("Friends list").child(key).child("Existing Friends List").child(uid).setValue(true);
                    // Remove the friend request
                    myRef1.child("Friends list").child(uid).child("Friend Request List").child(key).removeValue();
                }
                updateData();

                recyclerView.setOnClickListener(v -> {
                    if (item == null) {
                        Log.d("myTag", "Showing if null.");
                        return;
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
        finish();
    }

    public void updateData(){
        mDataSet.clear();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        database.getReference().child("Friends list").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds: snapshot.child("Friend Family Request List").getChildren()){
                        String identity = ds.getValue().toString();
                        String key = ds.getKey();
                        String friendName = Main_screen.userList.get(key).name;
                        mDataSet.add(new Notification_Center_Requests_Adapter.Item(
                                friendName, "family", key, identity
                        ));
                    }

                    for (DataSnapshot ds: snapshot.child("Friend Request List").getChildren()){
                        String key2 = ds.getKey();
                        String friendName2 = Main_screen.userList.get(key2).name;
                        mDataSet.add(new Notification_Center_Requests_Adapter.Item(
                                friendName2, "friend", key2, ""
                        ));
                    }
                    adapter.setLocalDataSet(mDataSet);
                }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println(error.getMessage());
                    }
                });
        adapter.setLocalDataSet(mDataSet);
    }

    public void getFriendList() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateData();
    }
}