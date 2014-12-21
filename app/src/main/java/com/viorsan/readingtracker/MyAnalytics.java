package com.viorsan.readingtracker;

import android.util.Log;
import com.parse.ParseAnalytics;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 21.12.14.
 * Wrapper for Parse Analytics and (in future) other analytics systems
 * Also takes in account running under test harness
 *
 */
public class MyAnalytics {

    public static final String TAG = "MyAnalytics";

    public static void trackAppOpened(MyApplication app,android.content.Intent intent) {
        if (app==null) {
            Log.e(TAG,"trackAppOpened: app is null");
            System.out.println(TAG+":trackAppOpened: app is null");
            return;
        }
        //don't track anything if test harness active
        if (!app.testHarnessActive) {
            Log.d(TAG,"Sending intent "+intent.toString()+" to analytics service");
            ParseAnalytics.trackAppOpened(intent);
        }
        else {
            System.out.println(TAG +":trackAppOpened not sending intent "+intent.toString()+" to analytics service"+". Test harness said so");
        }
    }
    public static void trackEvent(MyApplication app,String name, java.util.Map<java.lang.String,java.lang.String> dimensions) {
        if (app==null) {
            Log.e(TAG,"trackEvent (with dimensions): app is null");
            System.out.println(TAG+":trackEvent (withDimensions): app is null");
            return;
        }
        //don't track anything if test harness active
        if (!app.testHarnessActive) {
            Log.d(TAG,"Sending event "+name+" (with dimensions) to analytics service");
            ParseAnalytics.trackEvent(name,dimensions);
        }
        else {
            System.out.println(TAG +":trackAppOpened not sending event "+name+" (with dimensions) to analytics service"+". Test harness said so");
        }
    }
    public static void trackEvent(MyApplication app,String name) {
        if (app==null) {
            Log.e(TAG,"trackEvent: app is null");
            System.out.println(TAG+":trackEvent: app is null");
            return;
        }
        //don't track anything if test harness active
        if (!app.testHarnessActive) {
            Log.d(TAG,"Sending event "+name+" to analytics service");
            ParseAnalytics.trackEvent(name);
        }
        else {
            System.out.println(TAG +":trackAppOpened not sending event "+name+" to analytics service"+". Test harness said so");
        }

    }
}

