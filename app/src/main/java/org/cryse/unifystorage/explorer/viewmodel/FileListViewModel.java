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
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.UnifyStorageApplication;
import org.cryse.unifystorage.explorer.data.StorageProviderDatabase;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.BrowserState;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.OpenFileUtils;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;
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
        CR extends Credential,
        SP extends StorageProvider<RF, CR>
        > implements ViewModel {
    private static final String TAG = FileListViewModel.class.getCanonicalName();

    public ObservableInt mInfoMessageVisibility;
    public ObservableInt mProgressVisibility;
    public ObservableInt mRecyclerViewVisibility;
    public ObservableField<String> mInfoMessage;

    private Context mContext;
    private DirectoryPair<RF, List<RF>> mDirectory;
    private List<RF> mHiddenFiles;
    private DataListener<RF, CR> mDataListener;
    private Subscription mLoadFilesSubscription;
    private Subscription mBuildStorageProviderSubscription;

    private RxStorageProvider<RF, CR, SP> mStorageProvider;
    private Comparator<AbstractFile> mFileComparator;
    protected Stack<BrowserState<RF>> mBackwardStack = new Stack<>();
    private boolean mShowHiddenFile = false;
    private StorageProviderBuilder<RF, CR, SP> mProviderBuilder;
    protected StorageProviderDatabase mStorageProviderDatabase;
    private CR mCredential;
    private int mStorageProviderRecordId = DataContract.CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID;

    public FileListViewModel(
            Context context,
            CR credential,
            StorageProviderBuilder<RF, CR, SP> providerBuilder,
            DataListener<RF, CR> dataListener) {
        this.mContext = context;
        this.mDataListener = dataListener;
        this.mInfoMessageVisibility = new ObservableInt(View.VISIBLE);
        this.mProgressVisibility = new ObservableInt(View.INVISIBLE);
        this.mRecyclerViewVisibility = new ObservableInt(View.INVISIBLE);
        this.mInfoMessage = new ObservableField<>(context.getString(R.string.info_message_empty_directory));
        this.mFileComparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
        this.mCredential = credential;
        this.mProviderBuilder = providerBuilder;
        // this.mStorageProvider = new RxStorageProvider<>(providerBuilder.buildStorageProvider(credential));
        this.mHiddenFiles = new ArrayList<>();
        this.mStorageProviderDatabase = new StorageProviderDatabase(mContext);
        buildStorageProvider();
    }

    public void setDataListener(DataListener<RF, CR> dataListener) {
        this.mDataListener = dataListener;
    }

    public void buildStorageProvider() {
        mProgressVisibility.set(View.VISIBLE);
        mRecyclerViewVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.INVISIBLE);
        RxSubscriptionUtils.checkAndUnsubscribe(mBuildStorageProviderSubscription);
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        mBuildStorageProviderSubscription = Observable.create(new Observable.OnSubscribe<SP>() {
            @Override
            public void call(Subscriber<? super SP> subscriber) {
                try {
                    subscriber.onNext(mProviderBuilder.buildStorageProvider(mCredential));
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        })
                .subscribeOn(application.defaultSubscribeScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SP>() {
                    @Override
                    public void onCompleted() {
                        loadFiles(null);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SP sp) {
                        mStorageProvider = new RxStorageProvider<>(sp);
                        if(mStorageProvider.shouldRefreshCredential()) {
                            updateCredential(mStorageProvider.getRefreshedCredential());
                        }
                    }
                });
    }

    public void setStorageProviderRecordId(int id) {
        this.mStorageProviderRecordId = id;
    }

    public void updateCredential(CR newCredential) {
        if(mDataListener != null)
            mDataListener.onCredentialRefreshed(newCredential);
        if(mStorageProviderDatabase != null && mStorageProviderRecordId != DataContract.CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID) {
            StorageProviderRecord record = mStorageProviderDatabase.getSavedStorageProvider(mStorageProviderRecordId);
            record.setCredentialData(newCredential.persist());
            mStorageProviderDatabase.updateStorageProviderRecord(record);
        }
    }

    public void loadFiles(RF parent) {
        mProgressVisibility.set(View.VISIBLE);
        mRecyclerViewVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.INVISIBLE);
        // mStorageProviderLatch.await();
        RxSubscriptionUtils.checkAndUnsubscribe(mLoadFilesSubscription);
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        Observable<DirectoryPair<RF, List<RF>>> listObservable = parent == null ? mStorageProvider.list() : mStorageProvider.list(parent);
        mLoadFilesSubscription = listObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<DirectoryPair<RF, List<RF>>>() {
                    @Override
                    public void onCompleted() {
                        if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory);
                        mProgressVisibility.set(View.INVISIBLE);
                        toggleRecyclerViewsSuccessState();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "Error loading files.", error);
                        toggleRecyclerViewsErrorState(error);
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

    protected void toggleRecyclerViewsSuccessState() {
        if (mDirectory != null && !mDirectory.files.isEmpty()) {
            mRecyclerViewVisibility.set(View.VISIBLE);
        } else {
            mInfoMessage.set(mContext.getString(R.string.info_message_empty_directory));
            mInfoMessageVisibility.set(View.VISIBLE);
        }
    }

    protected void toggleRecyclerViewsErrorState(Throwable throwable) {
        mProgressVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.VISIBLE);
    }

    protected void handleFileSort(DirectoryPair<RF, List<RF>> directory) {
        Collections.sort(directory.files, mFileComparator);
    }

    protected void handleHiddenFile(DirectoryPair<RF, List<RF>> directory) {
        if (!mShowHiddenFile) {
            for (Iterator<RF> iterator = directory.files.iterator(); iterator.hasNext(); ) {
                RF file = iterator.next();
                if (file.getName().startsWith(".")) {
                    mHiddenFiles.add(file);
                    iterator.remove();
                }
            }
        } else {
            if (!mHiddenFiles.isEmpty()) {
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
        if (file.isDirectory()) {
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
        for (int i = 0; i < mBackwardStack.size(); i++) {
            if (mBackwardStack.get(i).directory.directory.getPath().equals(targetPath)) {
                this.mDirectory = mBackwardStack.get(i).directory;
                mDataListener.onDirectoryChanged(mBackwardStack.get(i).directory);
                toggleRecyclerViewsSuccessState();
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
            toggleRecyclerViewsSuccessState();
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
        RxSubscriptionUtils.checkAndUnsubscribe(mLoadFilesSubscription);
        RxSubscriptionUtils.checkAndUnsubscribe(mBuildStorageProviderSubscription);
        mStorageProviderDatabase.destroy();
        mContext = null;
        mDataListener = null;
    }

    public interface DataListener<RF extends RemoteFile, CR extends Credential> {
        void onDirectoryChanged(DirectoryPair<RF, List<RF>> directory);
        void onCollectionViewStateRestore(CollectionViewState collectionViewState);
        void onCredentialRefreshed(CR credential);
    }
}
