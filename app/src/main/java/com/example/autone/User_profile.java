package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User_profile extends AppCompatActivity {

    //Firebase
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String Name, DateOfBirth, Gender, Address;
    TextView mAddress, mGender, mDateOfBirth, mName, mName2;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        ImageView previousbtn = findViewById(R.id.imageView6);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        TextView ViewStatistic = findViewById(R.id.textView18);
        ViewStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), User_Identity_view.class);
                startActivity(intent);
            }
        });

        Button logoutButton = findViewById(R.id.button102);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new ProgressDialog(User_profile.this);
                mDialog.setMessage("Processing...");
                mDialog.show();
                firebaseAuth.signOut();
                Intent intent = new Intent(User_profile.this, Entry_screen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        myRef1.child("User").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Identity_View group = dataSnapshot.getValue(Identity_View.class);

                mName = (TextView)findViewById(R.id.textView107);
                mDateOfBirth = (TextView)findViewById(R.id.textView22);
                mGender = (TextView)findViewById(R.id.textView17);
                mAddress = (TextView) findViewById(R.id.textView108);
                mName2 = (TextView) findViewById(R.id.textView14);

                //각각의 값 받아오기 get어쩌구 함수들은 Together_group_list.class에서 지정한것
                if (group != null) {
                    Name = group.getName();
                    Gender = group.getGender();
                    DateOfBirth = group.getDate_of_Birth();
                    Address = group.getAddress();
                }

                //텍스트뷰에 받아온 문자열 대입하기
                mName.setText(Name);
                mDateOfBirth.setText(DateOfBirth);
                mGender.setText(Gender);
                mAddress.setText(Address);
                mName2.setText(Name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
    }
}