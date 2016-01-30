package com.viorsan.readingtracker;

import android.app.*;
import android.os.IBinder;

import android.content.*;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.*;

import java.util.*;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 23.05.14.
 */

public class CoreService extends Service  {


    /*
    public static final String FAKEAPP_DEVICELOCKED = "com.viorsan.readingtracker.DeviceLocked";
    public static final String FAKEAPP_SCREENOFF = "com.viorsan.readingtracker.ScreenOff";
    */

    public static final long YEAR_IN_MS = 365 * 86400 * 1000;
    public static final int PROCESSLIST_RESCAN_INTERVAL_MILLIS = 3000;//ONLY used to check if reader app is currently active
    public static final int REPORT_SENDING_RETRY_MILLIS = 3000;
    public static final String TAG = "ReadingTracker::C.S.";
    public static final String USER_LOGGED_OUT_REPORT = "com.viorsan.readingtracker.user_logged_out";
    public static final String ERRORID_NO_CURRENT_PARSE_USER = "NO_CURRENT_PARSE_USER";
    public static final String ERRORCLASS_PARSE_INTERFACE = "PARSE_INTERFACE";

    private BroadcastReceiver currentlyReadingMessageReceiver=null;

    public static String ourDeviceID = "";


    CoreBroadcastReceiver broadcastReceiver = null;

    //private String previousForegroundTask;//can have 'fake' data
    private Boolean isDeviceLocked = false;
    private Boolean isDeviceScreenOff = false;

    private final IBinder mBinder = new LocalBinder();

    private BroadcastReceiver userLoggedOutReceiver=null;



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

        Log.i(TAG, "Dreaming stopped");
        MyAnalytics.trackEvent("DreamingStopped");

    }

    public void onScreenOn() {

        Log.i(TAG,"Screen is on");

        isDeviceScreenOff = false;
        //updateActiveProcessList();
        MyAnalytics.trackEvent("ScreenOn");
    }

    public void onScreenOff() {
        Log.i(TAG,"Screen is off");
        BookReadingsRecorder.getBookReadingsRecorder(this).recordSwitchAwayFromBook(this,SystemClock.elapsedRealtime());
        MyAnalytics.trackEvent("ScreenOff");
        isDeviceScreenOff = true;
        //updateActiveProcessList();
    }

    /*
    public synchronized void updateActiveProcessList() {
        updateActiveProcessListInner();
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

            // Book Scrobbler logic
            BookReadingsRecorder.getBookReadingsRecorder(this).checkIfReadingAppActive(this);

            previousForegroundTask = newForegroundTask;

        }
    }
*/









    protected void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }


    /**
     * Initializes CoreService
     */
    private void init() {

        Log.i(TAG,"Book reading tracker main service starting up");

        //initialize updates for server-side configs
        ParseConfigHelper.refreshConfig();

        MyAnalytics.startAnalyticsWithContext(this);

        //write our device id
        //TODO:modify to work on Android 6.0, disable until it's done
        //ourDeviceID = new DeviceInfoManager().getDeviceId(this);

        //die if not user logged in
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Log.i(TAG, "Not logged in. Service will NOT be started");
            stopSelf();
            return;
        }


        //configure icon
        configureForeground();


        //No longer used in builds which support Android 6.0, in fact I could do without it anyway
        /*

        //find out previous active task
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        if (activityManager == null) {
            previousForegroundTask = BuildConfig.APPLICATION_ID;
        } else {
            List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
            previousForegroundTask = appProcesses.get(0).topActivity.getPackageName();
        }



        //confugure process list updater
        new CountDownTimer(YEAR_IN_MS, PROCESSLIST_RESCAN_INTERVAL_MILLIS) {
            public void onTick(long msUntilFinish) {

                updateActiveProcessList();
            }

            public  void  onFinish() {}
        }.start();
        */


        broadcastReceiver = new CoreBroadcastReceiver();
        broadcastReceiver.setService(this);

        registerReceiver(broadcastReceiver, getFilters());

        //register 'userLoggedOut' receiver so we can die if user logs out
        userLoggedOutReceiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "UI signaled that user is no longer here. Stopping service");
                Log.d(TAG, "BookReadings code signaled to stop sending us data. Stopping us");

                //commit suicide
                stopSelf();
                Log.d(TAG, "stopSelf() called");
            }
        };


        LocalBroadcastManager.getInstance(this).registerReceiver(
                userLoggedOutReceiver,new IntentFilter(USER_LOGGED_OUT_REPORT)
        );



        //ask for registration for reading updates
        registerForReadingUpdates();

        showToast(getResources().getString(R.string.app_started_notification));
    }


    /**
     * Log memory pressure events
     */
    @Override
    public void onLowMemory()
    {
        Log.w(TAG, "onLowMemory");
    }

    /**
     * Log memory pressure events
     * @param level
     */
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

    /**
     * Collects list of IntentFilters to use
     * @return list of intents we want to look at
     */
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


    /**
     * Performs configuration of App's notification icon to run forever
     */
    private void configureForeground() {

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Notification.Builder builder=new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.push_icon);
        builder.setOngoing(true);
        builder.setAutoCancel(false);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.tap_me));
        builder.setContentIntent(pi);
        startForeground(R.drawable.push_icon, builder.build());
    }

    /**
     * Register handlers to update notification message based on reports from BookReading service
     * So user can see pages per minute, title, current page,etc
     */
    private void registerForReadingUpdates() {
        final CoreService self=this;
        currentlyReadingMessageReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String bookTitle=intent.getStringExtra(BookReadingsRecorder.BOOK_TITLE);
                String bookAuthor=intent.getStringExtra(BookReadingsRecorder.BOOK_AUTHOR);
                String bookTags=intent.getStringExtra(BookReadingsRecorder.BOOK_TAGS);
                Double totalTime=intent.getDoubleExtra(BookReadingsRecorder.READING_SESSION_TIME,0);
                String currentPageS=intent.getStringExtra(BookReadingsRecorder.CURRENT_PAGE);
                String totalPageS=intent.getStringExtra(BookReadingsRecorder.TOTAL_PAGES);

                Long pagesRead=intent.getLongExtra(BookReadingsRecorder.PAGES_READ,0);
                Long numPagePageSwitches=intent.getLongExtra(BookReadingsRecorder.NUM_PAGE_SWITCHES,0);

                double pagesPerSecond=pagesRead.doubleValue()/totalTime.doubleValue();


                String msg;
                String title;
                if (currentPageS!=null) {
                    if (pagesRead==0) {
                        msg=getResources().getString(R.string.tapMeCurrentlyReadingLongZeroSpeed,bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0);
                    }
                    else {
                        msg=getResources().getString(R.string.tapMeCurrentlyReadingLong,bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0,pagesPerSecond*60.0);
                    }
                }
                else
                {
                    msg=getResources().getString(R.string.tapMeCurrentlyReadingShort,bookTitle,bookAuthor);
                }
                title=getResources().getString(R.string.titleCurrentlyReading,bookTitle,bookAuthor);

                Log.i(TAG,"Got reading update:"+msg);

                Notification note = new Notification(R.drawable.push_icon,
                        getResources().getString(R.string.app_started_notification),
                        System.currentTimeMillis());

                PendingIntent pi = PendingIntent.getActivity(self, 0, new Intent(self, MainActivity.class), 0);


                Notification.Builder builder=new Notification.Builder(context);
                builder.setSmallIcon(R.drawable.push_icon);
                builder.setOngoing(true);
                builder.setAutoCancel(false);
                builder.setContentTitle(title);
                builder.setContentText(msg);
                builder.setContentIntent(pi);


                //update notification
                NotificationManager manager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
                manager.notify(R.drawable.push_icon,builder.build());


            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                currentlyReadingMessageReceiver,new IntentFilter(BookReadingsRecorder.BOOK_READING_STATUS_UPDATE)
        );
    }


    /**
     * Handles & dispatches received broadcasts
     * @param intent broadcast intent to handle
     */
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

    /**
     * Unregisters Broadcast Receivers
     */
    private void stopReceivers() {
        Log.d(TAG,"Unregistering receivers");
        if (broadcastReceiver!=null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (userLoggedOutReceiver!=null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(userLoggedOutReceiver);
        }
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