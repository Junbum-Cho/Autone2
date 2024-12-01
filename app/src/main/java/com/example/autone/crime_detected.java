package com.example.autone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class crime_detected extends AppCompatActivity {

    private Vibrator vibrator;
    private CountDownTimer countdownTimer;
    private TextView countdownText;
    private boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_detected);

        countdownText = findViewById(R.id.textView118);
        Button cancelButton = findViewById(R.id.button301);
        Button reportButton = findViewById(R.id.button402);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //startVibration();

        cancelButton.setOnClickListener(v -> {
            isCanceled = true;
            stopVibration();
            finish();
        });

        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Report_progress_screen.class);
            startActivity(intent);
            stopVibration();
            finish();
        });

        startCountdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }

    private void startCountdown() {
        countdownTimer = new CountDownTimer(20000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                if (!isCanceled) {
                    startActivity(new Intent(getApplicationContext(), Report_progress_screen.class));
                    finish();
                }
            }
        }.start();
    }

    private void startVibration() {
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                //vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
            } else {
                //vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
            }
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}