package com.example.autone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.media.audiofx.AutomaticGainControl; // Import AGC
import android.media.audiofx.NoiseSuppressor;       // Import Noise Suppressor
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundClassifierHelper {
    private static final String TAG = "SoundClassifierHelper";
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final int SAMPLE_RATE = 16000;
    private static final int AUDIO_DURATION_SECONDS = 10; // 10 seconds of audio

    // Target spectrogram dimensions
    private static final int TARGET_HEIGHT = 438;
    private static final int TARGET_WIDTH = 256;

    private Interpreter tflite;
    private AudioRecord audioRecord;
    public AtomicBoolean isRecording = new AtomicBoolean(false);
    private Handler handler = new Handler(Looper.getMainLooper());

    private Context context;
    private Vibrator vibrator;

    private static SoundClassifierHelper instance;

    private WeakReference<SoundClassifierListener> listenerRef;
    private WeakReference<PermissionRequestListener> permissionListenerRef;

    public interface SoundClassifierListener {
        void onSoundDetected(String detectedClassName, float confidence);
    }

    public interface PermissionRequestListener {
        void onPermissionRequired();
    }

    public boolean isRecording() {
        return isRecording.get();  // Return the current recording state
    }

    private SoundClassifierHelper(Context context) {
        this.context = context.getApplicationContext(); // Use application context to prevent leaks

        // Initialize TensorFlow Lite Interpreter
        try {
            tflite = new Interpreter(loadModelFile(), getInterpreterOptions());
            Log.d(TAG, "TensorFlow Lite model loaded.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load TensorFlow Lite model.", e);
        }

        // Initialize Vibrator service
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static synchronized SoundClassifierHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SoundClassifierHelper(context);
        }
        return instance;
    }

    public void setListener(SoundClassifierListener listener) {
        if (listener != null) {
            this.listenerRef = new WeakReference<>(listener);
        } else {
            this.listenerRef = null;
        }
    }

    public void setPermissionRequestListener(PermissionRequestListener listener) {
        if (listener != null) {
            this.permissionListenerRef = new WeakReference<>(listener);
        } else {
            this.permissionListenerRef = null;
        }
    }

    // Load the TensorFlow Lite Model
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("crime_detection_model_31.tflite"); // Use the correct model file name
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        try {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } finally {
            inputStream.close();
        }
    }

    // Configure Interpreter Options (e.g., threading, delegates)
    private Interpreter.Options getInterpreterOptions() {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(Runtime.getRuntime().availableProcessors());
        return options;
    }

    public void startRecordingWithSlidingWindow() {
        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid AudioRecord buffer size");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted");
            // Notify listener to request permission
            if (permissionListenerRef != null && permissionListenerRef.get() != null) {
                handler.post(() -> permissionListenerRef.get().onPermissionRequired());
            }
            return;
        }

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, // Using MIC to capture all sounds
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
        );

        // Enable Automatic Gain Control if available
        if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl agc = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            if (agc != null) {
                agc.setEnabled(true);  // Enable AGC for gain control
                Log.d(TAG, "Automatic Gain Control enabled.");
            }
        } else {
            Log.w(TAG, "Automatic Gain Control not available.");
        }

        // Enable Noise Suppressor if available
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
            if (noiseSuppressor != null) {
                noiseSuppressor.setEnabled(true);
                Log.d(TAG, "Noise Suppressor enabled.");
            }
        } else {
            Log.w(TAG, "Noise Suppressor not available.");
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed.");
            audioRecord = null;
            return;
        }

        audioRecord.startRecording();
        isRecording.set(true);

        new Thread(() -> {
            try {
                // Buffer to accommodate 10 seconds of audio
                final short[] slidingWindowBuffer = new short[SAMPLE_RATE * AUDIO_DURATION_SECONDS];
                final int[] slidingWindowPosition = {0};
                short[] readBuffer = new short[minBufferSize];

                Log.d(TAG, "Recording started, processing audio...");

                while (isRecording.get()) {
                    int shortsRead = audioRecord.read(readBuffer, 0, minBufferSize);

                    if (shortsRead > 0) {
                        // Copy audio data to the sliding window buffer
                        int availableSpace = slidingWindowBuffer.length - slidingWindowPosition[0];
                        int toCopy = Math.min(shortsRead, availableSpace);
                        System.arraycopy(readBuffer, 0, slidingWindowBuffer, slidingWindowPosition[0], toCopy);
                        slidingWindowPosition[0] += toCopy;

                        // Once the buffer is filled (10 seconds of audio), process it
                        if (slidingWindowPosition[0] >= slidingWindowBuffer.length) {
                            Log.d(TAG, "10 seconds of recording completed. Starting analysis.");

                            float[][] spectrogram = computeSpectrogram(slidingWindowBuffer);
                            if (spectrogram != null && spectrogram.length > 0) {
                                Log.d(TAG, "Spectrogram generated. Shape: " + spectrogram.length + " x " + spectrogram[0].length);
                                runInference(spectrogram);
                            } else {
                                Log.e(TAG, "Failed to generate spectrogram.");
                            }

                            // Reset sliding window position for the next segment
                            slidingWindowPosition[0] = 0;
                        }
                    } else {
                        Log.e(TAG, "AudioRecord read failed.");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in recording thread.", e);
            } finally {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                } else {
                    Log.e(TAG, "AudioRecord is null. Cannot stop or release.");
                }
                Log.d(TAG, "Recording stopped.");
            }
        }).start();
    }

    public void stopRecording() {
        if (audioRecord != null && isRecording.get()) {
            isRecording.set(false);
        }
    }

    private float[][] computeSpectrogram(short[] audioBuffer) {
        int fftSize = 512;  // Frame length
        int hopSize = 256;  // Frame step

        int numFrames = (audioBuffer.length - fftSize) / hopSize + 1;
        float[][] spectrogram = new float[numFrames][fftSize / 2 + 1];

        // Initialize FFT transformer
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        // Hann window (to match the training code)
        double[] window = new double[fftSize];
        for (int j = 0; j < fftSize; j++) {
            window[j] = 0.5 * (1 - Math.cos(2 * Math.PI * j / (fftSize - 1)));  // Hann window
        }

        for (int i = 0; i < numFrames; i++) {
            double[] frame = new double[fftSize];
            int startIdx = i * hopSize;

            for (int j = 0; j < fftSize; j++) {
                // Normalize using 32767.0 to match training code
                frame[j] = audioBuffer[startIdx + j] / 32767.0 * window[j];
            }

            // Perform FFT
            Complex[] fftResult = fft.transform(frame, TransformType.FORWARD);
            for (int j = 0; j < fftSize / 2 + 1; j++) {
                spectrogram[i][j] = (float) fftResult[j].abs();  // Magnitude
            }
        }

        // Resize spectrogram to (438, 256)
        return resizeSpectrogram(spectrogram, TARGET_HEIGHT, TARGET_WIDTH);
    }

    private void runInference(float[][] spectrogram) {
        // Spectrogram is already resized to (438, 256)
        // Prepare input ByteBuffer with channel dimension
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * TARGET_HEIGHT * TARGET_WIDTH * 1 * 4); // float32
        inputBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = inputBuffer.asFloatBuffer();

        for (int i = 0; i < TARGET_HEIGHT; i++) {
            for (int j = 0; j < TARGET_WIDTH; j++) {
                floatBuffer.put(spectrogram[i][j]);
            }
        }

        // Prepare output buffer
        float[][] output = new float[1][6];  // 1 sample, 6 classes

        if (tflite != null) {
            try {
                tflite.run(inputBuffer, output);
                processModelOutput(output);
            } catch (Exception e) {
                Log.e(TAG, "Model inference failed.", e);
            }
        } else {
            Log.e(TAG, "TensorFlow Lite interpreter not initialized.");
        }
    }

    private void processModelOutput(float[][] output) {
        String[] classNames = {"배경음", "비명소리", "총소리", "유리창 깨지는 소리", "사이렌 소리", "신고 시그널"};
        float maxConfidence = output[0][0];
        int detectedClassIndex = 0;

        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxConfidence) {
                maxConfidence = output[0][i];
                detectedClassIndex = i;
            }
        }

        Log.d(TAG, classNames[detectedClassIndex] + "을 " + maxConfidence + "의 정확도로 판별함.");

        // Apply consistent confidence threshold
        if ((maxConfidence > CONFIDENCE_THRESHOLD && detectedClassIndex != 0)||(detectedClassIndex == 5 && maxConfidence > 0.45)) { // Exclude "Background Noise"
            final String detectedClassName = classNames[detectedClassIndex];
            final float confidence = maxConfidence;

            // 리스너에게 감지된 클래스와 신뢰도를 전달
            if (listenerRef != null && listenerRef.get() != null) {
                handler.post(() -> listenerRef.get().onSoundDetected(detectedClassName, confidence));
            }

            // Notify the listener
            if (listenerRef != null && listenerRef.get() != null) {
                handler.post(() -> listenerRef.get().onSoundDetected(detectedClassName, confidence));
            }

            // Optional: Trigger vibration or other notifications
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        }
    }

    private float[][] resizeSpectrogram(float[][] spectrogram, int targetHeight, int targetWidth) {
        float[][] resizedSpectrogram = new float[targetHeight][targetWidth];
        int originalHeight = spectrogram.length;
        int originalWidth = spectrogram[0].length;

        for (int i = 0; i < targetHeight; i++) {
            float originalIdxHeight = ((float) i * originalHeight) / targetHeight;
            int idx1Height = (int) originalIdxHeight;
            int idx2Height = Math.min(idx1Height + 1, originalHeight - 1);
            float fractionHeight = originalIdxHeight - idx1Height;

            for (int j = 0; j < targetWidth; j++) {
                float originalIdxWidth = ((float) j * originalWidth) / targetWidth;
                int idx1Width = (int) originalIdxWidth;
                int idx2Width = Math.min(idx1Width + 1, originalWidth - 1);
                float fractionWidth = originalIdxWidth - idx1Width;

                // Bilinear interpolation
                float value1 = (1 - fractionHeight) * spectrogram[idx1Height][idx1Width] + fractionHeight * spectrogram[idx2Height][idx1Width];
                float value2 = (1 - fractionHeight) * spectrogram[idx1Height][idx2Width] + fractionHeight * spectrogram[idx2Height][idx2Width];
                resizedSpectrogram[i][j] = (1 - fractionWidth) * value1 + fractionWidth * value2;
            }
        }

        return resizedSpectrogram;
    }
}
