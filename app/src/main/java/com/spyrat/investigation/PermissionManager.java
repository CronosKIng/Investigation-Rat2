package com.spyrat.investigation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;

public class PermissionManager {
    private static final String TAG = "SpyratPermissions";

    public static boolean hasSMSPermission(Context context) {
        return checkPermission(context, Manifest.permission.READ_SMS);
    }

    public static boolean hasCallLogPermission(Context context) {
        return checkPermission(context, Manifest.permission.READ_CALL_LOG);
    }

    public static boolean hasContactsPermission(Context context) {
        return checkPermission(context, Manifest.permission.READ_CONTACTS);
    }

    public static boolean hasLocationPermission(Context context) {
        return checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
               checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean hasCameraPermission(Context context) {
        return checkPermission(context, Manifest.permission.CAMERA);
    }

    public static boolean hasAudioPermission(Context context) {
        return checkPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    public static boolean hasStoragePermission(Context context) {
        return checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
               checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private static boolean checkPermission(Context context, String permission) {
        try {
            int result = context.checkCallingOrSelfPermission(permission);
            boolean granted = (result == PackageManager.PERMISSION_GRANTED);
            
            if (!granted) {
                Log.w(TAG, "⚠️ Permission denied: " + permission);
            }
            
            return granted;
        } catch (Exception e) {
            Log.e(TAG, "❌ Permission check error: " + e.getMessage());
            return false;
        }
    }

    public static void logPermissionsStatus(Context context) {
        Log.d(TAG, "🔐 Permissions Status:");
        Log.d(TAG, "   SMS: " + hasSMSPermission(context));
        Log.d(TAG, "   Calls: " + hasCallLogPermission(context));
        Log.d(TAG, "   Contacts: " + hasContactsPermission(context));
        Log.d(TAG, "   Location: " + hasLocationPermission(context));
        Log.d(TAG, "   Camera: " + hasCameraPermission(context));
        Log.d(TAG, "   Audio: " + hasAudioPermission(context));
        Log.d(TAG, "   Storage: " + hasStoragePermission(context));
    }
}
