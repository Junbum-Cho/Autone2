package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Add_family_member extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userKey, name;
    EditText mName, mRelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_family_member);

        mName = findViewById(R.id.textView41);
        //name = Main_screen.userList.get(userKey).name;
        //mName.setText(name);

        mRelation = findViewById(R.id.editTextTextPersonName3);

        Button next = findViewById(R.id.button101);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getText().toString().trim();
                String relation = mRelation.getText().toString().trim();
                if (name.isEmpty() || relation.isEmpty()) {
                    Toast.makeText(Add_family_member.this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    myRef1.child("Friends list").child(userKey).child("Friend Family Request List").child(user.getUid()).setValue(relation);
                    onBackPressed();
                }
            }
        });

        ImageView previousbtn = findViewById(R.id.imageView110);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        Intent intent = getIntent();
        userKey = intent.getStringExtra("userKey");
//        mName = findViewById(R.id.textView41);
//        name = Main_screen.userList.get(userKey).name;
//        mName.setText(name);

    }
    @Override
    public void onBackPressed () {
        super.onBackPressed();
    }
}