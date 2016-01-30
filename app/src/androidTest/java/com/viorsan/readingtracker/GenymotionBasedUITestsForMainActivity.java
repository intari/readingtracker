package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 07.01.15.
 */

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import org.junit.Before;
import org.junit.Test;
import com.genymotion.api.GenymotionManager;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.01.15.
 * Prototype for Genymotion-based tests
 */
@SmallTest
public class GenymotionBasedUITestsForMainActivity extends MyInstrumentationTestCase {

    public static final String TAG = "ReadingTracker:GenymotionBasedUITestsForMainActivity";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }
    @Test
    public void testBasicGenymotion() {
        //only run this test if it's genymotion
        if (!GenymotionManager.isGenymotionDevice()) {
            Log.d(TAG,"Not running under Genymotion");
            return; //don't execute this test
        }
        Log.d(TAG,"Running under Genymotion. proceed with test");
        GenymotionManager genymotion;
        genymotion = GenymotionManager.getGenymotionManager(getInstrumentation().getTargetContext());
        //TODO:actually test something

       }
}

