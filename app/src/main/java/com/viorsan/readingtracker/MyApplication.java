package com.viorsan.readingtracker;

import com.parse.*;


/**
 * Created by dkzm on 23.05.14.
 */




public class MyApplication extends android.app.Application {
    @Override
    public void onCreate() {


        // Enable Parse-based Crash Reporting
        ParseCrashReporting.enable(this);
        //enable local datastore (we are write-mostly anyway)
        //Parse.enableLocalDatastore(this);
        //init Parse
        Parse.initialize(this,BuildConfig.PARSE_APP_ID,BuildConfig.PARSE_CLIENT_KEY);
        //enable automatic user support support
        //ParseUser.enableAutomaticUser();

        //activate extra logging to debug login system
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        //init ACLs
        //Dropbox-style-only-this-user-can-access-this
        //this could cause issues if there is no cached user
        ParseACL.setDefaultACL(new ParseACL(), true);

        //init logger
        if (Debug.D) {
            Debug.enableDebug(this, true);
            //Debug.L.setRemoteHost("site.domain.com", 50000, true);      // change to your mac's IP address, set a fixed TCP port in the Prefs in desktop NSLogger
            //Debug.L.LOG_MARK("ReadingTracker startup");
        }
        super.onCreate();
    }
}


