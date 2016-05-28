package org.cryse.unifystorage.providers.localstorage;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import org.cryse.unifystorage.AbstractStorageProvider;
import org.cryse.unifystorage.ConflictBehavior;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.FileUpdater;
import org.cryse.unifystorage.HashAlgorithm;
import org.cryse.unifystorage.StorageException;
import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.io.FileUtils;
import org.cryse.unifystorage.providers.localstorage.utils.LocalStorageUtils;
import org.cryse.unifystorage.utils.OperationResult;
import org.cryse.unifystorage.utils.DirectoryInfo;
import org.cryse.unifystorage.io.IOUtils;
import org.cryse.unifystorage.utils.Path;
import org.cryse.unifystorage.utils.ProgressCallback;
import org.cryse.unifystorage.utils.hash.Sha1HashAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public class LocalStorageProvider extends AbstractStorageProvider {
    private Context mContext;
    private String mStartPath;
    private LocalStorageFile mStartFile;
    private Uri mSdcardUri;

    public LocalStorageProvider(Context context, String startPath) {
        this.mContext = context;
        this.mStartPath = startPath;
    }

    public void setSdcardUri(Uri uri) {
        this.mSdcardUri = uri;
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
    public DirectoryInfo list(DirectoryInfo directoryInfo) throws StorageException {
        RemoteFile directory = directoryInfo.directory;
        if(directory == null) return list();

        File file = new File(directory.getPath());
        List<RemoteFile> list = new ArrayList<>();
        File[] children = file.listFiles();
        if(children != null) {
            for(File f : children){
                list.add(new LocalStorageFile(f));
            }
        }
        return DirectoryInfo.create(directory, list);
    }

    @Override
    public LocalStorageFile createDirectory(RemoteFile parent, String name) throws StorageException {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExtSdCard(mContext, parent.getPath())) {
            DocumentFile documentFile = getDocumentFile(mContext, mSdcardUri, ((LocalStorageFile)parent).getFile(), true);
            DocumentFile newFile = documentFile.createDirectory(name);
        } else {
            File file = new File(Path.combine(parent.getPath(), name));
            boolean res = file.mkdir();
            if(!res) throw new StorageException();
        }
        File file = new File(Path.combine(parent.getPath(), name));
        return new LocalStorageFile(file);
    }

    @Override
    public LocalStorageFile createFile(RemoteFile parent, String name, InputStream input, ConflictBehavior behavior) throws StorageException {
        return null;
    }

    @Override
    public boolean exists(RemoteFile parent, String name) throws StorageException {
        return new File(Path.combine(parent, name)).exists();
    }

    @Override
    public LocalStorageFile getFile(RemoteFile parent, String name) throws StorageException {
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
    public LocalStorageFile updateFile(RemoteFile remote, InputStream input, FileUpdater updater) throws StorageException {

        // copy content
        try {
            if(updater != null) {
                updater.update(remote, input);
            } else {
                IOUtils.copyFile(input, new File(remote.getId()));
            }
            return (LocalStorageFile)remote;
        }
        catch (IOException e) {
            //Log.e(getName(), "update()", e);
            return null;
        }
    }

    @Override
    public OperationResult deleteFile(RemoteFile file) {
        if (isOnExtSdCard(mContext, file.getPath())) {
            if (mSdcardUri == null)
                return OperationResult.create(file, false);
            DocumentFile documentFile = getDocumentFile(mContext, mSdcardUri, ((LocalStorageFile)file).getFile(), file.isDirectory());
            if (documentFile == null)
                return OperationResult.create(file, false);
            return OperationResult.create(file, documentFile.delete());
        } else {
            return OperationResult.create(file, FileUtils.deleteQuietly(((LocalStorageFile)file).getFile()));
        }
    }

    @Override
    public void copyFile(RemoteFile target, final ProgressCallback callback, RemoteFile...files) {
        List<File> sourceFileList = new ArrayList<>();
        List<File> targetFileList = new ArrayList<>();
        for(RemoteFile remoteFile : files) {
            LocalStorageFile file = (LocalStorageFile)remoteFile;
            File localFile = file.getFile();
            if(localFile.isDirectory()) {
                Collection<File> allSubFiles = LocalStorageUtils.listFilesRecursive(localFile);
                sourceFileList.addAll(allSubFiles);
                for(File subFile : allSubFiles) {
                    File targetFile = new File(subFile.getPath().replace(Path.getDirectory(localFile.getPath()), target.getPath()));
                    targetFileList.add(targetFile);
                }
            } else {
                sourceFileList.add(localFile);
                File targetFile = new File(localFile.getPath().replace(Path.getDirectory(localFile.getPath()), target.getPath()));
                targetFileList.add(targetFile);
            }
        }
        int sourceFileListCount = sourceFileList.size();

        for(int i = 0; i < sourceFileListCount; i++) {
            copyFileSingle(sourceFileList.get(i), targetFileList.get(i), callback);
        }

        /*File fromFile = file.getFile();
        if(fromFile.isDirectory()) {
            StringBuilder builder = new StringBuilder();
            StringBuilder stargetBuilder = new StringBuilder();
            Collection<File> allSubFiles = FileUtils.listFiles(fromFile, null, true);
            for (File sub : allSubFiles) {
                builder.append(sub.getPath()).append("\n");
                stargetBuilder.append(sub.getPath().replace(Path.getDirectory(fromFile.getPath()), target.getPath())).append("\n");
            }
            String fileList = builder.toString();
            String newfileList = stargetBuilder.toString();
            int len = fileList.length();
            int len2 = newfileList.length();
        } else {
            copyFileSingle(fromFile, new File(Path.combine(target.getPath(), file.getName())), callback);
        }*/
    }

    private void copyFileSingle(File sourceFile, File targetFile, final ProgressCallback callback) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(sourceFile);
            File parent = targetFile.getParentFile();
            boolean parentExists = parent.exists();
            if(!parent.exists())
                parentExists = parent.mkdirs();

            if (parentExists && isWritable(targetFile)) {
                outStream = new FileOutputStream(targetFile);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(mContext, mSdcardUri, targetFile, false);
                    outStream =
                            mContext.getContentResolver().openOutputStream(targetDocument.getUri());
                }
                else if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {
                    if (callback != null)
                        callback.onFailure(new Exception());
                    /*// Workaround for Kitkat ext SD card
                    Uri uri = MediaStoreHack.getUriFromFile(target.getAbsolutePath(),context);
                    outStream = context.getContentResolver().openOutputStream(uri);*/
                }
                else {
                    if (callback != null)
                        callback.onFailure(new Exception());
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[16384]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException e) {
            if (callback != null)
                callback.onFailure(e);
        } finally {
            try {
                if(inChannel != null)
                    inChannel.close();
                if(outChannel != null)
                    outChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            IOUtils.safeClose(inStream);
            IOUtils.safeClose(outStream);
        }
    }

    @Override
    public void moveFile(RemoteFile target, final ProgressCallback callback, RemoteFile...files){
        /*try {
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
        }*/
    }

    @Override
    public LocalStorageFile getFileDetail(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public LocalStorageFile getFilePermission(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public LocalStorageFile updateFilePermission(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public StorageUserInfo getUserInfo(boolean forceRefresh) throws StorageException {
        return null;
    }

    @Override
    public Request download(RemoteFile file) throws StorageException {
        return null;
    }

    @Override
    public HashAlgorithm getHashAlgorithm() {
        return Sha1HashAlgorithm.getInstance();
    }

    private static boolean isOnExtSdCard(Context context, final String path) {
        return LocalStorageUtils.isOnSdcard(context, path);
    }

    public static DocumentFile getDocumentFile(Context context, Uri sdcardUri, final File file, final boolean isDirectory) {
        String sdcardDirectory = LocalStorageUtils.getSdcardDirectory(context, file.getPath());
        boolean isSdcardDirectoryRoot=false;
        if (sdcardDirectory == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if(!sdcardDirectory.equals(fullPath))
                relativePath = fullPath.substring(sdcardDirectory.length() + 1);
            else isSdcardDirectoryRoot=true;
        }
        catch (IOException e) {
            return null;
        }
        catch (Exception f){
            isSdcardDirectoryRoot = true;
            //continue
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, sdcardUri);
        if(isSdcardDirectoryRoot)
            return document;
        String[] parts = relativePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                }
                else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file
     *            The file
     * @return true if the file is writable.
     */
    public static final boolean isWritable(final File file) {
        if(file==null)
            return false;
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete();
        }

        return result;
    }
}
