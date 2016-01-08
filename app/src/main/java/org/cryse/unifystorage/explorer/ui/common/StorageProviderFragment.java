package org.cryse.unifystorage.explorer.ui.common;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.impression.widget.breadcrumbs.BreadCrumbLayout;
import com.afollestad.impression.widget.breadcrumbs.Crumb;
import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialcab.Util;
import com.afollestad.materialdialogs.MaterialDialog;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.PrefsConst;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.databinding.FragmentStorageProviderBinding;
import org.cryse.unifystorage.explorer.service.FileOperation;
import org.cryse.unifystorage.explorer.service.LongOperationService;
import org.cryse.unifystorage.explorer.ui.MainActivity;
import org.cryse.unifystorage.explorer.ui.adapter.FileAdapter;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.MenuUtils;
import org.cryse.unifystorage.explorer.utils.ResourceUtils;
import org.cryse.unifystorage.explorer.viewmodel.FileListViewModel;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.FileSizeUtils;
import org.cryse.utils.preference.BooleanPrefs;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.selector.SelectableRecyclerViewAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class StorageProviderFragment<
        RF extends RemoteFile,
        CR extends Credential,
        SP extends StorageProvider<RF, CR>
        > extends AbstractFragment implements
        FileAdapter.OnFileClickListener<RF>,
        FileListViewModel.DataListener<RF, CR>,
        SelectableRecyclerViewAdapter.OnSelectionListener,
        MaterialCab.Callback {
    private AtomicBoolean mDoubleBackPressedOnce = new AtomicBoolean(false);
    private Handler mHandler = new Handler();

    private final Runnable mBackPressdRunnable = new Runnable() {
        @Override
        public void run() {
            mDoubleBackPressedOnce.set(false);
        }
    };
    protected int mStorageProviderRecordId;
    protected CR mCredential;

    protected FragmentStorageProviderBinding mBinding;
    protected FileListViewModel<RF, CR, SP> mViewModel;
    protected FileAdapter<RF> mCollectionAdapter;
    protected MaterialDialog mDownloadDialog;

    protected BooleanPrefs mShowHiddenFilesPrefs;
    protected MenuItem mShowHiddenFilesMenuItem;
    protected MaterialCab mCab;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.breadCrumbs)
    BreadCrumbLayout mBreadCrumbLayout;

    @Bind(R.id.fragment_storageprovider_recyclerview_files)
    RecyclerView mCollectionView;

    private int mPrimaryColor;
    private String mATEKey;
    private int mToolbarContentColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        readArguments();
        mCollectionAdapter = new FileAdapter<>(getActivity());
        mCollectionAdapter.setOnFileClickListener(this);
        mCollectionAdapter.setOnSelectionListener(this);
        mViewModel = buildViewModel(mCredential);
        mShowHiddenFilesPrefs = Prefs.getBooleanPrefs(
                PrefsConst.PREFS_SHOW_HIDDEN_FILES,
                PrefsConst.PREFS_SHOW_HIDDEN_FILES_VALUE
        );
        mViewModel.setShowHiddenFiles(mShowHiddenFilesPrefs.get());
    }

    protected void readArguments() {

    }

    protected abstract Class<RF> getRemoteFileClass();

    protected abstract String getLogTag();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_storage_provider, container, false);
        mBinding.setViewModel(mViewModel);
        View fragmentView = mBinding.getRoot();

        ButterKnife.bind(this, fragmentView);
        setupToolbar();
        setupRecyclerView();
        setupBreadCrumb();
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mATEKey = Util.resolveString(getActivity(), R.attr.ate_key);
        mPrimaryColor = Config.primaryColor(getContext(), mATEKey);
        mToolbarContentColor = ResourceUtils.toolbarTextColor(getContext(), mATEKey, mToolbar);
        int textColorHint = ResourceUtils.adjustAlpha(mToolbarContentColor, 0.54f);
        int arrowColor = ResourceUtils.adjustAlpha(mToolbarContentColor, 1.0f);
        mBreadCrumbLayout.setCrumbActiveColor(mToolbarContentColor);
        mBreadCrumbLayout.setCrumbInactiveColor(textColorHint);
        mBreadCrumbLayout.setArrowColor(arrowColor);
        mBreadCrumbLayout.setBackgroundColor(mPrimaryColor);
        ATE.apply(mBreadCrumbLayout, Util.resolveString(getActivity(), R.attr.ate_key));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().invalidateOptionsMenu();
        if (getView() != null) {
            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
            getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if(mCollectionAdapter.isInSelection() || !mViewModel.isAtTopPath()) {
                            if (!StorageProviderFragment.this.mDoubleBackPressedOnce.get()) {
                                StorageProviderFragment.this.mDoubleBackPressedOnce.set(true);
                                mHandler.postDelayed(mBackPressdRunnable, 400);
                                if (mCollectionAdapter.isInSelection()) {
                                    mCollectionAdapter.clearSelection();
                                } else if (!mViewModel.isAtTopPath()) {
                                    mViewModel.onBackPressed();
                                }
                            } else {
                                // Click Back too quick, ignore.
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void setupToolbar() {
        getAppCompatActivity().setSupportActionBar(mToolbar);
        ActionBar actionBar = getAppCompatActivity().getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu_drawer);
        }
    }

    private void setupBreadCrumb() {
        mBreadCrumbLayout.setCallback(new BreadCrumbLayout.SelectionCallback() {
            @Override
            public void onCrumbSelection(Crumb crumb, int index) {
                if (index == -1) {
                    //onBackPressed();
                } else {
                    String activeFile = null;
                    if (mBreadCrumbLayout.getActiveIndex() > -1) {
                        activeFile = mBreadCrumbLayout.getCrumb(mBreadCrumbLayout.getActiveIndex()).getPath();
                    }
                    if (crumb.getPath() != null && activeFile != null &&
                            crumb.getPath().equals(activeFile)) {
                        // When the target path is current, scroll file list to top.
                        mCollectionView.scrollToPosition(0);
                        /*Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
                        ((MediaFragment) frag).jumpToTop(true);*/
                    } else {
                        mViewModel.jumpBack(crumb.getPath(), getCollectionViewState());
                    }
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_filelist, menu);
        mShowHiddenFilesMenuItem = menu.findItem(R.id.action_show_hidden_files);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(mShowHiddenFilesMenuItem != null) {
            mShowHiddenFilesMenuItem.setChecked(mShowHiddenFilesPrefs.get());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getNavigationDrawer().openDrawer();
                    return true;
                } else {
                    return false;
                }
            case R.id.action_show_hidden_files:
                boolean isShow = mShowHiddenFilesPrefs.get();
                mShowHiddenFilesPrefs.set(!isShow);
                if(mShowHiddenFilesMenuItem!= null)
                    mShowHiddenFilesMenuItem.setChecked(!isShow);
                mViewModel.setShowHiddenFiles(!isShow);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mViewModel.destroy();
        if (mHandler != null) { mHandler.removeCallbacks(mBackPressdRunnable); }
    }

    private void setupRecyclerView() {
        mCollectionView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCollectionView.setAdapter(mCollectionAdapter);
    }

    protected abstract FileListViewModel<RF, CR, SP> buildViewModel(CR credential);

    @Override
    public void onFileClick(View view, int position, RF file) {
        if(mCollectionAdapter.isInSelection()) {
            mCollectionAdapter.toggleSelection(position);
        } else {
            mViewModel.onFileClick(file, getCollectionViewState());
        }
    }

    @Override
    public void onFileLongClick(View view, int position, RF file) {
        mCollectionAdapter.toggleSelection(position);
        mViewModel.onFileLongClick(file);
    }

    private CollectionViewState getCollectionViewState() {
        LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getLayoutManager();
        int position = manager.findFirstVisibleItemPosition();
        View firstItemView = manager.findViewByPosition(position);
        if(firstItemView != null) {
            float offset = firstItemView.getTop();
            return new CollectionViewState(position, offset);
        } else
            return CollectionViewState.EMPTY;
    }

    @Override
    public void onDirectoryChanged(DirectoryInfo<RF, List<RF>> directory) {
        mCollectionAdapter.replaceWith(directory.files);
        updateBreadcrumb(directory.directory.getPath());
    }

    @Override
    public void onDirectoryItemDelete(int position) {
        mCollectionAdapter.remove(position);
    }

    @Override
    public void onCollectionViewStateRestore(CollectionViewState collectionViewState) {
        LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getLayoutManager();
        manager.scrollToPositionWithOffset(collectionViewState.position, (int) collectionViewState.offset);
    }

    @Override
    public void onCredentialRefreshed(CR credential) {
        this.mCredential = credential;
    }

    @Override
    public void onShowDownloadDialog(String fileName, long fileSize, DialogInterface.OnDismissListener dismissListener) {
        if(mDownloadDialog != null && mDownloadDialog.isShowing()) mDownloadDialog.dismiss();
        mDownloadDialog = new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_title_add_storage_provider)
                .content(R.string.dialog_content_opening)
                .progress(false, 100, false)
                .dismissListener(dismissListener)
                .show();
    }

    @Override
    public void onUpdateDownloadDialog(String fileName, long readSize, long fileSize) {
        if(mDownloadDialog != null && mDownloadDialog.isShowing()) {
            int newPercent = (int) Math.round(((double) readSize  / (double) fileSize) * 100.0d);
            int currentPercent = mDownloadDialog.getCurrentProgress();
            if(newPercent > currentPercent) {
                mDownloadDialog.incrementProgress(newPercent - currentPercent);
                mDownloadDialog.setContent(
                        String.format(
                                "%s / %s",
                                FileSizeUtils.humanReadableByteCount(readSize, true),
                                FileSizeUtils.humanReadableByteCount(fileSize, true)
                        ));
            }
        }
    }

    @Override
    public void onDismissDownloadDialog() {
        if(mDownloadDialog != null && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
            mDownloadDialog = null;
        }
    }

    @Override
    public void onShowErrorDialog(String title, String content) {
        new MaterialDialog.Builder(getContext())
                .title(title)
                .content(content)
                .positiveText(android.R.string.ok)
                .show();
    }

    public void updateBreadcrumb(String path) {
        if (path == null || TextUtils.isEmpty(mBreadCrumbLayout.getTopPath())) {
            // Initial directory
            // path = mCurrentDirectory.getPath();
            mBreadCrumbLayout.setTopPath(path);
        }

        Crumb crumb = new Crumb(getContext(), path);
        updateBreadcrumb(crumb, true, true);
    }

    public void updateBreadcrumb(Crumb crumb, boolean forceRecreate, boolean addToHistory) {
        if (forceRecreate) {
            // Rebuild artificial history, most likely first time load
            mBreadCrumbLayout.clearHistory();
            String path = crumb.getPath();
            while (path != null) {
                mBreadCrumbLayout.addHistory(new Crumb(getContext(), path));
                if (mBreadCrumbLayout.isTopPath(path)) {
                    break;
                }
                path = new File(path).getParent();
            }
            mBreadCrumbLayout.reverseHistory();
        } else if (addToHistory) {
            mBreadCrumbLayout.addHistory(crumb);
        }
        mBreadCrumbLayout.setActiveOrAdd(crumb, forceRecreate);
    }

    @Override
    public void onSelectionStart() {
        /*if(mActionMode != null)
            mActionMode.finish();
        mActionMode = mToolbar.startActionMode(mActionModeCallback);*/
        mCab = new MaterialCab(getAppCompatActivity(), R.id.cab_stub)
                .setMenu(R.menu.menu_cab_fileselection)
                .setBackgroundColor(mPrimaryColor)
                .setCloseDrawableRes(R.drawable.ic_action_menu_close)
                .start(this);
        Log.e(getLogTag(), "SelectionStart");
    }

    @Override
    public void onSelectionEnd() {
        Log.e(getLogTag(), "SelectionEnd");
        if(mCab != null && mCab.isActive())
            mCab.finish();
    }

    @Override
    public void onSelect(int currentSelectionCount, int...positions) {
        String selectInfo = positions == null || positions.length == 0 ? "Select all" : String.format(
                "Select %d items: %s",
                currentSelectionCount,
                Arrays.toString(positions)
        );
        if(mCab != null && mCab.isActive())
            mCab.setTitle(Integer.toString(currentSelectionCount));
        Log.e(getLogTag(), selectInfo);
    }

    @Override
    public void onDeselect(int currentSelectionCount, int...positions) {
        String selectInfo = positions == null || positions.length == 0 ? "Clear selection" : String.format(
                "Deselect %d items: %s",
                currentSelectionCount,
                Arrays.toString(positions)
        );
        if(mCab != null && mCab.isActive())
            mCab.setTitle(Integer.toString(currentSelectionCount));
        Log.e(getLogTag(), selectInfo);
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        Log.e(getLogTag(), "onCabCreated");
        MenuUtils.showMenuItemIcon(menu);
        ATE.applyMenu(getActivity(), mATEKey, menu);
        DrawableCompat.setTint(materialCab.getToolbar().getNavigationIcon(), mToolbarContentColor);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        Log.e(getLogTag(), menuItem.getTitle().toString());
        int menuItemId = menuItem.getItemId();
        switch (menuItemId) {
            case R.id.action_cab_delete:
                menuDeleteFile();
                break;
            case R.id.action_cab_select_all:
                menuSelectAll();
                break;
        }
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        Log.e(getLogTag(), "onCabFinished");
        if(mCollectionAdapter.isInSelection())
            mCollectionAdapter.clearSelection();
        return true;
    }

    protected void menuDeleteFile() {
        RF[] files = mCollectionAdapter.getSelectionItems(getRemoteFileClass());
        mCollectionAdapter.clearSelection();
        LongOperationService.LongOperationBinder longOperationBinder = getMainActivity().getLongOperationBinder();
        longOperationBinder.<RF, CR, SP>doOperation(
                new FileOperation<>(
                        FileOperation.FileOperationCode.DELETE,
                        new Date().getTime(),
                        mStorageProviderRecordId,
                        mViewModel.getDirectory().directory,
                        files
                ),
                (Class<RF>) mViewModel.getDirectory().directory.getClass()
        );
        // mViewModel.deleteFile(files);
    }

    protected void menuSelectAll() {
        mCollectionAdapter.selectAll();
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getAppCompatActivity();
    }
}
