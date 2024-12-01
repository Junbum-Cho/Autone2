package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class Friends_List extends AppCompatActivity {
    ArrayList<Friends_List_Adapter.Item> mDataSet = new ArrayList<>();
    Friends_List_Adapter adapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    HashSet<String> keySet = new HashSet<>();
    delete_friend_popup friend_popup;
    EditText searchFriend;
    ImageView searchIcon;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();
    FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        ImageView previousbtn = findViewById(R.id.imageView12);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ImageView settingbtn = findViewById(R.id.imageView43);
        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.editMode = ! adapter.editMode;
                adapter.setLocalDataSet(mDataSet);
                System.out.println("Friends_List_editMode: "+ adapter.editMode.toString());
            }
        });


        searchFriend = findViewById(R.id.editTextTextPersonName2);
        searchIcon = findViewById(R.id.imageView66);
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick (View view) {
                String emailToSearch = searchFriend.getText().toString().trim();
                if (Main_screen.userList != null) {
                    for (Main_screen.User user : Main_screen.userList.values()) {
                        if (emailToSearch.equals(user.email) && (!user.email.equals(user2.getEmail()))) {
                            addFriend(user);
                            Toast.makeText(getApplicationContext(), "friend request sent", Toast.LENGTH_SHORT);
                            break;
                        }
                    }
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView3);
        adapter = new Friends_List_Adapter(mDataSet);
        recyclerView.setAdapter(adapter);
        adapter.setOnImageClickListener(new Friends_List_Adapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                Intent intent = new Intent(getApplicationContext(), Add_family_member.class);
                Friends_List_Adapter.Item item = adapter.localDataSet.get(position);
                intent.putExtra("userKey",item.user_key);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Friends_List_Adapter.Item item = adapter.localDataSet.get(position);
                friend_popup = new delete_friend_popup();
                friend_popup.denyListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getSupportFragmentManager().beginTransaction().remove(friend_popup).commit();
                    }
                };
                friend_popup.acceptListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference mDatabase =FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("Friends list").child(uid).child("Existing Family List").child(item.user_key)
                                        .removeValue();
                        mDatabase.child("Friends list").child(item.user_key).child("Existing Family List").child(uid)
                                .removeValue();
                        updateData();
                        getSupportFragmentManager().beginTransaction().remove(friend_popup).commit();
                    }
                };
                transaction.replace(R.id.friends_list_popup, friend_popup).commit();
            }

            @Override
            public void onRemoveClick(int position) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Friends_List_Adapter.Item item = adapter.localDataSet.get(position);
                friend_popup = new delete_friend_popup();
                friend_popup.denyListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getSupportFragmentManager().beginTransaction().remove(friend_popup).commit();
                    }
                };
                friend_popup.acceptListener = new View.OnClickListener () {
                    @Override
                    public void onClick (View view){
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("Friends list").child(uid).child("Existing Friends List").child(item.user_key)
                                .removeValue();
                        mDatabase.child("Friends list").child(item.user_key).child("Existing Friends List").child(uid)
                                .removeValue();
                        updateData();
                        getSupportFragmentManager().beginTransaction().remove(friend_popup).commit();
                    }
                };
                transaction.replace(R.id.friends_list_popup, friend_popup).commit();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void addFriend(Main_screen.User user) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Main_screen.User currentUser = Main_screen.userList.get(currentUserId);
        myRef1.child("Friends list").child(user.key).child("Friend Request List").child(currentUserId)
                .setValue(currentUser.name);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
    }

    public void updateData() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        for(String key: Main_screen.userList.keySet()){
            Log.d("Friends_List: userList", key);
        }

        mDataSet = new ArrayList<>();
        keySet.clear();
        firebaseDatabase.getReference().child("Friends list").child(uid)
                .child("Existing Family List").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String familyKey = ds.getKey();
                            String name = Main_screen.userList.get(familyKey).name;
                            mDataSet.add(new Friends_List_Adapter.Item(name, familyKey, true, true));
                            keySet.add(familyKey);

                        }
                        getFriendList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println(error.getMessage());
                    }
                });

    }
    public void getFriendList() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase.getReference().child("Friends list").child(uid)
                .child("Existing Friends List").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String familyKey = ds.getKey();
                            if (!keySet.contains(familyKey)) {
                                String name = Main_screen.userList.get(familyKey).name;
                                mDataSet.add(new Friends_List_Adapter.Item(name, familyKey, false, true));
                            }
                        }
                        adapter.setLocalDataSet(mDataSet);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println(error.getMessage());
                    }
                });
    }
}