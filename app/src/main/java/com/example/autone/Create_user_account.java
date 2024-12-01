package com.example.autone;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Create_user_account extends AppCompatActivity {

    private static final String TAG = "CreateUserAccount";
    EditText mEmailText, mPasswordText;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1 = database.getReference("User");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        mEmailText = findViewById(R.id.editTextTextPersonName4);
        mPasswordText = findViewById(R.id.editTextTextPassword);

        ImageView previousbtn = findViewById(R.id.imageView79);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Entry_screen.class);
                startActivity(intent);
            }
        });

        Button next = findViewById(R.id.button0);
        next.setOnClickListener(view -> {
            final String email = mEmailText.getText().toString().trim();
            String pwd = mPasswordText.getText().toString().trim();

            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(Create_user_account.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Create_user_account.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pwd.length() < 6) {
                Toast.makeText(Create_user_account.this, "Password should be at least 6 characters long.", Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressDialog mDialog = new ProgressDialog(Create_user_account.this);
            mDialog.setMessage("Processing...");
            mDialog.show();

            firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                        if (emailExists) {
                            mDialog.dismiss();
                            Toast.makeText(Create_user_account.this, "This email address is already in use.", Toast.LENGTH_SHORT).show();
                        } else {
                            createUser(email, pwd, mDialog);
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(Create_user_account.this, "Error checking email. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error checking email: " + task.getException().getMessage());
                    }
                }
            });
        });
    }

    private void createUser(String email, String pwd, ProgressDialog mDialog) {
        firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(Create_user_account.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    DatabaseReference userRef = myRef1.child(uid);

                    HashMap<String, Object> userData = new HashMap<>();
                    userData.put("Email", email);

                    userRef.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mDialog.dismiss();
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), user_identification_upload_before_account.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "Failed to save user data: " + task.getException().getMessage());
                                Toast.makeText(Create_user_account.this, "Failed to save user data. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    mDialog.dismiss();
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthUserCollisionException e) {
                        Toast.makeText(Create_user_account.this, "This email address is already in use.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Email collision: " + e.getMessage());
                    } catch (Exception e) {
                        Toast.makeText(Create_user_account.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Registration error: " + e.getMessage());
                    }
                }
            }
        });
    }
}
