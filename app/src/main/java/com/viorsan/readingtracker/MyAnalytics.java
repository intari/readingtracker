package com.viorsan.readingtracker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.parse.ParseAnalytics;

import java.util.HashMap;
import java.util.Map;

import ly.count.android.api.Countly;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 21.12.14.
 * Wrapper for Parse Analytics and (in future) other analytics systems
 * Also takes in account running in situations where analytics should be disabled
 *
 */
public class MyAnalytics {

    public static final String TAG = "ReadingTracker::Anal";
    public static final String APP_OPENED = "appOpened";

    private static HashMap<String, String> userData = new HashMap<String, String>();

    private static final boolean flurryEnabled=false;

    private static boolean countlyStarted=false;
    private static MyApplication app;
    public static void init(MyApplication newApp,Context context) {
        Log.d(TAG,"init");
        app=newApp;
        if (AppHelpers.isRunningTestBuild()) {
            Log.d(TAG,"Automatically disabling analytics in test build");
            MyApplication.setAnalyticsEnabled(false);
        }

        if (MyApplication.isAnalyticsEnabled()) {
            Countly.sharedInstance().init(context, BuildConfig.COUNTLY_SERVER, BuildConfig.COUNTLY_APP_KEY);

            if (flurryEnabled) {
                Log.d(TAG,"Flurry Analytics enabled");
                // configure Flurry
                FlurryAgent.setCaptureUncaughtExceptions(false);//Parse will do this for us and in correct way
                FlurryAgent.setLogEnabled(true);
                FlurryAgent.setLogEvents(true);
                // init Flurry
                FlurryAgent.init(context, BuildConfig.FLURRY_API_KEY);
            }
            else {
                Log.d(TAG,"Flurry Analytics not enabled");
            }
        }

    }
    public static void provideUserdata(String key, String value) {
        userData.put(key,value);
    }
    public static void setUserId(String userId) {
        if (MyApplication.isAnalyticsEnabled()) {
            FlurryAgent.setUserId(userId);
        }
    }
    public static void sendUserData() {
        if (MyApplication.isAnalyticsEnabled()) {
            Bundle bundle=new Bundle();
            for (String key:userData.keySet()) {
                bundle.putString(key,userData.get(key));
            }
            Countly.sharedInstance().setUserData(bundle);

        }
    }
    /**
     * Starts 3rd-party analytics, will call {@link MyAnalytics.starAnalytics()}
     * @param context - context to use. cannot be application's context. must be Activity/Service one or at least base
     */
    public static void startAnalyticsWithContext(Context context) {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"startAnalyticsWithContext()");
            if (flurryEnabled) {
                FlurryAgent.onStartSession(context);
            }
        }
        startAnalytics();
    }

    /**
     * Stops 3rd-party analytics, will call {@link MyAnalytics.stopAnalytics()}
     * @param context - context to use. cannot be application's context. must be Activity/Service one or at least base
     */
    public static void stopAnalyticsWithContext(Context context) {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"stopAnalyticsWithContext()");
            if (flurryEnabled) {
                FlurryAgent.onEndSession(context);
            }
        }
        stopAnalytics();
    }
    /**
     * starts 3rd party analytics. for analytics systems which don't need context
     */
    public static void startAnalytics() {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"startAnalytics()");
            Countly.sharedInstance().onStart();
            countlyStarted=true;
        }
    }

    /**
     * pauses 3rd party analytics (and possibile stops). for analytics systems which don't need context
     */
    public static void stopAnalytics() {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"stopAnalytics()");
            if (countlyStarted) {
                Countly.sharedInstance().onStop();
                countlyStarted=false;
            }
        }
    }

    /**
     * Signals 'app opened' intent to 3rd-part analytics systems.
     * @param intent - intent used to open app
     */
    public static void trackAppOpened(android.content.Intent intent) {
        if (app==null) {
            Log.e(TAG,"trackAppOpened: app is null");
            return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending intent "+intent.toString()+" to analytics service");
            ParseAnalytics.trackAppOpened(intent);
            Map<String, String> dimensions = new HashMap<String, String>();
            dimensions.put("intent",intent.toString());
            if (intent.getAction()!=null) {
                dimensions.put("action",intent.getAction());
            }
            if (intent.getPackage()!=null) {
                dimensions.put("package",intent.getPackage());
            }
            if (intent.getType()!=null) {
                dimensions.put("type",intent.getType());
            }
            //TODO:put extras?
            if (flurryEnabled) {
                FlurryAgent.logEvent(APP_OPENED, dimensions);
            }
            Countly.sharedInstance().recordEvent(APP_OPENED,dimensions,1);
        }
        else {
            Log.d(TAG, "trackAppOpened not sending intent " + intent.toString() + " to analytics service");
        }
    }

    /**
     * Signals event to be recorded by 3rd-party analytics systems.
     * @param name - event name
     * @param dimensions -  additional event information
     */
    public static void trackEvent(String name, java.util.Map<java.lang.String,java.lang.String> dimensions) {
        if (app==null) {
            Log.e(TAG,"trackEvent (with dimensions): app is null");
            return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending event "+name+" (with dimensions) to analytics service");
            ParseAnalytics.trackEvent(name,dimensions);
            Countly.sharedInstance().recordEvent(name,dimensions,1);
            if (flurryEnabled) {
                FlurryAgent.logEvent(name, dimensions);
            }
        }
        else {
            Log.d(TAG,"trackEvent not sending event " + name + " (with dimensions) to analytics service");
        }
    }

    /**
     * Signals start of timed event to 3rd-party analytics systems.
     * Currently only Flurry is supported (not Count.ly)
     * @param name - event name
     */
    public static void trackTimedEventStart(String name) {
        if (app==null) {
            Log.e(TAG,"trackTimedEventStart: app is null");
             return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending start of event "+name+"  to analytics service");
            //ParseAnalytics.trackEvent(name,dimensions);
            //Countly.sharedInstance().recordEvent(name,dimensions,1);
            if (flurryEnabled) {
                FlurryAgent.logEvent(name, true);
            }
        }
        else {
            Log.d(TAG,"trackTimedEventStart not sending event " + name + "  to analytics service");
        }
    }

    /**
     * Signals start of timed event to 3rd-party analytics systems.
     * Currently only Flurry is supported (not Count.ly)
     * @param name - event name
     * @param dimensions - additional event information
     */
    public static void trackTimedEventStart(String name, java.util.Map<java.lang.String,java.lang.String> dimensions) {
        if (app==null) {
            Log.e(TAG,"trackTimedEventStart (with dimensions): app is null");
            return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending start of event "+name+" (with dimensions) to analytics service");
            //ParseAnalytics.trackEvent(name,dimensions);
            //Countly.sharedInstance().recordEvent(name,dimensions,1);
            if (flurryEnabled) {
                FlurryAgent.logEvent(name, dimensions, true);
            }
        }
        else {
           Log.d(TAG,"trackTimedEventStart not sending event " + name + " (with dimensions) to analytics service");
        }
    }
    /**
     * Signals stop of timed event to 3rd-party analytics systems.
     * Currently only Flurry is supported (not Count.ly)
     * @param name - event name
     */
    public static void trackTimedEventStop(String name) {
        if (app==null) {
            Log.e(TAG,"trackTimedEventStop: app is null");
            return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending stop of event "+name+" (with dimensions) to analytics service");
            //ParseAnalytics.trackEvent(name,dimensions);
            //Countly.sharedInstance().recordEvent(name,dimensions,1);
            if (flurryEnabled) {
                FlurryAgent.endTimedEvent(name);
            }
        }
        else {
            Log.d(TAG, "trackTimedEventStop not sending event " + name + " (with dimensions) to analytics service");
        }
    }
    /**
     * Signals end of timed event to 3rd-party analytics systems.
     * Currently only Flurry is supported (not Count.ly)
     * @param name - event name
     * @param dimensions - additional event information
     */
    public static void trackTimedEventStop(String name, java.util.Map<java.lang.String,java.lang.String> dimensions) {
        if (app==null) {
            Log.e(TAG,"trackTimedEventStop (with dimensions): app is null");
            return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending stop of event "+name+" (with dimensions) to analytics service");
            //ParseAnalytics.trackEvent(name,dimensions);
            //Countly.sharedInstance().recordEvent(name,dimensions,1);
            if (flurryEnabled) {
                FlurryAgent.endTimedEvent(name, dimensions);
            }
        }
        else {
            Log.d(TAG,"trackTimedEventStop not sending event " + name + " (with dimensions) to analytics service" );
        }
    }
    /**
     * Signals event to be recorded by 3rd-party analytics systems.
     * @param name - event name
     */
    public static void trackEvent(String name) {
        if (app==null) {
            Log.e(TAG,"trackEvent: app is null");
            return;
        }
        //don't track anything if this is disabled on global level
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"Sending event "+name+" to analytics service");
            ParseAnalytics.trackEvent(name);
            Countly.sharedInstance().recordEvent(name,1);
            if (flurryEnabled) {
                FlurryAgent.logEvent(name);
            }
        }
        else {
            Log.d(TAG, "trackEvent not sending event " + name + " to analytics service");
        }

    }
}

