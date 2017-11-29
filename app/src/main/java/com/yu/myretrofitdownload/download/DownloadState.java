package com.yu.myretrofitdownload.download;

/**
 * Created by yu on 2017/11/29.
 * 下载状态
 */

public enum DownloadState {
    WAITING,        //等待状态
    START,          //开始下载
    DOWNLOADING,    //正在下载
    PAUSE,          //暂停
    CANCEL,         //取消
    ERROR,          //出错
    FINISH,         //完成
}
