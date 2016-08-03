package com.viorsan.readingtracker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseAnalytics;

import java.util.HashMap;
import java.util.Map;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.rollbar.android.Rollbar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 21.12.14.
 * Wrapper for Mixpanel and (in future) other analytics systems
 * Also takes in account running in situations where analytics should be disabled
 *
 */
public class MyAnalytics {

    public static final String TAG = "ReadingTracker::Anal";
    public static final String APP_OPENED = "appOpened";
    public static final String APP_STARTED = "AppStarted";
    public static final String USER_USERNAME = "username";
    public static final String USER_EMAIL = "email";
    public static final String USER_FULLNAME = "name";

    private static HashMap<String, String> userData = new HashMap<String, String>();

    private static MyApplication app;
    private static String storedUserId;
    private static Context storedContext;
    public static void init(MyApplication newApp,Context context) {
        Log.d(TAG, "init");
        //we sometimes gets called twice (from app startup and from main activity)
        if (app==null) {
            app=newApp;
        }
        if (storedContext==null) {
            storedContext=context;
        }
        if (AppHelpers.isRunningTestBuild()) {
            Log.d(TAG,"Automatically disabling analytics in test build");
            MyApplication.setAnalyticsEnabled(false);
        }

        if (MyApplication.isAnalyticsEnabled()) {
            //Mixpanel
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
        }

    }
    public static void provideUserdata(String key, String value) {
        userData.put(key,value);
    }
    public static void setUserId(String userId) {
        if (userId==null) {
            return;
        }
        if (MyApplication.isAnalyticsEnabled()) {
            storedUserId=userId;
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
            mixpanel.identify(userId);
        }
    }
    public static void sendUserData() {
        if (MyApplication.isAnalyticsEnabled()) {
            /*
              Prepare and send data for  Mixpanel
             */
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext, BuildConfig.MIXPANEL_TOKEN);
            mixpanel.getPeople().identify(storedUserId);
            for (String key:userData.keySet()) {
                mixpanel.getPeople().set(key,userData.get(key));
            }

        }
    }
    /**
     * Starts 3rd-party analytics, will call {@link MyAnalytics.starAnalytics()}
     * @param context - context to use. cannot be application's context. must be Activity/Service one or at least base
     */
    public static void startAnalyticsWithContext(Context context) {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"startAnalyticsWithContext()");
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
        }
        stopAnalytics();
    }
    /**
     * starts 3rd party analytics. for analytics systems which don't need context
     */
    public static void startAnalytics() {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"startAnalytics()");
        }
    }

    /**
     * pauses 3rd party analytics (and possibile stops). for analytics systems which don't need context
     */
    public static void stopAnalytics() {
        if (MyApplication.isAnalyticsEnabled()) {
            Log.d(TAG,"stopAnalytics()");
            if (storedContext==null) {
                Log.d(TAG,"null storedContext");
                return;
            }
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
            if (mixpanel==null) {
                Log.d(TAG,"null mixpanel?!");
                Rollbar.reportMessage("stopAnalytics() - mixpanel is null", "info");
            }
            else {
                mixpanel.flush();
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
            //TODO:send APP_OPENED event with thos dimensions to Mixpanel
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
            try {

                ParseAnalytics.trackEvent(name,dimensions);
                MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
                JSONObject props = new JSONObject();
                //convert to mixpanel's format
                for (Map.Entry<String, String> entry : dimensions.entrySet())
                {
                    props.put(entry.getKey(),entry.getValue());
                }
                mixpanel.track(name, props);

            } catch (Exception ex) {
                Log.d(TAG,"Failed to log event due to exception:"+ex.toString());
                ex.printStackTrace();
            }
        }
        else {
            Log.d(TAG,"trackEvent not sending event " + name + " (with dimensions) to analytics service");
        }
    }

    /**
     * Signals start of timed event to 3rd-party analytics systems.
     * Currently only Mixpanel is supported
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
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
            mixpanel.timeEvent(name);
        }
        else {
            Log.d(TAG,"trackTimedEventStart not sending event " + name + "  to analytics service");
        }
    }

    /**
     * Signals stop of timed event to 3rd-party analytics systems.
     * Currently only Mixpanel is supported
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
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
            mixpanel.track(name);
        }
        else {
            Log.d(TAG, "trackTimedEventStop not sending event " + name + " (with dimensions) to analytics service");
        }
    }
    /**
     * Signals end of timed event to 3rd-party analytics systems.
     * Currently only Mixpanel is suppored
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
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
            try {
                JSONObject props = new JSONObject();
                //convert to mixpanel's format
                for (Map.Entry<String, String> entry : dimensions.entrySet())
                {
                    props.put(entry.getKey(),entry.getValue());
                }
                mixpanel.track(name, props);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to add properties to JSONObject", e);
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
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(storedContext,BuildConfig.MIXPANEL_TOKEN);
            mixpanel.track(name);
        }
        else {
            Log.d(TAG, "trackEvent not sending event " + name + " to analytics service");
        }

    }
}

