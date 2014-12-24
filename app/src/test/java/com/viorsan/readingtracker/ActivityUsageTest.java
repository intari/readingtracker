package com.viorsan.readingtracker;

import android.content.Intent;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.FragmentTestUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        TextView accessGrantedTextView=(TextView)activity.findViewById(R.id.accessGranted);
        assertNotNull(accessGrantedTextView);
    }

    @Test
    public void testMainActivityHasCurrentlyReadingMessageTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView currentlyReadingMessageTextView=(TextView)activity.findViewById(R.id.currentlyReadingMessage);
        assertNotNull(currentlyReadingMessageTextView);
    }

    @Test
    public void testMainActivityHasSupportedEbookReaderReaderInstalledOkTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView supportedEbookReaderInstalledOkTextView=(TextView)activity.findViewById(R.id.supportedEbookReaderInstalledStatus);
        assertNotNull(supportedEbookReaderInstalledOkTextView);
    }
    @Test
    public void testMainActivityHasEmailTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView emailTextView=(TextView)activity.findViewById(R.id.profile_email);
        assertNotNull(emailTextView);
    }
    @Test
    public void testMainActivityHasTitleTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView titleTextView=(TextView)activity.findViewById(R.id.profile_title);
        assertNotNull(titleTextView);
    }
    @Test
    public void testMainActivityHasNameTextView() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        TextView nameTextView=(TextView)activity.findViewById(R.id.profile_name);
        assertNotNull(nameTextView);
    }
    @Test
    public void testMainActivityHasLoginLogoutButton() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        Button loginLogoutButton=(Button)activity.findViewById(R.id.login_or_logout_button);
        assertNotNull(loginLogoutButton);
    }
    @Test
    public void testMainActivityHasLoginLogoutButtonTryPress() throws Exception {
        MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        Button loginLogoutButton=(Button)activity.findViewById(R.id.login_or_logout_button);
        loginLogoutButton.performClick();
        //TODO:check that correct handler was called (depending on login state)

    }

    /*
    This will wait until I fix issues with Travis CI and Robolectric
    //check that dialog used in  'showGoToAccessibilitySettingsDialog()' could be shown
    @Test
    public void testGoToAccessibilitySettingsDialogWorks() throws Exception {
        //MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
        GoToAccessibilitySettingsDialogFragment newFragment=new GoToAccessibilitySettingsDialogFragment();
        assertNotNull(newFragment);
        FragmentTestUtil.startFragment(newFragment,MyActivity.class);

        //TODO:complete this.  are buttons shown? is message correct?  is dialog visible? Is my understanding of Robolectric correct? -:)
     }
*/
    //Translation testing
    //Test message directly
    @Test
    @Config(qualifiers="en")
    public void shouldHaveEnglishAccessibilityMessage() throws Exception
    {
        assertTrue(Robolectric.application.getString(R.string.accessibilityDescription).equals("Monitor your book reading habits"));
    }
    /*
    @Test
    @Config(qualifiers="en")
    public void shouldHaveEnglishAccessibilityMessageTestViaMainActivity() throws Exception
    {
        String message=new MyActivity().getResources().getString(R.string.accessibilityDescription);
        assertThat(message, equalTo("Monitor your book reading habits"));
    }
    */
    @Test
    @Config(qualifiers="ru")
    public void shouldHaveRussianAccessibilityMessage() throws Exception
    {
        assertTrue(Robolectric.application.getString(R.string.accessibilityDescription).equals("Отслеживать читаемые книги"));
    }


}
