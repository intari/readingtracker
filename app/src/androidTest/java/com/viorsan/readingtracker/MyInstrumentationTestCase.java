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
        super.setUp();
        systemAnimations = new SystemAnimations(getInstrumentation().getContext());
        systemAnimations.disableAll();
        getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        systemAnimations.enableAll();
    }
}
