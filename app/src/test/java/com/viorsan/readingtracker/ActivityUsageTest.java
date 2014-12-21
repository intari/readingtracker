package com.viorsan.readingtracker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Button;
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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
        //MyActivity activity = Robolectric.setupActivity(MyActivity.class);
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
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
        TextView accessGrantedTextView=(TextView)activity.findViewById(R.id.accessGrantedTextView);
        assertNotNull(accessGrantedTextView);
    }
    @Test
    public void testMainActivityHasAccessGrantedTextViewOfCorrectType() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView accessGrantedTextView=(TextView)activity.findViewById(R.id.accessGrantedTextView);
        assertThat(accessGrantedTextView,instanceOf(TextView.class));
    }

    @Test
    public void testMainActivityHasCurrentlyReadingMessageTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView currentlyReadingMessageTextView=(TextView)activity.findViewById(R.id.currentlyReadingMessage);
        assertNotNull(currentlyReadingMessageTextView);
    }
    @Test
    public void testMainActivityHasCurrentlyReadingMessageTextViewOnCorrectType() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView currentlyReadingMessageTextView=(TextView)activity.findViewById(R.id.currentlyReadingMessage);
        assertThat(currentlyReadingMessageTextView, instanceOf(TextView.class));
    }

    @Test
    public void testMainActivityHasMantanoReaderInstalledOkTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView mantanoReaderInstalledOkTextView=(TextView)activity.findViewById(R.id.mantanoReaderInstalledOkTextView);
        assertNotNull(mantanoReaderInstalledOkTextView);
    }
    @Test
    public void testMainActivityHasMantanoReaderInstalledOkTextViewOfCorrectType() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView mantanoReaderInstalledOkTextView=(TextView)activity.findViewById(R.id.mantanoReaderInstalledOkTextView);
        assertThat(mantanoReaderInstalledOkTextView,instanceOf(TextView.class));
    }

    @Test
    public void testMainActivityHasEmailTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView emailTextView=(TextView)activity.findViewById(R.id.profile_email);
        assertNotNull(emailTextView);
    }
    @Test
    public void testMainActivityHasEmailTextViewOfCorrectType() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView emailTextView=(TextView)activity.findViewById(R.id.profile_email);
        assertThat(emailTextView, instanceOf(TextView.class));
    }
    @Test
    public void testMainActivityHasTitleTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView titleTextView=(TextView)activity.findViewById(R.id.profile_title);
        assertNotNull(titleTextView);
    }
    @Test
    public void testMainActivityHasTitleTextViewOfCorrectType() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView titleTextView=(TextView)activity.findViewById(R.id.profile_title);
        assertThat(titleTextView, instanceOf(TextView.class));
    }
    @Test
    public void testMainActivityHasNameTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView nameTextView=(TextView)activity.findViewById(R.id.profile_name);
        assertNotNull(nameTextView);
        assertThat(nameTextView, instanceOf(TextView.class));
    }
    @Test
    public void testMainActivityHasNameTextViewOfCorrectType() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView nameTextView=(TextView)activity.findViewById(R.id.profile_name);
        assertThat(nameTextView,instanceOf(TextView.class));
    }
    @Test
    public void testMainActivityHasLoginLogoutButton() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        Button loginLogoutButton=(Button)activity.findViewById(R.id.login_or_logout_button);
        assertThat(loginLogoutButton,instanceOf(TextView.class));
    }

}
