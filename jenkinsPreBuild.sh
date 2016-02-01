#!/bin/bash
echo "Performing initial tasks..."

echo "Stopping old instances of android emulator"
killall emulator64-arm

echo "Creating Android Virtual Device..."
android create avd --force -n myemulator-google -t "Google Inc.:Google APIs:22" --abi google_apis/armeabi-v7a

echo "Starting emulator..."
emulator64-arm -avd myemulator-google -no-audio -no-window &
echo "Wait until emulator starts up"
./android-wait-for-emulator.sh
#unlock emulator screen
sleep 30
echo "Pressing menu in emulator"
adb shell input keyevent 82

