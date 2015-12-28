package org.cryse.unifystorage.demo;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends AbstractActivity {
    private Drawer mDrawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_drawer_menu);
        }
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_local_internal_storage)
                                .withIcon(R.drawable.ic_drawer_internal_storage),
                        new PrimaryDrawerItem().withName(R.string.drawer_local_external_storage)
                                .withIcon(R.drawable.ic_drawer_sdcard),
                        new PrimaryDrawerItem().withName(R.string.drawer_add_storage_provider)
                                .withIcon(R.drawable.ic_drawer_add_storage_provider),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.drawer_help_and_feedback),
                        new PrimaryDrawerItem().withName(R.string.drawer_github_repo),
                        new PrimaryDrawerItem().withName(R.string.drawer_settings)
                )
                .build();
        mDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = mDrawer.getDrawerLayout();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if(mDrawer.isDrawerOpen())
                    mDrawer.closeDrawer();
                else
                    mDrawer.openDrawer();
                return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
