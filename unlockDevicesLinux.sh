#!/bin/bash
#unlock connected android devices
#Linux-only (not OS X)
devices=`adb devices | grep 'device$' | cut -f1`
pids=""

for device in $devices
do
    echo "Unlocking device $device"
    #adb -s $device shell input keyevent 82
    #adb -s $device shell input keyevent 4
    #This is needed for at least 4.3+
    displayState=`adb -s $device shell dumpsys power | grep mScreenOn`
    displayState=$(echo $displayState | sed -e 's/\r//g' )

    if [ "$displayState" = "mScreenOn=true"  ]
    then
       echo "$device:screen arleady on"
    else
       echo "$device:screen was off"
       adb -s $device shell input keyevent 26
    fi
    pids="$pids $!"
done

