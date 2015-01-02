package com.viorsan.readingtracker;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 02.01.15.
 */
public class AppHelpers {

    public static final String TAG = "ReadingTracker::AppHelpers";

    public static String buildAuthority() {
        String authority = BuildConfig.APPLICATION_ID+".";
        authority += BuildConfig.FLAVOR;
        if (BuildConfig.DEBUG) {
            authority += ".debug";
        }
        return authority;
    }
    static public void writeLogBanner(String tag, Context context) {
        Log.i(TAG, " (c) Dmitriy Kazimirov 2013-2014");
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
}
