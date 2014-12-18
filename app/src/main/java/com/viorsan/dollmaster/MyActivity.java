package com.viorsan.dollmaster;

import android.app.Activity;
//import android.bluetooth.BluetoothManager;
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
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;


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
                updateDebugInfo("Last Request ID:"+requestId);
                Log.d("***REMOVED***", "updating GUI to Last Request ID:" + requestId);


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

        ParseUser currentUser=ParseUser.getCurrentUser();

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
        startService(new Intent(this, ***REMOVED***Service.class));

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

        //temp hack to disable FB Auth
    }
    private void loginToParseOld() {
        //Debug.L.LOG_MARK("Logging in to parse");

        //ask EVERYTHING we can. Facebook WILL nag about this. TODO: change to more...limited set of permissions if we ever be public app

       /*
        Collection<String> permissionsToAsk = Arrays.asList(
                ParseFacebookUtils.Permissions.User.EMAIL,
                ParseFacebookUtils.Permissions.User.ABOUT_ME,
                ParseFacebookUtils.Permissions.User.PHOTOS,
                ParseFacebookUtils.Permissions.User.HOMETOWN,
                ParseFacebookUtils.Permissions.User.LOCATION,
                ParseFacebookUtils.Permissions.User.BIRTHDAY,
                ParseFacebookUtils.Permissions.User.ACTIVITIES,
                ParseFacebookUtils.Permissions.User.CHECKINS,
                ParseFacebookUtils.Permissions.User.EDUCATION_HISTORY,
                ParseFacebookUtils.Permissions.User.EVENTS,
                ParseFacebookUtils.Permissions.User.GROUPS,
                ParseFacebookUtils.Permissions.User.INTERESTS,
                ParseFacebookUtils.Permissions.User.LIKES,
                ParseFacebookUtils.Permissions.User.NOTES,
                ParseFacebookUtils.Permissions.User.ONLINE_PRESENCE,
                ParseFacebookUtils.Permissions.User.QUESTIONS,
                ParseFacebookUtils.Permissions.User.RELATIONSHIPS,
                ParseFacebookUtils.Permissions.User.RELATIONSHIP_DETAILS,
                ParseFacebookUtils.Permissions.User.RELIGION_POLITICS,
                ParseFacebookUtils.Permissions.User.STATUS,
                ParseFacebookUtils.Permissions.User.VIDEOS,
                ParseFacebookUtils.Permissions.User.WEBSITE,
                ParseFacebookUtils.Permissions.User.WORK_HISTORY,

                ParseFacebookUtils.Permissions.Friends.ABOUT_ME,
                ParseFacebookUtils.Permissions.Friends.ACTIVITIES,
                ParseFacebookUtils.Permissions.Friends.BIRTHDAY,
                ParseFacebookUtils.Permissions.Friends.CHECKINS,
                ParseFacebookUtils.Permissions.Friends.EDUCATION_HISTORY,
                ParseFacebookUtils.Permissions.Friends.EVENTS,
                ParseFacebookUtils.Permissions.Friends.GROUPS,
                ParseFacebookUtils.Permissions.Friends.HOMETOWN,
                ParseFacebookUtils.Permissions.Friends.INTERESTS,
                ParseFacebookUtils.Permissions.Friends.LIKES,
                ParseFacebookUtils.Permissions.Friends.LOCATION,
                ParseFacebookUtils.Permissions.Friends.NOTES,
                ParseFacebookUtils.Permissions.Friends.ONLINE_PRESENCE,
                ParseFacebookUtils.Permissions.Friends.PHOTOS,
                ParseFacebookUtils.Permissions.Friends.QUESTIONS,
                ParseFacebookUtils.Permissions.Friends.RELATIONSHIPS,
                ParseFacebookUtils.Permissions.Friends.RELATIONSHIP_DETAILS,
                ParseFacebookUtils.Permissions.Friends.RELIGION_POLITICS,
                ParseFacebookUtils.Permissions.Friends.STATUS,
                ParseFacebookUtils.Permissions.Friends.VIDEOS,
                ParseFacebookUtils.Permissions.Friends.WEBSITE,
                ParseFacebookUtils.Permissions.Friends.WORK_HISTORY

        );
        */

        List<String> permissionsToAskSmall = Arrays.asList("user_about_me",
                "user_relationships", "user_birthday", "user_location","public_profile","user_friends");
                                                      /*
        Collection<String> permissionsToAskSmall = Arrays.asList(
                "basic_info",
                ParseFacebookUtils.Permissions.User.RELATIONSHIPS,
                ParseFacebookUtils.Permissions.User.RELATIONSHIP_DETAILS,
                ParseFacebookUtils.Permissions.User.EMAIL,
                ParseFacebookUtils.Permissions.User.ACTIVITIES,
                ParseFacebookUtils.Permissions.User.EVENTS,
                ParseFacebookUtils.Permissions.User.GROUPS,
                ParseFacebookUtils.Permissions.User.INTERESTS,
                ParseFacebookUtils.Permissions.User.LIKES,
                ParseFacebookUtils.Permissions.User.ABOUT_ME

        );                                           */

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "using cached login for "+currentUser.getUsername());
            // user is cached but is it linked to FB?(old user,etc)
            // this shouldn't happen
            if (!ParseFacebookUtils.isLinked(currentUser)) {
                ParseFacebookUtils.link(currentUser, permissionsToAskSmall,this, new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        ParseUser currentUser = ParseUser.getCurrentUser();

                        if (ParseFacebookUtils.isLinked(currentUser)) {
                            Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User linked pre-existing account to Facebook!");
                            getFaceBookGraphObject();

                        }
                    }
                });
            }
            else
            {
                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User linked to Facebook. Asking for graph update");
                getFaceBookGraphObject();


            }
        } else {
            Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "no cached login, performing login..");

            ParseFacebookUtils.logIn(permissionsToAskSmall,
                    this, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException err) {
                    // Code to handle login.
                    if (user == null) {
                        Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Uh oh. The user cancelled the Facebook login.");
                        updateTitles();
                        //TODO:we can't allow this?
                    } else if (user.isNew()) {
                        Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User signed up and logged in through Facebook!");
                        getFaceBookGraphObject();
                    } else {
                        Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User logged in through Facebook!");
                        getFaceBookGraphObject();

                    }
                }
            });

        }

    }
    private void updateDebugInfo(String msg) {
        //TextView mTextView=(TextView)findViewById(R.id.debug_info);
        //mTextView.setText(msg);
    }


    public void getFaceBookGraphObject(){

        /*
        Session session =  ParseFacebookUtils.getSession();
        if (session!=null && session.isOpened()) {
            RequestAsyncTask requestAsyncTask = Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "GraphUser is " + user.toString());
                        Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "GraphUser's inner JSON is  " + user.getInnerJSONObject().toString());


                        String name = user.getName();
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        if (name == null) {
                            Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Null name from FB. WTF?");
                            return;

                        }

                        if (currentUser != null) {
                            if (name!=null) {
                                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "user's facebook name is " + name);
                                userName = name;
                                currentUser.setUsername(name);

                                currentUser.put(FieldIdentifiers.facebookName, name);
                                //TODO:check!
                                currentUser.put(FieldIdentifiers.facebookId,user.getId());

                                JSONObject jsonObject=user.getInnerJSONObject();
                                try {
                                    String political=(String)jsonObject.get("political");
                                    String relationshipStatus=(String)jsonObject.get("relationship_status");
                                    String gender=(String)jsonObject.get("gender");
                                    String religion=(String)jsonObject.get("religion");
                                    String email=(String)jsonObject.get("email");
                                    String currentUserEmail=currentUser.getEmail();
                                    //user could choose NOT to provide data
                                    if (political!=null)
                                        currentUser.put(FieldIdentifiers.facebookPolitical,political);
                                    if (relationshipStatus!=null)
                                        currentUser.put(FieldIdentifiers.facebookRelationshipStatus,relationshipStatus);
                                    if (gender!=null)
                                        currentUser.put(FieldIdentifiers.facebookGender,gender);
                                    if (religion!=null)
                                        currentUser.put(FieldIdentifiers.facebookReligion,religion);
                                    if (email!=null) {
                                        if (currentUserEmail!=null) {
                                            if (currentUserEmail.equals(email))
                                                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User's e-mail was " + currentUser.getEmail() + " but FB email is " + email + " will fix");
                                        }
                                        else
                                        {
                                             Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "User's e-mail was empty  but FB email is " + email + " will fix");

                                        }
                                        currentUser.setEmail(email);
                                    }
                                    ParseUser.getCurrentUser().put(FieldIdentifiers.facebookId, user.getId());
                                       

                                } catch (JSONException ex)
                                {
                                    Debug.L.LOG_EXCEPTION(ex);
                                }




                            }
                            else
                            {
                                Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "null name?! cant'!. WTF?");

                            }

                        }

                        currentUser.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Saved currentUser  to parse");
                                } else {
                                    Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "Not saved currentUser to parse " + e.toString());
                                    Debug.L.LOG_EXCEPTION(e);
                                }
                            }
                        });
                        Debug.L.LOG_MARK("Logged in and get graph object");

                        updateTitles();
                        //start our service if it's not arleady started
                        startService();
                    }

                }
            });
        }
        else
        {
            Debug.L.LOG_UI(Debug.L.LOGLEVEL_INFO, "can't update graph because session is not opened");
        }

*/

    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Debug.L.LOG_UI(Debug.L.LOGLEVEL_ERROR, "onActivityResult");
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
    */

    @Override
    public void onResume() {
        super.onResume();
        checkForCrashes();
        checkForUpdates();
        Intent intent=new Intent(AccessibilityRecorderService.ACTIVITY_MONITORING_STATUS_UPDATE_REQUEST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        updateReaderStatus();

    }
    @Override
    public void onPause() {
        super.onPause();
    }

    private void checkForCrashes() {
        //@TODO:remove ACRA-based code and enable this one
        //CrashManager.register(this, "e05dc7c06e0cde53ef5170bae4fef932");

    }
    private void checkForUpdates() {
        UpdateManager.register(this, "***REMOVED***", new UpdateManagerListener() {
            public void onUpdateAvailable() {
                // Something you want to do when an update is available, e.g.
                // enable a button to install the update. Note that the manager
                // shows an alert dialog after the method returns.
                Log.d("***REMOVED***", "Update is available");


                Toast.makeText(self, R.string.update_is_available, Toast.LENGTH_LONG).show();
            }

            public void onNoUpdateAvailable() {
                Log.d("***REMOVED***", "No updates found");
                Toast.makeText(self, R.string.update_is_not_available, Toast.LENGTH_SHORT).show();
            }
        });

    }
    protected void onStart()
    {
        super.onStart();
        Log.d("***REMOVED***", "start");
        //new CheckStatusTask().execute(getResources().getString(R.string.api_status) + "?device_udid=" + new DeviceInfoManager().getDeviceId(getBaseContext()));
        updateReaderStatus();

    }




    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
    }



}