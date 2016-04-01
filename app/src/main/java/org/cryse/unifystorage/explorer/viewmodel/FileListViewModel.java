package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import org.apache.commons.io.FileUtils;
import org.cryse.unifystorage.AbstractFile;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.application.UnifyStorageApplication;
import org.cryse.unifystorage.explorer.data.UnifyStorageDatabase;
import org.cryse.unifystorage.explorer.event.FileDeleteEvent;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.utils.BrowserState;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.LocalCachesUtils;
import org.cryse.unifystorage.explorer.utils.OpenFileUtils;
import org.cryse.unifystorage.explorer.utils.RxSubscriptionUtils;
import org.cryse.unifystorage.explorer.utils.StorageProviderBuilder;
import org.cryse.unifystorage.io.ProgressInputStream;
import org.cryse.unifystorage.io.StreamProgressListener;
import org.cryse.unifystorage.io.comparator.NameFileComparator;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.IOUtils;
import org.cryse.unifystorage.utils.Path;

import java.io.File;
import java.io.IOException;
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

public class FileListViewModel implements ViewModel {
    private static final String TAG = FileListViewModel.class.getCanonicalName();

    public ObservableInt mInfoMessageVisibility;
    public ObservableInt mProgressVisibility;
    public ObservableInt mRecyclerViewVisibility;
    public ObservableField<String> mInfoMessage;

    private Context mContext;
    private DirectoryInfo mDirectory;
    private List<RemoteFile> mHiddenFiles;
    private DataListener mDataListener;
    private Subscription mLoadFilesSubscription;
    private Subscription mCreateFolderSubscription;
    private Subscription mDownloadFileSubscription;

    private RxStorageProvider mStorageProvider;
    private Comparator<AbstractFile> mFileComparator;
    protected Stack<BrowserState> mBackwardStack = new Stack<>();
    private boolean mShowHiddenFile = false;
    private StorageProviderBuilder mProviderBuilder;
    protected UnifyStorageDatabase mUnifyStorageDatabase;
    private Credential mCredential;
    private int mStorageProviderRecordId = DataContract.CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID;
    private StorageProviderRecord mStorageProviderRecord;

    public FileListViewModel(
            Context context,
            int storageProviderRecordId,
            Credential credential,
            StorageProviderBuilder providerBuilder,
            DataListener dataListener) {
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
        this.mUnifyStorageDatabase = UnifyStorageDatabase.getInstance();
        this.mStorageProviderRecordId = storageProviderRecordId;
        this.mStorageProviderRecord = mUnifyStorageDatabase.getSavedStorageProvider(mStorageProviderRecordId);
    }

    public void setDataListener(DataListener dataListener) {
        this.mDataListener = dataListener;
    }

    public void buildStorageProvider() {
        mProgressVisibility.set(View.VISIBLE);
        mRecyclerViewVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.INVISIBLE);

        StorageProvider storageProvider = mProviderBuilder.buildStorageProvider(mCredential);
        mStorageProvider = new RxStorageProvider(storageProvider);
        storageProvider.setOnTokenRefreshListener(new StorageProvider.OnTokenRefreshListener() {
            @Override
            public void onTokenRefresh(Credential refreshedCredential) {
                if (mUnifyStorageDatabase != null) {
                    mStorageProviderRecord.setCredentialData(refreshedCredential.persist());
                    Handler mainHandler = new Handler(mContext.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mUnifyStorageDatabase.updateStorageProviderRecord(mStorageProviderRecord);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                FileListViewModel.this.mCredential = refreshedCredential;
                mDataListener.onCredentialRefreshed(refreshedCredential);
            }
        });
        if(mDataListener != null)
            mDataListener.onStorageProviderReady();
        loadFiles(null);
    }

    public void loadFiles(RemoteFile parent) {
        mProgressVisibility.set(View.VISIBLE);
        mRecyclerViewVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.INVISIBLE);
        RxSubscriptionUtils.checkAndUnsubscribe(mLoadFilesSubscription);
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        Observable<DirectoryInfo> listObservable = parent == null ? mStorageProvider.list() : mStorageProvider.list(parent);
        mLoadFilesSubscription = listObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<DirectoryInfo>() {
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
                    public void onNext(DirectoryInfo files) {
                        Log.i(TAG, "Files loaded " + files);
                        mHiddenFiles = new ArrayList<RemoteFile>();
                        handleFileSort(files);
                        handleHiddenFile(files);
                        FileListViewModel.this.mDirectory = files;
                    }
                });
    }

    public void createDirectory(RemoteFile parent, String name) {
        RxSubscriptionUtils.checkAndUnsubscribe(mCreateFolderSubscription);
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        mCreateFolderSubscription = mStorageProvider.createDirectory(parent, name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<RemoteFile>() {
                    @Override
                    public void onCompleted() {
                        if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory);
                    }

                    @Override
                    public void onError(Throwable error) {
                    }

                    @Override
                    public void onNext(RemoteFile newDirectory) {
                        Log.i(TAG, "Files loaded " + newDirectory);
                        // Here need some improvement.
                        FileListViewModel.this.mDirectory.files.add(newDirectory);
                        FileListViewModel.this.mDirectory.files.addAll(mHiddenFiles);
                        mHiddenFiles.clear();
                        handleFileSort(FileListViewModel.this.mDirectory);
                        handleHiddenFile(FileListViewModel.this.mDirectory);
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

    protected void handleFileSort(DirectoryInfo directory) {
        Collections.sort(directory.files, mFileComparator);
    }

    protected void handleHiddenFile(DirectoryInfo directory) {
        if (!mShowHiddenFile) {
            for (Iterator<RemoteFile> iterator = directory.files.iterator(); iterator.hasNext(); ) {
                RemoteFile file = iterator.next();
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
        if(mDirectory != null) {
            handleHiddenFile(mDirectory);
            handleFileSort(mDirectory);
            if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory);
        }
    }

    public void onFileClick(RemoteFile file, CollectionViewState collectionViewState) {
        if (file.isDirectory()) {
            mBackwardStack.push(new BrowserState(mDirectory, collectionViewState, mHiddenFiles));
            loadFiles(file);
        } else {
            if(file.needsDownload()) {
                downloadFile(file);
            } else {
                openFile(file);
            }
        }
    }

    public void onFileLongClick(RemoteFile file) {

    }

    private void downloadFile(final RemoteFile file) {
        final String fileName = file.getName();
        final long fileSize = file.size();
        final String localPath = LocalCachesUtils.<RemoteFile>getFullCachePath(
                mContext,
                mStorageProvider.getStorageProviderName(),
                mStorageProviderRecord.getUuid(),
                file
        );

        if(mDataListener != null) {
            mDataListener.onShowDownloadDialog(file.getName(), fileSize, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    RxSubscriptionUtils.checkAndUnsubscribe(mDownloadFileSubscription);
                }
            });
        }

        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        RxSubscriptionUtils.checkAndUnsubscribe(mDownloadFileSubscription);
        Observable<Long> downloadObservable = Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(final Subscriber<? super Long> subscriber) {
                RemoteFileDownloader downloader = null;
                try {
                    downloader = mStorageProvider.getStorageProvider().download(file);
                    File targetFile = new File(localPath);
                    String parentDirectoryPath = Path.getDirectory(localPath);
                    if(parentDirectoryPath != null) {
                        File parentDirectory = new File(parentDirectoryPath);
                        if(!parentDirectory.exists()) parentDirectory.mkdirs();
                    }
                    if(!targetFile.exists()) targetFile.createNewFile();
                    if(!targetFile.canWrite()) throw new IOException("Target file not writable.");
                    ProgressInputStream progressInputStream = downloader.getProgressDataStream();
                    progressInputStream.addListener(new StreamProgressListener() {
                        @Override
                        public void onProgress(ProgressInputStream stream, long current, long total, double rate) {
                            subscriber.onNext(current);
                        }
                    });
                    FileUtils.copyInputStreamToFile(downloader.getDataStream(), targetFile);
                    progressInputStream.removeAllListener();
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                } finally {
                    if(downloader != null)
                        IOUtils.safeClose(downloader.getDataStream());
                }
            }
        });

        mDownloadFileSubscription = downloadObservable
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        if(mDataListener != null) {
                            mDataListener.onDismissDownloadDialog();
                        }
                        Uri fileUri = FileProvider.getUriForFile(
                                mContext,
                                mContext.getString(R.string.authority_file_provider),
                                new File(localPath));
                        OpenFileUtils.openFile(mContext, fileUri, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(mDataListener != null) {
                            mDataListener.onDismissDownloadDialog();
                            mDataListener.onShowErrorDialog(
                                    mContext.getString(R.string.dialog_title_error),
                                    mContext.getString(R.string.dialog_content_error_open_file)
                            );
                        }
                    }

                    @Override
                    public void onNext(Long readSize) {
                        if(mDataListener != null) {
                            mDataListener.onUpdateDownloadDialog(fileName, readSize, fileSize);
                        }
                    }
                });
    }

    public boolean isAtTopPath() {
        return mBackwardStack.empty();
    }

    public void jumpBack(String targetPath, CollectionViewState collectionViewState) {
        mBackwardStack.push(new BrowserState(mDirectory, collectionViewState, mHiddenFiles));
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
            BrowserState currentState = mBackwardStack.pop();
            mDirectory = currentState.directory;
            handleHiddenFile(mDirectory);
            this.mDataListener.onDirectoryChanged(mDirectory);
            toggleRecyclerViewsSuccessState();
            this.mDataListener.onCollectionViewStateRestore(currentState.collectionViewState);
            return true;
        } else {
            return false;
        }
    }

    public DirectoryInfo getDirectory() {
        return mDirectory;
    }

    private void openFile(RemoteFile file) {
        OpenFileUtils.openFile(mContext, file.getPath(), true);
    }

    public void onDeleteFileEvent(FileDeleteEvent event) {
        for(Iterator<BrowserState> browserStateIterator = mBackwardStack.iterator(); browserStateIterator.hasNext();) {
            BrowserState browserState = browserStateIterator.next();
            if (browserState.directory.directory.getId().compareTo(event.fileId) == 0 && event.success) {
                // Remove the current element from the iterator and the list.
                browserStateIterator.remove();
            } else {
                for (Iterator<RemoteFile> iterator = browserState.directory.files.iterator(); iterator.hasNext();) {
                    RemoteFile rf = iterator.next();
                    if (rf.getId().compareTo(event.fileId) == 0 && event.success) {
                        // Remove the current element from the iterator and the list.
                        iterator.remove();
                    }
                }
            }
        }
        if (event.targetId.compareTo(mDirectory.directory.getId()) == 0) {
            int position = 0;
            for (Iterator<RemoteFile> iterator = mDirectory.files.iterator(); iterator.hasNext(); ) {
                RemoteFile rf = iterator.next();
                if (rf.getId().compareTo(event.fileId) == 0 && event.success) {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();
                    if (mDataListener != null) {
                        mDataListener.onDirectoryItemDelete(position);
                    }
                }
                position++;
            }
        }
    }

    @Override
    public void destroy() {
        RxSubscriptionUtils.checkAndUnsubscribe(mLoadFilesSubscription);
        RxSubscriptionUtils.checkAndUnsubscribe(mDownloadFileSubscription);
        mContext = null;
        mDataListener = null;
    }

    public RxStorageProvider getStorageProvider() {
        return mStorageProvider;
    }

    public interface DataListener {
        void onStorageProviderReady();
        void onDirectoryChanged(DirectoryInfo directory);
        void onDirectoryItemDelete(int position);
        void onCollectionViewStateRestore(CollectionViewState collectionViewState);
        void onCredentialRefreshed(Credential credential);
        void onShowDownloadDialog(String fileName, long fileSize, DialogInterface.OnDismissListener dismissListener);
        void onUpdateDownloadDialog(String fileName, long readSize, long fileSize);
        void onDismissDownloadDialog();
        void onShowErrorDialog(String title, String content);
    }
}
