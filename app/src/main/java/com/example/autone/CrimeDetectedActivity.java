package com.example.autone;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CrimeDetectedActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private TextView countdownTextView;
    private int countdown = 20; // 20 seconds countdown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_detected);

        countdownTextView = findViewById(R.id.textView118);
        Button noIssueButton = findViewById(R.id.button301);
        Button emergencyButton = findViewById(R.id.button402);

        noIssueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                finish();
            }
        });

        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                // Implement emergency call or notification logic here
            }
        });

        startCountdown();
    }

    private void startCountdown() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (countdown > 0) {
                    countdownTextView.setText(String.valueOf(countdown));
                    countdown--;
                    handler.postDelayed(this, 1000);
                } else {
                    // Trigger emergency action if countdown reaches zero
                    triggerEmergencyAction();
                }
            }
        };
        handler.post(runnable);
    }

    private void triggerEmergencyAction() {
        // Implement emergency call or notification logic here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}