package com.viorsan.readingtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 22.06.15.
 */
public class ActivityWithDrawer extends ActionBarActivity {
    public static final String TAG = ActivityWithDrawer.class.getName();

    //navigation drawer
    private Drawer drawer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate:setContentView");

    }

    protected void setupDrawer() {
        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new Error("Can't find tool bar, did you forget to add it in Activity layout file?");
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        toolbar.inflateMenu(R.menu.main);
        //init new-style navigation drawer
        drawer=new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeaderClickable(false)
                .withHeaderDivider(false)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.action_about).withIdentifier(R.string.action_about)
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        //update selection
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                    }


                    @Override
                    public void onDrawerSlide(View view, float v) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    //handle clock
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        //handle header click
                        if (position == 0) {
                            onNavigationDriverHeaderSelected();
                            return true;
                        }
                        //handle click on regular primary item
                        if (drawerItem instanceof PrimaryDrawerItem) {
                            onNavigationDrawerItemSelected(drawerItem.getIdentifier());
                            drawer.setSelectionByIdentifier(drawerItem.getIdentifier(), false);
                            return true;
                        }
                        return false;
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    // Обработка длинного клика, например, только для SecondaryDrawerItem
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof SecondaryDrawerItem) {

                            // Toast.makeText(MainActivity.this, MainActivity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })


                .build();


    }
    public void onNavigationDriverHeaderSelected() {

    }
    public void onNavigationDrawerItemSelected(int identifier) {
        // TODO:show necessary fragment (or activity?)
        Log.d(TAG, "onNavigationDriverSelected(" + identifier + ")");
        if (identifier==R.string.action_about) {
            openApplicationAbout();
        }

    }


    /**
     * Helper to implement onCreateOptionsMenu in subclasses
     * @return is navigation drawer opened
     */
    protected boolean isDrawerOpen() {
        return drawer.isDrawerOpen();
    }



    /**
     * Open About activity
     */
    protected void openApplicationAbout() {
        Intent intent=new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
