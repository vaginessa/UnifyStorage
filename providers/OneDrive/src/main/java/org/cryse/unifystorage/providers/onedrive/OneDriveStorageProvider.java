package org.cryse.unifystorage.providers.onedrive;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.microsoft.services.msa.InternalOneDriveAuthenticator;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.extensions.Drive;
import com.onedrive.sdk.extensions.Folder;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.Item;
import com.onedrive.sdk.extensions.OneDriveClient;
import com.onedrive.sdk.logger.LoggerLevel;
import com.onedrive.sdk.options.Option;
import com.onedrive.sdk.options.QueryOption;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.utils.DirectoryPair;
import org.cryse.unifystorage.utils.IOUtils;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OneDriveStorageProvider extends AbstractStorageProvider<OneDriveFile, OneDriveCredential> {
    private String mClientId;
    private IOneDriveClient mOneDriveClient;
    private OneDriveFile mRootFile;
    private OneDriveUserInfo mOwnerUserInfo;
    private OneDriveCredential mRefreshedCredential;

    public OneDriveStorageProvider(Activity activity, String clientId, final OneDriveCredential credential) {
        this.mClientId = clientId;
        final InternalOneDriveAuthenticator internalOneDriveAuthenticator = new InternalOneDriveAuthenticator() {
            @Override
            public String getClientId() {
                return mClientId;
            }

            @Override
            public String[] getScopes() {
                return credential.getScopesArray();
            }
        };
        internalOneDriveAuthenticator.applyCredential(credential);
        final IClientConfig config = DefaultClientConfig.createWithAuthenticator(internalOneDriveAuthenticator);
        config.getLogger().setLoggingLevel(LoggerLevel.Debug);
        mOneDriveClient = new OneDriveClient
                .Builder()
                .fromConfig(config)
                .loginAndBuildClient(activity);
        if(mOneDriveClient != null) {
            mRefreshedCredential = new OneDriveCredential(
                    credential.getAccountName(),
                    credential.getAccountType(),
                    internalOneDriveAuthenticator.getSession());
        }
    }

    public OneDriveStorageProvider(IOneDriveClient client) {
        this.mOneDriveClient = client;
    }

    @Override
    public OneDriveFile getRootDirectory() throws StorageException {
        if(mRootFile == null) {
            mRootFile = new OneDriveFile(mOneDriveClient.getDrive().getRoot().buildRequest().get());
        }
        return mRootFile;
    }

    @Override
    public DirectoryPair<OneDriveFile, List<OneDriveFile>> list(OneDriveFile parent) throws StorageException {
        List<Item> children = mOneDriveClient
                .getDrive()
                .getItems(parent.getId())
                .getChildren()
                .buildRequest()
                .get()
                .getCurrentPage();
        List<OneDriveFile> list = new ArrayList<OneDriveFile>();
        for (final Item childItem : children) {
            list.add(new OneDriveFile(childItem));
        }
        return DirectoryPair.create(parent, list);
    }

    @Override
    public OneDriveFile createDirectory(OneDriveFile parent, String name) throws StorageException {
        try {
            if(TextUtils.isEmpty(name)) throw new NullPointerException("name");
            final Item newItem = new Item();
            newItem.name = name;
            newItem.folder = new Folder();
            Item createdItem = mOneDriveClient
                    .getDrive()
                    .getItems(parent.getId())
                    .getChildren()
                    .buildRequest()
                    .create(newItem);
            return new OneDriveFile(createdItem);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public OneDriveFile createFile(OneDriveFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        try {
            final Option option = new QueryOption("@name.conflictBehavior", conflictBehaviorToString(behavior));
            Item newItem = mOneDriveClient
                    .getDrive()
                    .getItems(parent.getId())
                    .getChildren()
                    .byId(name)
                    .getContent()
                    .buildRequest(Collections.singletonList(option))
                    .put(IOUtils.toByteArray(input));
            return new OneDriveFile(newItem);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public boolean exists(OneDriveFile parent, String name) throws StorageException {
        try {
            Item item = mOneDriveClient
                    .getDrive()
                    .getItems(parent.getId())
                    .getItemWithPath(Path.combine(parent, name))
                    .buildRequest()
                    .get();
            return item != null;
        } catch (Throwable throwable) {
            // TODO: maybe network exception here, would lead to wrong result.
            return false;
        }
    }

    @Override
    public OneDriveFile getFile(OneDriveFile parent, String name) throws StorageException {
        try {
            Item item = mOneDriveClient
                    .getDrive()
                    .getItems(parent.getId())
                    .getItemWithPath(Path.combine(parent, name))
                    .buildRequest()
                    .get();
            return new OneDriveFile(item);
        } catch (Throwable throwable) {
            throw new StorageException(throwable);
        }
    }

    @Override
    public OneDriveFile getFileById(String id) throws StorageException {
        try {
            Item item = mOneDriveClient
                    .getDrive()
                    .getItems(id)
                    .buildRequest()
                    .get();
            return new OneDriveFile(item);
        } catch (Throwable throwable) {
            throw new StorageException(throwable);
        }
    }

    @Override
    public OneDriveFile updateFile(OneDriveFile remote, InputStream input, FileUpdater updater) throws StorageException {
        try {
            final Option option = new QueryOption("@name.conflictBehavior", conflictBehaviorToString(ConflictBehavior.REPLACE));
            Item newItem = mOneDriveClient
                    .getDrive()
                    .getItems(remote.getId())
                    .getChildren()
                    .byId(remote.getId())
                    .getContent()
                    .buildRequest(Collections.singletonList(option))
                    .put(IOUtils.toByteArray(input));
            return new OneDriveFile(newItem);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public boolean deleteFile(OneDriveFile file) throws StorageException {
        try {
            mOneDriveClient
                    .getDrive()
                    .getItems(file.getId())
                    .buildRequest()
                    .delete();
            return true;
        } catch (Throwable throwable) {
            throw new StorageException(throwable);
        }
    }

    @Override
    public OneDriveFile getFileDetail(OneDriveFile file) throws StorageException {
        return null;
    }

    @Override
    public OneDriveFile getFilePermission(OneDriveFile file) throws StorageException {
        return null;
    }

    @Override
    public OneDriveFile updateFilePermission(OneDriveFile file) throws StorageException {
        return null;
    }

    @Override
    public StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException {
        try {
            if(mOwnerUserInfo == null || forceRefresh) {
                Drive drive = mOneDriveClient.getDrive().buildRequest().get();
                mOwnerUserInfo = new OneDriveUserInfo(drive.owner);
            }
            return mOwnerUserInfo;
        } catch (Throwable throwable) {
            throw new StorageException(throwable);
        }
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return Sha1HashAlgorithm.getInstance();
    }

    private static String conflictBehaviorToString(ConflictBehavior behavior) {
        switch (behavior) {
            case RENAME:
                return "rename";
            case REPLACE:
                return "replace";
            case FAIL:
            default:
                return "fail";
        }
    }

    @Override
    public OneDriveCredential getRefreshedCredential() {
        return mRefreshedCredential;
    }

    @Override
    public RemoteFileDownloader<OneDriveFile> download(OneDriveFile file) throws StorageException {
        try {
            OkHttpClient client = RemoteFileDownloader.HttpClient.getHttpClient();
            Request request = new Request.Builder().url(file.getUrl()).build();
            Response response = client.newCall(request).execute();
            return new RemoteFileDownloader<>(file, response.body().byteStream());
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public boolean shouldRefreshCredential() {
        return true;
    }
}
