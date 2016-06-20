package org.cryse.unifystorage.explorer.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
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
import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

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
import org.cryse.unifystorage.explorer.utils.exception.ExceptionUtils;
import org.cryse.unifystorage.explorer.utils.openfile.AndroidOpenFileUtils;
import org.cryse.unifystorage.explorer.utils.openfile.OpenFileUtils;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.utils.file.OnFileChangedListener;
import org.cryse.utils.preference.BooleanPrefs;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.selector.SelectableRecyclerViewAdapter;
import org.cryse.widget.StateView;
import org.cryse.widget.recyclerview.Bookends;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilesFragment extends AbstractFragment implements
        FilesContract.View,
        FilesAdapter.OnFileClickListener,
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
    private Bookends<FilesAdapter> mWrapperAdapter;
    private OpenFileUtils mOpenFileUtils;
    private LocalFileWatcher mFileWatcher;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.breadCrumbs)
    BreadCrumbLayout mBreadCrumbLayout;

    @Bind(R.id.fragment_files_recyclerview_files)
    SuperRecyclerView mCollectionView;

    @Bind(R.id.fragment_files_fab_menu)
    FloatingActionMenu mFabMenu;

    @Bind(R.id.fragment_files_fab_new_directory)
    FloatingActionButton mFabNewDirectory;

    @Bind(R.id.fragment_files_fab_new_file)
    FloatingActionButton mFabNewFile;

    @Bind(R.id.fragment_files_fab_paste)
    FloatingActionButton mFabPaste;

    @Bind(R.id.fragment_files_state_view)
    StateView mStateView;

    @Bind(R.id.fragment_files_loading_progressbar)
    ProgressBar mLoadingProgress;

    private ProgressBar mMoreProgressBar;

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
        mCollectionAdapter.setOnFileClickListener(this);
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
        setupStateView();
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
        mCollectionView.getRecyclerView().setItemAnimator(new DefaultItemAnimator());
        mCollectionView.setOnMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                if (!isLoadingMore.get() && mPresenter.getDirectory().hasMore) {
                    mPresenter.loadFiles(mPresenter.getDirectory(), true, false, getCollectionViewState());
                    isLoadingMore.set(true);
                    // getPresenter().loadThreadList(mUserAccountManager.getAuthObject(), mForumId, mLastItemSortKey, mCurrentListType, true);
                } else {
                    mCollectionView.setLoadingMore(false);
                    mCollectionView.hideMoreProgress();
                }
            }

            @Override
            public void onChangeMoreVisibility(int visibility) {
                mMoreProgressBar.setVisibility(visibility);
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
        mWrapperAdapter = new Bookends<>(mCollectionAdapter);
        mCollectionView.setAdapter(mWrapperAdapter);
        mMoreProgressBar = new ProgressBar(getActivity());
        RecyclerView.LayoutParams moreProgressLP = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMoreProgressBar.setLayoutParams(moreProgressLP);
        mMoreProgressBar.setVisibility(View.INVISIBLE);
        mWrapperAdapter.addFooter(mMoreProgressBar);
        mWrapperAdapter.notifyDataSetChanged();
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

    protected void setupStateView() {
        mStateView.setOnStateChangeListener(new StateView.OnStateChangeListener() {
            @Override
            public void onStateChange(StateView.State state) {

            }

            @Override
            public void onButtonClick(StateView.State state) {
                if (state == StateView.State.ERROR) {
                    if (mPresenter.getDirectory() != null)
                        mPresenter.loadFiles(mPresenter.getDirectory(), true);
                }
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataContract.Action.NewOperation);
        intentFilter.addAction(DataContract.Action.ShowOperationDialog);
        intentFilter.addAction(DataContract.Action.OpenFile);
        getActivity().registerReceiver(mOperationReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOperationReceiver);
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
        if (active)
            hideStateView();
        mLoadingProgress.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
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
        if (mCollectionView.isLoadingMore()) {
            isLoadingMore.set(false);
            mCollectionView.setLoadingMore(false);
            mCollectionView.hideMoreProgress();
        }
        if (directory != null && directory.files != null) {
            if (mPresenter.showWatchChanges())
                mFileWatcher.startWatching(directory.directory.getPath());
            mCollectionAdapter.replaceWith(directory.files);
            if (directory.files.size() > 0) {
                hideStateView();
                if (collectionViewState != null) {
                    LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getRecyclerView().getLayoutManager();
                    manager.scrollToPositionWithOffset(collectionViewState.position, (int) collectionViewState.offset);
                }
            } else {
                showNoFilesView();
            }
            // updateBreadcrumb(directory.directory.getPath());
        } else {
            showNoFilesView();
        }
    }

    @Override
    public void showError(DirectoryInfo directory, Throwable throwable) {
        if (directory != null && directory.directory != null) {
            // updateBreadcrumb(directory.directory.getPath());
        }
        if (directory != null)
            mCollectionAdapter.replaceWith(directory.files);
        showRetryView(ExceptionUtils.exceptionToStringRes(throwable));
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
    public void openFileByUri(String uriString, boolean useSystemSelector) {
        mOpenFileUtils.openFileByUri(uriString, useSystemSelector);
    }

    @Override
    public void setPresenter(FilesContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

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
    }

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
        RemoteFile[] files = mCollectionAdapter.getSelectionItems(RemoteFile.class);
        mCollectionAdapter.clearSelection();
        CopyManager.getInstance().setCopyTask(new CopyTask(mPresenter.getStorageProviderInfo(), files));
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
        RemoteFile[] files = mCollectionAdapter.getSelectionItems(RemoteFile.class);
        mCollectionAdapter.clearSelection();
        mPresenter.deleteFiles(files);
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getAppCompatActivity();
    }

    @Override
    public void onFileClick(View view, int position, RemoteFile file) {
        if (mCollectionAdapter.isInSelection()) {
            mCollectionAdapter.toggleSelection(position);
        } else {
            mPresenter.onFileClick(file, getCollectionViewState());
        }
    }

    @Override
    public void onFileLongClick(View view, int position, RemoteFile file) {
        mCollectionAdapter.toggleSelection(position);
        mPresenter.onFileLongClick(file);
    }

    @Override
    public void onSelectionStart() {
        mCab = new MaterialCab(getAppCompatActivity(), R.id.cab_stub)
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

    private BroadcastReceiver mOperationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DataContract.Action.NewOperation:
                    String token = intent.getStringExtra(DataContract.Argument.OperationToken);
                    OperationProgressDialog dialog = OperationProgressDialog.create(token);
                    dialog.show(getChildFragmentManager(), null);
                    break;
                case DataContract.Action.OpenFile:
                    if(!intent.hasExtra(DataContract.Argument.Opened)) {
                        String savePath = intent.getStringExtra(DataContract.Argument.SavePath);
                        intent.putExtra(DataContract.Argument.Opened, true);
                        openFileByPath(savePath, true);
                    }
                    break;
            }
        }
    };
}
