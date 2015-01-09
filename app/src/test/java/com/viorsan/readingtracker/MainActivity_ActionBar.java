package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 10.01.15.
 */

import com.parse.ParseException;
import com.parse.ParseUser;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.openContextualActionModeOverflowMenu;



import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.viorsan.readingtracker.TestHelpers.SECONDS_30;
import static com.viorsan.readingtracker.TestHelpers.screenshot;
import static com.viorsan.readingtracker.TestHelpers.waitId;
import static org.hamcrest.CoreMatchers.*;
import static com.viorsan.readingtracker.CustomMatchers.withResourceName;

import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.01.15.
 * Tests ActionBar
 */
@LargeTest
public class MainActivity_ActionBar extends MyInstrumentationTestCase { // ActivityInstrumentationTestCase2<MainActivity> {

    public static final String TAG = "ReadingTrackerTests::MainActivity_ActionBar";
    public static final int DEFAULT_SLEEP_TIME = 1337;//sometimes even 100 ms ok

    public MainActivity_ActionBar() {
        super();//MainActivity.class);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
        //Logout from Parse
        ParseUser.logOut();
    }



    public void testAboutButtonWorks() {
        String TEST_TAG="testAboutButtonWorks";
        //screenshot initial state
        onView(isRoot()).perform(screenshot(R.id.MainActivity,TEST_TAG+"_ActionBarTests_initialState"));
        // Open the overflow menu from action bar
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        //click on about box
        onView(withText(R.string.action_about))
                .perform(click());

        //check that about box displayed
        onView(withId(R.id.readingTrackerSlogan))
                .check(matches(withText(R.string.readingTrackerSlogan)));
        //screenshot about activity too
        onView(isRoot()).perform(screenshot(R.id.AboutActivity,TEST_TAG+"_ActionBarTests_AboutActivity"));


    }

}
