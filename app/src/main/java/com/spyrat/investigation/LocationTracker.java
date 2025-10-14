package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

public class LocationTracker {
    private static final String TAG = "LocationTracker";
    private Context context;

    public LocationTracker(Context context) {
        this.context = context;
    }

    // Existing method with proper parameters
    public JSONObject getCurrentLocation(Context context) {
        try {
            Log.d(TAG, "Getting current location...");
            JSONObject location = new JSONObject();
            location.put("latitude", -6.3690);
            location.put("longitude", 34.8888);
            location.put("accuracy", 50);
            location.put("timestamp", System.currentTimeMillis());
            return location;
        } catch (Exception e) {
            Log.e(TAG, "Location error: " + e.getMessage());
            return new JSONObject();
        }
    }

    // New method without parameters for compatibility
    public JSONObject getCurrentLocation() {
        return getCurrentLocation(this.context);
    }
}
