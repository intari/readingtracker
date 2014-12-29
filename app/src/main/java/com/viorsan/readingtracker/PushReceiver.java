package com.viorsan.readingtracker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 29.12.14.
 */
public class PushReceiver  extends ParsePushBroadcastReceiver {

    public static final String TAG = "ReadingTracker::PushReceiver";

    /**
     * handler for https://parse.com/docs/push_guide#receiving-responding/Android
     * called when user clicked on notification
     * @param context
     * @param intent - intent which was
     */
    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.e(TAG, "OnPushOpen");
        MyAnalytics.startAnalyticsWithContext(context);
        MyAnalytics.trackAppOpened(intent);
        MyAnalytics.trackEvent("userClickedOnPushNotification");
        //just start main activity for now
        Intent i = new Intent(context, MyActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        MyAnalytics.stopAnalyticsWithContext(context);
    }
}
