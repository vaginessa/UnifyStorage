package org.cryse.unifystorage.explorer.service.operation;

import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.explorer.utils.http.ProgressResponseBody;
import org.cryse.unifystorage.utils.Path;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class DownloadOperation extends RemoteOperation {
    public static final String OP_NAME = "OP_DOWNLOAD";
    private String mToken;
    private RemoteFile mRemoteFile;
    private String mSavePath;
    private Call mCall;
    private WeakReference<Interceptor> mDebugInterceptor;

    public DownloadOperation(String operationToken, RemoteFile remoteFile, String savePath) {
        super(operationToken);
        this.mToken = operationToken;
        this.mRemoteFile = remoteFile;
        this.mSavePath = savePath;
    }

    public void setDebugInterceptor(Interceptor interceptor) {
        mDebugInterceptor = new WeakReference<>(interceptor);
    }

    @Override
    public boolean shouldRefresh() {
        return false;
    }

    @Override
    public String getOperationName() {
        return OP_NAME;
    }

    @Override
    protected RemoteOperationResult run(RemoteOperationContext operationContext, final OnRemoteOperationListener listener, final Handler listenerHandler) {
        if (listenerHandler != null && listener != null) {
            listenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onRemoteOperationStart(
                            DownloadOperation.this
                    );
                }
            });
        }
        try {
            Request request = operationContext.getStorageProvider().download(mRemoteFile);

            final File targetFile = new File(mSavePath);
            String parentDirectoryPath = Path.getDirectory(mSavePath);
            if (parentDirectoryPath != null) {
                File parentDirectory = new File(parentDirectoryPath);
                if (!parentDirectory.exists()) parentDirectory.mkdirs();
            }
            if (!targetFile.exists()) targetFile.createNewFile();
            if (!targetFile.canWrite())
                throw new IOException("Target file not writable.");

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            return originalResponse.newBuilder()
                                    .body(
                                            new ProgressResponseBody(
                                                    originalResponse.body(),
                                                    new ProgressResponseBody.ProgressListener() {
                                                        @Override
                                                        public void update(final long bytesRead, final long contentLength, boolean done) {
                                                            int newPercent = (int) Math.round(((double) bytesRead / (double) contentLength) * 100.0d);
                                                            if (done) {
                                                            } else {
                                                                if (listenerHandler != null && listener != null) {
                                                                    listenerHandler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            listener.onRemoteOperationProgress(
                                                                                    DownloadOperation.this,
                                                                                    bytesRead,
                                                                                    contentLength
                                                                            );
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }
                                                    }
                                            )
                                    )
                                    .build();
                        }
                    });
            if (mDebugInterceptor != null && mDebugInterceptor.get() != null) {
                builder.addNetworkInterceptor(mDebugInterceptor.get());
            }
            OkHttpClient okHttpClient = builder.build();
            try {
                mCall = okHttpClient.newCall(request);
                Response response = mCall.execute();
                BufferedSink sink = Okio.buffer(Okio.sink(targetFile));
                sink.writeAll(response.body().source());
                sink.close();
            } catch (final IOException e) {
                if (listenerHandler != null && listener != null) {
                    listenerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onRemoteOperationFinish(
                                    DownloadOperation.this,
                                    new RemoteOperationResult(e)
                            );
                        }
                    });
                }
            }

            // Maybe should call mDownloadListener.onFinished() here.
        } catch (final IOException ex) {
            // Send error broadcast here to dismiss progress dialog and show error message
            if (listenerHandler != null && listener != null) {
                listenerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onRemoteOperationFinish(
                                DownloadOperation.this,
                                new RemoteOperationResult(ex)
                        );
                    }
                });
            }
        } finally {
            // IOUtils.closeQuietly(downloadStream);
        }
        return new RemoteOperationResult(RemoteOperationResult.ResultCode.OK);
    }

    public String getSavePath() {
        return mSavePath;
    }
}
