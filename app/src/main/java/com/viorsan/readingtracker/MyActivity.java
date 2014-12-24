package com.viorsan.readingtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.*;
import com.parse.ui.ParseLoginBuilder;

import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyActivity extends FragmentActivity implements GoToAccessibilitySettingsDialogFragment.GoToAccessibilitySettingsDialogListener {
    public static final String FULL_USER_NAME = "name";
    public static final String USER_GENDER = "gender";
    public static final int TIME_BEFORE_ASKING_USER_TO_GO_TO_ACCESSIBILITY_SETTINGS = 5 * 1000;//5 seconds to wait before checking if we should ask user to go to Accessibility settings
    public static final int TIME_BEFORE_ASKING_FOR_MONITORING_STATUS_UPDATE = 5 * 1000;
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
    private static final String TAG = "ReadingTracker::Activity";

    private static final int LOGIN_REQUEST = 0;


    private ParseUser currentUser;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyAnalytics.trackAppOpened(getMyApp(), getIntent());
        setContentView(R.layout.main);
        ButterKnife.inject(this);
        init();

    }
    private MyApplication getMyApp() {
        return (MyApplication)getApplication();
    }
    private void handleUserLogin()  {
        Log.d(TAG,"Signaling everybody that user was logged in");
        startService();
        //it will autostop if no user active

    }
    private void handleUserLogout() {
        //ask core service to stop
        Log.d(TAG,"Signaling everybody that user was logged out");
        Intent intent = new Intent(CoreService.USER_LOGGED_OUT_REPORT);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        Log.d(TAG,"Signeled everybody that user was logged out");
    }
    @OnClick(R.id.login_or_logout_button)
    public void loginLogoutButtonClicked(View v) {
        if (currentUser != null) {
            // User clicked to log out.
            ParseUser.logOut();
            currentUser = null;
            handleUserLogout();
            showProfileLoggedOut();
        } else {
            // User clicked to log in.
            ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                    MyActivity.this);
            startActivityForResult(loginBuilder.build(), LOGIN_REQUEST);
        }
    }
    private void init(){
        self = this;

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
            }
            else
            {
                Log.i(TAG, "ParseUser here. NOT Authenticated. ");

            }
        }
        //initServiceReceiver();

        checkForUpdates();

        try {
            PackageManager pm=getPackageManager();
            if (pm!=null) {
                PackageInfo info = pm.getPackageInfo(BuildConfig.APPLICATION_ID,     PackageManager.GET_SIGNATURES);
                if (info!=null) {
                    if (info.signatures!=null) {
                        for (android.content.pm.Signature signature : info.signatures) {
                            MessageDigest md = MessageDigest.getInstance("SHA");
                            md.update(signature.toByteArray());
                            String sign=Base64.encodeToString(md.digest(), Base64.DEFAULT);
                            Log.i(TAG, "Key hash is " + sign);
                            //Toast.makeText(getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        //This happens with (for example) Robolectric
                        Log.d(TAG,"Cannot get package signatures (info.signatures is null)");
                        System.out.println(TAG+":Cannot get package signatures (info.signatures is null)");
                    }
                }
                else {
                    Log.d(TAG,"Cannot get package info");
                }
            }
            else
            {
                Log.d(TAG,"Cannot get package manager");
            }

        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }

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
                String msg;
                if (currentPageS!=null) {
                    //update
                    //msg=String.format("%s от %s (%s/%s ). %.2f минут",bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0);
                    msg=getResources().getString(R.string.guiCurrentlyReadingLong,bookTitle,bookAuthor,currentPageS,totalPageS,totalTime/60.0);
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

    private void updateMonitoringStatus() {
        if (activityRecorderConnected=true) {
            accessGrantedTextView.setText(getResources().getText(R.string.accessGrantedOK));
        }
        else
        {
            accessGrantedTextView.setText(getResources().getText(R.string.accessGrantedNotOk));
        }

    }


    private void updateReaderStatus() {
        if (isSupportedEbookReaderInstalled()) {
           supportedEbookReaderInstalledTextView.setText(getResources().getText(R.string.supportedEbookReaderInstalled));
        }
        else {
            supportedEbookReaderInstalledTextView.setText(getResources().getText(R.string.supportedEbookReaderNotInstalled));
        }
    }

    //is one of supported E-Book readers installed
    private boolean isSupportedEbookReaderInstalled()
    {
      return (
              appInstalledOrNot(AccessibilityRecorderService.MANTANO_READER_PACKAGE_NAME)
              /* ||
              appInstalledOrNot(AccessibilityRecorderService.MANTANO_READER_ESSENTIALS_PACKAGE_NAME) ||
              appInstalledOrNot(AccessibilityRecorderService.MANTANO_READER_LITE_PACKAGE_NAME)
              */
      );

    };
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed ;
    }
    private void startService() {
        startService(new Intent(this, CoreService.class));

    }

    /*
  * This dialog can have significant amount of text so make it possible for it be shown in fullscreen way
  * and not only as regular dialog
  */
    public void showGoToAccessibilitySettingsDialog() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        GoToAccessibilitySettingsDialogFragment newFragment=new GoToAccessibilitySettingsDialogFragment();

        newFragment.show(fragmentManager, "goToAccessibilitySettingsDialog");

    }
    // implementation of GoToAccessibilitySettingsDialogListener
    public void onGoToAccessibilitySettingsDialogPositiveClick(GoToAccessibilitySettingsDialogFragment dialog) {
        // Users wants to go to settings
        Log.d(TAG,"User chooses to go to settings");
        openAccessibilitySettings();
    }
    // implementation of GoToAccessibilitySettingsDialogListener
    public void onGoToAccessibilitySettingsNegativeClick(GoToAccessibilitySettingsDialogFragment dialog) {
        // User cancelled the dialog
        Log.d(TAG,"User chooses not to go to settings");
    }
    // open Accessibility settings
    private void openAccessibilitySettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 0);
    }
    // Asks monitoring service if it was correctly connected
    private void askForActivityMonitoringUpdate() {

        if (activityRecorderConnected==false) {
            Log.d(TAG, "asking for update on monitoring status");

            Intent intent=new Intent(AccessibilityRecorderService.ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            //wait a little, so service will send us details
            new CountDownTimer(TIME_BEFORE_ASKING_USER_TO_GO_TO_ACCESSIBILITY_SETTINGS, TIME_BEFORE_ASKING_USER_TO_GO_TO_ACCESSIBILITY_SETTINGS) {
                public void onTick(long msUntilFinish) {
                }

                public  void  onFinish() {
                    Log.d(TAG,"check if it's time to ask user to enable our Accessibility Service");
                    if ((activityRecorderConnected==false) && (goToSettingsToEnableAccessibilityServiceDialogShown==false)) {
                        Log.d(TAG,"yes it is - asking");
                        goToSettingsToEnableAccessibilityServiceDialogShown=true;
                        showGoToAccessibilitySettingsDialog();
                        Log.d(TAG,"yes it is - asked");
                    }
                    else {
                        Log.d(TAG,"Not it is not");

                    }
                }
            }.start();

        }
        else
        {
            Log.d(TAG,"No need to ask, we have it arleady");
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        checkForUpdates();
        goToSettingsToEnableAccessibilityServiceDialogShown=false;//may be user changed her mind? we really can't work without!
        askForActivityMonitoringUpdate();
        updateReaderStatus();

    }
    @Override
    public void onPause() {
        super.onPause();
    }

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
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "start");
        currentUser = ParsePlatformUtils.getCurrentParseUser();
        if (currentUser != null) {
            showProfileLoggedIn();
            handleUserLogin();
        } else {
            showProfileLoggedOut();
            handleUserLogout();
        }
        updateReaderStatus();
        askForActivityMonitoringUpdate();

    }




    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
    }



}