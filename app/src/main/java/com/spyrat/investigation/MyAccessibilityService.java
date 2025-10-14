package com.spyrat.investigation;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            Log.d(TAG, "Accessibility event: " + event.getEventType());
            // Handle accessibility events here
        } catch (Exception e) {
            Log.e(TAG, "Accessibility event error: " + e.getMessage());
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");
        
        // Now this method exists in RemoteController
        RemoteController.setAccessibilityService(this);
    }
}
