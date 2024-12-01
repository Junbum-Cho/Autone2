package com.example.autone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Timer;
import java.util.TimerTask;

public class Load_Autone_Screen extends AppCompatActivity {

    private Timer timer;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_autone_screen);

        firebaseAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth instance

        setupAuthStateListener(); // Initialize the AuthStateListener

        // Check for current authenticated user
        startLoginCheckTimer();
    }

    private void startLoginCheckTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null && !currentUser.isAnonymous()) {
                    // User is properly logged in, navigate to Main_screen
                    Intent intent = new Intent(Load_Autone_Screen.this, Main_screen.class);
                    startActivity(intent);
                } else {
                    // No user is logged in or user is anonymous, navigate to Entry_screen
                    Intent intent = new Intent(Load_Autone_Screen.this, Entry_screen.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 3000);
    }

    // Setup FirebaseAuth AuthStateListener for real-time authentication changes
    private void setupAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null && !currentUser.isAnonymous()) {
                    // User is properly logged in, navigate to Main_screen
                    Intent intent = new Intent(Load_Autone_Screen.this, Main_screen.class);
                    startActivity(intent);
                } else {
                    // No user is logged in or user is anonymous, navigate to Entry_screen
                    Intent intent = new Intent(Load_Autone_Screen.this, Entry_screen.class);
                    startActivity(intent);
                }
                finish();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener); // Start listening for changes
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener); // Stop listening for changes
        }
    }
}