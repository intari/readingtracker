package com.viorsan.readingtracker;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 24.12.14.
 *  Custom dialog to show 'Go to Accessibility Settings and enable us here'
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * Custom dialog to show 'Go to Accessibility Settings and enable us here'
 *
 */
public class GoToAccessibilitySettingsDialogFragment extends DialogFragment {

    public static final String TAG = "ReadingTracker::GoToAccessibilitySettingsDialogFragment";

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface GoToAccessibilitySettingsDialogListener {
        public void onGoToAccessibilitySettingsDialogPositiveClick(GoToAccessibilitySettingsDialogFragment dialog);
        public void onGoToAccessibilitySettingsNegativeClick(GoToAccessibilitySettingsDialogFragment dialog);
    }
    // Use this instance of the interface to deliver action events
    GoToAccessibilitySettingsDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the GoToAccessibilitySettingsDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = ( GoToAccessibilitySettingsDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement  GoToAccessibilitySettingsDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.goToAccessibilitySettingsMessage)
                .setPositiveButton(R.string.goToAccessibilitySettingsOK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onGoToAccessibilitySettingsDialogPositiveClick(GoToAccessibilitySettingsDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.goToAccessibilitySettingsCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onGoToAccessibilitySettingsNegativeClick(GoToAccessibilitySettingsDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
