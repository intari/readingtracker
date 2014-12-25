package com.viorsan.readingtracker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.parse.ParseAnalytics;

import java.util.HashMap;

import ly.count.android.api.Countly;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 21.12.14.
 * Wrapper for Parse Analytics and (in future) other analytics systems
 * Also takes in account running under test harness
 *
 */
public class MyAnalytics {

    public static final String TAG = "MyAnalytics";

    private static HashMap<String, String> userData = new HashMap<String, String>();

    private static MyApplication app;
    public static void init(MyApplication newApp,Context context) {
        Log.d(TAG,"init");
        app=newApp;
        if (!app.testHarnessActive) {
            Countly.sharedInstance().init(context, BuildConfig.COUNTLY_SERVER, BuildConfig.COUNTLY_APP_KEY);

            // configure Flurry
            FlurryAgent.setLogEnabled(false);
            // init Flurry
            FlurryAgent.init(context, BuildConfig.FLURRY_API_KEY);
        }

    }
    public static void provideUserdata(String key, String value) {
        userData.put(key,value);
    }

    public static void sendUserData() {
        if (!app.testHarnessActive) {
            Bundle bundle=new Bundle();
            for (String key:userData.keySet()) {
                bundle.putString(key,userData.get(key));
            }
            Countly.sharedInstance().setUserData(bundle);

        }
    }
    public static void startAnalyticsWithContext(Context context) {
        Log.d(TAG,"startAnalyticsWithContext()");
        if (!app.testHarnessActive) {
            FlurryAgent.onStartSession(context);
        }
    }

    public static void stopAnalyticsWithContext(Context context) {
        Log.d(TAG,"stopAnalyticsWithContext()");
        if (!app.testHarnessActive) {
            FlurryAgent.onEndSession(context);
        }
    }
    public static void startAnalytics() {
        Log.d(TAG,"startAnalytics()");
        if (!app.testHarnessActive) {
            Countly.sharedInstance().onStart();
        }
    }
    public static void stopAnalytics() {
        Log.d(TAG,"stopnalytics()");
        if (!app.testHarnessActive) {
            Countly.sharedInstance().onStop();
        }
    }
    public static void trackAppOpened(android.content.Intent intent) {
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
    public static void trackEvent(String name, java.util.Map<java.lang.String,java.lang.String> dimensions) {
        if (app==null) {
            Log.e(TAG,"trackEvent (with dimensions): app is null");
            System.out.println(TAG+":trackEvent (withDimensions): app is null");
            return;
        }
        //don't track anything if test harness active
        if (!app.testHarnessActive) {
            Log.d(TAG,"Sending event "+name+" (with dimensions) to analytics service");
            ParseAnalytics.trackEvent(name,dimensions);
            Countly.sharedInstance().recordEvent(name,dimensions,1);
            FlurryAgent.logEvent(name,dimensions);
        }
        else {
            System.out.println(TAG +":trackAppOpened not sending event "+name+" (with dimensions) to analytics service"+". Test harness said so");
        }
    }
    public static void trackEvent(String name) {
        if (app==null) {
            Log.e(TAG,"trackEvent: app is null");
            System.out.println(TAG+":trackEvent: app is null");
            return;
        }
        //don't track anything if test harness active
        if (!app.testHarnessActive) {
            Log.d(TAG,"Sending event "+name+" to analytics service");
            ParseAnalytics.trackEvent(name);
            Countly.sharedInstance().recordEvent(name,1);
            FlurryAgent.logEvent(name);
        }
        else {
            System.out.println(TAG +":trackAppOpened not sending event "+name+" to analytics service"+". Test harness said so");
        }

    }
}

