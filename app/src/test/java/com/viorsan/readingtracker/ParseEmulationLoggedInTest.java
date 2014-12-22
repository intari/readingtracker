package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.12.14.
 * Tests for my emulation of Parse Platform's ParseUser Class (in logged-in state)
 */

import android.os.Build;

import com.parse.ParseException;
import com.parse.ParseUser;

import junit.framework.Assert;

import org.hamcrest.CoreMatchers;
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
import static org.junit.Assert.fail;

@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR1) //Robolectric support API level 18,17, 16, but not 19
@RunWith(RobolectricTestRunner.class)
public class ParseEmulationLoggedInTest {
    public static final String TAG = "ParseEmulationLoggedIn";
    private ParsePlatformUtils.ParsePlatformMode oldMode;
    @Before
    public void setup() {
        //do whatever is necessary before every test
        System.out.println(TAG+":setting up test");
        //store old mode
        oldMode= ParsePlatformUtils.getParsePlatformMode();
        //set mode for test
        ParsePlatformUtils.setParsePlatformMode(ParsePlatformUtils.ParsePlatformMode.TEST_LOGGED_IN);
    }
    @After
    public void done() {
        System.out.println(TAG+":stopping test");
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
    //This applies to my test framework only
    @Test
    public void testLoggenInParseObjectAuthenticated() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertTrue("isAuthenticated() must return true for emulated logged in ParseUser", currentUser.isAuthenticated());
    }
    //This applies to my test framework only
    @Test
    public void testLoggenInParseObjectNew() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertFalse("isNew() must return false for emulated logged in ParseUser",currentUser.isNew());
    }
    @Test
    public void testLoggedInFetchIfNeeded() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        try {
            currentUser.fetchIfNeeded();
        } catch (ParseException e) {
            fail("fetchIfNeeded threw ParseException in emulated logged in ParseUser. it should not");
        }
    }
    @Test
    public void testEmailWorksForRFC6530EAddresses() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        currentUser.setEmail("чебурашка@ящик-с-апельсинами.рф");
        assertThat("rfc6530 e-mail addresses must be supported for e-mail",currentUser.getEmail(),equalTo("чебурашка@ящик-с-апельсинами.рф"));
    }
    @Test
    public void testEmailWorks() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        currentUser.setEmail("user@example.net");
        assertThat(currentUser.getEmail(),equalTo("user@example.net"));
    }
    @Test
    public void testUsernameWorks() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        currentUser.setUsername("nevesta1337");
        assertThat(currentUser.getUsername(),equalTo("nevesta1337"));
    }

    @Test
    public void testCustomFieldsWorkName() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        currentUser.put("name","Princess Bride");
        assertThat(currentUser.getString("name"),equalTo("Princess Bride"));
    }
    @Test
    public void testCustomFieldsWorkForObject() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        Double sourceNumber=new Double(Math.PI);
        Double checkNumber=new Double(Math.PI);
        currentUser.put("numberPi",sourceNumber);
        assertThat(currentUser.get("numberPi"), CoreMatchers.<Object>equalTo(checkNumber));
    }
    @Test
    public void testSetObjectIdWorks() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        currentUser.setObjectId("Z1997");
        assertThat(currentUser.getObjectId(),equalTo("Z1997"));
    }
    @Test
    public void testObjectIdContainsInitialValue() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertNotNull(currentUser.getObjectId());
    }
    @Test
    public void testEmailContainsInitialValue() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertNotNull(currentUser.getEmail());
    }
    @Test
    public void testUsernameContainsInitialValue() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertNotNull(currentUser.getUsername());
        System.out.println("TTT:"+currentUser.getUsername());
    }
    @Test
    public void testCustomFieldNameContainsInitialValue() {
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertNotNull(currentUser.getString("name"));
    }


    @Test
    public void testBasicArithmetic() {
        assertThat("2+2=4",2+2,equalTo(4));
    }

}


