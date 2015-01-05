package com.viorsan.readingtracker;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.01.15.
 */
@SmallTest
public class SimpleTest {
    @Test
    public void testFail() {
        Assert.assertEquals("2+2=5 in this universe!",2+2,5);
    }
    @Test
    public void testPass() {
        Assert.assertEquals(2+2,4);
    }
}
