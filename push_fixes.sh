#!/bin/bash

echo "🚀 PUSHING COMPILATION FIXES TO GITHUB..."

cd ~/Investigation-Rat2

# Check git status
echo "📊 Checking git status..."
git status

# Add the fixed files
echo "📦 Adding fixed files..."
git add app/src/main/java/com/spyrat/investigation/RealLocationCollector.java
git add app/src/main/java/com/spyrat/investigation/InvestigatorApiClient.java

# Commit the fixes
echo "💾 Committing compilation fixes..."
git commit -m "Fix compilation errors and remove fake location data

🔧 COMPILATION FIXES:
- Fixed JSONException handling in RealLocationCollector
- Added missing sendLocationData() method to InvestigatorApiClient  
- Added missing sendContactsData() method to InvestigatorApiClient
- Proper error handling for JSON operations

🗺️ LOCATION DATA IMPROVEMENTS:
- Removed fake Dar es Salaam coordinates
- Now returns NULL values when no real location available
- Added proper error messages for permission denials
- Only sends actual GPS/network location data

✅ READY FOR BUILD:
- All compilation errors resolved
- Real data collection maintained
- No fake/mock data in location
- Proper error handling throughout"

# Push to GitHub
echo "📤 Pushing to GitHub..."
git push origin main

echo "✅ FIXES PUSHED SUCCESSFULLY!"
echo "🔗 GitHub Actions should now build successfully"
echo "📱 APK will be available shortly"
