package com.viorsan.dollmaster;

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
import android.widget.TextView;
import android.widget.Toast;
import com.parse.*;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MyActivity extends Activity {
    public static final String FULL_USER_NAME = "fullName";
    public static final String USER_GENDER = "gender";
    private TextView currentlyReadingTextView;
    private TextView accessGrantedTextView;
    private TextView mantanoReaderInstalledTextView;

    private boolean activityRecorderConnected=false;

    private String userName;
    private BroadcastReceiver messageReceiver;
    private BroadcastReceiver activityMonitoringMessageReceiver;
    private BroadcastReceiver currentlyReadingMessageReceiver;

    private Activity self;
    private static final String TAG = "***REMOVED***::Activity";



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParseAnalytics.trackAppOpened(getIntent());
        setContentView(R.layout.main);
        init();
        startService();

    }

    private void init(){
        self = this;


        ParseAnalytics.trackAppOpened(getIntent());

        userName=getResources().getString(R.string.defaultUserName);
        //TODO:get cached version if it's exist
        ParseUser currentUser=ParseUser.getCurrentUser();
        if (currentUser!=null) {
            try{
                currentUser.fetchIfNeeded();

            } catch (ParseException ex)
            {
                Debug.L.LOG_EXCEPTION(ex);
            }

            if (currentUser.isAuthenticated()) {
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "ParseUser here. Authenticated. ");

            }
            else
            {
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "ParseUser here. NOT Authenticated. ");

            }

            userName=currentUser.getString(FULL_USER_NAME);
            if (userName==null) {
                //part of initial setup
                configureInitialAccountInfo(currentUser);
            }

        }
        else
        {
            //no current user
            Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User NOT logged in. Forcing user creation");
            signup();
        }

        currentlyReadingTextView=initCurrentlyReadingTextView();
        accessGrantedTextView=initAccessGrantedTextView();
        mantanoReaderInstalledTextView=initMantoReaderInstalledTextView();
        //initServiceReceiver();

        updateTitles();
        checkForUpdates();

        try {
            PackageInfo info =     getPackageManager().getPackageInfo("com.viorsan.dollmaster",     PackageManager.GET_SIGNATURES);
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
                currentlyReadingTextView.setText(msg);

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                currentlyReadingMessageReceiver,new IntentFilter(BookReadingsRecorder.BOOK_READING_STATUS_UPDATE)
        );

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

    private void updateTitles()
    {

        TextView mTextView = (TextView)findViewById(R.id.WelcomeMessage);

        String greeting=getResources().getString(R.string.GreetingsStranger);
        //we have initial username anyway but may be we use cached data
        if (mTextView!=null)
        {
            //TODO: make it work with strange languages
            greeting=getResources().getString(R.string.GreetingsMessage)+" "+userName;
        }

        mTextView.setText(greeting);
        Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Updated greeting");


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

    private String getUserName() {
        return "***REMOVED***";
    }
    private String getPassword() {
        return "***REMOVED***";
    }
    private String getEmail() {
        return "***REMOVED***@viorsan.com";
    }
    private String getFullName() {
        return "***REMOVED***";
    }
    private String getGender() {
        return "female";
    }
    private void signup() {
        ParseUser user = new ParseUser();
        user.setUsername(getUserName());
        user.setPassword(getPassword());
        user.setEmail(getEmail());

        //other fields can be set just like with ParseObject
        //user.put("phone", "650-253-0000");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Sign-up complete"); 
                    configureInitialAccountInfo(ParseUser.getCurrentUser());
                    updateTitles();
                    startService();

                } else {
                    // try login
                    loginToParse(getUserName(),getPassword());

                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    //Debug.L.LOG_EXCEPTION(e);
                }
            }
        });
    }

    //hard coded!
    private void configureInitialAccountInfo(ParseUser user) {
        user.put(FULL_USER_NAME,getFullName());
        user.put(USER_GENDER,getGender());

        //save user
        user.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Saved initial user information  to parse");
                } else {
                    Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Not saved initial user information to parse " + e.toString());
                    Debug.L.LOG_EXCEPTION(e);
                }
            }
        });


    }

    private void loginToParse(String userName, String password) {
        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Login complete");
                    updateTitles();

                    startService();

                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Debug.L.LOG_EXCEPTION(e);
                }
            }
        });

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
        updateReaderStatus();

    }




    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
    }



}