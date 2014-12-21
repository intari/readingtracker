package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 20.12.14.
 */

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 17) //Robolectric support API level 18,17, 16, but not 19
@RunWith(RobolectricTestRunner.class)
public class ReadingTrackerUnitTests {
    @Before
    public void setup() {
        //do whatever is necessary before every test
    }

    @Test
    public void testVeryComplex() {
        Assert.assertTrue(Boolean.TRUE);
    }
}
