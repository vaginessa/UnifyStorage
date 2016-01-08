package org.cryse.unifystorage.providers.localstorage;

import android.support.v4.util.Pair;

import org.apache.commons.io.FileUtils;
import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.io.ProgressInputStream;
import org.cryse.unifystorage.io.StreamProgressListener;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.utils.IOUtils;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.ProgressCallback;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageProvider extends AbstractStorageProvider<LocalStorageFile, LocalCredential> {
    private String mStartPath;
    private LocalStorageFile mStartFile;
    public LocalStorageProvider(String startPath) {
        this.mStartPath = startPath;
    }

    @Override
    public String getStorageProviderName() {
        return LocalProviderConst.NAME_STORAGE_PROVIDER;
    }

    @Override
    public LocalStorageFile getRootDirectory() throws StorageException {
        File file = new File(mStartPath);
        if(mStartFile == null) {
            mStartFile = new LocalStorageFile(file);
        }
        return mStartFile;
    }

    @Override
    public DirectoryInfo<LocalStorageFile, List<LocalStorageFile>> list(LocalStorageFile parent) throws StorageException {
        if(parent == null) return list();

        File file = new File(parent.getPath());
        List<LocalStorageFile> list = new ArrayList<LocalStorageFile>();
        File[] children = file.listFiles();
        if(children != null) {
            for(File f : children){
                list.add(new LocalStorageFile(f));
            }
        }
        return DirectoryInfo.create(parent, list);
    }

    @Override
    public LocalStorageFile createDirectory(LocalStorageFile parent, String name) throws StorageException {
        File file = new File(Path.combine(parent.getPath(), name));
        file.mkdir();
        return new LocalStorageFile(file);
    }

    @Override
    public LocalStorageFile createFile(LocalStorageFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        return null;
    }

    @Override
    public boolean exists(LocalStorageFile parent, String name) throws StorageException {
        return new File(Path.combine(parent, name)).exists();
    }

    @Override
    public LocalStorageFile getFile(LocalStorageFile parent, String name) throws StorageException {
        File target = new File(Path.combine(parent, name));
        if(target.exists())
            return new LocalStorageFile(target);
        else
            return null;
    }

    @Override
    public LocalStorageFile getFileById(String id) throws StorageException {
        File target = new File(id);
        if(target.exists())
            return new LocalStorageFile(target);
        else
            return null;
    }

    @Override
    public LocalStorageFile updateFile(LocalStorageFile remote, InputStream input, FileUpdater updater) throws StorageException {

        // copy content
        try {
            if(updater != null) {
                updater.update(remote, input);
            } else {
                IOUtils.copyFile(input, new File(remote.getId()));
            }
            return remote;
        }
        catch (IOException e) {
            //Log.e(getName(), "update()", e);
            return null;
        }
    }

    @Override
    public Pair<LocalStorageFile, Boolean> deleteFile(LocalStorageFile file) {
        return Pair.create(file, FileUtils.deleteQuietly(file.getFile()));
    }

    @Override
    public void copyFile(LocalStorageFile target, LocalStorageFile file, final ProgressCallback callback) {
        try {
            InputStream input = new FileInputStream(file.getFile());
            ProgressInputStream progressInputStream = new ProgressInputStream(input, file.size());
            progressInputStream.addListener(new StreamProgressListener() {
                @Override
                public void onProgress(ProgressInputStream stream, long current, long total, double rate) {
                    if(callback != null)
                        callback.onProgress(current, total);
                }
            });
            String targetPath = Path.combine(target.getPath(), file.getName());
            File targetFile = new File(targetPath);
            FileUtils.copyInputStreamToFile(progressInputStream, targetFile);
            progressInputStream.removeAllListener();
        } catch (IOException e) {
            if(callback != null)
                callback.onFailure(e);
        }
    }

    @Override
    public void moveFile(LocalStorageFile target, LocalStorageFile file, final ProgressCallback callback){
        try {
            InputStream input = new FileInputStream(file.getFile());
            ProgressInputStream progressInputStream = new ProgressInputStream(input, file.size());progressInputStream.addListener(new StreamProgressListener() {
                @Override
                public void onProgress(ProgressInputStream stream, long current, long total, double rate) {
                    if(callback != null)
                        callback.onProgress(current, total);
                }
            });
            String targetPath = Path.combine(target.getPath(), file.getName());
            File targetFile = new File(targetPath);
            FileUtils.copyInputStreamToFile(progressInputStream, targetFile);
            if(!file.getFile().delete())
                throw new IOException("Failed to delete file.");
            progressInputStream.removeAllListener();
        } catch (IOException e) {
            if(callback != null)
                callback.onFailure(e);
        }
    }

    @Override
    public LocalStorageFile getFileDetail(LocalStorageFile file) throws StorageException {
        return null;
    }

    @Override
    public LocalStorageFile getFilePermission(LocalStorageFile file) throws StorageException {
        return null;
    }

    @Override
    public LocalStorageFile updateFilePermission(LocalStorageFile file) throws StorageException {
        return null;
    }

    @Override
    public StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException {
        return null;
    }

    @Override
    public LocalCredential getRefreshedCredential() {
        return null;
    }

    @Override
    public RemoteFileDownloader<LocalStorageFile> download(LocalStorageFile file) throws StorageException {
        return null;
    }

    @Override
    public boolean shouldRefreshCredential() {
        return false;
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return Sha1HashAlgorithm.getInstance();
    }
}
