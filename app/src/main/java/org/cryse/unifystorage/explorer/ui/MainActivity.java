package org.cryse.unifystorage.explorer.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialcab.Util;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.AppPermissions;
import org.cryse.unifystorage.explorer.databinding.ActivityMainBinding;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.service.LongOperationService;
import org.cryse.unifystorage.explorer.ui.common.AbstractActivity;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.explorer.viewmodel.MainViewModel;
import org.cryse.unifystorage.providers.dropbox.DropboxAuthenticator;
import org.cryse.unifystorage.providers.dropbox.DropboxCredential;
import org.cryse.unifystorage.providers.onedrive.OneDriveAuthenticator;
import org.cryse.unifystorage.providers.onedrive.OneDriveCredential;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AbstractActivity implements EasyPermissions.PermissionCallbacks, MainViewModel.DataListener {
    private static final int RC_AUTHENTICATE_ONEDRIVE = 101;
    private static final int RC_AUTHENTICATE_DROPBOX = 102;
    private ActivityMainBinding mBinding;
    private MainViewModel mMainViewModel;

    private Drawer mDrawer;
    int mCurrentSelection = 0;
    boolean mIsRestorePosition = false;
    private Handler mHandler = new Handler();
    private Runnable mPendingRunnable = null;
    ServiceConnection mLongOperationServiceConnection;
    private LongOperationService.LongOperationBinder mLongOperationBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default config
        if (!ATE.config(this, "light_theme").isConfigured(1)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .lightToolbarMode(Config.LIGHT_TOOLBAR_AUTO)
                    .coloredActionBar(true)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(1)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .lightToolbarMode(Config.LIGHT_TOOLBAR_AUTO)
                    .coloredActionBar(true)
                    .coloredNavigationBar(true)
                    .usingMaterialDialogs(true)
                    .commit();
        }

        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainViewModel = new MainViewModel(this, this);
        mBinding.setViewModel(mMainViewModel);

        if (savedInstanceState != null && savedInstanceState.containsKey("selection_item_position")) {
            mCurrentSelection = savedInstanceState.getInt("selection_item_position");
            mIsRestorePosition = true;
        } else {
            mCurrentSelection = DrawerItemUtils.DRAWER_ITEM_NONE;
            mIsRestorePosition = false;
        }

        initDrawer();
        checkStoragePermissions();
        mLongOperationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mLongOperationBinder = (LongOperationService.LongOperationBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mLongOperationBinder = null;
            }
        };
    }

    private void initDrawer() {
        mDrawer = new DrawerBuilder()
                .withActivity(this)
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
                        }
                    }

                    @Override
                    public void onDrawerSlide(View view, float v) {

                    }
                })
                .build();
        mDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, final IDrawerItem drawerItem) {
                if (drawerItem instanceof PrimaryDrawerItem && drawerItem.isSelectable())
                    mCurrentSelection = drawerItem.getIdentifier();
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mMainViewModel.onNavigationSelected(drawerItem);
                        mPendingRunnable = null;
                    }
                };
                return false;
            }
        });
        String key = Util.resolveString(this, R.attr.ate_key);
        int primaryDark = Config.primaryColorDark(this, key);
        mDrawer.setStatusBarColor(primaryDark);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this.getApplicationContext(), LongOperationService.class);
        startService(service);
        this.bindService(service, mLongOperationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(mLongOperationServiceConnection);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (mDrawer.isDrawerOpen())
                    mDrawer.closeDrawer();
                else
                    mDrawer.openDrawer();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addStorageProviderItemsAndLoad() {
        mMainViewModel.updateDrawerItems(DrawerItemUtils.DRAWER_ITEM_NONE);
        if (mIsRestorePosition) {
            mDrawer.setSelection(mCurrentSelection, false);
        } else {
            mDrawer.setSelectionAtPosition(0, true);
        }
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selection_item_position", mCurrentSelection);
    }

    public void showAddProviderDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_add_storage_provider)
                .items(R.array.array_storage_providers)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                            mMainViewModel.addNewProvider(
                                    "Pictures",
                                    "Cryse Hillmes",
                                    StorageProviderRecord.PROVIDER_LOCAL_STORAGE,
                                    "",
                                    "/storage/emulated/0/Pictures"
                            );
                                break;
                            case 1:
                                OneDriveAuthenticator oneDriveAuthenticator = new OneDriveAuthenticator(
                                        DataContract.CONST_ONEDRIVE_CLIENT_ID,
                                        DataContract.CONST_ONEDRIVE_SCOPES
                                );
                                oneDriveAuthenticator.startAuthenticate(MainActivity.this, RC_AUTHENTICATE_ONEDRIVE);
                                break;
                            case 2:
                                DropboxAuthenticator dropboxAuthenticator = new DropboxAuthenticator(
                                        DataContract.CONST_DROPBOX_APP_KEY
                                );
                                dropboxAuthenticator.startAuthenticate(MainActivity.this, RC_AUTHENTICATE_DROPBOX);
                                break;
                        }
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
        LocalStorageFragment fragment = LocalStorageFragment.newInstance(Environment.getExternalStorageDirectory().getAbsolutePath(), DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE);
        switchContentFragment(fragment, null);
    }

    public void navigateToOtherLocalStorage(String path, int storageProviderRecordId) {
        LocalStorageFragment fragment = LocalStorageFragment.newInstance(path, storageProviderRecordId);
        switchContentFragment(fragment, null);
    }

    public void navigateToOneDriveStorage(OneDriveCredential credential, int storageProviderRecordId) {
        OneDriveStorageFragment fragment = OneDriveStorageFragment.newInstance(credential, storageProviderRecordId);
        switchContentFragment(fragment, null);
    }

    public void navigateToDropboxStorage(DropboxCredential credential, int storageProviderRecordId) {
        DropboxStorageFragment fragment = DropboxStorageFragment.newInstance(credential, storageProviderRecordId);
        switchContentFragment(fragment, null);
    }

    public Drawer getNavigationDrawer() {
        return mDrawer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void checkStoragePermissions() {
        if (EasyPermissions.hasPermissions(this, AppPermissions.PERMISSIONS)) {
            // Already have permission, do the thing
            // ...
            addStorageProviderItemsAndLoad();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.dialog_title_permission_storage),
                    AppPermissions.RC_PERMISSION_STORAGE, AppPermissions.PERMISSIONS);
        }
    }

    @Override
    public void onPermissionsGranted(List<String> permissions) {
        if (AppPermissions.PERMISSIONS_SET.containsAll(permissions)) {
            // Restarting application
            // Schedule start after 1 second
            PendingIntent pi = PendingIntent.getActivity(
                    this,
                    0,
                    new Intent(this, MainActivity.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);

            // Stop now
            finish();
            System.exit(0);
        } else {
            finish();
        }
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        finish();
    }

    @Override
    public void onDrawerItemsChanged(IDrawerItem[] drawerItems, int selectionIdentifier) {
        mDrawer.removeAllItems();
        mDrawer.addItems(drawerItems);
        if(selectionIdentifier >= 0)
            mDrawer.setSelection(selectionIdentifier, false);
    }

    @Override
    public void onNavigateTo(IDrawerItem drawerItem) {
        switch (drawerItem.getIdentifier()) {
            case DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE:
                navigateToInternalStorage();
                break;
            case DrawerItemUtils.DRAWER_ITEM_ADD_PROVIDER:
                showAddProviderDialog();
                break;
            case DrawerItemUtils.DRAWER_ITEM_HELP_FEEDBACK:
                //showAddProviderDialog();
                break;
            case DrawerItemUtils.DRAWER_ITEM_GITHUB_REPO:
                //showAddProviderDialog();
                break;
            case DrawerItemUtils.DRAWER_ITEM_SETTINGS:
                showThemeSettingsActivity();
                break;
            default:
                if(drawerItem.getIdentifier() >= DrawerItemUtils.STORAGE_PROVIDER_START) {
                    StorageProviderRecord record = (StorageProviderRecord) drawerItem.getTag();
                    switch (record.getProviderType()) {
                        case StorageProviderRecord.PROVIDER_LOCAL_STORAGE:
                            navigateToOtherLocalStorage(record.getExtraData(), record.getId());
                            break;
                        case StorageProviderRecord.PROVIDER_ONE_DRIVE:
                            navigateToOneDriveStorage(new OneDriveCredential(record.getCredentialData()), record.getId());
                            break;
                        case StorageProviderRecord.PROVIDER_DROPBOX:
                            navigateToDropboxStorage(new DropboxCredential(record.getCredentialData()), record.getId());
                            break;
                    }
                } else if(drawerItem.getIdentifier() <= DrawerItemUtils.STORAGE_DIRECTORY_EXTERNAl_STORAGE_START)
                    navigateToOtherLocalStorage((String) drawerItem.getTag(), drawerItem.getIdentifier());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_AUTHENTICATE_ONEDRIVE && data != null &&
                data.hasExtra(Credential.RESULT_KEY)) {
            if(resultCode == RESULT_OK) {
                OneDriveCredential credential = data.getParcelableExtra(Credential.RESULT_KEY);
                mMainViewModel.addNewProvider(
                        credential.getAccountType(),
                        credential.getAccountName(),
                        StorageProviderRecord.PROVIDER_ONE_DRIVE,
                        credential.persist(),
                        ""
                );
                mMainViewModel.updateDrawerItems(mCurrentSelection);
            } else {
                String errorMessage = data.getStringExtra(Credential.RESULT_KEY);
                new MaterialDialog.Builder(this)
                        .title(R.string.storage_provider_name_onedrive)
                        .content(errorMessage)
                        .positiveText(android.R.string.ok)
                        .show();
            }
        } else if(requestCode == RC_AUTHENTICATE_DROPBOX && data != null &&
                data.hasExtra(Credential.RESULT_KEY)) {
            if(resultCode == RESULT_OK) {
                DropboxCredential credential = data.getParcelableExtra(Credential.RESULT_KEY);
                mMainViewModel.addNewProvider(
                        credential.getAccountType(),
                        credential.getAccountName(),
                        StorageProviderRecord.PROVIDER_DROPBOX,
                        credential.persist(),
                        ""
                );
                mMainViewModel.updateDrawerItems(mCurrentSelection);
            } else {
                new MaterialDialog.Builder(this)
                        .title(R.string.dialog_title_error)
                        .content(R.string.dialog_content_error_dropbox_signin)
                        .positiveText(android.R.string.ok)
                        .show();
            }
        }
    }

    private void showThemeSettingsActivity() {
        startActivity(new Intent(MainActivity.this, ThemeSettingsActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainViewModel.destroy();
    }

    public LongOperationService.LongOperationBinder getLongOperationBinder() {
        return mLongOperationBinder;
    }
}
