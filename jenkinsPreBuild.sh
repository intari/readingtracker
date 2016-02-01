#!/bin/bash
echo "Performing initial tasks..."
echo $ANDROID_TARGET="Google Inc.:Google APIs:22"
echo $ANDROID_ABI=google_apis/armeabi-v7a

echo "Creating AVD..."
android create avd --force -n myemulator-google -t $ANDROID_TARGET --abi $ANDROID_ABI
echo "Starting emulator..."
emulator64-arm -avd myemulator-google -no-audio -no-window &
echo "Wait until emulator starts up"
./android-wait-for-emulator.sh
#unlock emulator screen
sleep 30
echo "Pressing menu in emulator"
adb shell input keyevent 82

