#!/bin/bash
echo "🔧 Inarekebisha Gradle wrapper..."

# Hakikisha folders zipo
mkdir -p gradle/wrapper

# Download gradle-wrapper.jar
wget https://github.com/gradle/gradle/raw/master/gradle/wrapper/gradle-wrapper.jar -O gradle/wrapper/gradle-wrapper.jar

# Hakikisha gradle-wrapper.properties iko sahihi
cat > gradle/wrapper/gradle-wrapper.properties << 'GRADLE_PROPS'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
GRADLE_PROPS

# Hakikisha gradlew ina ruhusa
chmod +x gradlew

echo "✅ Gradle wrapper imerekebishwa"
