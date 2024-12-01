package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.IOException;

public class User_Identity_view extends AppCompatActivity {

    //Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private FirebaseAuth firebaseAuth;
    String Name, DateOfBirth, Gender, IdentificationNumber, Address, DateOfExpirary;
    TextView mAddress, mDateOfExpiration, mGender, mIdentificationNumber, mDateOfBirth, mName;
    ImageView img_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_identity);

        ImageView userIdentityUpdate = findViewById(R.id.imageView49);
        userIdentityUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), user_identification_upload_before_account.class);
                startActivity(intent);
            }
        });
        ImageView previousbtn = findViewById(R.id.imageView44);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Main_screen.class);
                startActivity(intent);
            }
        });

        myRef1.child("User").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Identity_View group = dataSnapshot.getValue(Identity_View.class);

                mName = (TextView) findViewById(R.id.textView60);
                mDateOfBirth = (TextView) findViewById(R.id.textView59);
                mGender = (TextView) findViewById(R.id.textView58);
                mIdentificationNumber = (TextView) findViewById(R.id.textView53);
                mAddress = (TextView) findViewById(R.id.textView57);
                mDateOfExpiration = (TextView) findViewById(R.id.textView56);

                //각각의 값 받아오기 get어쩌구 함수들은 Together_group_list.class에서 지정한것
                if (group != null) {
                    Name = group.getName();
                    Gender = group.getGender();
                    DateOfBirth = group.getDate_of_Birth();
                    IdentificationNumber = group.Identification_Number();
                    Address = group.getAddress();
                    DateOfExpirary = group.getDate_of_Expiration();
                }

                //텍스트뷰에 받아온 문자열 대입하기
                mName.setText(Name);
                mDateOfBirth.setText(DateOfBirth);
                mGender.setText(Gender);
                mIdentificationNumber.setText(IdentificationNumber);
                mAddress.setText(Address);
                mDateOfExpiration.setText(DateOfExpirary);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });

        img_test = findViewById(R.id.imageView45);
        loadProfileImage();
    }

    private void loadProfileImage() {
        StorageReference profileRef = storageRef.child(user.getUid() + "/img/profile.jpg");

        profileRef.getBytes(1024 * 1024) // Set size as needed
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        img_test.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        img_test.setImageResource(R.drawable.identify_user_id_card); // Fallback image
                    }
                });
    }
}