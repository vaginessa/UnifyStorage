package org.cryse.unifystorage.explorer.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.AppPermissions;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.executor.JobExecutor;
import org.cryse.unifystorage.explorer.executor.UIThread;
import org.cryse.unifystorage.explorer.files.FilesFragment;
import org.cryse.unifystorage.explorer.files.FilesPresenter;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.model.StorageProviderType;
import org.cryse.unifystorage.explorer.service.OperationService;
import org.cryse.unifystorage.explorer.ui.common.AbstractActivity;
import org.cryse.unifystorage.explorer.utils.DrawerItemUtils;
import org.cryse.unifystorage.explorer.utils.cache.AndroidFileCacheRepository;
import org.cryse.unifystorage.providers.dropbox.DropboxAuthenticator;
import org.cryse.unifystorage.providers.dropbox.DropboxCredential;
import org.cryse.unifystorage.providers.onedrive.OneDriveAuthenticator;
import org.cryse.unifystorage.providers.onedrive.OneDriveCredential;
import org.cryse.widget.InkPageIndicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AbstractActivity implements EasyPermissions.PermissionCallbacks,
        MainContract.View {
    private static final int RC_AUTHENTICATE_ONEDRIVE = 101;
    private static final int RC_AUTHENTICATE_DROPBOX = 102;
    // private MainViewModel mMainViewModel;
    private MainContract.Presenter mPresenter;

    private Drawer mDrawer;
    private Handler mHandler = new Handler();
    private Runnable mPendingRunnable = null;
    AtomicBoolean mDoubleBackToExitPressedOnce = new AtomicBoolean(false);

    ServiceConnection mOperationServiceConnection;
    private OperationService.OperationBinder mOperationBinder;

    @Bind(R.id.container_viewpager)
    ViewPager mViewPager;
    @Bind(R.id.indicator)
    InkPageIndicator mPageIndicator;

    FilesFragmentAdapter mFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default config
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setPresenter(new MainPresenter(this, this));

        mFragmentAdapter = new FilesFragmentAdapter(mViewPager, getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentAdapter);
        // mPageIndicator.setViewPager(mViewPager);

        initDrawer();
        checkStoragePermissions();
        mOperationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mOperationBinder = (OperationService.OperationBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mOperationBinder = null;
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
                /*if (drawerItem instanceof PrimaryDrawerItem && drawerItem.isSelectable())
                    mCurrentSelection = drawerItem.getIdentifier();*/
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.onNavigationSelected(drawerItem);
                        mPendingRunnable = null;
                    }
                };
                return false;
            }
        });
        //mDrawer.setStatusBarColor(primaryDark);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start OperationService
        Intent operationServiceIntent = new Intent(this.getApplicationContext(), OperationService.class);
        startService(operationServiceIntent);
        this.bindService(operationServiceIntent, mOperationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(mOperationServiceConnection);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = mDrawer.getDrawerLayout();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if (mDoubleBackToExitPressedOnce.get()) {
            super.onBackPressed();
            return;
        } else {
            mDoubleBackToExitPressedOnce.set(true);
            Toast.makeText(this, R.string.toast_double_click_to_exit, Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDoubleBackToExitPressedOnce.set(false);
                }
            }, DataContract.CONST_DOUBLE_CLICK_TO_EXIT_INTERVAL);
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
        mPresenter.updateDrawerItems();
        /*if (mIsRestorePosition) {
            mDrawer.setSelection(mCurrentSelection, false);
        } else {
            mDrawer.setSelectionAtPosition(0, true);
        }*/
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // outState.putLong("selection_item_position", mCurrentSelection);
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
                                mPresenter.addNewProvider(
                                    "Pictures",
                                    "Cryse Hillmes",
                                    StorageProviderType.LOCAL_STORAGE,
                                    "",
                                    "/storage/emulated/0/Pictures"
                            );
                                break;
                            case 1:
                                OneDriveAuthenticator oneDriveAuthenticator = new OneDriveAuthenticator(
                                        DataContract.ClientIds.OneDriveClientId,
                                        DataContract.ClientIds.OneDriveScopes
                                );
                                oneDriveAuthenticator.startAuthenticate(MainActivity.this, RC_AUTHENTICATE_ONEDRIVE);
                                break;
                            case 2:
                                DropboxAuthenticator dropboxAuthenticator = new DropboxAuthenticator(
                                        DataContract.ClientIds.DropboxAppKey
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

    /*public void switchContentFragment(Fragment targetFragment, String backStackTag) {
        popEntireFragmentBackStack();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        if (backStackTag != null)
            fragmentTransaction.addToBackStack(backStackTag);
        fragmentTransaction.replace(R.id.frame_container, targetFragment);
        fragmentTransaction.commit();
    }*/

    public void showInitialFragments(IDrawerItem[] drawerItems) {
        Fragment fragment1 = getInternalStorageFragment((StorageProviderInfo) drawerItems[0].getTag());
        Fragment fragment2 = getInternalStorageFragment((StorageProviderInfo) drawerItems[0].getTag());
        switchContentFragment(fragment1, 0);
        switchContentFragment(fragment2, 0);
        mPageIndicator.setViewPager(mViewPager);
    }

    public void switchContentFragment(Fragment fragment, int index) {
        if(mViewPager.getChildCount() < 2) {
            mFragmentAdapter.addFragment(fragment);
        } else {
            mFragmentAdapter.replaceFragmentAt(index, fragment);
        }
    }

    public void navigateToStorageProvider(StorageProviderInfo providerInfo) {
        switch (providerInfo.getStorageProviderType()) {
            case LOCAL_STORAGE:
                navigateToOtherLocalStorage(providerInfo);
                break;
            case DROPBOX:
                navigateToDropboxStorage(providerInfo);
                break;
            case ONE_DRIVE:
                navigateToOneDriveStorage(providerInfo);
                break;
        }
    }

    public void navigateToInternalStorage(StorageProviderInfo storageProviderInfo) {
        FilesFragment filesFragment = getInternalStorageFragment(storageProviderInfo);
        switchContentFragment(filesFragment, mViewPager.getCurrentItem());
    }

    private FilesFragment getInternalStorageFragment(StorageProviderInfo storageProviderInfo) {
        FilesFragment filesFragment = FilesFragment.newInstance();
        new FilesPresenter.Builder()
                .view(filesFragment)
                .storageProviderInfo(storageProviderInfo)
                .threadExecutor(new JobExecutor())
                .postExecutionThread(new UIThread())
                .fileCacheRepository(new AndroidFileCacheRepository(this))
                .storageProvider(StorageProviderManager
                        .instance()
                        .createStorageProvider(
                                this,
                                storageProviderInfo
                        )
                ).build();
        return filesFragment;
    }

    public void navigateToOtherLocalStorage(StorageProviderInfo storageProviderInfo) {
        FilesFragment filesFragment = FilesFragment.newInstance();
        new FilesPresenter.Builder()
                .view(filesFragment)
                .storageProviderInfo(storageProviderInfo)
                .threadExecutor(new JobExecutor())
                .postExecutionThread(new UIThread())
                .fileCacheRepository(new AndroidFileCacheRepository(this))
                .storageProvider(StorageProviderManager
                        .instance()
                        .createStorageProvider(
                                this,
                                storageProviderInfo
                        )
                ).build();
        switchContentFragment(filesFragment, mViewPager.getCurrentItem());
    }

    public void navigateToOneDriveStorage(StorageProviderInfo storageProviderInfo) {
        FilesFragment filesFragment = FilesFragment.newInstance();
        new FilesPresenter.Builder()
                .view(filesFragment)
                .storageProviderInfo(storageProviderInfo)
                .threadExecutor(new JobExecutor())
                .postExecutionThread(new UIThread())
                .fileCacheRepository(new AndroidFileCacheRepository(this))
                //.extras(new String[] {DataContract.CONST_ONEDRIVE_CLIENT_ID})
                .storageProvider(StorageProviderManager
                        .instance()
                        .createStorageProvider(
                                this,
                                storageProviderInfo
                        )
                ).build();
        switchContentFragment(filesFragment, mViewPager.getCurrentItem());
    }

    public void navigateToDropboxStorage(StorageProviderInfo storageProviderInfo) {
        FilesFragment filesFragment = FilesFragment.newInstance();
        new FilesPresenter.Builder()
                .view(filesFragment)
                .storageProviderInfo(storageProviderInfo)
                .threadExecutor(new JobExecutor())
                .postExecutionThread(new UIThread())
                .fileCacheRepository(new AndroidFileCacheRepository(this))
                //.extras(new String[] {DataContract.CONST_DROPBOX_CLIENT_IDENTIFIER})
                .storageProvider(StorageProviderManager
                        .instance()
                        .createStorageProvider(
                                this,
                                storageProviderInfo
                        )
                ).build();
        switchContentFragment(filesFragment, mViewPager.getCurrentItem());
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
    public void onDrawerItemsChanged(IDrawerItem[] drawerItems) {
        mDrawer.removeAllItems();
        mDrawer.addItems(drawerItems);
        if(getFragmentManager().getBackStackEntryCount() == 0)
            showInitialFragments(drawerItems);
    }

    @Override
    public void onNavigateTo(IDrawerItem drawerItem) {
        switch ((int)drawerItem.getIdentifier()) {
            case DrawerItemUtils.STORAGE_DIRECTORY_INTERNAL_STORAGE:
                navigateToInternalStorage((StorageProviderInfo) drawerItem.getTag());
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
                navigateToStorageProvider((StorageProviderInfo) drawerItem.getTag());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_AUTHENTICATE_ONEDRIVE && data != null &&
                data.hasExtra(Credential.RESULT_KEY)) {
            if(resultCode == RESULT_OK) {
                OneDriveCredential credential = data.getParcelableExtra(Credential.RESULT_KEY);
                mPresenter.addNewProvider(
                        credential.getAccountType(),
                        credential.getAccountName(),
                        StorageProviderType.ONE_DRIVE,
                        credential.persist(),
                        ""
                );
                mPresenter.updateDrawerItems();
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
                mPresenter.addNewProvider(
                        credential.getAccountType(),
                        credential.getAccountName(),
                        StorageProviderType.DROPBOX,
                        credential.persist(),
                        ""
                );
                mPresenter.updateDrawerItems();
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
        //startActivity(new Intent(MainActivity.this, ThemeSettingsActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    public OperationService.OperationBinder getOperationBinder() {
        return mOperationBinder;
    }

    public static class FilesFragmentAdapter extends FragmentStatePagerAdapter {
        private FragmentManager fragmentManager;
        private ArrayList<Fragment> fragments;
        public FilesFragmentAdapter(ViewPager viewPager, FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
            this.fragments = new ArrayList<>(4);
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
            notifyDataSetChanged();
        }

        public void replaceFragmentAt(int index, Fragment fragment) {
            Fragment oldFragment = getItem(index);
            if(oldFragment == null) {
                return;
            }
            fragments.set(index, fragment);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if(!fragments.contains(object)) {
                return POSITION_NONE;
            } else {
                return super.getItemPosition(object);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
