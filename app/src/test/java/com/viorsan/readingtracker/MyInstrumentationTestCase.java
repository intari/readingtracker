package com.viorsan.readingtracker;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 */

public class MyInstrumentationTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    public static final String TAG = "ReadingTracker::MyInstrumentationTestCase";
    private SystemAnimations systemAnimations;

    public MyInstrumentationTestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        Log.d(TAG,"setUp() before super()");
        super.setUp();
        Log.d(TAG,"setUp() before disabling animations");
        systemAnimations = new SystemAnimations(getInstrumentation().getContext());
        systemAnimations.disableAll();
        Log.d(TAG,"setUp() before getActivity");
        getActivity();
        Log.d(TAG,"setUp() after getActivity");

    }

    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG,"tearDown(), before super()");
        super.tearDown();
        systemAnimations.enableAll();
        Log.d(TAG,"tearDown(), after enabling animations");
    }
}
