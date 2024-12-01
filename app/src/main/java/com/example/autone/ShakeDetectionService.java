package com.example.autone;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class ShakeDetectionService extends Service {

    private static final String TAG = "ShakeDetectionService";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;
    private PowerManager.WakeLock wakeLock;
    private Vibrator vibrator;
    private boolean isTestMode = false; // Set this to true during testing

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize sensors and managers
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.e(TAG, "SensorManager is null");
        }

        shakeDetector = new ShakeDetector();

        // Acquire a wake lock to keep the CPU running
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ShakeDetectionService::WakelockTag");
            wakeLock.acquire();
        } else {
            Log.e(TAG, "PowerManager is null");
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        startForegroundNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Register sensor listeners with a higher sampling rate
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.e(TAG, "SensorManager or Accelerometer is null");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister sensor listeners and release wake lock
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopVibration();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundNotification() {
        // Create a notification channel for foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ShakeDetectionServiceChannel",
                    "Shake Detection Service",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        }

        Intent notificationIntent = new Intent(this, Main_screen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        Notification notification = new NotificationCompat.Builder(this, "ShakeDetectionServiceChannel")
                .setContentTitle("Autone 비이성적인 흔들림 감지")
                .setContentText("폰의 흔들림 모니터링 중...")
                .setSmallIcon(R.drawable.icon_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
    }

    private void startVibration() {
        if (vibrator != null) {
            // Check for VIBRATE permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            } else {
                Log.e(TAG, "VIBRATE permission not granted");
            }
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private class ShakeDetector implements SensorEventListener {

        // Thresholds for detecting shaking
        private final float SHAKE_THRESHOLD = isTestMode ? 5.0f : 15.0f; // Adjust these values as needed
        private final int SHAKE_COUNT_THRESHOLD = isTestMode ? 3 : 4; // Number of shakes required
        private final long SHAKE_TIME_WINDOW_MS = isTestMode ? 1000 : 1500; // Time window in milliseconds

        private long firstShakeTime = 0;
        private int shakeCount = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Calculate the acceleration magnitude
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float accelerationMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

                // Normalize acceleration (subtract gravity)
                float acceleration = accelerationMagnitude - SensorManager.GRAVITY_EARTH;

                // Log the acceleration magnitude
                Log.d(TAG, "Acceleration Magnitude: " + acceleration);

                if (acceleration > SHAKE_THRESHOLD) {
                    long currentTime = System.currentTimeMillis();

                    if (firstShakeTime == 0) {
                        // First shake detected
                        firstShakeTime = currentTime;
                        shakeCount = 1;
                        Log.d(TAG, "First shake detected. Time: " + firstShakeTime);
                    } else {
                        if ((currentTime - firstShakeTime) <= SHAKE_TIME_WINDOW_MS) {
                            shakeCount++;
                            Log.d(TAG, "Shake count incremented. Count: " + shakeCount);

                            if (shakeCount >= SHAKE_COUNT_THRESHOLD) {
                                handleShakeDetected();
                                resetShakeDetection();
                            }
                        } else {
                            // Time window exceeded, reset shake detection
                            resetShakeDetection();
                            Log.d(TAG, "Shake time window exceeded. Resetting shake detection.");
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not used
        }

        private void resetShakeDetection() {
            firstShakeTime = 0;
            shakeCount = 0;
        }

        private void handleShakeDetected() {
            Log.d(TAG, "Severe shaking detected.");

            // Start vibration
            startVibration();

            // Check if the app is in the foreground
            if (isAppInForeground()) {
                // Start the activity directly
                Intent intent = new Intent(ShakeDetectionService.this, ShakingDetectedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                // Build a notification to alert the user
                Intent intent = new Intent(ShakeDetectionService.this, ShakingDetectedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        ShakeDetectionService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ShakeDetectionService.this, "ShakeDetectionServiceChannel")
                        .setSmallIcon(R.drawable.icon_notification)
                        .setContentTitle("심각한 흔들림 감지됨")
                        .setContentText("앱을 열려면 눌러주세요.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_ALARM);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ShakeDetectionService.this);

                // Check for POST_NOTIFICATIONS permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(ShakeDetectionService.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "POST_NOTIFICATIONS permission not granted. Notification not sent.");
                        return;
                    }
                }

                notificationManager.notify(2, builder.build());
            }
        }
    }

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;

        final String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}