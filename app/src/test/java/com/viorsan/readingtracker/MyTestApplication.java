package com.viorsan.readingtracker;

import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 */
public class MyTestApplication extends ApplicationTestCase<MyApplication> {

    public static final String TAG = "ReadingTracker::MyTestApplication";

    public MyTestApplication() {
        super(MyApplication.class);
        Log.d(TAG,"constructor, super called");
    }

    @Override
    protected void setUp() throws Exception {
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
