package org.cryse.unifystorage;

import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;

public class RemoteFileDownloader<RF extends RemoteFile> {
    public static enum HttpClient {
        INSTANCE;
        private OkHttpClient okHttpClient = new OkHttpClient();
        public static OkHttpClient getHttpClient() {
            return INSTANCE.okHttpClient;
        }
    }
    private InputStream dataStream;
    private RF file;
    public RemoteFileDownloader(RF file, InputStream inputStream) {
        this.file = file;
        this.dataStream = inputStream;
    }

    public InputStream getDataStream() {
        return dataStream;
    }

    public RF getFile() {
        return file;
    }
}
