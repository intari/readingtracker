#! /bin/bash
devices=`adb devices | grep 'device$' | cut -f1`
pids=""

for device in $devices
do
    echo "Clearing device logs from $device "
    adb -s $device logcat -c
    pids="$pids $!"
done
