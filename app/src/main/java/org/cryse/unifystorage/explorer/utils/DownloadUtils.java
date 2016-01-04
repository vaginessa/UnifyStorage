package org.cryse.unifystorage.explorer.utils;

import org.apache.commons.io.FileUtils;
import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.RemoteFileDownloader;
import org.cryse.unifystorage.io.ProgressInputStream;
import org.cryse.unifystorage.io.StreamProgressListener;
import org.cryse.unifystorage.utils.IOUtils;
import org.cryse.unifystorage.utils.Path;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class DownloadUtils {
    public static final String TAG = DownloadUtils.class.getCanonicalName();
    public static <RF extends RemoteFile> Observable<Long> download(final RemoteFileDownloader<RF> downloader, final String targetPath) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(final Subscriber<? super Long> subscriber) {
                try {
                    File targetFile = new File(targetPath);
                    String parentDirectoryPath = Path.getDirectory(targetPath);
                    File parentDirectory = new File(parentDirectoryPath);
                    if(!parentDirectory.exists()) parentDirectory.mkdirs();
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
                    IOUtils.safeClose(downloader.getDataStream());
                }
            }
        });
    }


}
