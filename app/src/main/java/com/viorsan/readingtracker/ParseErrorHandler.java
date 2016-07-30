package com.viorsan.readingtracker;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ParseInstallation;
import com.parse.ParseSession.*;
import com.rollbar.android.Rollbar;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 30.07.16.
 * global session handler https://web.archive.org/web/20160715105749/https://parse.com/docs/android/guide#sessions to ask for re-login
 */
public class ParseErrorHandler {
    public static final String SESSION_IS_INVALID = "com.viorsan.readingtracker.ParseErrorHandler.SessionIsInvalid";

    public static void handleParseError(Context context, ParseException e, String message) {
        Rollbar.reportException(e, "warning", message);
        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN:handleInvalidSessionToken(context);
                break;
        }

    }
    private static void handleInvalidSessionToken(Context context) {
        //--------------------------------------
        // Option 1: Show a message asking the user to log out and log back in.
        //--------------------------------------
        // If the user needs to finish what they were doing, they have the opportunity to do so.
        //
        // new AlertDialog.Builder(getActivity())
        //   .setMessage("Session is no longer valid, please log out and log in again.")
        //   .setCancelable(false).setPositiveButton("OK", ...).create().show();

        //--------------------------------------
        // Option #2: Show login screen so user can re-authenticate.
        //--------------------------------------
        // You may want this if the logout button could be inaccessible in the UI.
        //
        // startActivityForResult(new ParseLoginBuilder(getActivity()).build(), 0);
        //
        // send request to GUI

        Intent intent=new Intent(SESSION_IS_INVALID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
