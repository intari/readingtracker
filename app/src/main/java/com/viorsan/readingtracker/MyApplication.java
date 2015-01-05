package com.viorsan.readingtracker;

import android.util.Log;

import com.parse.*;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 23.05.14.
 */




public class MyApplication extends android.app.Application {
    public static final String TAG = "ReadingTracker::MyApplication";

    static protected boolean useParseCrashReporting=true;//should we activate Parse's crash reporting ourselves?
    static protected boolean initParse=true;//should we init Parse ourselves?
    static protected boolean analyticsEnabled=true;//true - no analytics should be used

    public static boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }
    public static void setUseParseCrashReporting(boolean newValue) {
        useParseCrashReporting=newValue;
        if (useParseCrashReporting) {
            Log.d(TAG,"Parse crash reporting enabled by giher forces");
        }
        else {
            Log.d(TAG,"Parse crash reporting disabled by higher forces");
        }
    }
    public static void setInitParse(boolean newValue) {
        initParse=newValue;
        if (initParse) {
            Log.d(TAG,"Parse init enabled by higher forces");
        }
        else {
            Log.d(TAG,"Parse init disabled by higher forces");
        }
    }
    public static void setAnalyticsEnabled(boolean newValue) {
        analyticsEnabled=newValue;
        if (!analyticsEnabled) {
            Log.d(TAG,"analytitcs force disable by higher forces");
        }
        else {
            Log.d(TAG,"analytics force enabled by higher forces (doesn't make a lot of sense)");
        }
    }

    @Override
    public void onCreate() {

        Log.d(TAG,":OnCreate");
        //Test harness could disallow us to do this
        if (MyApplication.useParseCrashReporting) {
            // Enable Parse-based Crash Reporting
            Log.d(TAG,"Activating Parse's crash reporting");
            ParseCrashReporting.enable(this);
        }
        else {
            System.out.println(TAG+":Don't activating Parse's crash reporting.");
        }
        
        AppHelpers.writeLogBanner("", getApplicationContext());

        if (MyApplication.initParse) {
            Log.d(TAG,"Performing Parse's initialization");
            //enable local datastore (we are write-mostly anyway)
            //Parse.enableLocalDatastore(this);
            //init Parse
            Parse.initialize(this,BuildConfig.PARSE_APP_ID,BuildConfig.PARSE_CLIENT_KEY);
            //enable automatic user support support
            //ParseUser.enableAutomaticUser();
        }
        else {
            Log.d(TAG,"Don't performing Parse's initialization. ");
        }

        //activate extra logging to debug login system
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        //init ACLs
        //Dropbox-style-only-this-user-can-access-this
        //this could cause issues if there is no cached user
        ParseACL.setDefaultACL(new ParseACL(), true);

        super.onCreate();

        if (MyApplication.isAnalyticsEnabled()) {
            MyAnalytics.init(this,getApplicationContext());
            MyAnalytics.startAnalytics();

        }

    }
    @Override public void onTerminate() {
        System.out.println(TAG +":OnTerminate");
        MyAnalytics.stopAnalytics();

        super.onTerminate();
    }
}


