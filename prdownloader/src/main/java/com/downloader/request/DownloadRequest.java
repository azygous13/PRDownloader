/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.downloader.request;

import com.downloader.DownloadListener;
import com.downloader.Error;
import com.downloader.PRDownloaderConfig;
import com.downloader.PauseListener;
import com.downloader.Priority;
import com.downloader.ProgressListener;
import com.downloader.core.Core;
import com.downloader.internal.ComponentHolder;
import com.downloader.internal.DownloadRequestQueue;
import com.downloader.utils.Utils;

import java.util.concurrent.Future;

/**
 * Created by amitshekhar on 13/11/17.
 */

public class DownloadRequest {

    private Priority priority;
    private Object tag;
    private String url;
    private String dirPath;
    private String fileName;
    private int sequenceNumber;
    private Future future;
    private long downloadedBytes;
    private long totalBytes;
    private boolean paused;
    private int readTimeout;
    private int connectTimeout;
    private ProgressListener progressListener;
    private DownloadListener downloadListener;
    private PauseListener pauseListener;
    private int downloadId;

    DownloadRequest(DownloadRequestBuilder builder) {
        this.url = builder.url;
        this.dirPath = builder.dirPath;
        this.fileName = builder.fileName;
        this.priority = builder.priority;
        this.tag = builder.tag;
        this.readTimeout =
                builder.readTimeout != 0 ?
                        builder.readTimeout :
                        getReadTimeoutFromConfig();
        this.connectTimeout =
                builder.connectTimeout != 0 ?
                        builder.connectTimeout :
                        getConnectTimeoutFromConfig();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public DownloadRequest setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public DownloadRequest setPauseListener(PauseListener pauseListener) {
        this.pauseListener = pauseListener;
        return this;
    }

    public int start(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        DownloadRequestQueue.getInstance().addRequest(this);
        downloadId = Utils.getUniqueId(url, dirPath, fileName);
        return downloadId;
    }

    public void deliverError(final Error error) {
        Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
            public void run() {
                if (downloadListener != null) {
                    downloadListener.onError(error);
                }
                finish();
            }
        });
    }

    public void deliverSuccess() {
        Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
            public void run() {
                if (downloadListener != null) {
                    downloadListener.onDownloadComplete();
                }
                finish();
            }
        });
    }

    public void deliverPauseEvent() {
        Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
            public void run() {
                if (pauseListener != null) {
                    pauseListener.onPause();
                }
            }
        });
    }

    private void finish() {
        destroy();
        DownloadRequestQueue.getInstance().finish(this);
    }

    private void destroy() {
        this.progressListener = null;
        this.downloadListener = null;
        this.pauseListener = null;
    }

    private int getReadTimeoutFromConfig() {
        PRDownloaderConfig config = ComponentHolder.getInstance().getConfig();
        return config.getReadTimeout();
    }

    private int getConnectTimeoutFromConfig() {
        PRDownloaderConfig config = ComponentHolder.getInstance().getConfig();
        return config.getConnectTimeout();
    }
}