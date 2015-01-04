package com.viorsan.readingtracker;

import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 */
public class MyTestApplication extends ApplicationTestCase<MyApplication> {

    public static final String TAG = "ReadingTracker::MyTestApplication";

    private void Log(String msg) {
        Log.d(TAG,msg);
        System.out.println(TAG+" "+msg);

    }
    public MyTestApplication() {
        super(MyApplication.class);

        Log("constructor, super called");
    }

    @Override
    protected void setUp() throws Exception {
        Log("setUp(), will call createApplication()");
        createApplication();
        Log("setUp(), called createApplication()");
    }
    @Override
    protected void tearDown() throws Exception {
        Log("tearDown()");
        super.tearDown();

    }
}
