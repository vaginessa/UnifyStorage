package org.cryse.unifystorage.explorer.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.cryse.unifystorage.AbstractFile;
import org.cryse.unifystorage.RemoteFileDownloader;
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
    private DirectoryInfo<RF, List<RF>> mDirectory;
    private List<RF> mHiddenFiles;
    private DataListener<RF, CR> mDataListener;
    private Subscription mLoadFilesSubscription;
    private Subscription mBuildStorageProviderSubscription;
    private Subscription mDownloadFileSubscription;

    private RxStorageProvider<RF, CR, SP> mStorageProvider;
    private Comparator<AbstractFile> mFileComparator;
    protected Stack<BrowserState<RF>> mBackwardStack = new Stack<>();
    private boolean mShowHiddenFile = false;
    private StorageProviderBuilder<RF, CR, SP> mProviderBuilder;
    protected StorageProviderDatabase mStorageProviderDatabase;
    private CR mCredential;
    private int mStorageProviderRecordId = DataContract.CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID;
    private StorageProviderRecord mStorageProviderRecord;

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
        this.mStorageProviderRecord = mStorageProviderDatabase.getSavedStorageProvider(mStorageProviderRecordId);
    }

    public void updateCredential(CR newCredential) {
        if(mDataListener != null)
            mDataListener.onCredentialRefreshed(newCredential);
        if(mStorageProviderDatabase != null && mStorageProviderRecordId != DataContract.CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID) {
            mStorageProviderRecord.setCredentialData(newCredential.persist());
            mStorageProviderDatabase.updateStorageProviderRecord(mStorageProviderRecord);
        }
    }

    public void loadFiles(RF parent) {
        mProgressVisibility.set(View.VISIBLE);
        mRecyclerViewVisibility.set(View.INVISIBLE);
        mInfoMessageVisibility.set(View.INVISIBLE);
        RxSubscriptionUtils.checkAndUnsubscribe(mLoadFilesSubscription);
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        Observable<DirectoryInfo<RF, List<RF>>> listObservable = parent == null ? mStorageProvider.list() : mStorageProvider.list(parent);
        mLoadFilesSubscription = listObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<DirectoryInfo<RF, List<RF>>>() {
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
                    public void onNext(DirectoryInfo<RF, List<RF>> files) {
                        Log.i(TAG, "Files loaded " + files);
                        mHiddenFiles = new ArrayList<RF>();
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

    protected void handleFileSort(DirectoryInfo<RF, List<RF>> directory) {
        Collections.sort(directory.files, mFileComparator);
    }

    protected void handleHiddenFile(DirectoryInfo<RF, List<RF>> directory) {
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
        if(mDirectory != null) {
            handleHiddenFile(mDirectory);
            handleFileSort(mDirectory);
            if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory);
        }
    }

    public void onFileClick(RF file, CollectionViewState collectionViewState) {
        if (file.isDirectory()) {
            mBackwardStack.push(new BrowserState<RF>(mDirectory, collectionViewState, mHiddenFiles));
            loadFiles(file);
        } else {
            if(file.needsDownload()) {
                downloadFile(file);
            } else {
                openFile(file);
            }
        }
    }

    public void onFileLongClick(RF file) {

    }

    private void downloadFile(final RF file) {
        final String fileName = file.getName();
        final long fileSize = file.size();
        final String localPath = LocalCachesUtils.<RF>getFullCachePath(
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
                RemoteFileDownloader<RF> downloader = null;
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
        mBackwardStack.push(new BrowserState<RF>(mDirectory, collectionViewState, mHiddenFiles));
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
            mHiddenFiles = currentState.hiddenFiles;
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

    public Observable<Pair<RF,Boolean>> deleteFile(final RF...files) {
        return mStorageProvider.deleteFile(files[0]);
    }

    public void deleteFiles(final RF...files) {
        UnifyStorageApplication application = UnifyStorageApplication.get(mContext);
        mStorageProvider.deleteFile(files).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Subscriber<Pair<RF, Boolean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        String resultToast = String.format("Delete failed: %s", e.getMessage());
                        Log.e("DeleteFile", resultToast);
                        Toast.makeText(mContext, resultToast, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Pair<RF, Boolean> result) {
                        String resultToast = String.format("Delete %s %s", result.first.getName(), result.second ? "success" : "failed");
                        Log.e("DeleteFile", resultToast);
                        Toast.makeText(mContext, resultToast, Toast.LENGTH_SHORT).show();
                        int position = 0;
                        for (Iterator<RF> iterator = mDirectory.files.iterator(); iterator.hasNext();) {
                            RF rf = iterator.next();
                            if (rf.getId().compareTo(result.first.getId()) == 0 && result.second) {
                                // Remove the current element from the iterator and the list.
                                iterator.remove();
                                if(mDataListener != null) {
                                    mDataListener.onDirectoryItemDelete(position);
                                }
                            }
                            position++;
                        }
                    }
                });
    }

    @Override
    public void destroy() {
        RxSubscriptionUtils.checkAndUnsubscribe(mLoadFilesSubscription);
        RxSubscriptionUtils.checkAndUnsubscribe(mBuildStorageProviderSubscription);
        RxSubscriptionUtils.checkAndUnsubscribe(mDownloadFileSubscription);
        mStorageProviderDatabase.destroy();
        mContext = null;
        mDataListener = null;
    }

    public interface DataListener<RF extends RemoteFile, CR extends Credential> {
        void onDirectoryChanged(DirectoryInfo<RF, List<RF>> directory);
        void onDirectoryItemDelete(int position);
        void onCollectionViewStateRestore(CollectionViewState collectionViewState);
        void onCredentialRefreshed(CR credential);
        void onShowDownloadDialog(String fileName, long fileSize, DialogInterface.OnDismissListener dismissListener);
        void onUpdateDownloadDialog(String fileName, long readSize, long fileSize);
        void onDismissDownloadDialog();
        void onShowErrorDialog(String title, String content);
    }
}
