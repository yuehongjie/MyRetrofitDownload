package com.yu.myretrofitdownload.progress;

/**
 * Created by Administrator on 2017-11-28.
 * 进度监听回调
 */

public interface ProgressListener {

    void onError(long id, Exception e);

    void onProgress(ProgressInfo mProgressInfo);
}
