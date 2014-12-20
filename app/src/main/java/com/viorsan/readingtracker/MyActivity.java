package com.viorsan.readingtracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
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


public class MyActivity extends Activity {
    public static final String FULL_USER_NAME = "name";
    public static final String USER_GENDER = "gender";
    private TextView currentlyReadingTextView;
    private TextView accessGrantedTextView;
    private TextView mantanoReaderInstalledTextView;

    private boolean activityRecorderConnected=false;

    private BroadcastReceiver messageReceiver;
    private BroadcastReceiver activityMonitoringMessageReceiver;
    private BroadcastReceiver currentlyReadingMessageReceiver;

    private Activity self;
    private static final String TAG = "ReadingTracker::Activity";

    private static final int LOGIN_REQUEST = 0;

    private TextView titleTextView;
    private TextView emailTextView;
    private TextView nameTextView;
    private Button loginOrLogoutButton;

    private ParseUser currentUser;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParseAnalytics.trackAppOpened(getIntent());
        setContentView(R.layout.main);
        init();


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
    private void init(){
        self = this;

        titleTextView = (TextView) findViewById(R.id.profile_title);
        emailTextView = (TextView) findViewById(R.id.profile_email);
        nameTextView = (TextView) findViewById(R.id.profile_name);
        loginOrLogoutButton = (Button) findViewById(R.id.login_or_logout_button);
        titleTextView.setText(R.string.profile_title_logged_in);
        //even if user is not logged in we should configured other parse of interface

        currentlyReadingTextView=initCurrentlyReadingTextView();
        accessGrantedTextView=initAccessGrantedTextView();
        mantanoReaderInstalledTextView=initMantoReaderInstalledTextView();


        loginOrLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        //TODO:this is my old code
        //TODO:get cached version if it's exist
        ParseUser currentUser=ParseUser.getCurrentUser();

        if (currentUser!=null) {

            try{
                currentUser.fetchIfNeeded();

            } catch (ParseException ex)
            {
                Debug.L.LOG_EXCEPTION(ex);
            }

            //user can be automatic. allow for this in future
            if (currentUser.isAuthenticated()) {
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "ParseUser here. Authenticated. ");
                //set an ACL on the current user's data to not be publicly readable
                currentUser.setACL(new ParseACL(currentUser));
            }
            else
            {
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "ParseUser here. NOT Authenticated. ");

            }
        }
        //initServiceReceiver();

        checkForUpdates();

        try {
            PackageInfo info =     getPackageManager().getPackageInfo("com.viorsan.readingtracker",     PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign=Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Key hash is " + sign);
                //Toast.makeText(getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
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
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO,"Activity monitoring service ready");
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
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO,"Got reading update:"+msg);
                ParseUser currentUser=ParseUser.getCurrentUser();

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
        if (isMantanoReaderInstalled()) {
            mantanoReaderInstalledTextView.setText(getResources().getText(R.string.mantanoReaderInstalled));
        }
        else {
            mantanoReaderInstalledTextView.setText(getResources().getText(R.string.mantanoReaderNotInstalled));
        }
    }
    private TextView initMantoReaderInstalledTextView()
    {
        TextView mTextView=(TextView)findViewById(R.id.mantanoReaderInstalledOkTextView);
        return mTextView;
    }
    private  TextView initAccessGrantedTextView()
    {
        TextView mTextView=(TextView)findViewById(R.id.accessGrantedTextView);
        return mTextView;
    }

    private TextView initCurrentlyReadingTextView() {
        TextView mTextView=(TextView) findViewById(R.id.currentlyReadingMessage);
        return mTextView;
    }


    private boolean isMantanoReaderInstalled()
    {
      return appInstalledOrNot(AccessibilityRecorderService.MANTANO_READER_PACKAGE_NAME);


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


    @Override
    public void onResume() {
        super.onResume();
        checkForUpdates();
        Intent intent=new Intent(AccessibilityRecorderService.ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
        currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            showProfileLoggedIn();
            handleUserLogin();
        } else {
            showProfileLoggedOut();
            handleUserLogout();
        }
        updateReaderStatus();

    }




    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
    }



}