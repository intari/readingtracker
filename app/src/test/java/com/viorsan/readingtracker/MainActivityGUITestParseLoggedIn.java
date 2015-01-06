package com.viorsan.readingtracker;

import com.parse.ParseException;
import com.parse.ParseUser;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.viorsan.readingtracker.TestHelpers.SECONDS_15;
import static com.viorsan.readingtracker.TestHelpers.waitId;
import static org.hamcrest.CoreMatchers.*;


import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 * Tests MainActivity's GUI if user inititally logged in to Parse Platform
 *
 */

@LargeTest
public class MainActivityGUITestParseLoggedIn extends MyInstrumentationTestCase { // ActivityInstrumentationTestCase2<MainActivity> {

    public static final String TAG = "ReadingTrackerTests::MainActivityGUITestParseLoggedIn";
    public static final int DEFAULT_SLEEP_TIME = 1337;//sometimes even 100 ms ok

    public MainActivityGUITestParseLoggedIn() {
        super();//MainActivity.class);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
        //login to Parse
        ParseUser.logIn(BuildConfig.PARSE_USERNAME_FOR_TEST_HARNESS,BuildConfig.PARSE_PASSWORD_FOR_TEST_HARNESS);
    }

    public void testCurrentlyReadingMessageDisplayed() {
        onView(withId(R.id.currentlyReadingMessage))
                .check(matches(isDisplayed()));
    }
    public void testAccessGrantedDisplayed() {
        onView(withId(R.id.accessGranted))
                .check(matches(isDisplayed()));
    }

    public void testSupportedEbookReaderInstalledStatusDisplayed() {
        onView(withId(R.id.supportedEbookReaderInstalledStatus))
                .check(matches(isDisplayed()));
    }
    public void testProfileTitleDisplayed() {
        onView(withId(R.id.profile_title))
                .check(matches(isDisplayed()));
    }

    public void testProfileNameDisplayed() {
        onView(withId(R.id.profile_name))
                .check(matches(isDisplayed()));
    }

    public void testLoginLogoutButtonClickable() {
        onView(withId(R.id.login_or_logout_button))
                .check(matches(isClickable()));
    }

    /**
     * Test that LoginLogout correct in logged in state
     * Login to Parse if necessary
     */
    public void testLoginLogoutButtonStateLoggedIn() throws ParseException {
        Log.d(TAG,"checking login button in correct state - it should be logged in now");
        onView(withId(R.id.login_or_logout_button))
                .check(matches(withText(R.string.profile_logout_button_label)));
    }


}
