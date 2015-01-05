#! /bin/bash

rm -rf logcat_*.log
#sleep 5
devices=`adb devices | grep 'device$' | cut -f1`
pids=""

for device in $devices
do
    log_file="logcat_$device-`date +%d-%m-%H_%M_%S`.log"
    echo "Logging device $device to \"$log_file\""
    adb -s $device logcat -v time -d > $log_file
    pids="$pids $!"
done

