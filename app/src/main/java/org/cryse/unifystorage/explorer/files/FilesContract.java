package org.cryse.unifystorage.explorer.files;

import android.support.annotation.Nullable;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.base.BasePresenter;
import org.cryse.unifystorage.explorer.base.BaseView;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.utils.DirectoryInfo;

public interface FilesContract {
    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void onLeaveDirectory(DirectoryInfo directory);

        void updatePath(String path);

        void showFiles(DirectoryInfo directory, @Nullable CollectionViewState collectionViewState);

        void showError(DirectoryInfo directory, Throwable throwable);

        void showAddFile();

        void showFileDetailsUi(String fileId);

        void showVisibleFilter();

        void showAllFilter();

        void openFileByPath(String filePath, boolean useSystemSelector);

        void openFileByUri(String uriString, boolean useSystemSelector);
    }

    interface Presenter extends BasePresenter {
        String getStorageProviderName();

        boolean showWatchChanges();

        StorageProviderInfo getStorageProviderInfo();

        void result(int requestCode, int resultCode);

        boolean isAtTopPath();

        void backToParent(String targetPath, CollectionViewState collectionViewState);

        boolean onBackPressed();

        DirectoryInfo getDirectory();

        void setShowHiddenFiles(boolean show);

        void loadFiles(DirectoryInfo directoryInfo, boolean forceUpdate);

        void loadFiles(DirectoryInfo directoryInfo, boolean forceUpdate, boolean showLoadingUI, CollectionViewState collectionViewState);

        void loadFiles(String targetPath, boolean forceUpdate, boolean showLoadingUI, CollectionViewState collectionViewState);

        void createFile();

        void createFolder(RemoteFile parent, String name);

        void openFileDetails(RemoteFile requestedFile);

        void deleteFiles(RemoteFile[] filesToDelete);

        void onFileClick(RemoteFile file, CollectionViewState collectionViewState);

        void onFileLongClick(RemoteFile file);
    }
}
