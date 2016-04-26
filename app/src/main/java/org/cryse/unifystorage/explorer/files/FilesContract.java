package org.cryse.unifystorage.explorer.files;

import android.support.annotation.Nullable;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.base.BasePresenter;
import org.cryse.unifystorage.explorer.base.BaseView;
import org.cryse.unifystorage.explorer.event.FileDeleteEvent;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.openfile.OpenFileUtils;
import org.cryse.unifystorage.utils.DirectoryInfo;

import java.util.List;

public interface FilesContract {
    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void onLeaveDirectory(DirectoryInfo directory);

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
        boolean showWatchChanges();

        StorageProviderInfo getStorageProviderInfo();

        void result(int requestCode, int resultCode);

        boolean isAtTopPath();

        void backToParent(String targetPath, CollectionViewState collectionViewState);

        boolean onBackPressed();

        DirectoryInfo getDirectory();

        void setShowHiddenFiles(boolean show);

        void loadFiles(RemoteFile parent, boolean forceUpdate);

        void loadFiles(RemoteFile parent, boolean forceUpdate, boolean showLoadingUI, CollectionViewState collectionViewState);

        void createFile();

        void createFolder(RemoteFile parent, String name);

        void openFileDetails(RemoteFile requestedFile);

        void deleteFiles(List<RemoteFile> filesToDelete);

        void onDeleteFileEvent(FileDeleteEvent event);

        void onFileClick(RemoteFile file, CollectionViewState collectionViewState);

        void onFileLongClick(RemoteFile file);
    }
}
