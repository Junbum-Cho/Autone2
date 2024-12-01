package com.example.autone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User_confirm_details_before_creating_account extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference();
    FirebaseAuth firebaseAuth;
    EditText mUserName, mDateOfBirth, mGender, mIdentificationNumber, mAddress, mDateOfExpirary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_confirm_detail_before_account);

        firebaseAuth = FirebaseAuth.getInstance();

        //firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        mUserName = findViewById(R.id.textView86);
        mDateOfBirth = findViewById(R.id.textView88);
        mGender = findViewById(R.id.textView89);
        mIdentificationNumber = findViewById(R.id.textView82);
        mAddress = findViewById(R.id.textView90);
        mDateOfExpirary = findViewById(R.id.textView91);

        Button next = findViewById(R.id.button13);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = firebaseAuth.getCurrentUser().getUid();
                String name = mUserName.getText().toString();
                String dateOfBirth = mDateOfBirth.getText().toString();
                String gender = mGender.getText().toString();
                String identificationNumber = mIdentificationNumber.getText().toString();
                String address = mAddress.getText().toString();
                String dateOfExpirary = mDateOfExpirary.getText().toString();
                myRef1.child("User").child(uid).child("Name").setValue(name);
                myRef1.child("User").child(uid).child("Date_of_Birth").setValue(dateOfBirth);
                myRef1.child("User").child(uid).child("Gender").setValue(gender);
                myRef1.child("User").child(uid).child("Idenification_Number").setValue(identificationNumber);
                myRef1.child("User").child(uid).child("Address").setValue(address);
                myRef1.child("User").child(uid).child("Date_of_Expiration").setValue(dateOfExpirary);

                Intent intent = new Intent(getApplicationContext(), Main_screen.class);
                startActivity(intent);
            }
        });

        ImageView edit = findViewById(R.id.imageView77);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), user_identification_upload_before_account.class);
                startActivity(intent);
            }
        });
    }


}
