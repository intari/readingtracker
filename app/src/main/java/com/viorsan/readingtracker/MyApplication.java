package com.viorsan.readingtracker;

import android.util.Log;

import com.parse.*;


/**
 * Created by dkzm on 23.05.14.
 */




public class MyApplication extends android.app.Application {
    public static final String TAG = "MyApplication";
    protected boolean useParseCrashReporting=true;//should we activate Parse's crash reporting ourselves?
    protected boolean initParse=true;//should we init Parse ourselves?
    protected boolean testHarnessActive=false;//we are running under Robolectric's test harness
    @Override
    public void onCreate() {

        System.out.println(TAG +":OnCreate");
        if (testHarnessActive) {
            System.out.println(TAG +":Test harness active");
            Log.d(TAG,"Test harness active");
        }
        else
        {
            System.out.println(TAG +":Test harness is not active");
            Log.d(TAG,"Test harness is not active");
        }
        //Test harness could disallow us to do this
        if (useParseCrashReporting) {
            // Enable Parse-based Crash Reporting
            Log.d(TAG,"Activating Parse's crash reporting");
            ParseCrashReporting.enable(this);
        }
        else {
            System.out.println(TAG+":Don't activating Parse's crash reporting. test harness said so");
        }
        //Test harness could disallow us to do this
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
            System.out.println(TAG+":Don't performing Parse's initialization. test harness said so");
        }

        //activate extra logging to debug login system
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        //init ACLs
        //Dropbox-style-only-this-user-can-access-this
        //this could cause issues if there is no cached user
        ParseACL.setDefaultACL(new ParseACL(), true);

        System.out.println(TAG +":OnCreate, calling super's one");
        super.onCreate();
    }
}


