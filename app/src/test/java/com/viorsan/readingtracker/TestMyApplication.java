package com.viorsan.readingtracker;

import android.app.Application;
import android.os.Build;

import com.parse.Parse;

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

    // init Parse Platform using our test keys
    private void initParseForTests() {
        Parse.initialize(this, BuildConfig.PARSE_APP_ID_FOR_TEST_HARNESS, BuildConfig.PARSE_CLIENT_KEY_FOR_TEST_HARNESS);
    }
    @Override
    public void onCreate() {
        System.out.println(TAG +":OnCreate");
        super.useParseCrashReporting=false;//don't use Parse's Crash Reporting (or any other)
        super.initParse=false;//don't init Parse Platform in our app class (we do this ourselves)
        super.testHarnessActive=true;//mark test harness as active (This is easy but not fully correct workaround Parse Analytics,etc issues)
        initParseForTests();
        System.out.println(TAG +":OnCreate. Done. Calling super.OnCreate()");
        super.onCreate();
        System.out.println(TAG +":OnCreate. super.OnCreate() done");

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