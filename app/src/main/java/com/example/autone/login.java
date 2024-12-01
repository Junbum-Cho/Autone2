package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class login extends AppCompatActivity {


    Button mLoginBtn;
    EditText mEmailText, mPasswordText;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        //버튼 등록하기
        mLoginBtn = findViewById(R.id.button100);
        mEmailText = findViewById(R.id.editTextTextPersonName1);
        mPasswordText = findViewById(R.id.editTextTextPassword5);

        ImageView previousbtn = findViewById(R.id.imageView79);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Entry_screen.class);
                startActivity(intent);
            }
        });

        //로그인 버튼이 눌리면
        mLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(login.this);
                mDialog.setMessage("Processing...");
                mDialog.show();

                String email = mEmailText.getText().toString().trim();
                String pwd = mPasswordText.getText().toString().trim();

                if (email.isEmpty() || pwd.isEmpty()) {
                    mDialog.dismiss();
                    Toast.makeText(login.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mDialog.dismiss();
                    Toast.makeText(login.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                login(email, pwd);
                mDialog.dismiss();
            }
        });
    }

    void login(String email, String pwd) {
        firebaseAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), Main_screen.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(login.this, "Error. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
