package org.cryse.unifystorage.explorer.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.mikepenz.materialize.color.Material;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.PrefsConst;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.event.AbstractEvent;
import org.cryse.unifystorage.explorer.event.CancelTaskEvent;
import org.cryse.unifystorage.explorer.event.EventConst;
import org.cryse.unifystorage.explorer.event.FileDeleteEvent;
import org.cryse.unifystorage.explorer.event.FileDeleteResultEvent;
import org.cryse.unifystorage.explorer.event.FrontUIDismissEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.message.BasicMessage;
import org.cryse.unifystorage.explorer.message.DownloadFileMessage;
import org.cryse.unifystorage.explorer.service.FileOperation;
import org.cryse.unifystorage.explorer.service.FileOperationTaskEvent;
import org.cryse.unifystorage.explorer.service.StopDownloadEvent;
import org.cryse.unifystorage.explorer.service.operation.CreateFolderOperation;
import org.cryse.unifystorage.explorer.service.operation.DeleteOperation;
import org.cryse.unifystorage.explorer.service.operation.DownloadOperation;
import org.cryse.unifystorage.explorer.service.operation.OnRemoteOperationListener;
import org.cryse.unifystorage.explorer.service.operation.OperationObserverManager;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperation;
import org.cryse.unifystorage.explorer.service.operation.RemoteOperationResult;
import org.cryse.unifystorage.explorer.ui.MainActivity;
import org.cryse.unifystorage.explorer.ui.common.AbstractFragment;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.MenuUtils;
import org.cryse.unifystorage.explorer.utils.RandomUtils;
import org.cryse.unifystorage.explorer.utils.ResourceUtils;
import org.cryse.unifystorage.explorer.utils.copy.CopyManager;
import org.cryse.unifystorage.explorer.utils.copy.CopyTask;
import org.cryse.unifystorage.explorer.utils.exception.ExceptionUtils;
import org.cryse.unifystorage.explorer.utils.openfile.AndroidOpenFileUtils;
import org.cryse.unifystorage.explorer.utils.openfile.OpenFileUtils;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.FileSizeUtils;
import org.cryse.utils.file.OnFileChangedListener;
import org.cryse.utils.preference.BooleanPrefs;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.selector.SelectableRecyclerViewAdapter;
import org.cryse.widget.StateView;
import org.cryse.widget.recyclerview.Bookends;

import java.io.File;
import java.util.Hashtable;
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
        MaterialCab.Callback,
        OnRemoteOperationListener {

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
    private Hashtable<Integer, MaterialDialog> mMaterialDialogs = new Hashtable<>();

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
        getActivity().registerReceiver(this.mDownloadStartReceiver, new IntentFilter(DataContract.DOWNLOAD_BROADCAST_START_IDENTIFIER));
        getActivity().registerReceiver(this.mDownloadProgressReceiver, new IntentFilter(DataContract.DOWNLOAD_BROADCAST__PROGRESS_IDENTIFIER));
        getActivity().registerReceiver(this.mDownloadSuccessReceiver, new IntentFilter(DataContract.DOWNLOAD_BROADCAST_SUCCESS_IDENTIFIER));
        getActivity().registerReceiver(this.mDownloadErrorReceiver, new IntentFilter(DataContract.DOWNLOAD_BROADCAST_ERROR_IDENTIFIER));
        OperationObserverManager.instance().addOperationListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mDownloadStartReceiver);
        getActivity().unregisterReceiver(this.mDownloadProgressReceiver);
        getActivity().unregisterReceiver(this.mDownloadSuccessReceiver);
        getActivity().unregisterReceiver(this.mDownloadErrorReceiver);
        for (MaterialDialog materialDialog : mMaterialDialogs.values()) {
            if (materialDialog != null && !materialDialog.isCancelled())
                materialDialog.dismiss();
        }
        mMaterialDialogs.clear();
        OperationObserverManager.instance().removeOperationListener(this);
        for (Iterator<Map.Entry<String, MaterialDialog>> iterator = mDialogMaps.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, MaterialDialog> entry = iterator.next();
            MaterialDialog dialog = entry.getValue();
            if (dialog != null && !dialog.isCancelled()) {
                dialog.dismiss();
            }
            iterator.remove();
        }
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
            updateBreadcrumb(directory.directory.getPath());
        } else {
            showNoFilesView();
        }
    }

    @Override
    public void showError(DirectoryInfo directory, Throwable throwable) {
        if (directory != null && directory.directory != null) {
            updateBreadcrumb(directory.directory.getPath());
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
    public void showMessage(BasicMessage basicMessage) {
        switch (basicMessage.getMsgType()) {
            case BasicMessage.MSG_TYPE:
                break;
            case DownloadFileMessage.MSG_TYPE:
                //showDownloadDialog((DownloadFileMessage) basicMessage);
                break;
        }
    }
/*
    private void showDownloadDialog(DownloadFileMessage message) {
        switch (message.getAction()) {
            case CREATE:
                if(mDownloadDialog != null && mDownloadDialog.isShowing()) mDownloadDialog.dismiss();
                mDownloadDialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.dialog_title_opening_file)
                        .content(R.string.dialog_content_opening)
                        .progress(false, 100, false)
                        .dismissListener(message.getOnDismissListener())
                        .show();
                break;
            case UPDATE:
                if(mDownloadDialog != null && mDownloadDialog.isShowing()) {
                    int newPercent = (int) Math.round(((double) message.getCurrentSize()  / (double) message.getFileSize()) * 100.0d);
                    int currentPercent = mDownloadDialog.getCurrentProgress();
                    if(newPercent > currentPercent) {
                        mDownloadDialog.incrementProgress(newPercent - currentPercent);
                        mDownloadDialog.setContent(
                                String.format(
                                        "%s / %s",
                                        FileSizeUtils.humanReadableByteCount(message.getCurrentSize(), true),
                                        FileSizeUtils.humanReadableByteCount(message.getFileSize(), true)
                                ));
                    }
                }
                break;
            default:
            case DISMISS:
                if(mDownloadDialog != null && mDownloadDialog.isShowing()) mDownloadDialog.dismiss();
                break;
        }
    }*/

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
            mEventBus.sendEvent(
                    new FileOperationTaskEvent(
                            new FileOperation(
                                    FileOperation.FileOperationCode.COPY,
                                    RandomUtils.nextInt(),
                                    mPresenter.getStorageProviderInfo(),
                                    mPresenter.getDirectory().directory,
                                    files
                            )
                    )
            );
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
            case EventConst.EVENT_ID_FILE_DELETE:
                onFileDeleteEvent((FileDeleteEvent) event);
                break;
            case EventConst.EVENT_ID_FILE_DELETE_RESULT:
                onFileDeleteResultEvent((FileDeleteResultEvent) event);
                break;
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
        }
    }

    protected void onFileDeleteEvent(FileDeleteEvent fileDeleteEvent) {
        if (isCurrentStorageProvider(fileDeleteEvent.providerId)) {
            if (fileDeleteEvent.success) {
                // mPresenter.onDeleteFileEvent(fileDeleteEvent);
            } else {
                Toast.makeText(getContext(), "Delete: " + fileDeleteEvent.fileName + " failed.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    protected void onFileDeleteResultEvent(FileDeleteResultEvent fileDeleteResultEvent) {
        if (isCurrentStorageProvider(fileDeleteResultEvent.providerId)) {
            if (fileDeleteResultEvent.succes) {
            } else {
                Toast.makeText(getContext(), fileDeleteResultEvent.errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected boolean isCurrentStorageProvider(int id) {
        return mPresenter.getStorageProviderInfo().getStorageProviderId() == id;
    }

    private BroadcastReceiver mDownloadStartReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            // Toast.makeText(getActivity(), "Start downloading " + intent.getStringExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME), Toast.LENGTH_SHORT).show();
            int token = intent.getIntExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, 0);
            MaterialDialog downloadingDialog = mMaterialDialogs.get(token);
            if (downloadingDialog == null || downloadingDialog.isCancelled()) {
                downloadingDialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.dialog_title_opening_file)
                        .content(R.string.dialog_content_opening)
                        .progress(false, 100, false)
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                RxEventBus.getInstance().sendEvent(new StopDownloadEvent(intent.getIntExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, 0)));
                            }
                        })
                        .show();
                mMaterialDialogs.put(token, downloadingDialog);
            }
        }
    };

    private BroadcastReceiver mDownloadProgressReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.e("PROGRESS", "Downloading " + intent.getStringExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME) + ": " + Integer.toString(intent.getIntExtra(DataContract.DOWNLOAD_BROADCAST_PERCENTAGE, 0)) + "%");
            int token = intent.getIntExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, 0);
            long readSize = intent.getLongExtra(DataContract.DOWNLOAD_BROADCAST_READ_SIZE, 0);
            long totalSize = intent.getLongExtra(DataContract.DOWNLOAD_BROADCAST_FILE_SIZE, 0);
            int newPercent = (int) Math.round(((double) readSize / (double) totalSize) * 100.0d);
            MaterialDialog downloadingDialog = mMaterialDialogs.get(token);
            if (downloadingDialog != null && !downloadingDialog.isCancelled()) {
                int currentPercent = downloadingDialog.getCurrentProgress();
                downloadingDialog.incrementProgress(newPercent - currentPercent);
                downloadingDialog.setContent(
                        String.format(
                                "%s / %s",
                                FileSizeUtils.humanReadableByteCount(readSize, true),
                                FileSizeUtils.humanReadableByteCount(totalSize, true)
                        ));
            }
        }
    };

    private BroadcastReceiver mDownloadSuccessReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Toast.makeText(getActivity(), "Download " + intent.getStringExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME) + " success.", Toast.LENGTH_SHORT).show();
            int token = intent.getIntExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, 0);
            MaterialDialog downloadingDialog = mMaterialDialogs.get(token);
            if (downloadingDialog != null && !downloadingDialog.isCancelled()) {
                downloadingDialog.dismiss();
            }
            if (intent.getBooleanExtra(DataContract.DOWNLOAD_BROADCAST_SUCCESS_OPEN, false)) {
                String localPath = intent.getStringExtra(DataContract.DOWNLOAD_BROADCAST_SUCCESS_PATH);
                Uri fileUri = FileProvider.getUriForFile(
                        getActivity(),
                        getActivity().getString(R.string.authority_file_provider),
                        new File(localPath));
                openFileByUri(fileUri.toString(), true);

            }
        }
    };

    private BroadcastReceiver mDownloadErrorReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Toast.makeText(getActivity(), "Download " + intent.getStringExtra(DataContract.DOWNLOAD_BROADCAST_FILENAME) + " failed.", Toast.LENGTH_SHORT).show();
            String errorMessage = intent.getStringExtra(DataContract.DOWNLOAD_BROADCAST_ERROR_MESSAGE);
            int token = intent.getIntExtra(DataContract.DOWNLOAD_BROADCAST_TOKEN, 0);
            MaterialDialog downloadingDialog = mMaterialDialogs.get(token);
            if (downloadingDialog != null && !downloadingDialog.isCancelled()) {
                downloadingDialog.dismiss();
            }
            MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_title_error)
                    .content(errorMessage)
                    .show();
        }
    };

    private ConcurrentHashMap<String, MaterialDialog> mDialogMaps = new ConcurrentHashMap<>();

    @Override
    public void onRemoteOperationStart(RemoteOperation caller) {
        if (isCurrentStorageProvider(caller.getOperationContext().getStorageProviderId())) {
            final String token = caller.getOperationToken();
            // Toast.makeText(getActivity(), String.format("%s onStart.", caller.getOperationName()), Toast.LENGTH_SHORT).show();
            if (mDialogMaps.containsKey(token)) {
                MaterialDialog dialog = mDialogMaps.get(token);
                if (dialog != null && !dialog.isCancelled() && dialog.isShowing())
                    dialog.dismiss();
                mDialogMaps.remove(token);
            } else {
                MaterialDialog dialog = null;
                if (caller instanceof DeleteOperation) {
                    dialog = new MaterialDialog.Builder(getContext())
                            .title(R.string.dialog_title_deleting_file)
                            .content(R.string.dialog_content_opening)
                            .progress(false, 100, false)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    RxEventBus.getInstance().sendEvent(new FrontUIDismissEvent(token));
                                }
                            })
                            .show();
                } else if (caller instanceof CreateFolderOperation) {
                    dialog = new MaterialDialog.Builder(getContext())
                            .title(R.string.dialog_title_create_new_directory)
                            .content(R.string.dialog_content_opening)
                            .progress(true, 100, false)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    RxEventBus.getInstance().sendEvent(new FrontUIDismissEvent(token));
                                }
                            })
                            .show();
                } else if (caller instanceof DownloadOperation) {
                    dialog = new MaterialDialog.Builder(getContext())
                            .title(R.string.dialog_title_opening_file)
                            .content(R.string.dialog_content_opening)
                            .progress(false, 100, false)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    RxEventBus.getInstance().sendEvent(new FrontUIDismissEvent(token));
                                }
                            })
                            .show();
                } else {

                }
                if (dialog != null) {
                    mDialogMaps.put(token, dialog);
                }
            }
        }
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (isCurrentStorageProvider(caller.getOperationContext().getStorageProviderId())) {
            // Toast.makeText(getActivity(), String.format("%s onFinish.", caller.getOperationName()), Toast.LENGTH_SHORT).show();
            String token = caller.getOperationToken();
            if (mDialogMaps.containsKey(token)) {
                final MaterialDialog dialog = mDialogMaps.get(token);
                if (dialog != null && !dialog.isCancelled()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
                mDialogMaps.remove(token);
            }
            if (!result.isSuccess()) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.dialog_title_error)
                        .content(result.getException().getMessage())
                        .show();
            } else {
                if (caller.shouldRefresh()) {
                    if (mPresenter.getDirectory() != null) {
                        mPresenter.getDirectory().clear();
                    }
                    mPresenter.loadFiles(mPresenter.getDirectory(), true, false, getCollectionViewState());
                }
                if (caller instanceof DownloadOperation) {
                    String localPath = ((DownloadOperation) caller).getSavePath();
                    Uri fileUri = FileProvider.getUriForFile(
                            getActivity(),
                            getActivity().getString(R.string.authority_file_provider),
                            new File(localPath));
                    openFileByUri(fileUri.toString(), true);
                }
            }
        }
    }

    @Override
    public void onRemoteOperationProgress(RemoteOperation caller, final long current, final long total) {
        if (isCurrentStorageProvider(caller.getOperationContext().getStorageProviderId())) {
            // Toast.makeText(getActivity(), String.format("%s onProgress.", caller.getOperationName()), Toast.LENGTH_SHORT).show();
            final String token = caller.getOperationToken();
            if (mDialogMaps.containsKey(token)) {
                final MaterialDialog dialog = mDialogMaps.get(token);
                if (dialog != null && !dialog.isCancelled()) {
                    int lastPercent = dialog.getCurrentProgress();
                    int newPercent = (int) Math.round(((double) current / (double) total) * 100.0d);
                    if (lastPercent < newPercent) {
                        dialog.incrementProgress(newPercent - lastPercent);
                    }
                }
            }
        }
    }
}