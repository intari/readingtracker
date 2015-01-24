package com.viorsan.readingtracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.parse.*;
import com.parse.ui.ParseLoginBuilder;

import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/*
 * Main activity of application
 *
 * This Activity extends from {@link ActionBarActivity}, which provides all of the function
 * necessary to display a compatible Action Bar on devices running Android v2.1+.
 */
public class MainActivity extends ActionBarActivity implements GoToAccessibilitySettingsDialogFragment.GoToAccessibilitySettingsDialogListener {
    public static final String FULL_USER_NAME = "name";
    public static final String USER_GENDER = "gender";
    public static final int TIME_BEFORE_ASKING_USER_TO_GO_TO_ACCESSIBILITY_SETTINGS = 5 * 1000;//5 seconds to wait before checking if we should ask user to go to Accessibility settings
    public static final int TIME_BEFORE_ASKING_FOR_MONITORING_STATUS_UPDATE = 5 * 1000;
    public static final String COUNTLY_USERNAME = "username";
    public static final String COUNTLY_EMAIL = "email";
    public static final String COUNTLY_FULLNAME = "name";
    public static final String LANGUAGE = "language";
    public static final String COUNTRY = "country";
    public static final String LOCALE = "locale";
    public static final String USER_LOCALE_SETTINGS = "userLocaleSettings";
    public static final String USERNAME = "username";
    @InjectView(R.id.currentlyReadingMessage) TextView currentlyReadingTextView;
    @InjectView(R.id.accessGranted) TextView accessGrantedTextView;
    @InjectView(R.id.supportedEbookReaderInstalledStatus) TextView supportedEbookReaderInstalledTextView;
    @InjectView(R.id.profile_title) TextView titleTextView;
    @InjectView(R.id.profile_name) TextView nameTextView;
    @InjectView(R.id.profile_email) TextView emailTextView;
    @InjectView(R.id.login_or_logout_button) Button loginOrLogoutButton;

    private boolean activityRecorderConnected=false;
    private boolean goToSettingsToEnableAccessibilityServiceDialogShown=false;//true - 'go to settings to enable us' dialog was shown

    private BroadcastReceiver messageReceiver;
    private BroadcastReceiver activityMonitoringMessageReceiver;
    private BroadcastReceiver currentlyReadingMessageReceiver;

    private Activity self;
    private static final String TAG = "ReadingTracker::MainActivity";

    private static final int LOGIN_REQUEST = 0;


    private ParseUser currentUser;
    private CountDownTimer timerToWaitBeforeAskingForAccessibilitySettings=null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyAnalytics.trackAppOpened(getIntent());
        setContentView(R.layout.main);
        ButterKnife.inject(this);
        init();

    }
    private MyApplication getMyApp() {
        return (MyApplication)getApplication();
    }

    /**
     * Support for user login
     * Starts CoreService (notification, etc)
     * Sends analytics event
     * Performs sanity check and sets user data for analytics from Parse data
     */
    private void handleUserLogin()  {
        Log.d(TAG,"Signaling everybody that user was logged in");
        startService();
        //it will autostop if no user active
        MyAnalytics.trackEvent("userLoggedIn");
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();

        if (currentUser!=null) {
            MyAnalytics.setUserId(currentUser.getUsername());
            /* Countly Community does not support those data
               It's currently Countly Enterprise but it's planned to be in Cloud
               see http://support.count.ly/discussions/questions/5624-userdata-vs-cloud-edition

             */
            MyAnalytics.provideUserdata(COUNTLY_USERNAME,currentUser.getUsername());
            if (currentUser.getEmail()!=null) {
                MyAnalytics.provideUserdata(COUNTLY_EMAIL,currentUser.getEmail());
            }
            String fullName = currentUser.getString(FULL_USER_NAME);
            if (fullName != null) {
                MyAnalytics.provideUserdata(COUNTLY_FULLNAME,fullName);
            }
            MyAnalytics.sendUserData();
        }
        else
        {
            MyAnalytics.trackEvent("userLoginNoCurrentUser");
            Log.d(TAG,"handleUserLogin but no current user?!");
        }
    }

    /**
     * Support for user logout.
     * Sends Local Broadcast so everybody who need will knew
     * Sends analytics event
     */
    private void handleUserLogout() {
        //ask core service to stop
        Log.d(TAG,"Signaling everybody that user was logged out");
        Intent intent = new Intent(CoreService.USER_LOGGED_OUT_REPORT);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        Log.d(TAG,"Signaled everybody that user was logged out");
        MyAnalytics.trackEvent("userLogout");
    }
    @OnClick(R.id.login_or_logout_button)
    public void loginLogoutButtonClicked(View v) {
        if (currentUser != null) {
            // User clicked to log out.
            MyAnalytics.trackEvent("userClickedLogoutButton");
            ParseUser.logOut();
            currentUser = null;
            handleUserLogout();
            showProfileLoggedOut();
        } else {
            // User clicked to log in.
            MyAnalytics.trackEvent("userClickedLoginButton");
            ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                    MainActivity.this);
            /*loginBuilder
                    .setFacebookLoginEnabled(true)
                    .setParseLoginEnabled(true);
                    //.setFacebookLoginPermissions(Arrays.asList("user_status", "read_stream"));
            */

            startActivityForResult(loginBuilder.build(), LOGIN_REQUEST);
        }
    }

    /**
     * Initializes push notification support from Parse Platform
     * see https://parse.com/tutorials/android-push-notifications and https://parse.com/docs/push_guide#top/Android
     */
    private void initPush() {
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e(TAG, "failed to subscribe for push", e);
                }
            }
        });

    }

    /**
     * Updates current 'installation' object
     * used to target Push Notifications
     * see https://parse.com/docs/push_guide#top/Android
     */
    private void updateInstallationObject() {
        //TODO:ParseInstallation also needs to be wrapped in ParsePlatformUtils
        ParseInstallation installation=ParseInstallation.getCurrentInstallation();
        DeviceInfoManager deviceInfoManager=new DeviceInfoManager();

        installation.put("ourDeviceId",deviceInfoManager.getDeviceId(this));
        installation.put("runtime",deviceInfoManager.getCurrentRuntimeValue());
        String simOperatorName=deviceInfoManager.getSimOperatorName(this);
        if (simOperatorName!=null) {
            installation.put("simOperatorName",simOperatorName);
        }
        String networkOperatorName=deviceInfoManager.getNetworkOperatorName(this);
        if (networkOperatorName!=null) {
            installation.put("networkOperatorName",networkOperatorName);
        }
        String simCountryISO=deviceInfoManager.getSimOperatorCountryISO(this);
        if (simCountryISO!=null) {
            installation.put("simCountryISO",simCountryISO);
        }
        String networkCountryISO=deviceInfoManager.getNetworkOperatorCountryISO(this);
        if (networkCountryISO!=null) {
            installation.put("networkCountryISO",networkCountryISO);
        }
        String language= Locale.getDefault().getLanguage();
        installation.put("language",language);
        String country= Locale.getDefault().getCountry();
        installation.put("country",country);
        String locale=Locale.getDefault().toString();
        installation.put("locale",locale);

        installation.put("appBuildFlavor",BuildConfig.FLAVOR);
        //deviceType used in book readings reports
        installation.put("deviceInfoString", BookReadingsRecorder.getDeviceInfoString());

        installation.put("deviceModel",Build.MODEL);
        installation.put("deviceManufacturer",Build.MANUFACTURER);
        installation.put("deviceProduct",Build.PRODUCT);

        // associate device with user
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();
        if (currentUser!=null) {
            installation.put("user",ParseUser.getCurrentUser());
            //configure access for this user only. Cloud Code functions could use MasterKey to enable access for themselves
            ParseACL acl=new ParseACL(currentUser);
            installation.setACL(acl);
        }
        //TODO: what else? some user groups?
        //TODO: when we have 'user groups' use channels
        //TODO:use genres user likes? or authors user likes?
        //TODO:make it possible to target user and not device


        //save updated object
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
    private void init(){
        self = this;

        //init Parse Platform's push support
        initPush();
        //update Installation class
        updateInstallationObject();

        titleTextView.setText(R.string.profile_title_logged_in);
        //even if user is not logged in we should configured other parse of interface


        //TODO:this is my old code
        //TODO:get cached version if it's exist
        ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();

        if (currentUser!=null) {

            try{
                currentUser.fetchIfNeeded();

            } catch (ParseException ex)
            {
                Log.d(TAG,"Parse Exception "+ex.toString());
            }

            //user can be automatic. allow for this in future
            if (currentUser.isAuthenticated()) {
                Log.i(TAG, "ParseUser here. Authenticated. ");
                //set an ACL on the current user's data to not be publicly readable
                currentUser.setACL(new ParseACL(currentUser));
                MyAnalytics.trackEvent("userLoggedInArleady");
            }
            else
            {
                Log.i(TAG, "ParseUser here. NOT Authenticated. ");
                MyAnalytics.trackEvent("userLoggedInWithoutAuth");//in case I forget and re-enable anonymous users
            }
            //update user's locale settings
            //it's likely every user's device will have same data (or at least country be same)
            Locale locale=Locale.getDefault();
            String userLanguage=locale.getLanguage();
            String userCountry=locale.getCountry();
            String userLocale=locale.toString();

            //send analytics event so I knew is this changed a lot between users
            Map<String, String> dimensions = new HashMap<String, String>();
            dimensions.put(LANGUAGE,userLanguage);
            dimensions.put(COUNTRY,userCountry);
            dimensions.put(LOCALE,userLocale);
            //May be I should use hashed version of current username to avoid revealing too much?
            dimensions.put(USERNAME,currentUser.getUsername());
            MyAnalytics.trackEvent(USER_LOCALE_SETTINGS, dimensions);

            //save to current user information
            currentUser.put(LANGUAGE, userLanguage);
            currentUser.put(COUNTRY,userCountry);
            currentUser.put(LOCALE,userLocale);

            currentUser.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(TAG,"Successfully updated user information");
                    } else {
                        Log.e(TAG,"Failed to subscribe due to exception "+e.toString());
                    }
                }
            });

        }

        checkForUpdates();


        /* debug support */
        messageReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                Long requestId=intent.getLongExtra("RequestID",0);
                Long sessionId=intent.getLongExtra("SessionID",0);
                Log.d(TAG, "updating GUI to Last Request ID:" + requestId);


            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("ReportSendingUpdates"));

        /* so we knew activity monitoring service is here */
        activityMonitoringMessageReceiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG,"Activity monitoring service ready");
                updateMonitoringStatus();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                activityMonitoringMessageReceiver,new IntentFilter(AccessibilityRecorderService.ACTIVITY_MONITORING_CONNECTED)
        );

        currentlyReadingMessageReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String bookTitle=intent.getStringExtra(BookReadingsRecorder.BOOK_TITLE);
                String bookAuthor=intent.getStringExtra(BookReadingsRecorder.BOOK_AUTHOR);
                String bookTags=intent.getStringExtra(BookReadingsRecorder.BOOK_TAGS);
                Double totalTime=intent.getDoubleExtra(BookReadingsRecorder.READING_SESSION_TIME,0);
                String currentPageS=intent.getStringExtra(BookReadingsRecorder.CURRENT_PAGE);
                String totalPageS=intent.getStringExtra(BookReadingsRecorder.TOTAL_PAGES);

                Long pagesRead=intent.getLongExtra(BookReadingsRecorder.PAGES_READ,0);
                Long numPagePageSwitches=intent.getLongExtra(BookReadingsRecorder.NUM_PAGE_SWITCHES,0);

                double pagesPerSecond=pagesRead.doubleValue()/totalTime.doubleValue();


                String msg;
                if (currentPageS!=null) {
                    if (pagesRead==0) {
                        msg=getResources().getString(R.string.guiCurrentlyReadingLongZeroSpeed,bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0);
                    }
                    else {
                        //update
                        //msg=String.format("%s от %s (%s/%s ). %.2f минут",bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0);
                        msg=getResources().getString(R.string.guiCurrentlyReadingLong,bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0,pagesPerSecond*60.0);

                    }
                }
                else
                {
                    //update
                    //msg=String.format("%s от %s",bookTitle,bookAuthor);
                    msg=getResources().getString(R.string.guiCurrentlyReadingShort,bookTitle,bookAuthor);
                }
                Log.i(TAG,"Got reading update:"+msg);
                ParseUser currentUser=ParsePlatformUtils.getCurrentParseUser();

                if (currentUser!=null) {
                    currentlyReadingTextView.setText(msg);
                }
                else {
                    currentlyReadingTextView.setText(getResources().getText(R.string.profile_title_logged_out));

                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                currentlyReadingMessageReceiver,new IntentFilter(BookReadingsRecorder.BOOK_READING_STATUS_UPDATE)
        );
        /*
        new CountDownTimer(TIME_BEFORE_ASKING_FOR_MONITORING_STATUS_UPDATE, TIME_BEFORE_ASKING_FOR_MONITORING_STATUS_UPDATE) {
            public void onTick(long msUntilFinish) { }
            public  void  onFinish() {
                Log.d(TAG,"check if it's time to ask user to enable our Accessibility Service");
                askForActivityMonitoringUpdate();
            }
        }.start();
        */

        timerToWaitBeforeAskingForAccessibilitySettings=       new CountDownTimer(TIME_BEFORE_ASKING_USER_TO_GO_TO_ACCESSIBILITY_SETTINGS, TIME_BEFORE_ASKING_USER_TO_GO_TO_ACCESSIBILITY_SETTINGS) {
            public void onTick(long msUntilFinish) {
            }

            public  void  onFinish() {
                Log.d(TAG,"check if it's time to ask user to enable our Accessibility Service");
                if ((activityRecorderConnected==false) && (goToSettingsToEnableAccessibilityServiceDialogShown==false)) {
                    Log.d(TAG,"yes it is - asking");
                    goToSettingsToEnableAccessibilityServiceDialogShown=true;

                    MyAnalytics.trackEvent("userAskedToGoToAccSettings");
                    showGoToAccessibilitySettingsDialog();
                    Log.d(TAG,"yes it is - asked");
                }
                else {
                    Log.d(TAG,"Not it is not");

                }
            }
        };

    }
    /**
     * Shows the profile of the given user.
     */
    private void showProfileLoggedIn() {
        titleTextView.setText(R.string.profile_title_logged_in);
        emailTextView.setText(currentUser.getEmail());
        String fullName = currentUser.getString(FULL_USER_NAME);
        if (fullName != null) {
            nameTextView.setText(fullName);
        }
        loginOrLogoutButton.setText(R.string.profile_logout_button_label);
    }

    /**
     * Show a message asking the user to log in, toggle login/logout button text.
     */
    private void showProfileLoggedOut() {
        titleTextView.setText(R.string.profile_title_logged_out);
        emailTextView.setText("");
        nameTextView.setText("");
        loginOrLogoutButton.setText(R.string.profile_login_button_label);
    }

    /**
     * Checks status variable and shows in GUI is our monitoring service connected or not
     */
    private void updateMonitoringStatus() {
        if (activityRecorderConnected=true) {
            accessGrantedTextView.setText(getResources().getText(R.string.accessGrantedOK));
        }
        else
        {
            accessGrantedTextView.setText(getResources().getText(R.string.accessGrantedNotOk));
        }

    }

    /**
     * Updates textview about supported reader status being installed based on result of {@link #isSupportedEbookReaderInstalled()}
     */
    private void updateReaderStatus() {
        if (isSupportedEbookReaderInstalled()) {
           supportedEbookReaderInstalledTextView.setText(getResources().getText(R.string.supportedEbookReaderInstalled));
        }
        else {
            supportedEbookReaderInstalledTextView.setText(getResources().getText(R.string.supportedEbookReaderNotInstalled));
        }
    }

    /**
     * Checks if one of supported E-Book readers installed
     * @return true if one of supported E-Book readers are found
     */
    private boolean isSupportedEbookReaderInstalled()
    {
      return (
              AppHelpers.appInstalledOrNot(this,AccessibilityRecorderService.MANTANO_READER_PACKAGE_NAME) ||
              AppHelpers.appInstalledOrNot(this,AccessibilityRecorderService.MANTANO_READER_ESSENTIALS_PACKAGE_NAME) ||
              AppHelpers.appInstalledOrNot(this,AccessibilityRecorderService.MANTANO_READER_LITE_PACKAGE_NAME) ||
              AppHelpers.appInstalledOrNot(this,AccessibilityRecorderService.FBREADER_PACKAGE_NAME)

      );

    };


    private void startService() {
        startService(new Intent(this, CoreService.class));

    }

   /**
    * This dialog can have significant amount of text so make it possible for it be shown in fullscreen way
    * and not only as regular dialog
    */
    public void showGoToAccessibilitySettingsDialog() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        GoToAccessibilitySettingsDialogFragment newFragment=new GoToAccessibilitySettingsDialogFragment();

        newFragment.show(fragmentManager, "goToAccessibilitySettingsDialog");

    }

    /**
     * implementation of GoToAccessibilitySettingsDialogListener
     */
    public void onGoToAccessibilitySettingsDialogPositiveClick(GoToAccessibilitySettingsDialogFragment dialog) {
        // Users wants to go to settings
        Log.d(TAG,"User chooses to go to settings");
        MyAnalytics.trackEvent("userWentToAccSettings");
        openAccessibilitySettings();
    }
    /**
     * implementation of GoToAccessibilitySettingsDialogListener
     */
    public void onGoToAccessibilitySettingsNegativeClick(GoToAccessibilitySettingsDialogFragment dialog) {
        // User cancelled the dialog
        Log.d(TAG,"User chooses not to go to settings");

        MyAnalytics.trackEvent("userDeclinedAccSettings");
    }

    /**
     *  open Accessibility settings
     */
    private void openAccessibilitySettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 0);
    }
    /**
     * Open About activity
     */
    private void openApplicationAbout() {
        Intent intent=new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    /**
     *  Asks monitoring service if it was correctly connected
     */
    private void askForActivityMonitoringUpdate() {

        if (activityRecorderConnected==false) {
            Log.d(TAG, "asking for update on monitoring status");

            Intent intent=new Intent(AccessibilityRecorderService.ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            if (!AppHelpers.isRunningTestBuild()) {
                //wait a little, so service will send us details
                timerToWaitBeforeAskingForAccessibilitySettings.start();
            }
            else {
                Log.d(TAG,"will not ask system to enable accessibility settings if we are in test session");
            }

        }
        else
        {
            Log.d(TAG,"No need to ask, we have it arleady");
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        updateUserLoggedInState();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        checkForUpdates();
        goToSettingsToEnableAccessibilityServiceDialogShown=false;//may be user changed her mind? we really can't work without!
        askForActivityMonitoringUpdate();
        updateReaderStatus();

    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        timerToWaitBeforeAskingForAccessibilitySettings.cancel();
    }

    /**
     * Register handlers for update checking from Hockeyapp
     * needed for non-playstore debug builds
     */
    private void checkForUpdates() {
        UpdateManager.register(this, BuildConfig.HOCKEYAPP_APP_ID, new UpdateManagerListener() {
            public void onUpdateAvailable() {
                // Something you want to do when an update is available, e.g.
                // enable a button to install the update. Note that the manager
                // shows an alert dialog after the method returns.
                Log.d(TAG, "Update is available");


                Toast.makeText(self, R.string.update_is_available, Toast.LENGTH_LONG).show();
            }

            public void onNoUpdateAvailable() {
                Log.d(TAG, "No updates found");
                Toast.makeText(self, R.string.update_is_not_available, Toast.LENGTH_SHORT).show();
            }
        });

    }

    protected void onStop()
    {
        Log.d(TAG,"onStop");
        MyAnalytics.stopAnalyticsWithContext(this);
        super.onStop();
    }
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart");
        MyAnalytics.startAnalyticsWithContext(this);
    }

    /**
     * Updates user's login state in GUI and calls necessary handlers
     * called from onResume
     */
    private void updateUserLoggedInState() {
        currentUser = ParsePlatformUtils.getCurrentParseUser();
        if (currentUser != null) {
            showProfileLoggedIn();
            handleUserLogin();
        } else {
            showProfileLoggedOut();
            handleUserLogout();
        }
    }



    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
    }

    /**
     * Use this method to instantiate your menu, and add your items to it. You
     * should return true if you have added items to it and want the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate our menu from the resources by using the menu inflater.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    /**
     * This method is called when one of the menu items to selected. These items
     * can be on the Action Bar, the overflow menu, or the standard options menu. You
     * should return true if you handle the selection.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Log.d(TAG,"menu_settings");
                break;
            case R.id.menu_about:
                // Here we would open up our settings activity
                Log.d(TAG,"menu_settings");
                openApplicationAbout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



}