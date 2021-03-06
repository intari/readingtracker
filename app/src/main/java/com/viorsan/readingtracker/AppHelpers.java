package com.viorsan.readingtracker;

import android.content.Context;
import android.util.Log;


import java.util.Locale;

import com.mixpanel.android.mpmetrics.MixpanelAPI;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 02.01.15.
 * Helper tools
 */
public class AppHelpers {

    public static final String TAG = "ReadingTracker::A.H.";

    /**
     * Checks if app runs 'testing' build. This is needed because test framework not always do what needs to be done
     * @return true - it's automated testing build
     */
    public static Boolean isRunningTestBuild() {
        if (!"testing".equals(BuildConfig.FLAVOR)) {
            return false;
        }
        else
        {
            return true ;
        }
    }

    /**
     * Get short one-line build identification
     * @return sort one line build identification
     */
    public static String buildAuthority() {
        String authority = BuildConfig.APPLICATION_ID+".";
        authority += BuildConfig.FLAVOR;
        if (BuildConfig.DEBUG) {
            authority += ".debug";
        }
        return authority;
    }

    /**
     * Writes log banner with build details to logcat
     * @param tag - which component of app requested banner (not currently used)
     * @param context - context to use (not currently used)
     */
    static public void writeLogBanner(String tag, Context context) {
        Log.i(TAG, " (c) Dmitriy Kazimirov 2013-2016");
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
        Log.i(TAG," Built on "+BuildConfig.BUILD_HOST+ " of type "+BuildConfig.BUILDER_TYPE+ " by user "+ BuildConfig.BUILD_USER+" at "+BuildConfig.BUILD_DATE_TIME);
        Log.i(TAG," Mixpanel version:"+com.mixpanel.android.BuildConfig.MIXPANEL_VERSION);
        //log extra device details
        DeviceInfoManager deviceInfoManager=new DeviceInfoManager();
        Log.i(TAG," Language:"+ Locale.getDefault().getLanguage());
        Log.i(TAG," Country:"+Locale.getDefault().getCountry());
        Log.i(TAG," Locale:"+Locale.getDefault().toString());

        Log.i(TAG," Device info string:"+BookReadingsRecorder.getDeviceInfoString());
        Log.i(TAG," Runtime type:"+deviceInfoManager.getCurrentRuntimeValue());


    }
}
