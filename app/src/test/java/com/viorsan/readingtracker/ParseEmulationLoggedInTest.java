package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 */

import android.os.Build;

import com.parse.ParseUser;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR1) //Robolectric support API level 18,17, 16, but not 19
@RunWith(RobolectricTestRunner.class)
public class ParseEmulationLoggedInTest {
    public static final String TAG = "ParseEmulationNotLoggedIn";
    private ParsePlatformUtils.ParsePlatformMode oldMode;
    @Before
    public void setup() {
        //do whatever is necessary before every test
        System.out.println(TAG+": setting up test");
        //store old mode
        oldMode= ParsePlatformUtils.getParsePlatformMode();
        //set mode for test
        ParsePlatformUtils.setParsePlatformMode(ParsePlatformUtils.ParsePlatformMode.TEST_LOGGED_IN);
    }
    @After
    public void done() {
        System.out.println(TAG+": stopping test");
        //restore old emulation mode
        ParsePlatformUtils.setParsePlatformMode(oldMode);
    }
    @Test
    public void testNotLoggedInEmulationInCorrectMode() {
        assertThat("Parse Platform's emulation is in 'logged in' mode", ParsePlatformUtils.getParsePlatformMode(), equalTo(ParsePlatformUtils.ParsePlatformMode.TEST_LOGGED_IN));
    }
    @Test
    public void testLoggedInGetCurrentUserReturnsNotNull() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertNotNull("Not Logged In ParseUser must be null",currentUser);

    }
    @Test
    public void testBasicArithmetic() {
        assertThat("2+2=4",2+2,equalTo(4));
    }

}


