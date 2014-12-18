package com.viorsan.dollmaster;

import android.app.IntentService;
import android.content.Intent;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.parse.ParseObject;

/**
 * Created by dkzm on 28.05.14.
 * Activity Monitoring IntentService
 * Consider this part of ***REMOVED***Service but unlike MyBluetoothDevice, lifecycles are not strictly lnkied
 * see https://developer.android.com/training/location/activity-recognition.html for help information about Activity Detection
 */
public class ActivityMonitoringService extends IntentService {

    public final static String ACTIVITY_UPDATE_AVAILABLE =
            "com.viorsan.dollmaster.activityMonitoringService.ACTIVITY_UPDATE_AVAILABLE";
    public final static String EXTRA_DATA_ACTIVITY_NAME =
            "com.viorsan.dollmaster.activityMonitoringService.EXTRA_DATA_ACTIVITY_NAME";
    public final static String EXTRA_DATA_ACTIVITY_CONFIDENCE =
            "com.viorsan.dollmaster.activityMonitoringService.EXTRA_DATA_ACTIVITY_CONFIDENCE";
    public static final String DEFAULT_WORK_THREAD_NAME = "***REMOVED***ActivityMonitoringServiceThread";


    /**
     * Map detected activity types to strings
     * See https://developer.android.com/reference/com/google/android/gms/location/DetectedActivity.html
     *@param activityType The detected activity type
     *@return A user-readable name for the type
     */
    public String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "running_or_walking";//Running or Walking but we don't knew exactly
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            int confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            int activityType = mostProbableActivity.getType();
            String activityName = getActivityNameFromType(activityType);
            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
            Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"(intentService) Activity: "+activityName+" with confidence "+confidence);


            /* configure & send intent */
            String intentAction=ACTIVITY_UPDATE_AVAILABLE;
            final Intent intentToSend=new Intent(intentAction);
            intentToSend.putExtra(EXTRA_DATA_ACTIVITY_NAME,activityName);
            intentToSend.putExtra(EXTRA_DATA_ACTIVITY_CONFIDENCE,confidence);
            sendBroadcast(intentToSend);


        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */

        }
    }


    public ActivityMonitoringService() {
        super(DEFAULT_WORK_THREAD_NAME);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Default constructor for ActivityMonitoringService called. using default name");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ActivityMonitoringService(String name) {
        super(name);
        Debug.L.LOG_SERVICE(Debug.L.LOGLEVEL_INFO,"Default constructor for ActivityMonitoringService called. name "+name);
    }

}
