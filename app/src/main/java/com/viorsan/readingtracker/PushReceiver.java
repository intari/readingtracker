package com.viorsan.readingtracker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 29.12.14.
 * TODO:update to use new push service
 */
public class PushReceiver  extends ParsePushBroadcastReceiver {

    public static final String TAG = "ReadingTracker::P.P.R.";

    /**
     * handler for https://parse.com/docs/push_guide#receiving-responding/Android
     * called when user clicked on notification
     * TODO:handle custom push options, see
     * http://blog.parse.com/2014/09/30/android-push-gets-major-refresh/
     * https://parse.com/docs/push_guide#options-data/Android
     * 'uri' will be handled for us but for others we should at least show popup message
     * @param context
     * @param intent - intent which was
     */
    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.e(TAG, "OnPushOpen");
        MyAnalytics.startAnalyticsWithContext(context);
        MyAnalytics.trackAppOpened(intent);
        MyAnalytics.trackEvent("userClickedOnPushNotification");
        if (intent!=null) {
            String pushChannel=intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_CHANNEL);
            String pushData=intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA);
            if (pushChannel!=null) {
                Log.d(TAG,"PushChannel is "+pushChannel+"|");
            }
            else {
                Log.d(TAG,"PushChannel is null");
            }
            if (pushData!=null) {
                Log.d(TAG,"PushData is "+pushData+"|");
            }
            else {
                Log.d(TAG,"PushData is null");
            }
        }
        else {
            Log.d(TAG,"intent is null");
        }
        //just start main activity for now
        Intent i = new Intent(context, MainActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        MyAnalytics.stopAnalyticsWithContext(context);
    }
}
