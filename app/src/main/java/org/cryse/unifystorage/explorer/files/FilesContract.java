package org.cryse.unifystorage.explorer.files;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.explorer.base.BasePresenter;
import org.cryse.unifystorage.explorer.base.BaseView;
import org.cryse.unifystorage.explorer.event.FileDeleteEvent;
import org.cryse.unifystorage.explorer.utils.CollectionViewState;
import org.cryse.unifystorage.explorer.utils.openfile.OpenFileUtils;
import org.cryse.unifystorage.utils.DirectoryInfo;

import java.util.List;

public interface FilesContract {
    interface View extends BaseView<Presenter> {
        OpenFileUtils openFileUtils();

        void setLoadingIndicator(boolean active);

        void showFiles(DirectoryInfo directory);

        void showAddFile();

        void showFileDetailsUi(String fileId);

        void showVisibleFilter();

        void showAllFilter();

        void onCredentialRefreshed(Credential credential);

        void onCollectionViewStateRestore(CollectionViewState collectionViewState);
    }

    interface Presenter extends BasePresenter {
        void result(int requestCode, int resultCode);

        boolean isAtTopPath();

        void backToParent(String targetPath, CollectionViewState collectionViewState);

        boolean onBackPressed();

        DirectoryInfo getDirectory();

        void setShowHiddenFiles(boolean show);

        void loadFiles(RemoteFile parent, boolean forceUpdate);

        void createFile();

        void createFolder(RemoteFile parent, String name);

        void openFileDetails(RemoteFile requestedFile);

        void deleteFiles(List<RemoteFile> filesToDelete);

        void onDeleteFileEvent(FileDeleteEvent event);

        void onFileClick(RemoteFile file, CollectionViewState collectionViewState);

        void onFileLongClick(RemoteFile file);
    }
}
