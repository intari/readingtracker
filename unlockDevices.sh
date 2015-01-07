#! /bin/bash
#unlock connected android devices
devices=`adb devices | grep 'device$' | cut -f1`
pids=""

for device in $devices
do
    echo "Unlocking device $device"
    adb -s $device shell input keyevent 82
    adb -s $device shell input keyevent 4
    pids="$pids $!"
done

