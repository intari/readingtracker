package com.viorsan.readingtracker;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import static junit.framework.Assert.assertNotNull;

import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 30.01.16.
 */
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {
    private MainActivity activity;
    // @Before => JUnit 4 annotation that specifies this method should run before each test is run
    // Useful to do setup for objects that are needed in the test
    @Before
    public void setup() {
        // Convenience method to run MainActivity through the Activity Lifecycle methods:
        // onCreate(...) => onStart() => onPostCreate(...) => onResume()
        activity = Robolectric.setupActivity(MainActivity.class);

        ShadowLog.stream = System.out; //This is for printing log messages in console
    }

    @Test
    public void testFailure() {
        assertTrue("this test will fail",false);
    }

    @Test
    public void testAccessGrantedTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.accessGranted);
        assertNotNull("No 'Access Granted' textview",textView);
    }
    @Test
    public void testAccessInAccessibilitySettingsTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.textView);
        assertNotNull("No 'Access in Accessibility Settings' textview",textView);
    }
    @Test
    public void testMantanoInstalledTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.textViewMantanoInstalled);
        assertNotNull("No 'Mantano Installed:' textview",textView);
    }
    @Test
    public void testSupportedEbookReaderStatusTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.supportedEbookReaderInstalledStatus);
        assertNotNull("No 'supported ebook reader status' textview",textView);
    }
    @Test
    public void testCurrentlyReadingMessageTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.currentlyReadingMessage);
        assertNotNull("No 'currently reading message' textview",textView);
    }
    @Test
    public void testProfileTitleTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.profile_title);
        assertNotNull("No 'Profile Title' textview",textView);
    }
    @Test
    public void testProfileNameTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.profile_name);
        assertNotNull("No 'Profile Name' textview",textView);
    }
    @Test
    public void testProfileEmailTextView() {
        //test if this text view present
        TextView textView=(TextView) activity.findViewById(R.id.profile_email);
        assertNotNull("No 'Profile E-mail' textview", textView);
    }
    @Test
    public void testLoginLogoutButtonHere() {
        Button button=(Button) activity.findViewById(R.id.login_or_logout_button);
        assertNotNull("No 'Login/Logout' button", button);
    }
}
