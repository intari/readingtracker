package com.viorsan.readingtracker;

import android.os.Build;
import android.util.Log;

import com.parse.*;
import com.rollbar.*;
import com.rollbar.android.Rollbar;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 23.05.14.
 */




public class MyApplication extends android.app.Application {
    public static final String TAG = "ReadingTracker::MyApp";

    static protected boolean initParse=true;//should we init Parse ourselves?
    static protected boolean analyticsEnabled=true;//true - no analytics should be used
    static protected boolean espressoTestActive=false;//true - Espresso test is being run

    public static boolean isEspressoTestActive() {
        return espressoTestActive;
    }
    public static boolean isAnalyticsEnabled() {
        return analyticsEnabled;
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
            Log.d(TAG,"analytitcs force disabled by higher forces");
        }
        else {
            Log.d(TAG,"analytics force enabled by higher forces");
        }
    }

    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }
    @Override
    public void onCreate() {

        Log.d(TAG,"OnCreate");

        if (isRoboUnitTest()) {
            Log.d(TAG, "Don't activating Parse's crash reporting on Robolectric tests");
        } else {
            //Espresso Test harness could disallow us to do crash reporting
        }

        Rollbar.init(this, BuildConfig.ROLLBAR_API_KEY, "production");
        Rollbar.setIncludeLogcat(true);
        AppHelpers.writeLogBanner("", getApplicationContext());

        if (MyApplication.initParse) {
            Log.d(TAG,"Performing Parse's initialization");
            //enable local datastore (we are write-mostly anyway)
            //Parse.enableLocalDatastore(this);
            //init Parse
            //Parse.initialize(this,BuildConfig.PARSE_APP_ID,BuildConfig.PARSE_CLIENT_KEY);
            //https://github.com/ParsePlatform/parse-server/wiki/Parse-Server-Guide#using-parse-sdks-with-parse-server
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(BuildConfig.PARSE_APP_ID)
                    .clientKey(BuildConfig.PARSE_CLIENT_KEY)
                    //.clientKey(null)
                    .server(BuildConfig.API_SERVER)
            .build()
            );
            //enable revocable sessions (we don't actually have a choice on Parse Server so let's consider we still have it and enable manually -:)
            //https://parse.com/tutorials/session-migration-tutorial
            ParseUser.enableRevocableSessionInBackground();

            //enable automatic user support support
            //ParseUser.enableAutomaticUser();
            ParseFacebookUtils.initialize(this);
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


    }
    @Override public void onTerminate() {
        Log.d(TAG,"OnTerminate");
        MyAnalytics.stopAnalytics();

        super.onTerminate();
    }
}


