package com.viorsan.dollmaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by dkzm on 23.05.14.
 */
public class CoreBroadcastReceiver extends BroadcastReceiver {
    CoreService service;
    boolean mBound = false;

    public void setService(CoreService newService) {
        if (newService != null) {
            service = newService;
            mBound = true;
        }
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        if (intent == null) return;
        if (intent.getAction() == null) return;

        if (mBound) {
            service.onStartCommand(intent, 0, 0);
        } else {
            context.startService(new Intent(context, CoreService.class));
        }
    }

}
