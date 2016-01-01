package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.util.Log;
import android.view.View;

import org.cryse.unifystorage.AbstractFile;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.UnifyStorageApplication;
import org.cryse.unifystorage.explorer.utils.BrowserState;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.OpenFileUtils;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.io.comparator.NameFileComparator;
import org.cryse.unifystorage.utils.DirectoryPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class FileListViewModel<
        RF extends RemoteFile,
        SP extends StorageProvider<RF>,
        CR extends Credential
        > implements ViewModel {
    private static final String TAG = FileListViewModel.class.getCanonicalName();

    public ObservableInt mInfoMessageVisibility;
    public ObservableInt mProgressVisibility;
    public ObservableInt mRecyclerViewVisibility;
    public ObservableField<String> mInfoMessage;

    private Context mContext;
    private DirectoryPair<RF, List<RF>> mDirectory;
    private List<RF> mHiddenFiles;
    private DataListener<RF> mDataListener;
    private Subscription mSubscription;
    private RxStorageProvider<RF,SP> mStorageProvider;
    private Comparator<AbstractFile> mFileComparator;
    protected Stack<BrowserState<RF>> mBackwardStack = new Stack<>();
    private boolean mShowHiddenFile = false;

    public FileListViewModel(
            Context context,
            CR credential,
            StorageProviderBuilder<RF, SP, CR> providerBuilder,
            DataListener<RF> dataListener) {
        this.mContext = context;
        this.mDataListener = dataListener;
        this.mInfoMessageVisibility = new ObservableInt(View.VISIBLE);
        this.mProgressVisibility = new ObservableInt(View.INVISIBLE);
        this.mRecyclerViewVisibility = new ObservableInt(View.INVISIBLE);
        this.mInfoMessage = new ObservableField<>(context.getString(R.string.info_message_empty_directory));
        this.mFileComparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
        this.mStorageProvider = new RxStorageProvider<>(providerBuilder.buildStorageProvider(credential));
        this.mHiddenFiles = new ArrayList<>();
    }

    public void setDataListener(DataListener<RF> dataListener) {
        this.mDataListener = dataListener;
    }

    public void loadFiles(RF parent) {
        mProgressVisibility.set(View.VISIBLE);
        mRecyclerViewVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.INVISIBLE);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        Observable<DirectoryPair<RF, List<RF>>> listObservable = parent == null ? mStorageProvider.list() : mStorageProvider.list(parent);
        mSubscription = listObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<DirectoryPair<RF, List<RF>>>() {
                    @Override
                    public void onCompleted() {
                        if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory);
                        mProgressVisibility.set(View.INVISIBLE);
                        if (mDirectory != null && !mDirectory.files.isEmpty()) {
                            mRecyclerViewVisibility.set(View.VISIBLE);
                        } else {
                            mInfoMessage.set(mContext.getString(R.string.info_message_empty_directory));
                            mInfoMessageVisibility.set(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "Error loading GitHub files ", error);
                        mProgressVisibility.set(View.INVISIBLE);
                        /*if (isHttp404(error)) {
                            mInfoMessage.set(mContext.getString(R.string.error_username_not_found));
                        } else {
                            mInfoMessage.set(mContext.getString(R.string.error_loading_repos));
                        }*/
                        mInfoMessageVisibility.set(View.VISIBLE);
                    }

                    @Override
                    public void onNext(DirectoryPair<RF, List<RF>> files) {
                        Log.i(TAG, "Files loaded " + files);
                        mHiddenFiles.clear();
                        handleFileSort(files);
                        handleHiddenFile(files);
                        FileListViewModel.this.mDirectory = files;
                    }
                });
    }

    protected void handleFileSort(DirectoryPair<RF, List<RF>> directory) {
        Collections.sort(directory.files, mFileComparator);
    }

    protected void handleHiddenFile(DirectoryPair<RF, List<RF>> directory) {
        if(!mShowHiddenFile) {
            for(Iterator<RF> iterator = directory.files.iterator(); iterator.hasNext(); ) {
                RF file = iterator.next();
                if(file.getName().startsWith(".")) {
                    mHiddenFiles.add(file);
                    iterator.remove();
                }
            }
        } else {
            if(!mHiddenFiles.isEmpty()) {
                mDirectory.files.addAll(mHiddenFiles);
                mHiddenFiles.clear();
            }
        }
    }

    public void setShowHiddenFiles(boolean show) {
        this.mShowHiddenFile = show;
        handleHiddenFile(mDirectory);
        handleFileSort(mDirectory);
        if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory);
    }

    public void onFileClick(RF file, CollectionViewState collectionViewState) {
        if(file.isDirectory()) {
            mBackwardStack.push(new BrowserState<RF>(mDirectory, collectionViewState));
            loadFiles(file);
        } else {
            openFile(file);
        }
    }

    public void onFileLongClick(RF file) {

    }

    public boolean isAtTopPaht() {
        return mBackwardStack.empty();
    }

    public void jumpBack(String targetPath, CollectionViewState collectionViewState) {
        mBackwardStack.push(new BrowserState<RF>(mDirectory, collectionViewState));
        for(int i = 0; i < mBackwardStack.size(); i++) {
            if(mBackwardStack.get(i).directory.directory.getPath().equals(targetPath)) {
                this.mDirectory = mBackwardStack.get(i).directory;
                mDataListener.onDirectoryChanged(mBackwardStack.get(i).directory);
                mDataListener.onCollectionViewStateRestore(mBackwardStack.get(i).collectionViewState);
                break;
            }
        }
    }

    public boolean onBackPressed() {
        if (!mBackwardStack.empty()) {
            BrowserState<RF> currentState = mBackwardStack.pop();
            mDirectory = currentState.directory;
            this.mDataListener.onDirectoryChanged(mDirectory);
            this.mDataListener.onCollectionViewStateRestore(currentState.collectionViewState);
            return true;
        } else {
            return false;
        }
    }

    private void openFile(RF file) {
        OpenFileUtils.openFile(mContext, file.getPath(), true);
    }

    @Override
    public void destroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        mSubscription = null;
        mContext = null;
        mDataListener = null;
    }

    public interface DataListener<RF extends RemoteFile> {
        void onDirectoryChanged(DirectoryPair<RF, List<RF>> directory);
        void onCollectionViewStateRestore(CollectionViewState collectionViewState);
    }
}
