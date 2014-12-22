package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 * test our Parse Platform emulation for at least minimal correctness
 */

import android.os.Build;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR1) //Robolectric support API level 18,17, 16, but not 19
@RunWith(RobolectricTestRunner.class)
public class ParseEmulationBaseTest {
    public static final String TAG = "ParseEmulationTest";

    @Before
    public void setup() {
        //do whatever is necessary before every test
        //Parse Platform should arleady be in simulation mode?
        System.out.println(TAG+": setting up test()");
        //will be NOT_LOGGED_IN if no env var present.see TestMyApplication
        ParsePlatformUtils.ParsePlatformMode currentMode= ParsePlatformUtils.getParsePlatformMode();
        System.out.println("Mode:"+currentMode.toString());
    }
    @After
    public void done() {

    }
    @Test
    public void testParsePlatformIsInEmulationMode() {
        ParsePlatformUtils.ParsePlatformMode currentMode= ParsePlatformUtils.getParsePlatformMode();
        assertNotEquals("Parse Platform is not in native mode",currentMode, ParsePlatformUtils.ParsePlatformMode.NORMAL);
    }

    @Test
    public void testVeryComplex() {
        Assert.assertTrue(Boolean.TRUE);
    }
}

