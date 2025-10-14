package com.spyrat.investigation;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.net.wifi.WifiManager;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfoCollector {
    
    public static Map<String, String> collectDeviceInfo(Context context) {
        Map<String, String> deviceInfo = new HashMap<>();
        
        try {
            // Device information
            deviceInfo.put("model", Build.MODEL);
            deviceInfo.put("manufacturer", Build.MANUFACTURER);
            deviceInfo.put("android_version", Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", String.valueOf(Build.VERSION.SDK_INT));
            deviceInfo.put("product", Build.PRODUCT);
            deviceInfo.put("device", Build.DEVICE);
            deviceInfo.put("board", Build.BOARD);
            deviceInfo.put("hardware", Build.HARDWARE);
            
            // Network information
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                String macAddress = getMacAddress();
                deviceInfo.put("mac_address", macAddress);
            }
            
            // Telephony information (without IMEI)
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                deviceInfo.put("sim_operator", telephonyManager.getSimOperatorName());
                deviceInfo.put("network_operator", telephonyManager.getNetworkOperatorName());
                // IMEI removed due to permission requirements
                deviceInfo.put("imei", "not_available");
            }
            
        } catch (Exception e) {
            deviceInfo.put("error", e.getMessage());
        }
        
        return deviceInfo;
    }
    
    private static String getMacAddress() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }
                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "not_available";
    }
}
