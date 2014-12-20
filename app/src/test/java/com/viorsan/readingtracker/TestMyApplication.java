package com.viorsan.readingtracker;

import android.app.Application;
import android.os.Build;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycleApplication;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.jar.Manifest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
/**
 * Created by dkzm on 20.12.14.
 */

public class TestMyApplication extends MyApplication
        implements TestLifecycleApplication {

    public static final String TAG = "TestMyApplication";

    @Override
    public void onCreate() {
        System.out.println(TAG +":OnCreate");
        super.onCreate();
    }
    @Override public void beforeTest(Method method) {
        System.out.println(TAG +":beforeTest:"+method.toString());

    }

    @Override public void prepareTest(Object test) {
        System.out.println(TAG +":prepareTest:"+test.toString());

    }

    @Override public void afterTest(Method method) {
        System.out.println(TAG +":afterTest:"+method.toString());

    }
    @Override public void onTerminate() {
        System.out.println(TAG +":OnTerminate");
        super.onTerminate();
    }
}