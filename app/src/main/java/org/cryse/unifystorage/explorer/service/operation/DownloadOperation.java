package org.cryse.unifystorage.explorer.service.operation;

import android.content.Context;
import android.os.Handler;

import org.cryse.unifystorage.RemoteFile;
import org.cryse.unifystorage.StorageProvider;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.model.StorageProviderInfo;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.OperationState;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperation;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperationResult;
import org.cryse.unifystorage.explorer.utils.http.ProgressResponseBody;
import org.cryse.unifystorage.utils.FileSizeUtils;
import org.cryse.unifystorage.utils.Path;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class DownloadOperation extends RemoteOperation<DownloadOperation.Params> {
    public static final String OP_NAME = "OP_DOWNLOAD";
    private Call mCall;
    private Request mRequest;
    private OkHttpClient mOkHttpClient;
    private WeakReference<Interceptor> mDebugInterceptor;

    public DownloadOperation(String token, DownloadOperation.Params params) {
        super(token, params);
    }

    public DownloadOperation(String token, Params params, OnOperationListener listener, Handler listenerHandler) {
        super(token, params, listener, listenerHandler);
    }

    @Override
    protected void prepareOperation() {
        super.prepareOperation();
        getSummary().currentItemSize = getParams().getRemoteFile().size();
    }

    @Override
    protected RemoteOperationResult runOperation() {
        StorageProvider storageProvider = getParams().getSourceStorageProvider();
        try {
            mRequest = storageProvider.download(getParams().getRemoteFile());
            final File targetFile = new File(getParams().getSavePath());
            String parentDirectoryPath = Path.getDirectory(getParams().getSavePath());
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
                                                        long lastReadBytes = 0;
                                                        long lastReportMs = 0;
                                                        int lastPercent = 0;
                                                        @Override
                                                        public void update(final long bytesRead, final long contentLength, boolean done) {
                                                            if(shouldCancel()) {
                                                                throw new CancellationException();
                                                            }
                                                            int newPercent = (int) Math.round(((double) bytesRead / (double) contentLength) * 100.0d);
                                                            if (done) {
                                                            } else {
                                                                if (/*lastReadBytes == 0
                                                                        || bytesRead - lastReadBytes > 1024 * 512
                                                                        || newPercent > lastPercent
                                                                        || */System.currentTimeMillis() - lastReportMs > 1000 // A least report once for every 3 seconds
                                                                        ) {
                                                                    notifyOperationProgress(
                                                                            bytesRead,
                                                                            contentLength,
                                                                            bytesRead - lastReadBytes,
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            0
                                                                    );
                                                                    lastReportMs = System.currentTimeMillis();
                                                                    lastReadBytes = bytesRead;
                                                                }
                                                                lastPercent = newPercent;
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
            mOkHttpClient = builder.build();
            try {
                mCall = mOkHttpClient.newCall(mRequest);
                Response response = mCall.execute();
                BufferedSink sink = Okio.buffer(Okio.sink(targetFile));
                sink.writeAll(response.body().source());
                sink.close();
            } catch (final IOException e) {
                return new RemoteOperationResult(e);
            } catch (final CancellationException e) {
                return new RemoteOperationResult(e);
            }
            // Maybe should call mDownloadListener.onFinished() here.
        } catch (final IOException ex) {
            return new RemoteOperationResult(ex);
        } finally {
            // IOUtils.closeQuietly(downloadStream);
        }
        return new RemoteOperationResult(RemoteOperationResult.ResultCode.OK);
    }

    @Override
    public String getSummaryTitle(Context context) {
        return context.getString(R.string.operation_title_downloading_file);
    }

    @Override
    public String getSummaryContent(Context context) {
        String content = "";
        switch (getSummary().state) {
            case NEW:
            case PREPARING:
                content = context.getString(R.string.operation_content_preparing);
                break;
            case RUNNING:
                if(getSummaryProgress() < 0.5) {
                    content = context.getString(R.string.operation_content_connecting);
                } else {
                    content = context.getString(
                            R.string.operation_content_downloading_file,
                            getParams().getRemoteFile().getName(),
                            FileSizeUtils.humanReadableByteCount(getSummary().currentItemReadSize, false),
                            FileSizeUtils.humanReadableByteCount(getSummary().currentItemSize, false)
                    );
                }
                break;
        }
        return content;
    }

    @Override
    public String getSimpleSummaryContent(Context context) {
        return getSummaryContent(context);
    }

    @Override
    public double getSummaryProgress() {
        return getSummary().currentSizePercent * 100.0d;
    }

    @Override
    public String getSummaryCompletedTitle(Context context) {
        if(getState() == OperationState.COMPLETED) {
            return context.getString(R.string.operation_title_download_completed);
        } else if(getState() == OperationState.FAILED) {
            return context.getString(R.string.operation_title_download_completed);
        } else {
            return "";
        }
    }

    @Override
    public String getSummaryCompletedContent(Context context) {
        if(getState() == OperationState.COMPLETED) {
            return context.getString(R.string.operation_content_download_completed, getParams().getRemoteFile().getName());
        } else if(getState() == OperationState.FAILED) {
            return context.getString(R.string.operation_content_download_failed, getParams().getRemoteFile().getName());
        } else {
            return "";
        }
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

    public String getSavePath() {
        return getParams().getSavePath();
    }

    public static class Params extends RemoteOperation.Params {
        private RemoteFile mRemoteFile;
        private String mSavePath;
        public Params(
                Context context,
                StorageProviderInfo sourceProviderInfo,
                StorageProviderInfo targetProviderInfo,
                RemoteFile remoteFile,
                String savePath
        ) {
            super(context, sourceProviderInfo, targetProviderInfo);
            this.mRemoteFile = remoteFile;
            this.mSavePath = savePath;
        }

        public RemoteFile getRemoteFile() {
            return mRemoteFile;
        }

        public String getSavePath() {
            return mSavePath;
        }
    }
}
