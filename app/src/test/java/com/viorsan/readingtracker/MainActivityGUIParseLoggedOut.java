package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 06.01.15.
 */

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
import static com.viorsan.readingtracker.TestHelpers.SECONDS_30;
import static com.viorsan.readingtracker.TestHelpers.waitId;
import static org.hamcrest.CoreMatchers.*;


import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 * Tests MainActivity's GUI if user inititally not logged in to Parse Platform (including GUI login)
 */
@LargeTest
public class MainActivityGUIParseLoggedOut extends MyInstrumentationTestCase { // ActivityInstrumentationTestCase2<MainActivity> {

    public static final String TAG = "ReadingTrackerTests::MainActivityGUIParseLoggedOut";
    public static final int DEFAULT_SLEEP_TIME = 1337;//sometimes even 100 ms ok

    public MainActivityGUIParseLoggedOut() {
        super();//MainActivity.class);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
        //Logout from Parse
        ParseUser.logOut();
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

    public void testLoginLogoutButtonClickable() {
        onView(withId(R.id.login_or_logout_button))
                .check(matches(isClickable()));
    }

    /**
     * Test that LoginLogout correct in logged out state
     * Logout from Parse if necessary
     */
    public void testLoginLogoutButtonStateNotLoggedIn() {
        Log.d(TAG,"checking login button in correct state - it should be not logged in");
        onView(withId(R.id.login_or_logout_button))
                .check(matches(withText(R.string.profile_login_button_label)));
    }

    /**
     * Check that login button actually works
     */
    public void testLoginButtonWorksInitialStateLoggedOut() throws InterruptedException {
        Log.d(TAG,"Testing login button");
        //check we not logged in
        onView(withId(R.id.login_or_logout_button))
                .check(matches(withText(R.string.profile_login_button_label)));

        onView(withId(R.id.login_or_logout_button))
                .perform(click());
        Log.d(TAG,"Now entering login & password");
        // This view is in a different Activity, no need to tell Espresso.
        //TODO: use test build's account & test build auth
        onView(withId(R.id.login_username_input))
                .perform(typeText(BuildConfig.PARSE_USERNAME_FOR_TEST_HARNESS)
                );

        onView(withId(R.id.login_password_input))
                .perform(typeText(BuildConfig.PARSE_PASSWORD_FOR_TEST_HARNESS),
                        closeSoftKeyboard());

        Log.d(TAG,"Login button pressed now checking");
        onView(isRoot()).perform(waitId(R.id.parse_login, SECONDS_30));
        Thread.sleep(DEFAULT_SLEEP_TIME,0);//Highscreen Boost IIse, or Android 4.3, or my stupidity but without this test will fail
        Log.d(TAG,"Done sleeping. checking  button pressed now checking");

        onView(withId(R.id.parse_login_button))
                .perform(click());
        //we should now be on initial screen
        //but in logged in state
        Log.d(TAG,"Wait a little until we complete login");
        //TODO:'idling resources!'
        onView(isRoot()).perform(waitId(R.id.MainActivity,SECONDS_30));
        Thread.sleep(DEFAULT_SLEEP_TIME,0);
        Log.d(TAG,"Should now be logged in");
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        assertNotNull("Parse's currentUser should not be null after login",currentUser);

        Log.d(TAG, "Validating we were logged in correctly");
        onView(withId(R.id.login_or_logout_button))
                .check(matches(withText(R.string.profile_logout_button_label)));

        Log.d(TAG,"Logging out");
        onView(withId(R.id.login_or_logout_button))
                .perform(click());

    }

}

