package org.cryse.unifystorage.explorer.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.impression.widget.breadcrumbs.BreadCrumbLayout;
import com.afollestad.impression.widget.breadcrumbs.Crumb;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.PrefsConst;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.EventConst;

import org.cryse.unifystorage.explorer.ui.MainActivity;
import org.cryse.unifystorage.explorer.ui.common.AbstractFragment;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.MenuUtils;
import org.cryse.unifystorage.explorer.utils.ResourceUtils;
import org.cryse.unifystorage.explorer.utils.copy.CopyManager;
import org.cryse.unifystorage.explorer.utils.copy.CopyTask;
import org.cryse.unifystorage.explorer.utils.openfile.AndroidOpenFileUtils;
import org.cryse.unifystorage.explorer.utils.openfile.OpenFileUtils;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.utils.file.OnFileChangedListener;
import org.cryse.utils.preference.BooleanPrefs;
import org.cryse.utils.preference.Prefs;
import org.cryse.widget.cab.MaterialCab;
import org.cryse.widget.SelectableRecyclerViewAdapter;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilesFragment extends AbstractFragment implements
        FilesContract.View,
        SelectableRecyclerViewAdapter.OnSelectionListener,
        MaterialCab.Callback/*,
        OnRemoteOperationListener*/ {

    private AtomicBoolean mDoubleBackPressedOnce = new AtomicBoolean(false);
    private Handler mHandler = new Handler();

    private final Runnable mBackPressdRunnable = new Runnable() {
        @Override
        public void run() {
            mDoubleBackPressedOnce.set(false);
        }
    };

    private AtomicBoolean isLoadingMore = new AtomicBoolean(false);
    private FilesContract.Presenter mPresenter;
    private FilesAdapter mCollectionAdapter;
    private OpenFileUtils mOpenFileUtils;
    private LocalFileWatcher mFileWatcher;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.breadCrumbs)
    BreadCrumbLayout mBreadCrumbLayout;

    @Bind(R.id.fragment_files_recyclerview_files)
    EasyRecyclerView mCollectionView;

    @Bind(R.id.fragment_files_fab_menu)
    FloatingActionMenu mFabMenu;

    @Bind(R.id.fragment_files_fab_new_directory)
    FloatingActionButton mFabNewDirectory;

    @Bind(R.id.fragment_files_fab_new_file)
    FloatingActionButton mFabNewFile;

    @Bind(R.id.fragment_files_fab_paste)
    FloatingActionButton mFabPaste;

    protected MaterialCab mCab;

    protected BooleanPrefs mShowHiddenFilesPrefs;
    protected MenuItem mShowHiddenFilesMenuItem;

    public FilesFragment() {
        // Requires empty public constructor
    }

    public static FilesFragment newInstance() {
        return new FilesFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCollectionAdapter = new FilesAdapter(getActivity());
        mCollectionAdapter.setOnSelectionListener(this);
        mOpenFileUtils = new AndroidOpenFileUtils(getActivity());
        setupFileWatcher();
        mShowHiddenFilesPrefs = Prefs.getBooleanPrefs(
                PrefsConst.PREFS_SHOW_HIDDEN_FILES,
                PrefsConst.PREFS_SHOW_HIDDEN_FILES_VALUE
        );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_files, container, false);

        ButterKnife.bind(this, fragmentView);
        setupToolbar();
        setupRecyclerView();
        setupBreadCrumb();
        setupFab();
        return fragmentView;
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
                        if (mCollectionAdapter.isInSelection() || !mPresenter.isAtTopPath()) {
                            if (!FilesFragment.this.mDoubleBackPressedOnce.get()) {
                                FilesFragment.this.mDoubleBackPressedOnce.set(true);
                                mHandler.postDelayed(mBackPressdRunnable, 400);
                                if (mCollectionAdapter.isInSelection()) {
                                    mCollectionAdapter.clearSelection();
                                } else if (!mPresenter.isAtTopPath()) {
                                    mPresenter.onBackPressed();
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
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable indicatorDrawable = ResourceUtils.makeTintedDrawable(getActivity(), R.drawable.ic_action_menu_drawer, Color.WHITE);
            actionBar.setHomeAsUpIndicator(indicatorDrawable);
        }
    }

    public void onSelectedInViewPager() {
        getAppCompatActivity().setSupportActionBar(mToolbar);
    }

    private void setupBreadCrumb() {
        mBreadCrumbLayout.setRootPathName(mPresenter.getStorageProviderName());
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
                        mCollectionView.getRecyclerView().scrollToPosition(0);
                        /*Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
                        ((MediaFragment) frag).jumpToTop(true);*/
                    } else {
                        mPresenter.backToParent(crumb.getPath(), getCollectionViewState());
                    }
                }
            }
        });
    }

    private void setupFab() {
        if (CopyManager.getInstance().hasCopyTask()) {
            mFabMenu.setVisibility(View.GONE);
            mFabPaste.setVisibility(View.VISIBLE);
        } else {
            mFabMenu.setVisibility(View.VISIBLE);
            mFabPaste.setVisibility(View.GONE);
        }
        mFabNewDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .title(R.string.dialog_title_create_new_directory)
                        .content(R.string.dialog_content_create_new_directory)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                mPresenter.createFolder(mPresenter.getDirectory().directory, input.toString());
                            }
                        })
                        .show();
                mFabMenu.close(true);
            }
        });
        mFabPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuPasteFile();
            }
        });
        mFabNewDirectory.setImageDrawable(ResourceUtils.makeTintedDrawable(getActivity(), R.drawable.ic_format_folder, Color.WHITE));
        mFabNewFile.setImageDrawable(ResourceUtils.makeTintedDrawable(getActivity(), R.drawable.ic_format_file, Color.WHITE));
    }

    private void setupRecyclerView() {
        mCollectionView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, UIUtils.dip2px(getActivity(),0.5f), UIUtils.dip2px(getActivity(),72),0);
        // itemDecoration.setDrawLastItem(false);
        // mCollectionView.addItemDecoration(itemDecoration);
        mCollectionView.setAdapterWithProgress(mCollectionAdapter = new FilesAdapter(getActivity()));
        mCollectionAdapter.setMore(R.layout.view_more, new RecyclerArrayAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isLoadingMore.get() && mPresenter.getDirectory().hasMore) {
                    mPresenter.loadFiles(mPresenter.getDirectory(), true, false, getCollectionViewState());
                    isLoadingMore.set(true);
                    // getPresenter().loadThreadList(mUserAccountManager.getAuthObject(), mForumId, mLastItemSortKey, mCurrentListType, true);
                } else {
                    mCollectionAdapter.stopMore();
                }
            }
        });
        mCollectionAdapter.setNoMore(R.layout.view_nomore);
        mCollectionAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(int position) {
                mCollectionAdapter.toggleSelection(position);
                mPresenter.onFileLongClick(mCollectionAdapter.getItem(position).getRemoteFile());
                return true;
            }
        });
        mCollectionAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mCollectionAdapter.isInSelection()) {
                    mCollectionAdapter.toggleSelection(position);
                } else {
                    RemoteFile file = mCollectionAdapter.getItem(position).getRemoteFile();
                    mPresenter.onFileClick(file, getCollectionViewState());
                }
            }
        });
        mCollectionAdapter.setError(R.layout.view_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter.getDirectory() != null) {
                    mPresenter.getDirectory().clear();
                }
                mPresenter.loadFiles(mPresenter.getDirectory(), true, false, null);
            }
        });
        mCollectionView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mPresenter.getDirectory() != null) {
                    mPresenter.getDirectory().clear();
                }
                mPresenter.loadFiles(mPresenter.getDirectory(), true, false, null);
            }
        });
        mCollectionAdapter.setOnSelectionListener(this);
    }

    protected void setupFileWatcher() {
        mFileWatcher = new FileObserverWatcher();
        mFileWatcher.setOnFileChangeListener(new OnFileChangedListener() {
            @Override
            public boolean onFileCreate(String path, String file) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.loadFiles(mPresenter.getDirectory(), true, false, getCollectionViewState());
                    }
                });
                return true;
            }

            @Override
            public boolean onFileDelete(String path, String file) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.loadFiles(mPresenter.getDirectory(), true, false, getCollectionViewState());
                    }
                });
                return true;
            }

            @Override
            public boolean onFileModify(String path, String file) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.loadFiles(mPresenter.getDirectory(), true, false, getCollectionViewState());
                    }
                });
                return true;
            }

            @Override
            public boolean onFileEvent(int event, String path, String file) {
                return false;
            }
        });
    }

    public void updateBreadcrumb(String path) {
        if (path == null || TextUtils.isEmpty(mBreadCrumbLayout.getTopPath())) {
            // Initial directory
            // path = mCurrentDirectory.getPath();
            mBreadCrumbLayout.setTopPath(path);
        }

        Crumb crumb = new Crumb(getContext(), mBreadCrumbLayout.getRootPathName(),path);
        updateBreadcrumb(crumb, true, true);
    }

    public void updateBreadcrumb(Crumb crumb, boolean forceRecreate, boolean addToHistory) {
        if (forceRecreate) {
            // Rebuild artificial history, most likely first time load
            mBreadCrumbLayout.clearHistory();
            String path = crumb.getPath();
            while (path != null) {
                mBreadCrumbLayout.addHistory(new Crumb(getContext(), mBreadCrumbLayout.getRootPathName(), path));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_filelist, menu);
        mShowHiddenFilesMenuItem = menu.findItem(R.id.action_show_hidden_files);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mShowHiddenFilesMenuItem != null) {
            mShowHiddenFilesMenuItem.setChecked(mShowHiddenFilesPrefs.get());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getNavigationDrawer().openDrawer();
                    return true;
                } else {
                    return false;
                }
            case R.id.action_show_hidden_files:
                boolean isShow = mShowHiddenFilesPrefs.get();
                mShowHiddenFilesPrefs.set(!isShow);
                if (mShowHiddenFilesMenuItem != null)
                    mShowHiddenFilesMenuItem.setChecked(!isShow);
                mPresenter.setShowHiddenFiles(!isShow);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        if (mPresenter.showWatchChanges()) {
            if (mPresenter.getDirectory() != null && mPresenter.getDirectory().directory != null)
                mFileWatcher.startWatching(mPresenter.getDirectory().directory.getPath());
        }
        // OperationObserverManager.instance().addOperationListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFileWatcher.destroy();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (getView() == null) {
            return;
        }
        if(active)
            mCollectionView.showProgress();
        else {
            if(mCollectionAdapter.getCount() > 0)
                mCollectionView.showRecycler();
            else
                mCollectionView.showEmpty();
        }
        /*if (active)
            hideStateView();*/
        // mLoadingProgress.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onLeaveDirectory(DirectoryInfo directory) {
        if (directory != null && directory.directory != null)
            mFileWatcher.stopWatching(directory.directory.getPath());
    }

    @Override
    public void updatePath(String path) {
        updateBreadcrumb(path);
    }

    @Override
    public void showFiles(DirectoryInfo directory, @Nullable CollectionViewState collectionViewState) {
        isLoadingMore.set(false);
        mCollectionAdapter.stopMore();
        if (directory != null && directory.files != null) {
            if (mPresenter.showWatchChanges())
                mFileWatcher.startWatching(directory.directory.getPath());
            mCollectionAdapter.replaceWith(RemoteFileWrapper.wrap(directory.files));
            if (directory.files.size() > 0) {
                // hideStateView();
                if (collectionViewState != null) {
                    LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getRecyclerView().getLayoutManager();
                    manager.scrollToPositionWithOffset(collectionViewState.position, (int) collectionViewState.offset);
                }
            } else {
                // showNoFilesView();
            }
            // updateBreadcrumb(directory.directory.getPath());
        } else {
            mCollectionAdapter.clear();
            // showNoFilesView();
        }
    }

    @Override
    public void showError(DirectoryInfo directory, Throwable throwable) {
        if (directory != null && directory.directory != null) {
            // updateBreadcrumb(directory.directory.getPath());
        }
        if (directory != null)
            mCollectionAdapter.replaceWith(RemoteFileWrapper.wrap(directory.files));
        mCollectionView.showError();
        // showRetryView(ExceptionUtils.exceptionToStringRes(throwable));
    }

    @Override
    public void showAddFile() {

    }

    @Override
    public void showFileDetailsUi(String fileId) {

    }

    @Override
    public void showVisibleFilter() {

    }

    @Override
    public void showAllFilter() {

    }

    @Override
    public void openFileByPath(String filePath, boolean useSystemSelector) {
        mOpenFileUtils.openFileByPath(filePath, useSystemSelector);
    }

    @Override
    public void requestForDownload(final RemoteFile remoteFile) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_title_open_file_is_not_local)
                .content(getString(R.string.dialog_content_open_file_is_not_local, remoteFile.getName(), mPresenter.getStorageProviderName()))
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.dialog_button_download)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mPresenter.downloadFile(remoteFile, null, true);
                    }
                })
                .show();
    }

    @Override
    public void openFileByUri(String uriString, boolean useSystemSelector) {
        mOpenFileUtils.openFileByUri(uriString, useSystemSelector);
    }

    @Override
    public void setPresenter(FilesContract.Presenter presenter) {
        this.mPresenter = presenter;
    }
/*
    private void showNoFilesView() {
        // mCollectionView.setVisibility(View.INVISIBLE);
        mStateView.showEmptyViewByRes(R.drawable.ic_icon_empty_folder, R.string.info_message_empty_directory);
    }

    private void hideStateView() {
        // mCollectionView.setVisibility(View.VISIBLE);
        mStateView.hide();
    }

    private void showRetryView(int errorMessageResId) {
        // mCollectionView.setVisibility(View.INVISIBLE);
        mStateView.showErrorViewByRes(R.drawable.ic_icon_error, errorMessageResId, R.string.label_refresh);
    }*/

    private CollectionViewState getCollectionViewState() {
        LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getRecyclerView().getLayoutManager();
        int position = manager.findFirstVisibleItemPosition();
        View firstItemView = manager.findViewByPosition(position);
        if (firstItemView != null) {
            float offset = firstItemView.getTop();
            return new CollectionViewState(position, offset);
        } else
            return CollectionViewState.EMPTY;
    }

    protected void menuSelectAll() {
        mCollectionAdapter.selectAll();
    }

    protected void menuCopyFile() {
        RemoteFileWrapper[] files = mCollectionAdapter.getSelectionItems(RemoteFileWrapper.class);
        mCollectionAdapter.clearSelection();
        CopyManager.getInstance().setCopyTask(new CopyTask(mPresenter.getStorageProviderInfo(), RemoteFileWrapper.toArray(files)));
    }

    protected void menuPasteFile() {
        if (CopyManager.getInstance().hasCopyTask()) {
            CopyTask task = CopyManager.getInstance().getCurrentCopyTask();
            RemoteFile[] files = task.fileToCopy;
            Toast.makeText(getContext(), "Paste!", Toast.LENGTH_SHORT).show();
            CopyManager.getInstance().cancelCopyTask();
            /*mEventBus.sendEvent(
                    new FileOperationTaskEvent(
                            new FileOperation(
                                    FileOperation.FileOperationCode.COPY,
                                    RandomUtils.nextInt(),
                                    mPresenter.getStorageProviderInfo(),
                                    mPresenter.getDirectory().directory,
                                    files
                            )
                    )
            );*/
        }
    }

    protected void menuDeleteFile() {
        RemoteFileWrapper[] files = mCollectionAdapter.getSelectionItems(RemoteFileWrapper.class);
        mCollectionAdapter.clearSelection();
        mPresenter.deleteFiles(RemoteFileWrapper.toArray(files));
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getAppCompatActivity();
    }

    @Override
    public void onSelectionStart() {
        mCab = new MaterialCab(getAppCompatActivity(), getView(), R.id.cab_stub)
                .setMenu(R.menu.menu_cab_fileselection)
                .setBackgroundColor(mPrimaryColor)
                .setCloseDrawableRes(R.drawable.ic_action_menu_close)
                .start(this);
    }

    @Override
    public void onSelectionEnd() {
        if (mCab != null && mCab.isActive())
            mCab.finish();
    }

    @Override
    public void onSelect(int currentSelectionCount, int... positions) {
        if (mCab != null && mCab.isActive())
            mCab.setTitle(Integer.toString(currentSelectionCount));
    }

    @Override
    public void onDeselect(int currentSelectionCount, int... positions) {
        if (mCab != null && mCab.isActive())
            mCab.setTitle(Integer.toString(currentSelectionCount));
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        MenuUtils.showMenuItemIcon(menu);
        // ATE.applyMenu(getActivity(), mATEKey, menu);
        //DrawableCompat.setTint(materialCab.getToolbar().getNavigationIcon(), mToolbarContentColor);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        switch (menuItemId) {
            case R.id.action_cab_delete:
                menuDeleteFile();
                break;
            case R.id.action_cab_select_all:
                menuSelectAll();
                break;
            case R.id.action_cab_copy:
                menuCopyFile();
                break;
        }
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        if (mCollectionAdapter.isInSelection())
            mCollectionAdapter.clearSelection();
        return true;
    }

    @Override
    protected void onEvent(AbstractEvent event) {
        super.onEvent(event);
        int eventId = event.eventId();
        switch (eventId) {
            case EventConst.EVENT_ID_SELECT_COPY_EVENT:
                Toast.makeText(getContext(), String.format("Select %d files to copy.", CopyManager.getInstance().getCurrentCopyTask().fileToCopy.length), Toast.LENGTH_SHORT).show();
                mFabMenu.setVisibility(View.GONE);
                mFabPaste.setVisibility(View.VISIBLE);
                break;
            case EventConst.EVENT_ID_CANCEL_SELECT_COPY_EVENT:
                Toast.makeText(getContext(), "Copy cancel.", Toast.LENGTH_SHORT).show();
                mFabMenu.setVisibility(View.VISIBLE);
                mFabPaste.setVisibility(View.GONE);
                break;
            /*case EventConst.EVENT_ID_SHOW_PROGRESS:
                onRemoteOperationStart(((ShowProgressEvent)event).getOperation());*/
        }
    }

    protected boolean isCurrentStorageProvider(int id) {
        return mPresenter.getStorageProviderInfo().getStorageProviderId() == id;
    }
}
