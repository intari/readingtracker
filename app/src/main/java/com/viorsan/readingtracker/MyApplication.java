package com.viorsan.readingtracker;

import android.util.Log;

import com.parse.*;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 23.05.14.
 */




public class MyApplication extends android.app.Application {
    public static final String TAG = "ReadingTracker::MyApplication";
    protected boolean useParseCrashReporting=true;//should we activate Parse's crash reporting ourselves?
    protected boolean initParse=true;//should we init Parse ourselves?
    protected boolean disableAnalytics=false;//true - no analytics should be used
    @Override
    public void onCreate() {

        System.out.println(TAG +":OnCreate");
        //Test harness could disallow us to do this
        if (useParseCrashReporting) {
            // Enable Parse-based Crash Reporting
            Log.d(TAG,"Activating Parse's crash reporting");
            ParseCrashReporting.enable(this);
        }
        else {
            System.out.println(TAG+":Don't activating Parse's crash reporting.");
        }

        if (initParse) {
            Log.d(TAG,"Performing Parse's initialization");
            //enable local datastore (we are write-mostly anyway)
            //Parse.enableLocalDatastore(this);
            //init Parse
            Parse.initialize(this,BuildConfig.PARSE_APP_ID,BuildConfig.PARSE_CLIENT_KEY);
            //enable automatic user support support
            //ParseUser.enableAutomaticUser();
        }
        else {
            System.out.println(TAG+":Don't performing Parse's initialization. ");
        }

        //activate extra logging to debug login system
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        //init ACLs
        //Dropbox-style-only-this-user-can-access-this
        //this could cause issues if there is no cached user
        ParseACL.setDefaultACL(new ParseACL(), true);

        super.onCreate();

        MyAnalytics.init(this,getApplicationContext());
        MyAnalytics.startAnalytics();

    }
    @Override public void onTerminate() {
        System.out.println(TAG +":OnTerminate");
        MyAnalytics.stopAnalytics();

        super.onTerminate();
    }
}


