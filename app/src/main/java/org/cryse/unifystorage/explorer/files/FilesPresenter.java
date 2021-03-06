package org.cryse.unifystorage.explorer.files;


import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.cryse.unifystorage.AbstractFile;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RxStorageProvider;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.application.StorageProviderManager;
import org.cryse.unifystorage.explorer.base.BasePresenter;
import org.cryse.unifystorage.explorer.event.NewTaskEvent;
import org.cryse.unifystorage.explorer.event.RxEventBus;
import org.cryse.unifystorage.explorer.executor.PostExecutionThread;
import org.cryse.unifystorage.explorer.executor.ThreadExecutor;
import org.cryse.unifystorage.explorer.interactor.CreateFolderUseCase;
import org.cryse.unifystorage.explorer.interactor.DefaultSubscriber;
import org.cryse.unifystorage.explorer.interactor.GetFileUseCase;
import org.cryse.unifystorage.explorer.interactor.GetFilesUseCase;
import org.cryse.unifystorage.explorer.interactor.UseCase;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.model.StorageProviderRecord;
import org.cryse.unifystorage.explorer.service.task.CreateFolderTask;
import org.cryse.unifystorage.explorer.service.task.DeleteTask;
import org.cryse.unifystorage.explorer.service.task.DownloadTask;
import org.cryse.unifystorage.explorer.utils.BrowserState;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.MimeUtils;
import org.cryse.unifystorage.explorer.utils.cache.FileCacheRepository;
import org.cryse.unifystorage.io.comparator.NameFileComparator;
import org.cryse.unifystorage.providers.localstorage.LocalStorageProvider;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.Path;

import java.io.File;
import java.util.Comparator;
import java.util.Stack;

public class FilesPresenter implements FilesContract.Presenter {
    private final FilesContract.View mFilesView;
    // private int mStorageProviderRecordId = DataContract.CONST_EMPTY_STORAGE_PROVIDER_RECORD_ID;
    // private Credential mCredential;
    private StorageProviderInfo mStorageProviderInfo;

    private StorageProviderRecord mStorageProviderRecord;

    private final RxStorageProvider mRxStorageProvider;
    protected ThreadExecutor mThreadExecutor;
    protected PostExecutionThread mPostExecutionThread;
    protected FileCacheRepository mFileCacheRepository;
    private GetFilesUseCase mGetFilesUseCase;
    private GetFileUseCase mGetFileUseCase;
    private CreateFolderUseCase mCreateFolderUseCase;
    private boolean mFirstLoad = true;
    private boolean mShowHiddenFile = false;
    public DirectoryInfo mDirectory;
    private Comparator<AbstractFile> mFileComparator;
    protected Stack<BrowserState> mBackwardStack = new Stack<>();

    protected FilesPresenter(
            FilesContract.View filesView,
            StorageProviderInfo storageProviderInfo,
            StorageProvider storageProvider,
            ThreadExecutor threadExecutor,
            PostExecutionThread postExecutionThread,
            FileCacheRepository fileCacheRepository
    ) {
        this.mFilesView = filesView;
        this.mRxStorageProvider = new RxStorageProvider(storageProvider);
        this.mStorageProviderInfo = storageProviderInfo;
        this.mStorageProviderRecord = StorageProviderManager.instance().loadStorageProviderRecord(storageProviderInfo.getStorageProviderId());
        this.mThreadExecutor = threadExecutor;
        this.mPostExecutionThread = postExecutionThread;
        this.mFileCacheRepository = fileCacheRepository;
        this.mFileComparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;

        this.mGetFilesUseCase = new GetFilesUseCase(mRxStorageProvider, mThreadExecutor, mPostExecutionThread);
        this.mGetFileUseCase = new GetFileUseCase(mRxStorageProvider, mThreadExecutor, mPostExecutionThread);
        this.mCreateFolderUseCase = new CreateFolderUseCase(mRxStorageProvider, mThreadExecutor, mPostExecutionThread);

        this.mRxStorageProvider.getStorageProvider().setOnTokenRefreshListener(new StorageProvider.OnTokenRefreshListener() {
            @Override
            public void onTokenRefresh(Credential refreshedCredential) {
                mStorageProviderRecord.setCredentialData(refreshedCredential.persist());
                StorageProviderManager.instance().updateStorageProviderRecord(mStorageProviderRecord, true);

                FilesPresenter.this.mStorageProviderInfo.setCredential(refreshedCredential);
            }
        });
        this.mFilesView.setPresenter(this);
    }

    @Override
    public String getStorageProviderName() {
        return mRxStorageProvider.getStorageProviderName();
    }

    @Override
    public boolean showWatchChanges() {
        return isLocalStorage();
    }

    @Override
    public StorageProviderInfo getStorageProviderInfo() {
        return mStorageProviderInfo;
    }

    @Override
    public void setDocumentPrivilegeUri(Uri uri) {
        if(mRxStorageProvider != null && mRxStorageProvider.getStorageProvider() instanceof LocalStorageProvider) {
            LocalStorageProvider localStorageProvider = (LocalStorageProvider)mRxStorageProvider.getStorageProvider();
            localStorageProvider.setSdcardUri(uri);
        }
    }

    private boolean isLocalStorage() {
        return mRxStorageProvider.getStorageProvider() instanceof LocalStorageProvider;
    }

    @Override
    public void result(int requestCode, int resultCode) {

    }

    public boolean isAtTopPath() {
        return mBackwardStack.empty();
    }

    @Override
    public void backToParent(String targetPath, CollectionViewState collectionViewState) {
        mBackwardStack.push(new BrowserState(mDirectory, collectionViewState));
        for (int i = 0; i < mBackwardStack.size(); i++) {
            if (mBackwardStack.get(i).directory.directory.getPath().equals(targetPath)) {
                if(isLocalStorage()) {
                    this.mDirectory = mBackwardStack.get(i).directory;
                    loadFiles(mDirectory, true, false, mBackwardStack.get(i).collectionViewState);
                } else {
                    mFilesView.onLeaveDirectory(mDirectory);
                    this.mDirectory = mBackwardStack.get(i).directory;
                    mDirectory.setShowHiddenFiles(mShowHiddenFile, mFileComparator);
                    mFilesView.updatePath(mDirectory.directory.getPath());
                    mFilesView.showFiles(mDirectory, mBackwardStack.get(i).collectionViewState);
                    // mFilesView.onCollectionViewStateRestore(mBackwardStack.get(i).collectionViewState);
                }

                /*mDirectory.setShowHiddenFiles(mShowHiddenFile, mFileComparator);
                mFilesView.showFiles(mDirectory);
                mFilesView.setLoadingIndicator(false);
                mFilesView.onCollectionViewStateRestore(mBackwardStack.get(i).collectionViewState);*/
                return;
            }
        }
        loadFiles(targetPath, true, true, null);
    }

    public boolean onBackPressed() {
        if (!mBackwardStack.empty()) {
            BrowserState currentState = mBackwardStack.pop();
            mFilesView.onLeaveDirectory(mDirectory);
            mDirectory = currentState.directory;
            if(isLocalStorage()) {
                mDirectory = currentState.directory;
                loadFiles(mDirectory, true, false, currentState.collectionViewState);
            } else {
                mFilesView.onLeaveDirectory(mDirectory);
                mDirectory.setShowHiddenFiles(mShowHiddenFile, mFileComparator);
                mFilesView.updatePath(mDirectory.directory.getPath());
                mFilesView.showFiles(mDirectory, currentState.collectionViewState);
                // mFilesView.onCollectionViewStateRestore(currentState.collectionViewState);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DirectoryInfo getDirectory() {
        return mDirectory;
    }

    @Override
    public void loadFiles(DirectoryInfo directoryInfo, boolean forceUpdate) {
        loadFiles(directoryInfo, forceUpdate || mFirstLoad, true, null);
        mFirstLoad = false;
    }

    @Override
    public void loadFiles(final DirectoryInfo directoryInfo, boolean forceUpdate, final boolean showLoadingUI, final CollectionViewState state) {
        if(!forceUpdate) return;
        if (showLoadingUI/* && ((directoryInfo != null && !directoryInfo.hasMore) || (directoryInfo == null))*/) {
            mFilesView.setLoadingIndicator(true);
        }
        String path;
        if(directoryInfo == null) {
            if(mRxStorageProvider.getStorageProvider().isRemote())
                path = "/";
            else
                path = mRxStorageProvider.getStorageProvider().getRootDirectory().getPath();
        } else {
            path = directoryInfo.directory.getPath();
        }
        mFilesView.updatePath(path);
        this.mGetFilesUseCase.execute(
                new GetFilesUseCase.RequestValues(directoryInfo),
                new DefaultSubscriber<UseCase.SingleResponseValue<DirectoryInfo>>() {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        mFilesView.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        mFilesView.setLoadingIndicator(false);
                        mDirectory = directoryInfo;
                        mFilesView.showError(mDirectory, e);
                    }

                    @Override
                    public void onNext(UseCase.SingleResponseValue<DirectoryInfo> singleResponseValue) {
                        super.onNext(singleResponseValue);
                        mFilesView.onLeaveDirectory(mDirectory);
                        mDirectory = singleResponseValue.getValue();
                        mDirectory.setShowHiddenFiles(mShowHiddenFile, mFileComparator);
                        mFilesView.showFiles(mDirectory, state);
                    }
                }
        );
    }

    @Override
    public void loadFiles(String targetPath, boolean forceUpdate, final boolean showLoadingUI, final CollectionViewState state) {
        if(!forceUpdate) return;
        if (showLoadingUI/* && ((directoryInfo != null && !directoryInfo.hasMore) || (directoryInfo == null))*/) {
            mFilesView.setLoadingIndicator(true);
        }
        String path;
        if(TextUtils.isEmpty(targetPath)) {
            if(mRxStorageProvider.getStorageProvider().isRemote())
                path = "/";
            else
                path = mRxStorageProvider.getStorageProvider().getRootDirectory().getPath();
        } else {
            path = targetPath;
        }
        mFilesView.updatePath(path);
        this.mGetFilesUseCase.execute(
                new GetFilesUseCase.RequestValues(targetPath),
                new DefaultSubscriber<UseCase.SingleResponseValue<DirectoryInfo>>() {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        mFilesView.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        mFilesView.setLoadingIndicator(false);
                        mDirectory = null;
                        mFilesView.showError(mDirectory, e);
                    }

                    @Override
                    public void onNext(UseCase.SingleResponseValue<DirectoryInfo> singleResponseValue) {
                        super.onNext(singleResponseValue);
                        mFilesView.onLeaveDirectory(mDirectory);
                        mDirectory = singleResponseValue.getValue();
                        mDirectory.setShowHiddenFiles(mShowHiddenFile, mFileComparator);
                        mFilesView.showFiles(mDirectory, state);
                    }
                }
        );
    }

    @Override
    public void createFile() {

    }

    @Override
    public void createFolder(RemoteFile parent, String name) {
        RxEventBus.instance().sendEvent(
                new NewTaskEvent(
                        new CreateFolderTask(
                                mStorageProviderInfo,
                                parent,
                                name
                        )
                )
        );
    }

    public void deleteFiles(RemoteFile[] files) {
        RxEventBus.instance().sendEvent(
                new NewTaskEvent(
                        new DeleteTask(
                                mStorageProviderInfo,
                                false,
                                files
                        )
                )
        );
    }

    public void setShowHiddenFiles(boolean show) {
        this.mShowHiddenFile = show;
        if(mDirectory != null) {
            mDirectory.setShowHiddenFiles(mShowHiddenFile, mFileComparator);
            // mDirectory.notifyChange();
            mFilesView.showFiles(mDirectory, null);
            // if (mDataListener != null) mDataListener.onDirectoryChanged(mDirectory.get());
        }
    }

    @Override
    public void openFileDetails(RemoteFile requestedFile) {

    }

    @Override
    public void onFileClick(final RemoteFile file, final CollectionViewState collectionViewState) {
        mGetFileUseCase.execute(
                new GetFileUseCase.RequestValues(file), new DefaultSubscriber<UseCase.SingleResponseValue<RemoteFile>>() {
                    @Override
                    public void onNext(UseCase.SingleResponseValue<RemoteFile> remoteFileSingleResponseValue) {
                        super.onNext(remoteFileSingleResponseValue);
                        RemoteFile checkedFile = remoteFileSingleResponseValue.getValue();
                        if (checkedFile.isDirectory()) {
                            mBackwardStack.push(new BrowserState(mDirectory, collectionViewState));
                            loadFiles(DirectoryInfo.fromDirectory(checkedFile), true);
                        } else {
                            if (checkedFile.needsDownload()) {
                                // Popup a dialog
                                mFilesView.requestForDownload(checkedFile);
                                // downloadFile(checkedFile);
                            } else {
                                mFilesView.openFileByPath(checkedFile.getPath(), true);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e("ALIS", e.getMessage(), e);
                    }

                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                    }
                }
        );

    }

    @Override
    public void downloadFile(final RemoteFile file, String savePath, boolean openAfterDownload) {
        String remoteFileName = file.getName();
        String mime = MimeUtils.getMime(remoteFileName);
        String localPath = null;
        if(MimeUtils.isApkMime(mime)) {
            File downloadDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            localPath = Path.combine(Path.combine(downloadDir.getAbsolutePath(), "UnifyStorage"), remoteFileName);
        } else {
            localPath = TextUtils.isEmpty(savePath) ? mFileCacheRepository.getFullCachePathForFile(
                    mRxStorageProvider.getStorageProviderName(),
                    mStorageProviderRecord.getUuid(),
                    file
            ) : savePath;
        }

        RxEventBus.instance().sendEvent(
                new NewTaskEvent(
                        new DownloadTask(
                                mStorageProviderInfo,
                                file,
                                localPath,
                                false
                        )
                )
        );
    }

    @Override
    public void onFileLongClick(RemoteFile file) {

    }

    @Override
    public void start() {
        loadFiles(null, false);
    }

    @Override
    public void destroy() {

    }

    public static class Builder extends BasePresenter.Builder<FilesContract.Presenter, FilesContract.View> {
        private StorageProviderInfo storageProviderInfo;
        private StorageProvider storageProvider;
        private ThreadExecutor threadExecutor;
        private PostExecutionThread postExecutionThread;
        private FileCacheRepository fileCacheRepository;
        private String[] extras;

        public Builder() {

        }

        @Override
        public FilesPresenter build() {
            return new FilesPresenter(
                    view,
                    storageProviderInfo,
                    storageProvider,
                    threadExecutor,
                    postExecutionThread,
                    fileCacheRepository
            );
        }

        public Builder view(FilesContract.View view) {
            this.view = view;
            return this;
        }

        @Override
        public Builder threadExecutor(ThreadExecutor threadExecutor) {
            this.threadExecutor = threadExecutor;
            return this;
        }

        @Override
        public Builder postExecutionThread(PostExecutionThread postExecutionThread) {
            this.postExecutionThread = postExecutionThread;
            return this;
        }

        public Builder fileCacheRepository(FileCacheRepository fileCacheRepository) {
            this.fileCacheRepository = fileCacheRepository;
            return this;
        }

        public Builder storageProviderInfo(StorageProviderInfo storageProviderInfo) {
            this.storageProviderInfo = storageProviderInfo;
            return this;
        }

        public Builder storageProvider(StorageProvider storageProvider) {
            this.storageProvider = storageProvider;
            return this;
        }
    }
}
