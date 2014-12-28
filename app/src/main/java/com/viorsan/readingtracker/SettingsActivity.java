package com.viorsan.readingtracker;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 28.12.14.
 * Settings / Credits activity
 */
public class SettingsActivity extends ActionBarActivity {

    private static final String TAG = "ReadingTracker::SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        MyAnalytics.trackEvent("SettingsActivityCreated");
        Log.d(TAG, "onCreate");
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

        MyAnalytics.trackEvent("SettingsActivityOnStop");
        super.onStop();
    }

    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "start");

        MyAnalytics.trackEvent("SettingsActivityOnStart");
    }
}
