package com.viorsan.readingtracker;

import android.app.*;
import android.os.Handler;
import android.os.IBinder;

import android.content.*;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.parse.*;

import java.util.*;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 23.05.14.
 */

public class CoreService extends Service  {


    public static final String FAKEAPP_DEVICELOCKED = "com.viorsan.readingtracker.DeviceLocked";
    public static final String FAKEAPP_SCREENOFF = "com.viorsan.readingtracker.ScreenOff";


    public static final long YEAR_IN_MS = 365 * 86400 * 1000;
    public static final int PROCESSLIST_RESCAN_INTERVAL_MILLIS = 3000;//ONLY used to check if reader app is currently active
    public static final int REPORT_SENDING_RETRY_MILLIS = 3000;
    public static final String TAG = "ReadingTracker::CoreService";
    public static final String USER_LOGGED_OUT_REPORT = "com.viorsan.readingtracker.user_logged_out";
    private long lastAppCheckTime;


    // Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;


    private boolean userLoggedIn=false;

    private Handler mDelayReporter=null;

    public static String ourDeviceID = "";


    private static final String appID="ReadingTracker";

    CoreBroadcastReceiver broadcastReceiver = null;

    private String previousForegroundTask;//can have 'fake' data
    private Boolean isDeviceLocked = false;
    private Boolean isDeviceScreenOff = false;

    private final IBinder mBinder = new LocalBinder();

    private Long appActivationTime;
    private long nextRequestId;
    private long sessionId;
    private final Object requestIdSyncObject=new Object();

    private BroadcastReceiver userLoggedOutReceiver;


    private static final String CURRENT_SESSION_ID = "CURRENT_SESSION_ID";
    private static final String CURRENT_REQUEST_ID = "CURRENT_REQUEST_ID";





    public Long getRequestId() {
        synchronized (requestIdSyncObject)
        {
            Long res=nextRequestId;
            nextRequestId=nextRequestId+1L;
            //store request identifier
            SharedPreferences sharedPreferences = this.getSharedPreferences(CURRENT_SESSION_ID, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(CURRENT_REQUEST_ID,res);
            editor.commit();

            return res;
        }
    }

    public Long getSessionId() {
        return sessionId;
    }

    CoreService getService() {
        return CoreService.this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        CoreService getService() {
            return CoreService.this;
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig)  {

    }

    /* TODO: make it use keyguard */
    public void onDeviceUnlock() {
        Log.i(TAG,"Device unlocked");

        isDeviceLocked = false;
        //updateActiveProcessList();
        MyAnalytics.trackEvent("DeviceUnlocked");
    }

    /* TODO: make it use keyguard */
    public void onDeviceLock() {
        Log.i(TAG,"Device locked");
        BookReadingsRecorder.getBookReadingsRecorder(this).recordSwitchAwayFromBook(this,SystemClock.elapsedRealtime());
        MyAnalytics.trackEvent("DeviceLocked");

        isDeviceLocked = true;
        //updateActiveProcessList();
    }

    public void onDreamingStarted() {

        Log.i(TAG,"Dreaming started");
        BookReadingsRecorder.getBookReadingsRecorder(this).recordSwitchAwayFromBook(this,SystemClock.elapsedRealtime());
        MyAnalytics.trackEvent("DreamingStarted");

    }

    public void onDreamingStopped() {

        Log.i(TAG,"Dreaming stopped");
        MyAnalytics.trackEvent("DreamingStopped");

    }

    public void onScreenOn() {

        Log.i(TAG,"Screen is on");

        isDeviceScreenOff = false;
        updateActiveProcessList();
        MyAnalytics.trackEvent("ScreenOn");
    }

    public void onScreenOff() {
        Log.i(TAG,"Screen is off");
        BookReadingsRecorder.getBookReadingsRecorder(this).recordSwitchAwayFromBook(this,SystemClock.elapsedRealtime());
        MyAnalytics.trackEvent("ScreenOff");
        isDeviceScreenOff = true;
        updateActiveProcessList();
    }

    public synchronized void updateActiveProcessList() {
        long uptimeNow=SystemClock.uptimeMillis();

        updateActiveProcessListInner();

        long checkFinishedTime=SystemClock.uptimeMillis();
        lastAppCheckTime=checkFinishedTime;
    }

    private void updateActiveProcessListInner() {

        ActivityManager activityManager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);

        if (activityManager == null) {
            Log.i(TAG, "updateProcessList:null activity manager");
            return;
        }

        List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
        String newForegroundTask = appProcesses.get(0).topActivity.getPackageName();

        if (isDeviceLocked) {
            newForegroundTask = FAKEAPP_DEVICELOCKED;
        } else if (isDeviceScreenOff) {
            newForegroundTask = FAKEAPP_SCREENOFF;
        }

        if (!newForegroundTask.equals(previousForegroundTask)) {

            /* Book Scrobbler logic */
            BookReadingsRecorder.getBookReadingsRecorder(this).checkIfReadingAppActive(this);


            long now = SystemClock.elapsedRealtime();
            //long timeSpent = now - appActivationTime;

            appActivationTime = now;


            previousForegroundTask = newForegroundTask;

        }
    }




    //If you use this function from other classes this mean you KNOW what you are doing
    public void saveReportToParse(ParseObject report) {
        final ParseObject reportToSend=report;
        Thread reportThread=new Thread( new Runnable() {
            @Override
            public void run() {
                if (!saveReportToParseReal(reportToSend)) {
                    Log.i(TAG, "Save report to Parse failed. Will retry. Report type was "+reportToSend.getClassName());
                    if (mDelayReporter==null) {
                        Log.i(TAG, "Cannot retry. DelayReporter is null");
                        return;

                    }
                    else
                    {
                        mDelayReporter.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                Log.i(TAG, "Retrying "+reportToSend.getClassName());
                                if (!saveReportToParseReal(reportToSend)) {
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
    private Boolean saveReportToParseReal(ParseObject report) {
        if (!userLoggedIn) {
            Log.d(TAG, "User is not logged in. Will not send reports");
            return Boolean.FALSE;
        }

        report.put("deviceId",ourDeviceID);


        java.util.Date date = new java.util.Date();

        report.put("clientEventCreateTime", DateHelper.formatISO8601_iOS(date));
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Log.i(TAG, "Cannot save report to Parse. No current user. Report type was "+report.getClassName());
            return Boolean.FALSE;
        }
        report.put("user",currentUser);

        /* originating app details  */
        report.put("appBuildType",BuildConfig.BUILD_TYPE);
        report.put("appBuildFlavor",BuildConfig.FLAVOR);
        report.put("appBuildVersionCode",BuildConfig.VERSION_CODE);
        report.put("appBuildVersionName",BuildConfig.VERSION_NAME);
        report.put("appBuildApplicationID",BuildConfig.APPLICATION_ID);
        report.put("appBuildAuthority",buildAuthority());



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





    protected void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    private static String buildAuthority() {
        String authority = BuildConfig.APPLICATION_ID+".";
        authority += BuildConfig.FLAVOR;
        if (BuildConfig.DEBUG) {
            authority += ".debug";
        }
        return authority;
    }

    static public void writeLogBanner(String tag, Context context) {
        Log.i(TAG," (c) Dmitriy Kazimirov 2013-2014");
        Log.i(TAG," e-mail: dmitriy.kazimirov@viorsan.com");

        Log.i(TAG," BuildAuthority:"+buildAuthority());
        Log.i(TAG," ApplicationId:"+BuildConfig.APPLICATION_ID);
        Log.i(TAG," BuildType:"+BuildConfig.BUILD_TYPE);
        Log.i(TAG," VersionCode:"+BuildConfig.VERSION_CODE);
        Log.i(TAG," VersionName:"+BuildConfig.VERSION_NAME);
        Log.i(TAG," Flavor:"+BuildConfig.FLAVOR);
        if (BuildConfig.DEBUG) {
            Log.i(TAG," BuildConfig:DEBUG");
        }
        else
        {
            Log.i(TAG," BuildConfig:RELEASE");
        }
        Log.i(TAG," BuilderType:"+BuildConfig.BUILDER_TYPE);
        Log.i(TAG," Built on "+BuildConfig.BUILD_HOST+ " of type "+BuildConfig.BUILDER_TYPE+ " by user "+ BuildConfig.BUILD_USER);
        Log.i(TAG," Flurry release:"+ FlurryAgent.getReleaseVersion());








    }
    private void init() {

        Log.i(TAG,"Book reading tracker main service starting up");
        CoreService.writeLogBanner("", getApplicationContext());

        ParseConfigHelper.refreshConfig();
        MyAnalytics.startAnalyticsWithContext(this);
        
        ourDeviceID = new DeviceInfoManager().getDeviceId(this);

        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Log.i(TAG, "Not logged in. Service will NOT be started");
            stopSelf();
            return;
        }
        userLoggedIn=true;


        sessionId = System.currentTimeMillis();
        nextRequestId = 0L;
        //TODO:check for session restart

        Log.i(TAG, "Checking for session restart. new SID:" + sessionId + ". RID:" + nextRequestId);


        SharedPreferences sharedPreferences = getSharedPreferences(CURRENT_SESSION_ID, Context.MODE_PRIVATE);
        Long oldSID=sharedPreferences.getLong(CURRENT_SESSION_ID,sessionId);
        if (oldSID!=sessionId)
        {
            Long oldRID=sharedPreferences.getLong(CURRENT_REQUEST_ID,Context.MODE_PRIVATE);
            Log.i(TAG, "Session restart detected. was " + oldSID + " but now " + sessionId + ". Last stored RID was " + oldRID);


        }
        else
        {
           Log.i(TAG, "no session restart? at startup?!");
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(CURRENT_SESSION_ID,Long.valueOf(sessionId));
        editor.commit();

   
        configureForeground();


        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

        if (activityManager == null) {
            previousForegroundTask = BuildConfig.APPLICATION_ID;
        } else {
            List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
            previousForegroundTask = appProcesses.get(0).topActivity.getPackageName();
        }

        appActivationTime = SystemClock.elapsedRealtime();

        BookReadingsRecorder.getBookReadingsRecorder(this).setMasterService(this);


        //TODO:don't do this in deep sleep
        new CountDownTimer(YEAR_IN_MS, PROCESSLIST_RESCAN_INTERVAL_MILLIS) {
            public void onTick(long msUntilFinish) {

                updateActiveProcessList();
            }

            public  void  onFinish() {}
        }.start();



        broadcastReceiver = new CoreBroadcastReceiver();
        broadcastReceiver.setService(this);

        registerReceiver(broadcastReceiver, getFilters());

        userLoggedOutReceiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "UI signaled that user is no longer here. Stopping service");
                userLoggedIn=false;
                //tell BookReadingsRecorder we no longer accept data (even if parse helper won't save them anyway)
                BookReadingsRecorder.getBookReadingsRecorder(getBaseContext()).setMasterService(null);
                Log.d(TAG, "BookReadings code signaled to stop sending us data. Stopping us");

                //commit suicide
                stopSelf();
                Log.d(TAG, "stopSelf() called");
            }
        };


        LocalBroadcastManager.getInstance(this).registerReceiver(
                userLoggedOutReceiver,new IntentFilter(USER_LOGGED_OUT_REPORT)
        );


        //prepare handler for posting data in case of delays
        mDelayReporter=new Handler();


        showToast(getResources().getString(R.string.app_started_notification));
    }


    @Override
    public void onLowMemory()
    {
        Log.w(TAG, "onLowMemory");
    }
    @Override
    public void onTrimMemory(int level) {
        switch (level)
        {
            case TRIM_MEMORY_UI_HIDDEN:
                Log.w(TAG, "TRIM_MEMORY_UI_HIDDEN");
                break;
            case TRIM_MEMORY_BACKGROUND:
                Log.w(TAG, "TRIM_MEMORY_BACKGROUND");
                break;
            case TRIM_MEMORY_COMPLETE:
                Log.w(TAG, "TRIM_MEMORY_COMPLETE");
                break;
            case TRIM_MEMORY_MODERATE:
                Log.w(TAG, "TRIM_MEMORY_MODERATE");
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                Log.w(TAG, "TRIM_MEMORY_RUNNING_CRITICAL");
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                Log.w(TAG,"TRIM_MEMORY_RUNNING_LOW");
                break;
        }
    }
    private IntentFilter getFilters() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_CALL);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        //intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);

         //to be sure we get sleep
        intentFilter.addAction(Intent.ACTION_DREAMING_STARTED);
        intentFilter.addAction(Intent.ACTION_DREAMING_STOPPED);
        //TODO:phone logic too
        return intentFilter;
    }

    @Override
    public void onCreate() {
        init();
    }



    private void configureForeground() {
        Notification note = new Notification(R.drawable.readingtracker,
                getResources().getString(R.string.app_started_notification),
                System.currentTimeMillis());

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MyActivity.class), 0);

        note.setLatestEventInfo(this, getResources().getText(R.string.app_name),
                getResources().getText(R.string.tap_me), pi);
        note.flags |= Notification.FLAG_NO_CLEAR;
        note.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(R.drawable.readingtracker, note);
    }

    private void processBroadcastInternal(Intent intent) {
        if (intent == null) return;
        if (intent.getAction() == null) return;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        } else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            onDeviceUnlock(); //only at boot

        } else if (intent.getAction().equals(Intent.ACTION_CALL)) {
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            onScreenOff();
            onDeviceLock();

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            onDeviceUnlock();
            onScreenOn();

        } else if (intent.getAction().equals(Intent.ACTION_DREAMING_STARTED)) {
            onDreamingStarted();
        } else if (intent.getAction().equals(Intent.ACTION_DREAMING_STOPPED)) {
            onDreamingStopped();
        }

    }

    private void stopReceivers() {
        Log.d(TAG,"Unregistering receivers");
        unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userLoggedOutReceiver);
        Log.d(TAG,"Unregistered receivers");

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processBroadcastInternal(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"OnDestroy() called");

        stopReceivers();
        stopForeground(true);
        Log.d(TAG,"Called stopForeground()");
        MyAnalytics.startAnalyticsWithContext(this);
    }

}