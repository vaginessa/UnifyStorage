package org.cryse.unifystorage.providers.localstorage;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.utils.DirectoryPair;
import org.cryse.unifystorage.utils.IOUtils;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageProvider extends AbstractStorageProvider<LocalStorageFile> {
    private String mStartPath;
    private LocalStorageFile mStartFile;
    public LocalStorageProvider(String startPath) {
        this.mStartPath = startPath;
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
    public DirectoryPair<LocalStorageFile, List<LocalStorageFile>> list(LocalStorageFile parent) throws StorageException {
        if(parent == null) return list();

        File file = new File(parent.getPath());
        List<LocalStorageFile> list = new ArrayList<LocalStorageFile>();
        File[] children = file.listFiles();
        if(children != null) {
            for(File f : children){
                list.add(new LocalStorageFile(f));
            }
        }
        return DirectoryPair.create(parent, list);
    }

    @Override
    public LocalStorageFile createDirectory(LocalStorageFile parent, String name) throws StorageException {
        return null;
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
    public boolean deleteFile(LocalStorageFile file) throws StorageException {
        return file.getFile().delete();
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
    public HashAlgorithm getHashAlgorithm() {
        return Sha1HashAlgorithm.getInstance();
    }
}
