package com.viorsan.readingtracker;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 28.12.14.
 * About / Credits activity
 */
public class AboutActivity extends ActionBarActivity {

    private static final String TAG = "ReadingTracker::AboutActivity";

    @InjectView(R.id.readingTrackerVersion) TextView readingTrackerVersionTextView;
    @InjectView(R.id.readingTrackerBuildOn) TextView readingTrackerBuildOnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);

        MyAnalytics.trackEvent("AboutActivityCreated");
        Log.d(TAG, "onCreate");
    }

    //Load version info
    private void configureGUI() {
        String buildStr=getResources().getString(R.string.readingTrackerBuildOn,BuildConfig.BUILD_HOST,BuildConfig.BUILD_USER, BuildConfig.BUILD_DATE_TIME);
        String version=getResources().getString(R.string.readingTrackerVersion,BuildConfig.VERSION_NAME, BuildConfig.FLAVOR,BuildConfig.BUILD_TYPE);
        readingTrackerVersionTextView.setText(version);
        readingTrackerBuildOnTextView.setText(buildStr);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onStop()
    {
        Log.d(TAG, "stop");

        MyAnalytics.trackEvent("AboutActivityOnStop");
        super.onStop();
    }

    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "start");
        configureGUI();

        MyAnalytics.trackEvent("AboutActivityOnStart");
    }
}
