package com.viorsan.dollmaster;

import android.app.*;
import android.bluetooth.*;
import android.database.Cursor;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.provider.Browser;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.*;

import java.util.*;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by dkzm on 23.05.14.
 */

public class ***REMOVED***Service extends Service implements SensorEventListener {


    public static final String REPORT_TYPE_DEVICE_REPORT = "DeviceReport";
    public static final String REPORT_TYPE_APPSWITCH = "AppSwitch";
    public static final String REPORT_TYPE_ACTIVITY_UPDATE="ActivityUpdate";
    public static final String FAKEAPP_DEVICELOCKED = "com.viorsan.dollmaster.DeviceLocked";
    public static final String FAKEAPP_SCREENOFF = "com.viorsan.dollmaster.ScreenOff";


    public static final long YEAR_IN_MS = 365 * 86400 * 1000;
    public static final int PROCESSLIST_RESCAN_INTERVAL_MILLIS = 3000;//ONLY used to check if reader app is currently active
    public static final int REPORT_SENDING_RETRY_MILLIS = 3000;
    public static final int BROWSER_HISTORY_RESCAN_INTERVAL_MILLIS = 30 * 1000;
    private long lastAppCheckTime;

    private FileObservationHelper observationHelperStorage =null;
    private FileObservationHelper observationHelperSdcard=null;


    // helpers for Play Services
    // Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;


    private Handler mDelayReporter=null;

    public static long REPORTER_VERSION = 11;
    public static String ourDeviceID = "";

    public static long REPORT_SEND_INTERVAL = 3*60*1000;

    private static final String appID="***REMOVED***";
    private Location lastLocation;
    private Location lastLocationCoarse;
    private HandlerThread locationHandlerThread=null;
    private Handler locationHandler=null;
    private static String LOCATION_UPDATE_THREAD="LocationHandlerUpdateThread";

    ***REMOVED***Receiver broadcastReceiver = null;

    private String previousForegroundTask;//can have 'fake' data
    private Boolean isDeviceLocked = false;
    private Boolean isDeviceScreenOff = false;

    private final IBinder mBinder = new LocalBinder();

    private Long appActivationTime;
    private long nextRequestId;
    private long sessionId;
    private final Object requestIdSyncObject=new Object();
    private final Object reportSenderSyncObject=new Object();
    private boolean isPowerConnected = false;

   // private SQSReportManager sqsReportManager;



    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private int mStepCounter_LastCount, mStepCounter_InitialCount;
    private boolean mStepCounter_InitialCountInitialized;
    private int mStepCounter_LastDetectorCount;

    public static int batteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    public static int batteryPluggedStatus = 0;

    public static long totalMessagesSize = 0;

    public static String userMessage;


    private int currentBrowserHistoryPosition=-1;




    private static ParseObject mDetails =null;
    private static final String CURRENT_SESSION_ID = "CURRENT_SESSION_ID";
    private static final String CURRENT_REQUEST_ID = "CURRENT_REQUEST_ID";


    public static FixTimeBean gpsTime = new FixTimeBean(-1, -1, -1, false);
    public static FixTimeBean networkTime = new FixTimeBean(-1, -1, -1, false);



    public static class FixTimeBean {
        public long time;
        public long systemWallClock;
        public long systemUptime;

        public boolean isUserEnabled;

        public FixTimeBean(long time, long systemWallClock, long systemUptime, boolean isUserEnabled) {
            this.time = time;
            this.systemWallClock = systemWallClock;
            this.systemUptime = systemUptime;
            this.isUserEnabled = isUserEnabled;
        }
    }

    public boolean isExternalPowerConnected() {
        return isPowerConnected;
    }



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

    ***REMOVED***Service getService() {
        return ***REMOVED***Service.this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        ***REMOVED***Service getService() {
            return ***REMOVED***Service.this;
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig)  {

    }

    /* TODO: make it use keyguard */
    public void onDeviceUnlock() {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Device unlocked");

        //SQSReport report = new SQSReport(this, "LockStatusChange");

        //report.put("isDeviceLocked", getResources().getInteger(R.integer.DEVICE_NOT_LOCKED));
        //saveReportSQS(report);

        isDeviceLocked = false;
        //updateActiveProcessList();
    }

    /* TODO: make it use keyguard */
    public void onDeviceLock() {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Device locked");
        BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).recordSwitchAwayFromBook(this.getBaseContext(),SystemClock.elapsedRealtime());
        //SQSReport report = new SQSReport(this, "LockStatusChange");

        //report.put("isDeviceLocked", getResources().getInteger(R.integer.DEVICE_LOCKED));
        //saveReportSQS(report);

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
                ***REMOVED***Service.appID);

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

            //ParseObject report=new ParseObject(REPORT_TYPE_APPSWITCH);

            long now = SystemClock.elapsedRealtime();
            //long timeSpent = now - appActivationTime;

            appActivationTime = now;


            /*
            Map<String, String> dimensions = new HashMap<String, String>();

            report.put("name", getApplicationNameByPackageName(previousForegroundTask));
            report.put("appid", previousForegroundTask);
            dimensions.put("appid",previousForegroundTask);
            report.put("timeSpent", timeSpent);
            if (isHomeScreen(previousForegroundTask))
            {
                //report.put("isHomeScreen",isHomeScreen(previousForegroundTask)?1:0);
                report.put("isHomeScreen",1);
                dimensions.put("isHomeScreen","yes");
            }
            else
            {
                report.put("isHomeScreen",0);
                dimensions.put("isHomeScreen","no");
            }
            dimensions.put("timeSpent",new Long(timeSpent).toString());
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "ReportSendingUpdates:AppSwitch:AID:" + previousForegroundTask + ".TimeSpent:" + timeSpent);

            if (suInfoReady) {
                if (suAvailable) {
                    report.put("fullUserControl","1");

                }
                else
                {
                    report.put("fullUserControl","0");
                }
            }

            */
            previousForegroundTask = newForegroundTask;
            //saveReportToParse(report);

            // Parse Analytics test

            //ParseAnalytics.trackEvent("appSwitch", dimensions);

        }
    }

    /*
    private boolean isHomeScreen(String packageName) {
        final PackageManager pm = getBaseContext().getPackageManager();

        //code is suboptimal. we should implement cache and update it on app installs. TODO:update this.
        try {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> activities = pm.queryIntentActivities(home, 0);

            for (ResolveInfo ri: activities)
            {
                String pn=ri.activityInfo.applicationInfo.packageName;
                if (packageName.equals(pn))
                {
                    return true;
                }
            }

        } catch (Exception ex) {
            return false;
        }

        return false;

    }
    private String getApplicationNameByPackageName(String packageName) {
        final PackageManager pm = getBaseContext().getPackageManager();
        ApplicationInfo ai;

        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }

        return (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");
    }

    public Location getLocationCoarse() {
        return lastLocationCoarse;
    }
    */

    //TODO:extract this to other class
    //TODO:rewrite using google apps
    private void initLocationHandlerThread () {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "LocationHandler: Not yet implemented");
    }


    //If you use this function from other classes this mean you KNOW what you are doing
    public void saveReportToParse(ParseObject report) {
        final ParseObject reportToSend=report;
        Thread reportThread=new Thread( new Runnable() {
            @Override
            public void run() {
                //Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Sending report "+reportToSend.getClassName()+" in async task");
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

        //Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Will be sending report "+reportToSend.getClassName()+" in background thread");
        reportThread.start();

        //Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Will be sending report "+reportToSend.getClassName()+" in background thread..request sent!");


    }
    private Boolean saveReportToParseReal(ParseObject report) {
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

       /*
        String fbUserId=(String)currentUser.get(FieldIdentifiers.facebookId);

        if (fbUserId!=null) {
            report.put(FieldIdentifiers.facebookId,fbUserId);
        }
        */
        if (!AccessibilityRecorderService.ONLY_SCROBBLE) {
            //write body metrics sensors only if it's not scrobble-only mode
            //this decision may be reverted in future
            updateSensorInfoForReport(report);
        }


        final String reportClass=report.getClassName();
        //report.updateTimeAndLocationInfo();
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

    private void updateSensorInfoForReport(ParseObject report) {
        //save sensor data
        report.put("numSteps",getStepCounter());
        //heart rate
        //report.put("heartRate",mCurrentHeartRate);
        //HRM sensor's battery level
        //report.put("hrmBatteryLevel",mHRMBatteryLevel);


    }
    /*
    private Boolean saveReportSQS(SQSReport report) {
        report.put("deviceId", ourDeviceID);
        java.util.Date date = new java.util.Date();

        report.put("clientEventCreateTime", DateHelper.formatISO8601_iOS(date));
        report.updateTimeAndLocationInfo();

        Long ourRequestId=report.getRequestId();
        Long ourSessionId=report.getSessionId();
      */
        /* debug support only */
        /*
        Intent intent = new Intent("ReportSendingUpdates");
        intent.putExtra("RequestID",ourRequestId);
        intent.putExtra("SessionID",ourSessionId);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        */
      /*
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"ReportSendingUpdates:SessionID:"+ourSessionId+".RequestID:"+ourRequestId);

        sendSQSMessage(report.toString(), ourRequestId, ourSessionId);//, false);

        return  Boolean.TRUE;
    }
    */

    private boolean playServicesAvailable() {
        int resultCode=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode==ConnectionResult.SUCCESS;
    }

    private void reportDeviceInfo() {
        DeviceInfoManager deviceInfoManager = new DeviceInfoManager();


        ParseQuery query = new ParseQuery(REPORT_TYPE_DEVICE_REPORT);
        query.whereEqualTo("deviceId",ourDeviceID);

        final ***REMOVED***Service self=this;

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

//        report.put("details", getDetailsSubReport());
 //       report.put("extraInformation", getExtraSubReport());

     //   WifiApManager wifiApManager = new WifiApManager(getBaseContext());
        Integer totalMemory = deviceInfoManager.readTotalRam();
        String runtimeName=deviceInfoManager.getCurrentRuntimeValue();

        mDetails.put("userAssignedName", "Unknown");
        mDetails.put("systemName", "Android");
        mDetails.put("systemVersion", Build.VERSION.RELEASE);
        mDetails.put("model", Build.PRODUCT);

        mDetails.put("userInterfaceIdeom", "Android");
        mDetails.put("platformString", Build.MODEL);
        mDetails.put("platform", Build.BOARD);
        mDetails.put("hwModel", Build.HARDWARE);
        mDetails.put("bootloader",Build.BOOTLOADER);
        mDetails.put("userVisibleBuildID",Build.DISPLAY);
        mDetails.put("hwSerial",Build.SERIAL);
        mDetails.put("fingerprint",Build.FINGERPRINT);
        mDetails.put("manufacturer",Build.MANUFACTURER);
        mDetails.put("brand",Build.BRAND);

        mDetails.put("totalMemory", totalMemory.toString());
      //  mDetails.put("macAddress", wifiApManager.getWifiMac());


        mDetails.put("runtime",runtimeName);


        Context context=getBaseContext();

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Device Identifier:"+ourDeviceID);

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"systemVersion:"+Build.VERSION.RELEASE);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"model:"+Build.PRODUCT);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"platformString:"+Build.MODEL);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"platform:"+Build.BOARD);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"hwModel:"+Build.HARDWARE);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"bootloader:"+Build.HARDWARE);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"userVisibleBuildID:"+Build.HARDWARE);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"hwSerial:"+Build.SERIAL);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"fingerprint:"+Build.FINGERPRINT);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"manufacturer:"+Build.MANUFACTURER);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"brand:"+Build.BOARD);

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Runtime is " + runtimeName);




        if (playServicesAvailable()) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Google Play Services present");
            mDetails.put("GooglePlayServicesInstalled",Boolean.TRUE);
        }
        else
        {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Google Play Services not present");
            mDetails.put("GooglePlayServicesInstalled",Boolean.FALSE);
            //TODO: is is hard error?, should we add dialog per https://developer.android.com/training/location/activity-recognition.html example?


        }


        //report detailed device features
        final PackageManager pm = getBaseContext().getPackageManager();
        /*
        final FeatureInfo[] featuresList = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : featuresList) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Feauture:" + f.name+" present");

        }
        */

        //TODO:EXACT meaning? will GoogleEdition have this? will devices like jesssica26/jessica27?
        //TODO:do we really need all _this_ info for ***REMOVED***?
        //really needed information

       /*
        if (pm.hasSystemFeature(pm.FEATURE_SENSOR_STEP_DETECTOR)) {
            mDetails.put("StepDetector",Boolean.TRUE);
        }
        else {
            mDetails.put("StepDetector",Boolean.FALSE);
        }
        if (pm.hasSystemFeature(pm.FEATURE_SENSOR_STEP_COUNTER)) {
            mDetails.put("StepCounter",Boolean.TRUE);
        }
        else {
            mDetails.put("StepCounter",Boolean.FALSE);
        }
         */
        if (pm.hasSystemFeature(pm.FEATURE_NFC)) {
            mDetails.put("NFC",Boolean.TRUE);
        }
        else {
            mDetails.put("NFC",Boolean.FALSE);
        }

        if (pm.hasSystemFeature(pm.FEATURE_LOCATION_GPS)) {
            mDetails.put("GPS",Boolean.TRUE);
        }
        else {
            mDetails.put("GPS",Boolean.FALSE);
        }

        if (pm.hasSystemFeature(pm.FEATURE_CAMERA_FRONT)) {
            mDetails.put("frontCam",Boolean.TRUE);
        }
        else {
            mDetails.put("frontCam",Boolean.FALSE);
        }

        if (pm.hasSystemFeature(pm.FEATURE_CAMERA)) {
            mDetails.put("backCam",Boolean.TRUE);
        }
        else {
            mDetails.put("backCam",Boolean.FALSE);
        }



        if (pm.hasSystemFeature(pm.FEATURE_CAMERA_AUTOFOCUS)) {
            mDetails.put("featureCameraAutofocus",Boolean.TRUE);
        }
        else {
            mDetails.put("featureCameraAutofocus",Boolean.FALSE);
        }

        if (pm.hasSystemFeature(pm.FEATURE_LOCATION_NETWORK)) {
            mDetails.put("featureLocationNetwork",Boolean.TRUE);
        }
        else {
            mDetails.put("featureLocationNetwork",Boolean.FALSE);
        }
        if (pm.hasSystemFeature(pm.FEATURE_LOCATION)) {
            mDetails.put("featureLocation",Boolean.TRUE);
        }
        else {
            mDetails.put("featureLocation",Boolean.FALSE);
        }


        //TODO:Google Apps are here?
        if (pm.hasSystemFeature("com.google.android.feautre.GOOGLE_BUILD")) {
            mDetails.put("featureGoogleBuild",Boolean.TRUE);
        }
        else {
            mDetails.put("featureGoogleBuild",Boolean.FALSE);
        }
        //TODO:Nexuses/GPe devices with new launcher?
        //http://delphigl.de/glcapsviewer/gles_listreports.php?devicefeature=com.google.android.feature.GOOGLE_EXPERIENCE ?

        if (pm.hasSystemFeature("com.google.android.feature.GOOGLE_EXPERIENCE")) {
            mDetails.put("featureGoogleExperience",Boolean.TRUE);
        }
        else {
            mDetails.put("featureGoogleExperience",Boolean.FALSE);
        }
        if (pm.hasSystemFeature(pm.FEATURE_AUDIO_LOW_LATENCY)) {
            mDetails.put("featureLowLatencyAudio",Boolean.TRUE);
        }
        else {
            mDetails.put("featureLowLatencyAudio",Boolean.FALSE);
        }
        if (pm.hasSystemFeature("android.hardware.touchscreen.multitouch.jazzhand")) {
            mDetails.put("feautureMultituch5orMore_JazzHand",Boolean.TRUE);
        }
        else
        {
            mDetails.put("feautureMultituch5orMore_JazzHand",Boolean.FALSE);
        }

        if (pm.hasSystemFeature("com.vmware.mvp")) {
            mDetails.put("feautureVMWareMVP",Boolean.TRUE);
        }
        else
        {
            mDetails.put("feautureVMwareMVP",Boolean.FALSE);
        }

        if (pm.hasSystemFeature("com.nxp.mifare")) {
            mDetails.put("feautureNXPMifare",Boolean.TRUE);
        }
        else
        {
            mDetails.put("feautureNXPMifare",Boolean.FALSE);
        }


        if (pm.hasSystemFeature(pm.FEATURE_CAMERA_ANY)) {
            mDetails.put("featureCameraAny",Boolean.TRUE);
        }
        else {
            mDetails.put("featureCameraAny",Boolean.FALSE);
        }

        if (pm.hasSystemFeature(pm.FEATURE_CAMERA_FLASH)) {
            mDetails.put("featureCameraFlash",Boolean.TRUE);
        }
        else {
            mDetails.put("featureCameraFlash",Boolean.FALSE);
        }


        if (pm.hasSystemFeature(pm.FEATURE_USB_HOST)) {
            mDetails.put("featureUSBHost",Boolean.TRUE);
        }
        else {
            mDetails.put("featureUSBHost",Boolean.FALSE);

        }

        //Phone details
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        String imei=telephonyManager.getDeviceId();
        if (imei!=null) {
            mDetails.put("imei",imei);

            mDetails.put("operatorName", telephonyManager.getNetworkOperatorName());
            mDetails.put("simCountryCode", telephonyManager.getSimCountryIso());
            mDetails.put("simOperatorName", telephonyManager.getSimOperatorName());
            mDetails.put("simSerialNumber", telephonyManager.getSimSerialNumber());
            mDetails.put("subscriberId", telephonyManager.getSubscriberId());
        }




        saveReportToParse(mDetails);
    }

    public void checkOutstandingReports() {

    }


    protected void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    private static String buildAuthority() {
        String authority = BuildConfig.APPLICATION_ID+".";//"com.viorsan.";
        authority += BuildConfig.FLAVOR;
        //authority += ".dollmaster"; ??
        if (BuildConfig.DEBUG) {
            authority += ".debug";
        }
        return authority;
    }

    static public void writeLogBanner(String tag, Context context) {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" (c) Dmitriy Kazimirov 2013-2014");
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" e-mail: dmitriy.kazimirov@viorsan.com");
        String version=context.getResources().getString(R.string.build_version);
        String build=context.getResources().getString(R.string.build_id);

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,tag+" (manual)version "+version+" (build "+build+")");

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

        //Debug.L.LOG_MARK("BookTracker (***REMOVED***) starting up");

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Book reading tracker (***REMOVED***) main service starting up");
        ***REMOVED***Service.writeLogBanner("",getApplicationContext());

        ParseConfigHelper.refreshConfig();
        
        ourDeviceID = new DeviceInfoManager().getDeviceId(getBaseContext());

        reportDeviceInfo();


         /*  StepCounter */

        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // Batching for the step counter doesn't make sense (the buffer holds
        // just one step counter event anyway, as it's not a continuous event)

        //mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);

        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Not logged in. Service will NOT be started");
            stopSelf();
            return;
        }
        /*
        if (!ParseFacebookUtils.isLinked(currentUser)) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Logged in but not linked to Facebook. Service will NOT be started");
            stopSelf();
            return;
        }
        */
        //uploaderIcons = new UploaderIcons(getBaseContext());


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

        /*
        try {
            sqsReportManager = SQSReportManager.getInstance(this);
        } catch (DBStorage.CreateTableException e) {
            e.printStackTrace();
            showToast("CuriousApp:Database startup failed. Cannot continue");
            stopSelf();
            return;
        }
        */

        configureForeground();


        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

        if (activityManager == null) {
            previousForegroundTask = "com.viorsan.dollmaster";
        } else {
            List<ActivityManager.RunningTaskInfo> appProcesses = activityManager.getRunningTasks(1);
            previousForegroundTask = appProcesses.get(0).topActivity.getPackageName();
        }

        appActivationTime = SystemClock.elapsedRealtime();

        BookReadingsRecorder.getBookReadingsRecorder(this.getBaseContext()).setMasterService(this);


        initLocationHandlerThread();

        //TODO:don't do this in deep sleep
        new CountDownTimer(YEAR_IN_MS, PROCESSLIST_RESCAN_INTERVAL_MILLIS) {
            public void onTick(long msUntilFinish) {

                updateActiveProcessList();
            }

            public  void  onFinish() {}
        }.start();


        /*
        new CountDownTimer(YEAR_IN_MS, REPORT_SEND_INTERVAL) {
            public void onTick(long msUntilFinish) {
                checkOutstandingReports();

            }

            public  void  onFinish() {}
        }.start();

        */
        broadcastReceiver = new ***REMOVED***Receiver();
        broadcastReceiver.setService(this);

        registerReceiver(broadcastReceiver, getFilters());



        //prepare handler for posting data in case of delays
        mDelayReporter=new Handler();

        if (!AccessibilityRecorderService.ONLY_SCROBBLE) {
        /* activate Play Services */
            observationHelperSdcard=new FileObservationHelper("/mnt/sdcard");
            observationHelperSdcard.startWatching();
            observationHelperStorage =new FileObservationHelper("/storage");
            observationHelperStorage.startWatching();

        }

        /*
        if (!AccessibilityRecorderService.ONLY_SCROBBLE) {
            reportInitialBrowsingHistory();

            new CountDownTimer(YEAR_IN_MS, BROWSER_HISTORY_RESCAN_INTERVAL_MILLIS) {
                public void onTick(long msUntilFinish) {
                    reportBrowsingHistoryUpdates();
                }

                @Override
                public void onFinish() {

                }
            }.start();

        }
        */


        showToast("***REMOVED*** activated");
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        final int type = event.sensor.getType();
        /*
        if (type == Sensor.TYPE_STEP_COUNTER) {
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "New step counter value  "+(int)event.values[0]);

            //we need step counter since app startup and not since device startup
            if (!mStepCounter_InitialCountInitialized) {
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Initializing initial steps count:  " + (int) event.values[0]);
                mStepCounter_InitialCount = (int) event.values[0];
                mStepCounter_InitialCountInitialized = true;
            }
            mStepCounter_LastCount = (int) event.values[0] - mStepCounter_InitialCount;


        }
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Sensor accuracy changed. New value: " + accuracy);

    }
    public int getStepCounter()
    {
        return mStepCounter_LastCount;
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
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);


        //internal
        intentFilter.addAction(ActivityMonitoringService.ACTIVITY_UPDATE_AVAILABLE);

        //headset plug
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);

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

    private void onBatteryChanged(Intent intent) {
        batteryPluggedStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        if (batteryPluggedStatus == 0) {
            onPowerDisconnected();
        } else if (batteryPluggedStatus == BatteryManager.BATTERY_PLUGGED_AC) {
            onPowerConnected();
        } else if (batteryPluggedStatus == BatteryManager.BATTERY_PLUGGED_USB) {
            onPowerConnected();
        }
    }

    private void onPowerConnected() {
        isPowerConnected = true;
    }

    private void onPowerDisconnected() {
        isPowerConnected = false;
    }

    private void configureForeground() {
        Notification note = new Notification(R.drawable.dollmaster,
                getResources().getString(R.string.app_started_notification),
                System.currentTimeMillis());

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MyActivity.class), 0);

        note.setLatestEventInfo(this, getResources().getText(R.string.app_name),
                getResources().getText(R.string.tap_me), pi);
        note.flags |= Notification.FLAG_NO_CLEAR;
        note.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(R.drawable.dollmaster, note);
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

        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            onBatteryChanged(intent);

        } else if (intent.getAction().equals(Intent.ACTION_DREAMING_STARTED)) {
            onDreamingStarted();
        } else if (intent.getAction().equals(Intent.ACTION_DREAMING_STOPPED)) {
            onDreamingStopped();
        } else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            onHeadsetPlug(intent);
        }

    }

    private void onHeadsetPlug(Intent intent) {
        int state = intent.getIntExtra("state", -1);
        switch(state) {
            case(0):
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Headset unplugged");
                break;
            case(1):
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO, "Headset plugged");
                break;
            default:
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_ERROR, "Headset state incorrect");
        }
    }



    // browser history check
    public void reportInitialBrowsingHistory() {

        //based on https://stackoverflow.com/questions/2577084/android-read-browser-history
        /*
            issues:
            - on jessica29 'Internet' is used (not Chrome) even while it's Kitkat
            - on jessica22 with CyanogenMod (and likely other Cyanogenmod-based devices) their browser is used
            - google quick search box itself is not used
            - ONLY system default browser is used (it's Chrome on newer Androids...usually)
            - if we only need current data - we should just ingore initial update and few after
            - it LOOKS like some synced urls are here. or not?
            - EXACT URLs provided...и я например - не рискну со своих основных девайсов такое отсылать в SysMonitor...
            - а теперь смотрим кто такие привилегия просит..FB вроде ПОКА не просит

            Date=Unix Epoch


         */

        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Dumping browser history...");

        String title = "";
        String url = "";

        long date =-1L;
        //ArrayList<HistoryEntry> list = new ArrayList<HistoryEntry>();

        String[] proj = new String[] { Browser.BookmarkColumns.TITLE,
                Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE };
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history,
        // 1 = bookmark
        Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj,
                sel, null, null);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Browser history contains %d  elements. Starting from one",mCur.getCount());

        mCur.moveToFirst();
        currentBrowserHistoryPosition=1;


        if (mCur.moveToFirst() && mCur.getCount() > 0) {
            boolean cont = true;
            while (mCur.isAfterLast() == false && cont) {
                //HistoryEntry entry = new HistoryEntry();

                title = mCur.getString(mCur
                        .getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = mCur.getString(mCur
                        .getColumnIndex(Browser.BookmarkColumns.URL));

                date = mCur.getLong(mCur.
                        getColumnIndex(Browser.BookmarkColumns.DATE));

                // Do something with title and url
                //entry.setTitle(title);
                //entry.setUrl(url);
                //list.add(entry );
                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"%d. User visited %s at url %s at %d | ",currentBrowserHistoryPosition,title,url,date);
                //Log.d("TAG", "title   " + title);
                mCur.moveToNext();

                currentBrowserHistoryPosition++;
            }
        }

        mCur.close();


    }
    public void reportBrowsingHistoryUpdates() {
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Dumping browser history updates..last pos was %d",currentBrowserHistoryPosition);

        String title = "";
        String url = "";

        long date =-1;
        //ArrayList<HistoryEntry> list = new ArrayList<HistoryEntry>();

        String[] proj = new String[] { Browser.BookmarkColumns.TITLE,
                Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE };


        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history,
        // 1 = bookmark
        Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj,
                sel, null, null);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Browser history contains "+mCur.getCount()+ " elements. Starting from %d",currentBrowserHistoryPosition);

        mCur.moveToFirst();
        mCur.moveToPosition(currentBrowserHistoryPosition);


        if (mCur.getCount() > 0) {
            boolean cont = true;
            while (mCur.isAfterLast() == false && cont) {

                title = mCur.getString(mCur
                        .getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = mCur.getString(mCur
                        .getColumnIndex(Browser.BookmarkColumns.URL));
                date = mCur.getLong(mCur
                        .getColumnIndex(Browser.BookmarkColumns.DATE));

                Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"%d. User visited %s at url %s at %d| ",currentBrowserHistoryPosition,title,url,date);
                mCur.moveToNext();

                currentBrowserHistoryPosition++;
            }
        }

        mCur.close();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processBroadcastInternal(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //sqsReportManager.destroy();
        stopForeground(true);
        if (mSensorManager!=null) {
            mSensorManager.unregisterListener(this);

        }
        if (observationHelperSdcard!=null) {
            observationHelperSdcard.stopWatching();
        }
        if (observationHelperStorage !=null) {
            observationHelperStorage.stopWatching();
        }


    }









}