package com.viorsan.dollmaster;

import com.parse.*;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;


/**
 * Created by dkzm on 23.05.14.
 */



@ReportsCrashes(
        formKey = "***REMOVED***"
)

public class MyApplication extends android.app.Application {
    @Override
    public void onCreate() {


        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
        //ACRA.getErrorReporter().setReportSender(new HockeySender());

        // Enable Parse-based Crash Reporting
        ParseCrashReporting.enable(this);
        //enable local datastore (we are write-mostly anyway)
        //TODO:this causes problems with FB. Looks like I'm doing something wrong
        //Parse.enableLocalDatastore(this);
        //init Parse
        Parse.initialize(this, "***REMOVED***", "***REMOVED***");
        //ParseFacebookUtils.initialize("***REMOVED***");
        //enable automatic user support support (user will link to FB anyway)
        //ParseUser.enableAutomaticUser();

        //init ACLs
        //Dropbox-style-only-this-user-can-access-this
        //this could cause issues if there is no cached user
        ParseACL.setDefaultACL(new ParseACL(), true);

        //init logger
        if (Debug.D) {
            Debug.enableDebug(this, true);
            Debug.L.setRemoteHost("***REMOVED***", 50000, true);      // change to your mac's IP address, set a fixed TCP port in the Prefs in desktop NSLogger
            //Debug.L.LOG_MARK("***REMOVED*** startup");
        }
        super.onCreate();
    }
}


