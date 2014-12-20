package com.viorsan.readingtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by dkzm on 23.05.14.
 * My standard device detection code.
 */



public class DeviceInfoManager {
    private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
    private static final String LIB_DALVIK = "libdvm.so";
    private static final String LIB_ART = "libart.so";
    private static final String LIB_ART_D = "libartd.so";
    private static final String TAG="ReadingTracker:DeviceInfoManager";

    public String /* CharSequence */ getCurrentRuntimeValue() {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            try {
                Method get = systemProperties.getMethod("get",
                        String.class, String.class);
                if (get == null) {
                    return "WTF?!";
                }
                try {
                    final String value = (String)get.invoke(
                            systemProperties, SELECT_RUNTIME_PROPERTY,
                        /* Assuming default is */"Dalvik");
                    if (LIB_DALVIK.equals(value)) {
                        return "Dalvik";
                    } else if (LIB_ART.equals(value)) {
                        return "ART";
                    } else if (LIB_ART_D.equals(value)) {
                        return "ART debug build";
                    }

                    return value;
                } catch (IllegalAccessException e) {
                     Log.i(TAG, "Can't get runtime:IllegalAccessException");
                    return "Dalvik";
                } catch (IllegalArgumentException e) {
                     Log.i(TAG, "Can't get runtime:IllegalArgumentException");
                    return "Dalvik";
                } catch (InvocationTargetException e) {
                     Log.i(TAG, "Can't get runtime:InvocationTargetException");
                    return "Dalvik";
                }
            } catch (NoSuchMethodException e) {
                 Log.i(TAG,"Can't get runtime:SystemProperties.get(String key, String def) method is not found, assuming Dalvik");
                return "Dalvik";
            }
        } catch (ClassNotFoundException e) {
             Log.i(TAG,"Can't get runtime:SystemProperties class is not found,assuming Dalvik");
            return "Davlik";
        }
    }


    public long getTotalInternalMemorySize() {
        return getMemorySizeFromPath(Environment.getDataDirectory().getPath());
    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



    public String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        if ((telephonyManager != null) && (telephonyManager.getDeviceId() != null)) {
            if ((!telephonyManager.getDeviceId().equals("000000000000000")) &&
                    (!telephonyManager.getDeviceId().equals("00000000000000"))) {
                return telephonyManager.getDeviceId();

            }
        }

        String androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);

        if (androidId != null) {
            if (!androidId.equals("9774d56d682e549c")) {
                return androidId;
            }
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        String uniqueId = sharedPreferences.getString(PREF_UNIQUE_ID, null);

        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueId);
            editor.commit();
        }

        return uniqueId;
    }

    private long getMemorySizeFromPath(String path) {
        StatFs stat = new StatFs(path);

        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        return blockSize*totalBlocks;
    }

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
}
