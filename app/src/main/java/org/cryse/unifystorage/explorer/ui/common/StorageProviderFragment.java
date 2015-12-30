package org.cryse.unifystorage.explorer.ui.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.impression.widget.breadcrumbs.BreadCrumbLayout;
import com.afollestad.impression.widget.breadcrumbs.Crumb;

import org.cryse.unifystorage.AbstractFile;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.ui.MainActivity;
import org.cryse.unifystorage.explorer.ui.adapter.FileAdapter;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.sort.FileSorter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class StorageProviderFragment<
        RF extends RemoteFile,
        SP extends StorageProvider<RF>
        > extends AbstractFragment implements  FileAdapter.OnFileClickListener<RF> {
    private AtomicBoolean mDoubleBackPressedOnce = new AtomicBoolean(false);
    private Handler mHandler = new Handler();

    private final Runnable mBackPressdRunnable = new Runnable() {
        @Override
        public void run() {
            mDoubleBackPressedOnce.set(false);
        }
    };

    protected SP mStorageProvider;
    protected FileAdapter<RF> mCollectionAdapter;
    protected Credential mCredential;
    protected RF mCurrentDirectory;
    protected Stack<BrowserState<RF>> mBackwardStack = new Stack<>();
    protected Queue<BrowserState<RF>> mForwardQueue = new ArrayDeque<>();

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.breadCrumbs)
    BreadCrumbLayout mBreadCrumbLayout;

    List<RF> mFiles = new ArrayList<RF>();
    Comparator<AbstractFile> mFileComparator;

    @Bind(R.id.fragment_storageprovider_recyclerview_files)
    RecyclerView mCollectionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null)
            mCredential = bundle.getParcelable(DataContract.ARG_CREDENTIAL);
        mFileComparator = FileSorter.FileNameComparator.getInstance(true);
        mCollectionAdapter = new FileAdapter<>(getActivity(), mFiles);
        mCollectionAdapter.setOnFileClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_storageprovider, container, false);
        ButterKnife.bind(this, fragmentView);
        setupToolbar();
        setupRecyclerView();
        setupBreadCrumb();
        loadDefaultDirectory();
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
                        if(!mBackwardStack.empty()) {
                            if (!StorageProviderFragment.this.mDoubleBackPressedOnce.get()) {
                                BrowserState<RF> currentState = mBackwardStack.pop();
                                mCurrentDirectory = currentState.currentDirectory;
                                //loadDirectory(mCurrentDirectory);
                                mCollectionAdapter.replaceWith(currentState.files);
                                LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getLayoutManager();
                                manager.scrollToPositionWithOffset(currentState.scrollPosition, (int) currentState.scrollOffset);
                                StorageProviderFragment.this.mDoubleBackPressedOnce.set(true);
                                mHandler.postDelayed(mBackPressdRunnable, 400);
                                if(mBreadCrumbLayout.popHistory()) {
                                    switchDirectory(mBreadCrumbLayout.lastHistory(), true, false);
                                }
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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_drawer_menu);
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
                        /*Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
                        ((MediaFragment) frag).jumpToTop(true);*/
                    } else {
                        for(int i = 0; i < mBackwardStack.size(); i++) {
                            if(mBackwardStack.get(i).currentDirectory.getAbsolutePath().equals(crumb.getPath())) {
                                loadDirectory(mBackwardStack.get(i).currentDirectory, true);
                                switchDirectory(crumb, crumb.getPath() == null, false);
                                break;
                            }
                        }
                    }
                }
            }
        });
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mHandler != null) { mHandler.removeCallbacks(mBackPressdRunnable); }
    }

    private void setupRecyclerView() {
        mCollectionView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCollectionView.setHasFixedSize(true);
        mCollectionAdapter.replaceWith(mFiles);
        mCollectionView.setAdapter(mCollectionAdapter);
    }

    protected void loadDefaultDirectory() {
        mCurrentDirectory = mStorageProvider.getRootDirectory();
        mBreadCrumbLayout.setTopPath(mCurrentDirectory.getAbsolutePath());
        loadDirectory(mCurrentDirectory, false);
    }

    protected abstract SP buildStorageProvider(Credential credential);

    @Override
    public void onFileClick(View view, int position, RF file) {
        if(file.isDirectory()) {
            loadDirectory(file, true);
        } else {
            openFile(file);
        }
    }

    @Override
    public void onFileLongClick(View view, int position, RF file) {

    }

    protected void openFile(RF file) {

    }

    protected void loadDirectory(RF file, boolean saveStack) {
        if(saveStack) {
            mBackwardStack.push(saveBrowserState());
        }
        mCurrentDirectory = file;
        switchDirectory(file.getAbsolutePath());

        List<RF> files = mStorageProvider.list(file);
        handleFileSort(files);
        handleHiddenFile(files);
        mCollectionAdapter.replaceWith(files);
    }

    public void switchDirectory(String path) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        boolean initialCreate = (path == null);
        if (initialCreate) {
            // Initial directory
            path = mCurrentDirectory.getAbsolutePath();
            mBreadCrumbLayout.setTopPath(path);
        }

        Crumb crumb = new Crumb(getContext(), path);
        switchDirectory(crumb, initialCreate, true);
    }

    public void switchDirectory(Crumb crumb, boolean forceRecreate, boolean addToHistory) {
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

    protected void handleFileSort(List<RF> files) {
        Collections.sort(files, mFileComparator);
    }

    protected void handleHiddenFile(List<RF> files) {
        for(Iterator<RF> iterator = files.iterator(); iterator.hasNext(); ) {
            RF file = iterator.next();
            if(file.getName().startsWith("."))
                iterator.remove();
        }
    }

    private BrowserState<RF> saveBrowserState() {
        LinearLayoutManager manager = (LinearLayoutManager) mCollectionView.getLayoutManager();
        int firstItem = manager.findFirstVisibleItemPosition();
        View firstItemView = manager.findViewByPosition(firstItem);
        float topOffset = firstItemView.getTop();
        return new BrowserState<>(
                mCurrentDirectory,
                new ArrayList<>(mFiles),
                firstItem,
                topOffset
        );
    }

    private static class BrowserState<RF> {
        public RF currentDirectory;
        public List<RF> files;
        public int scrollPosition;
        public float scrollOffset;

        public BrowserState() {

        }

        public BrowserState(RF currentDirectory, List<RF> files, int scrollPosition, float scrollOffset) {
            this.files = files;
            this.currentDirectory = currentDirectory;
            this.scrollPosition = scrollPosition;
            this.scrollOffset = scrollOffset;
        }
    }
}
