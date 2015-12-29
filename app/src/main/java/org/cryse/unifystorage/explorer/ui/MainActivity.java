package org.cryse.unifystorage.explorer.ui;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.ui.common.AbstractActivity;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.utils.Path;

public class MainActivity extends AbstractActivity {
    private static final int DRAWER_ITEM_INTERNAL_STORAGE = 1000;
    private static final int DRAWER_ITEM_EXTERNAl_STORAGE = 1001;
    private static final int DRAWER_ITEM_ADD_PROVIDER = 1002;
    private static final int DRAWER_ITEM_HELP_FEEDBACK = 6001;
    private static final int DRAWER_ITEM_GITHUB_REPO = 6002;
    private static final int DRAWER_ITEM_SETTINGS = 6003;

    private Drawer mDrawer;
    int mCurrentSelection = 0;
    boolean mIsRestorePosition = false;
    private Handler mHandler = new Handler();
    private Runnable mPendingRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState!=null && savedInstanceState.containsKey("selection_item_position")) {
            mCurrentSelection = savedInstanceState.getInt("selection_item_position");
            mIsRestorePosition = true;
        } else {
            mCurrentSelection = DRAWER_ITEM_INTERNAL_STORAGE;
            mIsRestorePosition = false;
        }

        initDrawer();
    }

    private void initDrawer() {
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_local_internal_storage)
                                .withIcon(R.drawable.ic_drawer_internal_storage)
                                .withIdentifier(DRAWER_ITEM_INTERNAL_STORAGE)
                                .withSelectable(true)
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View view) {

                    }

                    @Override
                    public void onDrawerClosed(View view) {
                        supportInvalidateOptionsMenu();
                        // If mPendingRunnable is not null, then add to the message queue
                        if (mPendingRunnable != null) {
                            mHandler.post(mPendingRunnable);
                            mPendingRunnable = null;
                        }
                    }

                    @Override
                    public void onDrawerSlide(View view, float v) {

                    }
                })
                .build();
        String[] externalStoragePaths = LocalStorageUtils.getStorageDirectories(this);
        for (String path : externalStoragePaths) {
            if(path.equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()))
                continue;
            mDrawer.addItem(
                    new PrimaryDrawerItem().withName(Path.getFilename(path))
                            .withTag(path)
                            .withIcon(R.drawable.ic_drawer_sdcard)
                        .withIdentifier(DRAWER_ITEM_EXTERNAl_STORAGE)
                        .withSelectable(true)
            );
        }
        mDrawer.addItems(
                new PrimaryDrawerItem().withName(R.string.drawer_add_storage_provider)
                        .withIcon(R.drawable.ic_drawer_add_storage_provider)
                        .withIdentifier(DRAWER_ITEM_ADD_PROVIDER)
                        .withSelectable(false),
                new DividerDrawerItem(),
                new PrimaryDrawerItem().withName(R.string.drawer_help_and_feedback)
                        .withIdentifier(DRAWER_ITEM_HELP_FEEDBACK)
                        .withSelectable(false),
                new PrimaryDrawerItem().withName(R.string.drawer_github_repo)
                        .withIdentifier(DRAWER_ITEM_GITHUB_REPO)
                        .withSelectable(false),
                new PrimaryDrawerItem().withName(R.string.drawer_settings)
                        .withIdentifier(DRAWER_ITEM_SETTINGS)
                        .withSelectable(false)
        );
        mDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, final IDrawerItem drawerItem) {
                if (drawerItem instanceof PrimaryDrawerItem && drawerItem.isSelectable())
                    mCurrentSelection = drawerItem.getIdentifier();
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        onNavigationSelected(drawerItem);
                    }
                };
                return false;
            }
        });
        if(mCurrentSelection == DRAWER_ITEM_INTERNAL_STORAGE && !mIsRestorePosition) {
            mDrawer.setSelection(DRAWER_ITEM_INTERNAL_STORAGE, false);
            navigateToInternalStorage();
            // mNavigation.navigateToHomePageFragment();
        } else if(mIsRestorePosition) {
            mDrawer.setSelection(mCurrentSelection, false);
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selection_item_position", mCurrentSelection);
    }

    private void onNavigationSelected(IDrawerItem drawerItem) {
        switch (drawerItem.getIdentifier()) {
            case DRAWER_ITEM_INTERNAL_STORAGE:
                navigateToInternalStorage();
                break;
            case DRAWER_ITEM_EXTERNAl_STORAGE:
                navigateToOtherLocalStorage((String)drawerItem.getTag());
                break;
            case DRAWER_ITEM_ADD_PROVIDER:
                showAddProviderDialog();
                break;
            default:
                throw new IllegalArgumentException("Unknown NavigationDrawerItem position.");
        }
    }

    public void showAddProviderDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_add_storage_provider)
                .items(R.array.array_storage_providers)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public boolean popEntireFragmentBackStack() {
        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        // Clear Back Stack
        for (int i = 0; i < backStackCount; i++) {
            getSupportFragmentManager().popBackStack();
        }
        return backStackCount > 0;
    }

    public void switchContentFragment(Fragment targetFragment, String backStackTag) {
        popEntireFragmentBackStack();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        if (backStackTag != null)
            fragmentTransaction.addToBackStack(backStackTag);
        fragmentTransaction.replace(R.id.frame_container, targetFragment);
        fragmentTransaction.commit();
    }

    public void navigateToInternalStorage() {
        LocalStorageFragment fragment = LocalStorageFragment.newInstance(Environment.getExternalStorageDirectory().getAbsolutePath());
        switchContentFragment(fragment, null);
    }

    public void navigateToOtherLocalStorage(String path) {
        LocalStorageFragment fragment = LocalStorageFragment.newInstance(path);
        switchContentFragment(fragment, null);
    }

    public Drawer getNavigationDrawer() {
        return mDrawer;
    }
}
