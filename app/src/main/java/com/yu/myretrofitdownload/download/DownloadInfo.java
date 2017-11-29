package com.yu.myretrofitdownload.download;

/**
 * Created by yu on 2017/11/29.
 * 下载信息
 */

public class DownloadInfo {

    private String url; //下载连接
    private String savePath; // 保存的位置


    private long currentBytes; //当前已上传或下载的总长度
    private long contentLength; //数据总长度
    private boolean finish; //进度是否完成
}
