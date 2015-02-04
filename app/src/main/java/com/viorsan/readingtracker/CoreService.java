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
import org.geometerplus.android.fbreader.api.*;

import java.util.Date;
import java.util.List;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 23.05.14.
 */

public class CoreService extends Service implements ApiClientImplementation.ConnectionListener,ApiListener  {


    public static final String FAKEAPP_DEVICELOCKED = "com.viorsan.readingtracker.DeviceLocked";
    public static final String FAKEAPP_SCREENOFF = "com.viorsan.readingtracker.ScreenOff";


    public static final long YEAR_IN_MS = 365 * 86400 * 1000;
    public static final int PROCESSLIST_RESCAN_INTERVAL_MILLIS = 3000;//ONLY used to check if reader app is currently active
    public static final int FBREADER_POLL_INTERVAL_MILLIS = 5000;//poll FBReader for updates.

    public static final int REPORT_SENDING_RETRY_MILLIS = 3000;
    public static final String TAG = "ReadingTracker::CoreService";
    public static final String USER_LOGGED_OUT_REPORT = "com.viorsan.readingtracker.user_logged_out";
    public static final String ERRORID_NO_CURRENT_PARSE_USER = "NO_CURRENT_PARSE_USER";
    public static final String ERRORCLASS_PARSE_INTERFACE = "PARSE_INTERFACE";
    public static final double MINIMAL_READING_PROGRESS_FBREADER = 0.0001;//minimal reading progress we care for in case we monitor FBReader

    private BroadcastReceiver currentlyReadingMessageReceiver=null;

    public static String ourDeviceID = "";


    CoreBroadcastReceiver broadcastReceiver = null;

    private String previousForegroundTask;//can have 'fake' data
    private Boolean isDeviceLocked = false;
    private Boolean isDeviceScreenOff = false;

    private final IBinder mBinder = new LocalBinder();

    private BroadcastReceiver userLoggedOutReceiver=null;


    //FBReader support API client
    private ApiClientImplementation myApi;
    //hash of last book
    private String fbReaderHash="";
    private boolean connectedToFBReader=false;
    private boolean inReadingModeFBReader=false;
    //how much we read, if we don't knew yet...it's Not A Number. right?
    private float fbReadingProgress=Float.MIN_VALUE;

    CoreService getService() {
        return CoreService.this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * init connection with FBReader
     */
    private void initFBReader() {
        // Connect to FBReader API
        myApi = new ApiClientImplementation(this, this);
    }
    /**
     * (re)Connect to FB Reader
     */
    private void connectToFBReader() {
        if (myApi==null) {
            Log.e(TAG,"use initFBReader first");
        }
        else {
            myApi.connect();
        }
    }
    /**
     * Disconnect from FBReader
     */
    private void disconnectFromFBReader() {
        inReadingModeFBReader=false;
        if (myApi==null) {
            Log.e(TAG,"FBReader API not initialized");
            return;
        }
        try {
            myApi.clearHighlighting();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        myApi.disconnect();
    }
    /**
     * Is FBReader here at all?
     */
    private boolean isFBReaderInstalled() {
        return AppHelpers.appInstalledOrNot(this,AccessibilityRecorderService.FBREADER_PACKAGE_NAME);
    }
    /**
     * Is FBReader currently active?
     */
    private boolean isFBReaderTopActivity() {
        ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Log.i(TAG, "null activity manager");
            return false;
        }

        List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
        String topActivity = appProcesses.get(0).topActivity.getPackageName();
        if (topActivity.equals(AccessibilityRecorderService.FBREADER_PACKAGE_NAME)) {
            return true;
        }
        else {
            return false;
        }
    }
    /**
     * implements ApiClientImplementation.ConnectionListener
     */
    @Override
    public void onConnected() {
        Log.d(TAG,"onConnected() to FBReader");
        connectedToFBReader=true;
        inReadingModeFBReader=false;
        myApi.addListener(this);//manually add listener
        showConnectedBookInfo();
    }
    /**
     * implements ApiClientImplementation.OnEvent
     */
    public void onEvent(String event) {
        Log.d(TAG,"OnEvent:"+event+" FBReader");

       if (event.equals(ApiListener.EVENT_READ_MODE_OPENED)) {
           Log.d(TAG,"Book opened");
           inReadingModeFBReader=true;
           updateInitialFBReaderReadingData();
       } else if (event.equals(ApiListener.EVENT_READ_MODE_CLOSED)) {
           Log.d(TAG,"Book closed");
           inReadingModeFBReader=false;
           BookReadingsRecorder.getBookReadingsRecorder(this).recordSwitchAwayFromBook(this,SystemClock.elapsedRealtime());
       }


    }
    /* update FBReader's reading progress */
    private void updateFBReaderProgress() {
        if (!isFBReaderTopActivity()) {
            return;
        }
        if (!connectedToFBReader) {
            disconnectFromFBReader();
            initFBReader();
            return;
        }
        if (!inReadingModeFBReader) {
            return;
        }
        try {
            String bookHash=myApi.getBookHash();
            if (bookHash==null) {
                //null hash, no book or API error
                Log.e(TAG," FBReader API returned empty hash");
                return;
            }
            if (!bookHash.equals(fbReaderHash)) {
                Log.d(TAG, "Hash not same, new book?");
                showConnectedBookInfo();
                fbReaderHash=bookHash;
            }
            //same hash
            float bookProgress=myApi.getBookProgress();
            if (Math.abs(bookProgress-fbReadingProgress)> MINIMAL_READING_PROGRESS_FBREADER) {
                Log.d(TAG,String.format("Progress changed, old %f%%, new %f%%",fbReadingProgress*100.0,bookProgress*100.0));
                fbReadingProgress=bookProgress;
                //TODO:call for update
            }

        }   catch (ApiException e) {
            Log.d(TAG,"Error:"+e.toString());
            e.printStackTrace();
            disconnectFromFBReader();
            initFBReader();
        }
    }
    private void  updateInitialFBReaderReadingData() {
        updateFBReaderProgress();
    }
    public void showConnectedBookInfo() {
        if (myApi==null) {
            return;
        }
        try {
            final String bookTitle=myApi.getBookTitle();
            final String languageCode = myApi.getBookLanguage();
            final List<String> bookTags=myApi.getBookTags();
            final String hash=myApi.getBookHash();
            final List<String> bookAuthors=myApi.getBookAuthors();
            final float bookProgress=myApi.getBookProgress();
            //null
            //final String uniqueId=myApi.getBookUniqueId();
            final String fbreaderVersion=myApi.getFBReaderVersion();
            // final Date  lastPageTurning=myApi.getBookLastTurningTime();
            Log.d(TAG,"FBReader version:"+fbreaderVersion);
            Log.d(TAG,"hash:"+hash);
            Log.d(TAG,"book title:"+bookTitle);
            Log.d(TAG,"language:"+languageCode);
            StringBuilder tagStringBuilder=new StringBuilder();
            if (bookTags.size()>1) {
                for (String tag:bookTags) {
                    tagStringBuilder.append(",");
                    tagStringBuilder.append(tag);
                    Log.d(TAG,"Tag:"+tag);
                }
            }
            else {
                String tag=bookTags.get(0);
                tagStringBuilder.append(tag);
                Log.d(TAG,"(alone) tag:"+tag);
            }

            StringBuilder authorStringBuilder=new StringBuilder();
            if (bookAuthors.size()>1) {
                for (String author:bookAuthors) {
                    authorStringBuilder.append(",");
                    authorStringBuilder.append(author);
                    Log.d(TAG,"Author:"+author);
                }
            }
            else {
                String author=bookAuthors.get(0);
                authorStringBuilder.append(author);
                Log.d(TAG,"(alone) author:"+author);
            }
            Log.d(TAG,String.format("Progress:%f%%",bookProgress*100.0));

            try {
                BookReadingsRecorder.getBookReadingsRecorder(this).recordNewBook(this,SystemClock.elapsedRealtime(),
                        authorStringBuilder.toString(),bookTitle,tagStringBuilder.toString(),bookProgress);
            } catch (BookReadingsRecorder.InvalidArgumentsException e) {
                Log.d(TAG,"error recording new book,e:"+e.toString());
                e.printStackTrace();
            }

            //Log.d(TAG, String.format("LastPageTurning:%s", lastPageTurning.toLocaleString()));
        }
        catch (ApiException e) {
            Log.d(TAG,"Error:"+e.toString());
            e.printStackTrace();
        }
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

            /* Book Scrobbler logic */
            BookReadingsRecorder.getBookReadingsRecorder(this).checkIfReadingAppActive(this);
            previousForegroundTask = newForegroundTask;

        }
    }










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
        ourDeviceID = new DeviceInfoManager().getDeviceId(this);

        //die if not user logged in
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Log.i(TAG, "Not logged in. Service will NOT be started");
            stopSelf();
            return;
        }


        //configure icon
        configureForeground();


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
        //configure FBReader API support
        if (isFBReaderInstalled()) {
            Log.d(TAG,"FBReader installed. connecting");
            initFBReader();//init API session
            connectToFBReader();//request callbacks
            Log.d(TAG,"Connnected to FBReader");


            //confugure FBReader poller
            new CountDownTimer(YEAR_IN_MS, FBREADER_POLL_INTERVAL_MILLIS) {
                public void onTick(long msUntilFinish) {
                    updateFBReaderProgress();
                }

                public  void  onFinish() {}
            }.start();


        } else
        {
            Log.d(TAG,"FBReader not installed");
        }


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
        Notification note = new Notification(R.drawable.push_icon,
                getResources().getString(R.string.app_started_notification),
                System.currentTimeMillis());

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        note.setLatestEventInfo(this, getResources().getText(R.string.app_name),
                getResources().getText(R.string.tap_me), pi);
        note.flags |= Notification.FLAG_NO_CLEAR;
        note.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(R.drawable.push_icon, note);
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

                Integer pagesRead=intent.getIntExtra(BookReadingsRecorder.PAGES_READ,0);
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

                note.setLatestEventInfo(context, title,
                        msg, pi);
                note.flags |= Notification.FLAG_NO_CLEAR;
                note.flags |= Notification.FLAG_ONGOING_EVENT;
                //update notification
                NotificationManager manager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
                manager.notify(R.drawable.push_icon,note);


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
        myApi.removeListener(this);
        disconnectFromFBReader();
        Log.d(TAG,"Called stopForeground()");
        MyAnalytics.startAnalyticsWithContext(this);
    }

}