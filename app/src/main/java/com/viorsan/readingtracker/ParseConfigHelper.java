package com.viorsan.readingtracker;

import android.util.Log;

import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 18.12.14.
 */
public class ParseConfigHelper {
    private static String TAG="ReadingTrackerParseConfig";
    public static final long configRefreshInterval = 5*60*1000; //every 5 minutes //2 * 60 * 60 * 1000;//every hour

    public static final String SERVER_CONFIG_KEY_DEV_USER_LOGINS = "devUserLogins";
    private static long lastFetchedTime;

    private static ParseConfig cachedConfig=null;

    // Fetches the config at most once every 12 hours per app runtime
    public static void refreshConfig() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchedTime > configRefreshInterval) {
            lastFetchedTime = currentTime;
            ParseConfig.getInBackground(new ConfigCallback() {
                @Override
                public void done(ParseConfig config, ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "Fetched config from server.");
                    } else {
                        Log.e(TAG, "Failed to fetch config from server. Using Cached Config.");
                    }
                    cachedConfig = ParseConfig.getCurrentConfig();
                    Log.d(TAG,"Config is "+cachedConfig.toString());
               }
            });
        }
    }

    public static boolean isDevUser() {
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser==null) {
            Log.e(TAG,"current user is empty so it's not dev user");
            return false;
        }
        if (cachedConfig==null) {
            Log.e(TAG,"config is not ready yet. assuming we are not dev user -:(");
            return false;
        }
        List<String> devUsers=cachedConfig.getList(SERVER_CONFIG_KEY_DEV_USER_LOGINS);
        if (devUsers.contains(currentUser.getUsername())) {
            Log.d(TAG,"Current user, "+currentUser.getUsername()+ " will use all new functions");
            return true;
        }
        else
        {
            Log.d(TAG,"Current user, "+currentUser.getUsername()+ " will use only regular functions");
            return false;
        }
    }
}