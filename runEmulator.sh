#!/bin/bash
#Runs Android emulator for testing purposes
export ANDROID_TARGET=android-19
export ANDROID_ABI=armeabi-v7a
echo "Creating emulator..."
echo no | android create avd --force -n test -t $ANDROID_TARGET --abi $ANDROID_ABI
echo "Starting emulator..."
emulator -avd test -no-skin -no-audio -no-window &
echo "Emulator started. Waiting for startup to finish"
adb wait-for-device
echo "Pressing menu in emulator"
adb shell input keyevent 82 &
echo "Android emulator ready"
