package org.cryse.unifystorage;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

import org.cryse.unifystorage.io.ProgressInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class RemoteFileDownloader {
    private ProgressInputStream dataStream;
    private RemoteFile mFile;
    private Request mRequest;
    private Call mCall;
    private WeakReference<Interceptor> mDebugInterceptor;

    public RemoteFileDownloader(RemoteFile file, Request request, Interceptor interceptor) {
        this(file, request);
        this.mDebugInterceptor = new WeakReference<>(interceptor);
    }

    public RemoteFileDownloader(RemoteFile file, Request request) {
        this.mFile = file;
        // this.dataStream = new ProgressInputStream(inputStream, file.size());
        this.mRequest = request;
    }

    public InputStream getDataStream() {
        return dataStream;
    }

    public ProgressInputStream getProgressDataStream() {
        return dataStream;
    }

    public RemoteFile getFile() {
        return mFile;
    }

    public void download(File targetFile, final DownloadListener downloadListener) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(
                                        new ProgressResponseBody(
                                                originalResponse.body(),
                                                new ProgressListener() {
                                                    @Override
                                                    public void update(long bytesRead, long contentLength, boolean done) {
                                                        if(done) {
                                                            downloadListener.downloadFinished();
                                                        } else {
                                                            downloadListener.downloading(bytesRead, contentLength);
                                                        }
                                                    }
                                                }
                                        )
                                )
                                .build();
                    }
                });
        if(mDebugInterceptor != null && mDebugInterceptor.get() != null) {
            builder.addNetworkInterceptor(mDebugInterceptor.get());
        }
        OkHttpClient okHttpClient = builder.build();
        try {
            downloadListener.downloadStart();
            mCall = okHttpClient.newCall(mRequest);
            Response response = mCall.execute();
            BufferedSink sink = Okio.buffer(Okio.sink(targetFile));
            sink.writeAll(response.body().source());
            sink.close();
        } catch (IOException e) {
            downloadListener.downloadFailed(e);
        }
    }

    public void cancel() {
        if(mCall != null && !mCall.isCanceled() && !mCall.isCanceled())
            mCall.cancel();
    }

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override public long contentLength() {
            return responseBody.contentLength();
        }

        @Override public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }

    public interface DownloadListener {
        void downloadStart();
        void downloading(long readSize, long totalSize);
        void downloadFinished();
        void downloadFailed(Throwable throwable);
    }
}
