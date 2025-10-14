package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

public class DeviceInfoCollector {
    private static final String TAG = "DeviceInfoCollector";
    private Context context;

    public DeviceInfoCollector(Context context) {
        this.context = context;
    }

    // Existing method with proper parameters
    public JSONObject collectDeviceInfo(Context context) {
        try {
            Log.d(TAG, "Collecting device info...");
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("model", android.os.Build.MODEL);
            deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
            deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", android.os.Build.VERSION.SDK_INT);
            deviceInfo.put("device_id", android.os.Build.SERIAL);
            deviceInfo.put("timestamp", System.currentTimeMillis());
            return deviceInfo;
        } catch (Exception e) {
            Log.e(TAG, "Device info error: " + e.getMessage());
            return new JSONObject();
        }
    }

    // New method without parameters for compatibility
    public JSONObject collectDeviceInfo() {
        return collectDeviceInfo(this.context);
    }
}
