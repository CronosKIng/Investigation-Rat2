package com.spyrat.investigation;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkManager {
    private static final String TAG = "SpyratNetwork";
    
    public static String sendPost(String urlString, String postData) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                return response.toString();
            } else {
                Log.e(TAG, "❌ Server returned: " + responseCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Network error: " + e.getMessage());
        }
        return null;
    }
    
    public static String sendGet(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                return response.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ GET request error: " + e.getMessage());
        }
        return null;
    }
    
    // Verify investigator code
    public static boolean verifyInvestigatorCode(String code) {
        try {
            String json = "{\"investigator_code\":\"" + code + "\"}";
            String response = sendPost("https://GhostTester.pythonanywhere.com/api/investigator/verify-code", json);
            
            if (response != null) {
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
                return jsonResponse.getBoolean("valid");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Verify code error: " + e.getMessage());
        }
        return false;
    }
}
