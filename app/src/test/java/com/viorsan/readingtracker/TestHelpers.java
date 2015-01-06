package com.viorsan.readingtracker;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.concurrent.TimeoutException;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.01.15.
 * based off https://stackoverflow.com/questions/21417954/espresso-thread-sleep
 */
public class TestHelpers {
    public static final long MILLIS_IN_SECOND = 1000;
    public static final long SECONDS_3  = 3*MILLIS_IN_SECOND;
    public static final long SECONDS_7  = 7*MILLIS_IN_SECOND;
    public static final long SECONDS_15 =15*MILLIS_IN_SECOND;
    public static final long SECONDS_30 =30*MILLIS_IN_SECOND;


    /**
     * Perform action of waiting for a specific view id
     * based off https://stackoverflow.com/questions/21417954/espresso-thread-sleep
     */
    public static ViewAction waitId(final int viewId, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with id <" + viewId + "> during " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                final Matcher<View> viewMatcher = withId(viewId);

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return;
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50);
                }
                while (System.currentTimeMillis() < endTime);

                // timeout happens
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }
}
