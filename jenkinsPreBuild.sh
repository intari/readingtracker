#!/bin/bash
echo "Performing initial tasks..."
export ADB_INSTALL_TIMEOUT=8 # minutes (2 minutes by default)
export EMULATOR=emulator
#export ANDROID_ABI=google_apis/x86_64  # google_apis/armeabi-v7a
#export ANDROID_TARGET="\"Google Inc.:Google APIs:22\""  #"\"Google Inc.:Google APIs:22\""
export EMULATOR_OPTIONS="-verbose -no-audio -no-window"
#echo "Will be using target $ANDROID_TARGET with ABI $ANDROID_ABI"
adb start-server
echo "Stopping old instances of android emulator"
killall emulator64-arm
killall emulator-arm
killall emulator
killall emulator64-x86
echo "Creating Android Virtual Device..."
#android create avd --force -n myemulator -t "Google Inc.:Google APIs:22" --abi google_apis/armeabi-v7a -s "768x1280"
echo n|android create avd --force -n myemulator -t android-22 --abi default/armeabi-v7a -s "768x1280"
echo "Starting emulator..."
$EMULATOR -avd myemulator $EMULATOR_OPTIONS &
sleep 60
echo "Wait until emulator starts up"
./android-wait-for-emulator.sh
#wait for emulator to go on login screen
#unlock emulator screen
sleep 30
echo "Pressing menu in emulator"
adb shell input keyevent 82


