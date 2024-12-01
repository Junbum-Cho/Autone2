package com.example.autone;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrimeDataCleaner {
    private static final long CLEANUP_INTERVAL = 5 * 60 * 1000; // 5 minutes
    private static final long EXPIRATION_TIME = 15 * 60 * 1000; // 15 minutes
    private final Handler handler;
    private final DatabaseReference realTimeCrimeRef;

    public CrimeDataCleaner() {
        handler = new Handler(Looper.getMainLooper());
        realTimeCrimeRef = FirebaseDatabase.getInstance().getReference("RealTime Crime");
        scheduleCleanup();
    }

    private void scheduleCleanup() {
        handler.postDelayed(this::cleanUpOldCrimes, CLEANUP_INTERVAL);
    }

    private void cleanUpOldCrimes() {
        realTimeCrimeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot crimeSnapshot : citySnapshot.getChildren()) {
                        String timestamp = crimeSnapshot.child("timestamp").getValue(String.class);
                        Log.d("CrimeDataCleaner", "Checking crime with timestamp: " + timestamp);
                        if (timestamp != null && isCrimeExpired(timestamp)) {
                            Log.d("CrimeDataCleaner", "Removing expired crime: " + crimeSnapshot.getKey());
                            crimeSnapshot.getRef().removeValue();
                        }
                    }
                }
                scheduleCleanup(); // Schedule the next cleanup
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CrimeDataCleaner", "Failed to read crime data: " + error.getMessage());
                scheduleCleanup(); // Schedule the next cleanup even if there's an error
            }
        });
    }

    private boolean isCrimeExpired(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date crimeDate = sdf.parse(timestamp);
            if (crimeDate != null) {
                long crimeTime = crimeDate.getTime();
                long currentTime = System.currentTimeMillis();
                Log.d("CrimeDataCleaner", "Current time: " + currentTime + ", Crime time: " + crimeTime);
                return currentTime - crimeTime > EXPIRATION_TIME;
            }
        } catch (ParseException e) {
            Log.e("CrimeDataCleaner", "Failed to parse timestamp: " + e.getMessage());
        }
        return false;
    }
}
