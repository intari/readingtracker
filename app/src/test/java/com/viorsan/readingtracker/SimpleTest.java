package com.viorsan.readingtracker;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.01.15.
 */
@SmallTest
public class SimpleTest {

    public static final String TAG = "ReadingTracker:SimpleTest";

    @Before
    public void setUp() {
        Log.d(TAG,"Setting up test");
    }
    @Test
    public void testPass() {
        Assert.assertEquals(2+2,4);
    }
    @Test void testVeryComplex() {
        Assert.assertTrue(Boolean.TRUE);
    }
    
}
