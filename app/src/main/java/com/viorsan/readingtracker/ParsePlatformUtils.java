package com.viorsan.readingtracker;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 * Wrapper for some Parse Platform functions. mainly to make it easy to test my programs
 * and to wrap some commonly used (by me) Parse functions
 * */
public class ParsePlatformUtils {
    public static final String TAG = "ReadingTracker::ParsePlatformUtils";

    private static final int REPORT_SENDING_RETRY_MILLIS = 3000;

    private static final String ERRORID_NO_CURRENT_PARSE_USER = "NO_CURRENT_PARSE_USER";
    private static final String ERRORCLASS_PARSE_INTERFACE = "PARSE_INTERFACE";

    public enum ParsePlatformMode {NORMAL, TEST_LOGGED_IN, TEST_NOT_LOGGED_IN}
    //by default use real Parse Platform objects
    private static ParsePlatformMode parsePlatformMode=ParsePlatformMode.NORMAL;
    //emulation object
    private static ParseUser_TestLoggedIn cachedParseUserTestLoggedIn =null;

    private static Handler delayReporter=null;
    private static String ourDeviceId=null;

    //configure how 'real' we work
    public static void setParsePlatformMode(ParsePlatformMode newMode) {
        parsePlatformMode=newMode;
    }
    public static ParsePlatformMode getParsePlatformMode() {
        return parsePlatformMode;
    }
    static ParseUser_TestLoggedIn getLoggedInEmulationObject() {
        return cachedParseUserTestLoggedIn;
    }

    static public ParseUser getCurrentParseUser() {

        switch (parsePlatformMode) {
            case NORMAL:
                //no emulation
                return ParseUser.getCurrentUser();
             case TEST_NOT_LOGGED_IN:
                //assume not logged in
                return null;
            case TEST_LOGGED_IN:
                cachedParseUserTestLoggedIn =new ParseUser_TestLoggedIn();
                return cachedParseUserTestLoggedIn;

        }
        return null;//we should never reach here if platform mode was correctly setup
    }

    /**
     * Makes appropriate 'channel name'
     * @param name - source string
     * @return string which can be used as channel name without errors
     */
    static public String makeChannelName(String name){
        return name.replaceAll("\\s+","_");
    }


    private static void checkInit() {
        if (delayReporter==null) {
            //prepare handler for posting data in case of delays
            delayReporter=new Handler();
        }

    }

    /**
     * Saves report to ParsePlatform
     * Handles retry logic (saveReportToParseReal will handle necessary fields,etc)
     * @param report - report to save
     * @param context - context to use
     */
    //If you use this function from other classes this mean you KNOW what you are doing
    public static void saveReportToParse(final ParseObject report,final Context context) {
        checkInit();
        final ParseObject reportToSend=report;
        Thread reportThread=new Thread( new Runnable() {
            @Override
            public void run() {
                if (!saveReportToParseReal(reportToSend,context)) {
                    Log.i(TAG, "Save report to Parse failed. Will retry. Report type was " + reportToSend.getClassName());
                    if (delayReporter==null) {
                        Log.i(TAG, "Cannot retry. DelayReporter is null");
                        return;

                    }
                    else
                    {
                        delayReporter.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                Log.i(TAG, "Retrying "+reportToSend.getClassName());
                                if (!saveReportToParseReal(reportToSend,context)) {
                                    Log.i(TAG, "Save report to Parse failed on retry. Will retry. Report type was "+reportToSend.getClassName());

                                }
                            }
                        }, REPORT_SENDING_RETRY_MILLIS);
                    }

                }


            };

        });

        reportThread.start();

    }

    /**
     * Saves report to ParsePlatform (real thing)
     * Handles adding necessary fields,etc
     * @param report - report to save
     * @param context - context to use
     * @return was it ok
     */
    private static Boolean saveReportToParseReal(ParseObject report,Context context) {
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Log.d(TAG, "User is not logged in. Will not send reports");
            Log.i(TAG, "Cannot save report to Parse. No current user. Report type was "+report.getClassName());
            //TODO:do this on mixpanel
            //FlurryAgent.onError(ERRORID_NO_CURRENT_PARSE_USER, "cannot save object of class " + report.getClassName() + " - no current user!", ERRORCLASS_PARSE_INTERFACE);
            return Boolean.FALSE;
        }

        //TODO:do something on Android 6.0
        /*
        if (ourDeviceId==null) {
            ourDeviceId =new DeviceInfoManager().getDeviceId(context);
        }
        */

        //report.put("deviceId",ourDeviceId);


        java.util.Date date = new java.util.Date();

        report.put("clientEventCreateTime", DateHelper.formatISO8601_iOS(date));

        report.put("user",currentUser);

        /* originating app details  */
        report.put("appBuildType",BuildConfig.BUILD_TYPE);
        report.put("appBuildFlavor",BuildConfig.FLAVOR);
        report.put("appBuildVersionCode",BuildConfig.VERSION_CODE);
        report.put("appBuildVersionName",BuildConfig.VERSION_NAME);
        report.put("appBuildApplicationID",BuildConfig.APPLICATION_ID);
        report.put("appBuildAuthority",AppHelpers.buildAuthority());



        final String reportClass=report.getClassName();
        report.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    //Log.i(TAG, "Saved report "+reportClass+" to parse. heartrate "+mCurrentHeartRate);
                } else {
                    Log.i(TAG, "Not saved report "+reportClass+"to parse: " + e.toString());
                }
            }
        });

        return  Boolean.TRUE;

    }
}
