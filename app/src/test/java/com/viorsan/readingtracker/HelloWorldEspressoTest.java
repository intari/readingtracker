package com.viorsan.readingtracker;

import com.viorsan.readingtracker.MyActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.test.ActivityInstrumentationTestCase2;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;


import android.test.suitebuilder.annotation.LargeTest;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 */

@LargeTest
public class HelloWorldEspressoTest extends ActivityInstrumentationTestCase2<MyActivity> {

    public HelloWorldEspressoTest() {
        super(MyActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testCurrentlyReadingMessageDisplayed() {
        onView(withId(R.id.currentlyReadingMessage))
                .check(matches(isDisplayed()));
    }
    public void testListGoesOverTheFold() {
        onView(withText("Hello world")).check((android.support.test.espresso.ViewAssertion) ViewMatchers.isDisplayed());
    }
}
