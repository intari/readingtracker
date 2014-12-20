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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.*;

import java.util.*;


/**
 * Created by dkzm on 23.05.14.
 */

public class CoreService extends Service  {


    public static final String REPORT_TYPE_DEVICE_REPORT = "DeviceInformation";
    public static final String FAKEAPP_DEVICELOCKED = "com.viorsan.readingtracker.DeviceLocked";
    public static final String FAKEAPP_SCREENOFF = "com.viorsan.readingtracker.ScreenOff";


    public static final long YEAR_IN_MS = 365 * 86400 * 1000;
    public static final int PROCESSLIST_RESCAN_INTERVAL_MILLIS = 3000;//ONLY used to check if reader app is currently active
    public static final int REPORT_SENDING_RETRY_MILLIS = 3000;
    public static final String TAG = "ReadingTracker::CoreService";
    public static final String USER_LOGGED_OUT_REPORT = "com.viorsan.readingtracker.user_logged_out";
    private long lastAppCheckTime;


    // helpers for Play Services
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



    private static ParseObject mDetails =null;
    private static final String CURRENT_SESSION_ID = "CURRENT_SESSION_ID";
    private static final String CURRENT_REQUEST_ID = "CURRENT_REQUEST_ID";





    public Long getRequestId() {
        synchronized (requestIdSyncObject)
        {
            Long res=nextRequestId;
            nextRequestId=nextRequestId+1L;
            //store request identifier
            Context context=this.getBaseContext();
            SharedPreferences sharedPreferences = context.getSharedPreferences(CURRENT_SESSION_ID, Context.MODE_PRIVATE);
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
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Device unlocked");

        isDeviceLocked = false;
        //updateActiveProcessList();
    }

    /* TODO: make it use keyguard */
    public void onDeviceLock() {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Device locked");
        BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchAwayFromBook(this.getBaseContext(),SystemClock.elapsedRealtime());

        isDeviceLocked = true;
        //updateActiveProcessList();
    }

    public void onDreamingStarted() {

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Dreaming started");
        BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchAwayFromBook(this.getBaseContext(),SystemClock.elapsedRealtime());

    }

    public void onDreamingStopped() {

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Dreaming stopped");

    }

    public void onScreenOn() {

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Screen is on");

        isDeviceScreenOff = false;
        updateActiveProcessList();
    }

    public void onScreenOff() {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Screen is off");
        BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchAwayFromBook(this.getBaseContext(),SystemClock.elapsedRealtime());

        isDeviceScreenOff = true;
        updateActiveProcessList();
    }

    public synchronized void updateActiveProcessList() {

        PowerManager pm=(PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                CoreService.appID);

        wl.acquire();

        long uptimeNow=SystemClock.uptimeMillis();
        Long timeSinceLastCheck=uptimeNow-lastAppCheckTime;

        updateActiveProcessListInner();

        long checkFinishedTime=SystemClock.uptimeMillis();
        Long timeToCheck=checkFinishedTime-uptimeNow;
        if (timeToCheck>600)
        {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Process list check took  "+timeToCheck.toString()+",ms, >0 ms");
        }
        lastAppCheckTime=checkFinishedTime;
        wl.release();
    }

    private void updateActiveProcessListInner() {

        ActivityManager activityManager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);

        if (activityManager == null) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "updateProcessList:null activity manager");
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
            BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).checkIfReadingAppActive(this.getBaseContext());


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
                    Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Save report to Parse failed. Will retry. Report type was "+reportToSend.getClassName());
                    if (mDelayReporter==null) {
                        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Cannot retry. DelayReporter is null");
                        return;

                    }
                    else
                    {
                        mDelayReporter.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Retrying "+reportToSend.getClassName());
                                if (!saveReportToParseReal(reportToSend)) {
                                    Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Save report to Parse failed on retry. Will retry. Report type was "+reportToSend.getClassName());

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
            Log.d(TAG,"User is not logged in. Will not send reports");
            return Boolean.FALSE;
        }
        if (mDetails==null) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Cannot save report to Parse. No device details. Report type was "+report.getClassName());
            return Boolean.FALSE;
        }

        report.put("deviceId",ourDeviceID);

        //don't save link to this object itself
        if (report.getClassName()!=REPORT_TYPE_DEVICE_REPORT) {
            report.put("device",mDetails);
        }

        java.util.Date date = new java.util.Date();

        report.put("clientEventCreateTime", DateHelper.formatISO8601_iOS(date));
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Cannot save report to Parse. No current user. Report type was "+report.getClassName());
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
                    //Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Saved report "+reportClass+" to parse. heartrate "+mCurrentHeartRate);
                } else {
                    Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Not saved report "+reportClass+"to parse: " + e.toString());
                }
            }
        });

        return  Boolean.TRUE;

    }



    private boolean playServicesAvailable() {
        int resultCode=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode==ConnectionResult.SUCCESS;
    }

    private void reportDeviceInfo() {

        ParseQuery query = new ParseQuery(REPORT_TYPE_DEVICE_REPORT);
        query.whereEqualTo("deviceId",ourDeviceID);


        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null) {
                    if (list.size()>0) {
                        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_WARN,"retrived list with "+list.size()+" elements for deviceInfoList. killing extra");
                        for (ParseObject obj:list) {
                            obj.deleteInBackground();
                        }
                    }
                    Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "calling reportDeviceInfoReal in async way");
                    reportDeviceInfoReal();
                } else {
                    Debug.L.LOG_EXCEPTION(e);
                }
            }
        });

    }
    private void reportDeviceInfoReal() {
        if (mDetails ==null) {
            mDetails =new ParseObject(REPORT_TYPE_DEVICE_REPORT);
        }

        DeviceInfoManager deviceInfoManager = new DeviceInfoManager();

        String runtimeName=deviceInfoManager.getCurrentRuntimeValue();

        mDetails.put("systemName", "Android");
        mDetails.put("systemVersion", Build.VERSION.RELEASE);
        mDetails.put("model", Build.PRODUCT);

        mDetails.put("platformString", Build.MODEL);
        mDetails.put("platform", Build.BOARD);
        mDetails.put("hwModel", Build.HARDWARE);
        mDetails.put("bootloader",Build.BOOTLOADER);
        mDetails.put("userVisibleBuildID",Build.DISPLAY);
        mDetails.put("fingerprint",Build.FINGERPRINT);
        mDetails.put("manufacturer",Build.MANUFACTURER);
        mDetails.put("brand",Build.BRAND);

        mDetails.put("runtime",runtimeName);



        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Device Identifier:"+ourDeviceID);

        if (playServicesAvailable()) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Google Play Services present");
            mDetails.put("GooglePlayServicesInstalled",Boolean.TRUE);
        }
        else
        {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Google Play Services not present");
            mDetails.put("GooglePlayServicesInstalled",Boolean.FALSE);
        }


        //Phone network details
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager.getNetworkOperatorName()!=null) {
            mDetails.put("operatorName", telephonyManager.getNetworkOperatorName());
        }
        if (telephonyManager.getSimCountryIso()!=null) {
            mDetails.put("simCountryCode", telephonyManager.getSimCountryIso());
        }
        if (telephonyManager.getSimOperatorName()!=null) {
            mDetails.put("simOperatorName", telephonyManager.getSimOperatorName());
        }


        saveReportToParse(mDetails);
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
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" (c) Dmitriy Kazimirov 2013-2014");
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" e-mail: dmitriy.kazimirov@viorsan.com");

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" BuildAuthority:"+buildAuthority());
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" ApplicationId:"+BuildConfig.APPLICATION_ID);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" BuildType:"+BuildConfig.BUILD_TYPE);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" VersionCode:"+BuildConfig.VERSION_CODE);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" VersionName:"+BuildConfig.VERSION_NAME);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" Flavor:"+BuildConfig.FLAVOR);
        if (BuildConfig.DEBUG) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" BuildConfig:DEBUG");
        }
        else
        {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" BuildConfig:RELEASE");
        }









    }
    private void init() {

        //Debug.L.LOG_MARK("ReadingTracker starting up");

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Book reading tracker main service starting up");
        CoreService.writeLogBanner("", getApplicationContext());

        ParseConfigHelper.refreshConfig();
        
        ourDeviceID = new DeviceInfoManager().getDeviceId(getBaseContext());

        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Not logged in. Service will NOT be started");
            stopSelf();
            return;
        }
        userLoggedIn=true;
        reportDeviceInfo();

        sessionId = System.currentTimeMillis();
        nextRequestId = 0L;
        //TODO:check for session restart

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Checking for session restart. new SID:" + sessionId + ". RID:" + nextRequestId);


        Context context=this.getBaseContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(CURRENT_SESSION_ID, Context.MODE_PRIVATE);
        Long oldSID=sharedPreferences.getLong(CURRENT_SESSION_ID,sessionId);
        if (oldSID!=sessionId)
        {
            Long oldRID=sharedPreferences.getLong(CURRENT_REQUEST_ID,Context.MODE_PRIVATE);
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Session restart detected. was " + oldSID + " but now " + sessionId + ". Last stored RID was " + oldRID);


        }
        else
        {
           Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "no session restart? at startup?!");
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(CURRENT_SESSION_ID,new Long(sessionId));
        editor.commit();

   
        configureForeground();


        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

        if (activityManager == null) {
            previousForegroundTask = "com.viorsan.readingtracker";
        } else {
            List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
            previousForegroundTask = appProcesses.get(0).topActivity.getPackageName();
        }

        appActivationTime = SystemClock.elapsedRealtime();

        BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).setMasterService(this);


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
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "onLowMemory");
    }
    @Override
    public void onTrimMemory(int level) {
        switch (level)
        {
            case TRIM_MEMORY_UI_HIDDEN:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "TRIM_MEMORY_UI_HIDDEN");
                break;
            case TRIM_MEMORY_BACKGROUND:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "TRIM_MEMORY_BACKGROUND");
                break;
            case TRIM_MEMORY_COMPLETE:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "TRIM_MEMORY_COMPLETE");
                break;
            case TRIM_MEMORY_MODERATE:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "TRIM_MEMORY_MODERATE");
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_ERROR, "TRIM_MEMORY_RUNNING_CRITICAL");
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"TRIM_MEMORY_RUNNING_LOW");
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
    }

}