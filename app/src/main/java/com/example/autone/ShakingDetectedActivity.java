package com.example.autone;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShakingDetectedActivity extends AppCompatActivity {

    private CountDownTimer countdownTimer;
    private Vibrator vibrator;
    private TextView countdownText;
    private boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shaking_detected);

        countdownText = findViewById(R.id.textView115);
        Button cancelButton = findViewById(R.id.button300);
        Button reportButton = findViewById(R.id.button400);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //startVibration();

        cancelButton.setOnClickListener(v -> {
            isCanceled = true;
            stopVibration();
            finish();
        });

        reportButton.setOnClickListener(v -> {
            stopVibration();
            startActivity(new Intent(ShakingDetectedActivity.this, Report_progress_screen.class));
            finish();
        });

        startCountdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVibration();
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
                    stopVibration();
                    startActivity(new Intent(ShakingDetectedActivity.this, Report_progress_screen.class));
                    finish();
                }
            }
        }.start();
    }

    private void startVibration() {
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
            }
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
