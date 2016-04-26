package org.cryse.unifystorage;

import okhttp3.OkHttpClient;

import org.cryse.unifystorage.io.ProgressInputStream;

import java.io.InputStream;

public class RemoteFileDownloader {
    public static enum HttpClient {
        INSTANCE;
        private OkHttpClient okHttpClient = new OkHttpClient();
        public static OkHttpClient getHttpClient() {
            return INSTANCE.okHttpClient;
        }
    }
    private ProgressInputStream dataStream;
    private RemoteFile file;
    public RemoteFileDownloader(RemoteFile file, InputStream inputStream) {
        this.file = file;
        this.dataStream = new ProgressInputStream(inputStream, file.size());
    }

    public InputStream getDataStream() {
        return dataStream;
    }

    public ProgressInputStream getProgressDataStream() {
        return dataStream;
    }

    public RemoteFile getFile() {
        return file;
    }
}
