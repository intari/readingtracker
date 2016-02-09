#!/bin/bash
#it's assumed that this is run fro regular machine with x86 emulator
./gradlew --continue testInternalDebug connectedAndroidTestInternalDebug jacocoTestInternalDebugReport

