package com.viorsan.readingtracker;

import com.viorsan.readingtracker.MyActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.test.ActivityInstrumentationTestCase2;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;


import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 */

@LargeTest
public class HelloWorldEspressoTest extends ActivityInstrumentationTestCase2<MyActivity> {

    public static final String TAG = "ReadingTrackerTests::HelloWorldEspressoTest";

    public HelloWorldEspressoTest() {
        super(MyActivity.class);
        Log.d(TAG,"in test constructor, called super");

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Log.d(TAG,"Setting up test...");
    }

    public void testCurrentlyReadingMessageDisplayed() {
        onView(withId(R.id.currentlyReadingMessage))
                .check(matches(isDisplayed()));
    }
    public void testAccessGrantedDisplayed() {
        onView(withId(R.id.accessGranted))
                .check(matches(isDisplayed()));
    }
    public void testLoginLogoutButtonClickable() {
        onView(withId(R.id.login_or_logout_button))
                .check(matches(isClickable()));
    }

    /**
     * Check that login button actually works
     */
    public void testLoginButton() {
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

        onView(withId(R.id.parse_login_button))
                .perform(click());
        Log.d(TAG,"Login button pressed now checking");
        //we should now be on initial screen
        //but in logged in state

        onView(withId(R.id.login_or_logout_button))
            .check(matches(withText(R.string.profile_logout_button_label)));

        Log.d(TAG,"Validating we were logged in correctly");
    }

}
