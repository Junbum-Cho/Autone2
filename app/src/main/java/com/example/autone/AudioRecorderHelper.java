package com.example.autone;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecorderHelper {
    private static final int SAMPLE_RATE = 44100;
    private int bufferSize;
    private AudioRecord audioRecord;
    private static final String TAG = "AudioRecorderHelper";

    public AudioRecorderHelper(Context context) {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        Log.d(TAG, "AudioRecorderHelper initialized with buffer size: " + bufferSize);
    }

    public void startRecording() {
        try {
            audioRecord.startRecording();
            Log.d(TAG, "Recording started");
        } catch (SecurityException e) {
            Log.e(TAG, "Error starting recording", e);
        }
    }

    public int readAudioData(short[] buffer) {
        try {
            int readSize = audioRecord.read(buffer, 0, buffer.length);
            Log.d(TAG, "Audio data read: " + readSize + " bytes"); //이거 잘 나옴!
            for (int i = 0; i < readSize; i++) {
                Log.d(TAG, "Audio data[" + i + "]: " + buffer[i]);
            }
            return readSize;
        } catch (Exception e) {
            Log.e(TAG, "Error reading audio data", e);
            return 0;
        }
    }

    public void stopRecording() {
        try {
            audioRecord.stop();
            Log.d(TAG, "Recording stopped");
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping recording", e);
        }
    }
}