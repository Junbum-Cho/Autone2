package com.example.autone;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class STTService extends Service {

    private SpeechRecognizer speechRecognizer;
    private Intent intent;
    private boolean recording = false;
    private boolean sttActive = true;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the intent
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        startRecording();
    }

    private void startRecording() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(intent);
        recording = true;
        sttActive = true;
        System.out.println("aaa");

        new Handler(Looper.getMainLooper()).postDelayed(this::stopRecording, 8000);
    }

    private void stopRecording() {
        System.out.println("bbb");
        recording = false;
        restartRecording();
    }

    private void restartRecording() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!recording && sttActive) {
                startRecording();
            }
        }, 1000);  // Adjust the delay if necessary
    }

    private boolean matchPhrase(String inputText, String targetPhrase) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(targetPhrase) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputText);
        return matcher.find();
    }

    private final RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    return; // Client error, no message
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    restartRecording();
                    System.out.println("no");
                    return; // No match, no message
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "Recognizer busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "Server error";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "Speech timeout";
                    break;
                default:
                    message = "Unknown error";
                    break;
            }
            Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
            recording = false;
            restartRecording();
        }

        @Override
        public void onResults(Bundle results) {
            recording = false;
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null) {
                String newText = String.join(" ", matches);
                //Toast.makeText(getApplicationContext(), newText, Toast.LENGTH_LONG).show();

                if (matchPhrase(newText, "죄송합니다 죄송합니다 죄송합니다")||newText.contains("알겠습니다 알겠습니다 알겠습니다")) {
                    Toast.makeText(getApplicationContext(), "사용자 시그널 감지됨!: "+ newText, Toast.LENGTH_LONG).show();
                    changeAction();
                    triggerReport();
                } else {
                    System.out.println(newText);
                    restartRecording();
                }
            }
        }

        private void triggerReport() {
            Intent intent = new Intent(getApplicationContext(), crime_detected.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        private void changeAction() {
            stopRecording();
            sttActive = false;

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                sttActive = true;
                restartRecording();
            }, 40000); // Adjust the delay if necessary
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}