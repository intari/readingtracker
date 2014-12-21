package com.viorsan.readingtracker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.TextView;

import com.viorsan.readingtracker.MyActivity;
import com.viorsan.readingtracker.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.jar.Manifest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 17)
//Our big thanks to https://github.com/robolectric/robolectric/issues/1025, this is why those hacks are needed
//@Config(manifest = "./build/intermediates/manifests/full/internal/debug/AndroidManifest.xml", resourceDir = "../../../../res/internal/debug/", emulateSdk = 17)
@RunWith(RobolectricTestRunner.class)
public class ActivityUsageTest {


    @Before
    public void setup() {
        //do whatever is necessary before every test
        ShadowApplication app=Robolectric.getShadowApplication();
        //stop nagging from Parse
        app.grantPermissions(android.Manifest.permission.ACCESS_NETWORK_STATE);
    }
    @Test
    public void testWhichWork() throws Exception {
        assertTrue(true);
    }

    @Test
    public void shouldHaveApplicationName() throws Exception
    {
        String appName=new MyActivity().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("Reading Tracker"));
    }

    @Test
    public void testMainActivityCreation() throws Exception {
        MyActivity activity = Robolectric.setupActivity(MyActivity.class);
        //MyActivity activity = Robolectric.buildActivity(MyActivity.class).withApplication(Robolectric.application).create().get();
        assertNotNull(activity);
    }


    @Test
    public void testMainActivityCreationWithTestingIntent() throws Exception {
        Intent i= new Intent(Robolectric.application, MyActivity.class);
        i.putExtra("isTesting", true);
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).withIntent(i).create().get();
        assertNotNull(activity);
    }


    @Test
    public void testMainActivityHasAccessGrantedTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        assertNotNull(activity);
        TextView accessGrantedTextView=(TextView)activity.findViewById(R.id.accessGrantedTextView);
        assertNotNull(accessGrantedTextView);
    }

}
