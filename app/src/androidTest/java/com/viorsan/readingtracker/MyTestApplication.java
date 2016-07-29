package com.viorsan.readingtracker;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.parse.Parse;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 */
public class MyTestApplication extends ApplicationTestCase<MyApplication> {

    public static final String TAG = "ReadingTracker::MyTestApplication";

    public MyTestApplication() {
        super(MyApplication.class);
        Log.d(TAG,"constructor, super called");
        Log.d(TAG,"Flavour:"+BuildConfig.FLAVOR);

    }

    @Override
    protected void setUp() throws Exception {
        Log.d(TAG,"setUp()");
        MyApplication.setInitParse(false);
        Parse.initialize(getSystemContext(), BuildConfig.PARSE_APP_ID, BuildConfig.PARSE_CLIENT_KEY);
        MyApplication.setAnalyticsEnabled(false);
        MyApplication.espressoTestActive=true;
        Log.d(TAG,"setUp(), will call createApplication()");
        createApplication();
        Log.d(TAG,"setUp(), called createApplication()");
    }
    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG,"tearDown()");
        super.tearDown();

    }
}
