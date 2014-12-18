package com.viorsan.dollmaster;

/**
 * Created by dkzm on 24.05.14.
 */

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.acra.ACRA;

import java.lang.reflect.Method;



public class WifiApManager {
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLING = 2;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    public static final int WIFI_AP_STATE_FAILED = 4;

    private final WifiManager mWifiManager;

    public WifiApManager(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE_ENABLED;
    }

    public boolean isWifiConnected() {
        WifiInfo w = mWifiManager.getConnectionInfo();

        return (w.getNetworkId() != -1);
    }

    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        try {
            if (enabled) {
                mWifiManager.setWifiEnabled(false);
            }

            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);

            return (Boolean) method.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            Debug.L.LOG_EXCEPTION(e);

            ACRA.getErrorReporter().putCustomData("tag", "WiFiApManager");
            ACRA.getErrorReporter().handleException(e,false);

            return false;
        }
    }

    public int getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");

            return (Integer) method.invoke(mWifiManager);
        } catch (Exception e) {

            Debug.L.LOG_EXCEPTION(e);

            ACRA.getErrorReporter().putCustomData("tag", "WiFiApManager");
            ACRA.getErrorReporter().handleException(e,false);

            return WIFI_AP_STATE_FAILED;
        }
    }

    public String getWifiMac() {
        WifiInfo w = mWifiManager.getConnectionInfo();

        if (w.getMacAddress() != null) {
            return w.getMacAddress();
        } else {
            return "None";
        }
    }

}
