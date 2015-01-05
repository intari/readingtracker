package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 * test our Parse Platform emulation for at least minimal correctness
 */

import android.os.Build;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ParseEmulationBaseTest {
    public static final String TAG = "ReadingTracker::ParseEmulationBaseTest";

    @Before
    public void setUp() {
        //do whatever is necessary before every test
        Log.d(TAG,"Setting up test");
        //TODO:save and restore parse platform mode
        //ParsePlatformUtils.ParsePlatformMode currentMode= ParsePlatformUtils.getParsePlatformMode();

    }
    @After
    public void tearDown() {
        Log.d(TAG,"Terminating test");
        //TODO:restore Parse Platform mode to native
    }

    @Test
    public void testParsePlatformIsInNativeModeByDefault() {
        ParsePlatformUtils.ParsePlatformMode currentMode= ParsePlatformUtils.getParsePlatformMode();
        assertThat(currentMode, is(ParsePlatformUtils.ParsePlatformMode.NORMAL));
    }


    @Test
    public void testVeryComplex() {
        Assert.assertTrue(Boolean.TRUE);
    }
}

