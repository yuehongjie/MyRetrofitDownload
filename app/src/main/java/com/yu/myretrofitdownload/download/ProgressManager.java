package com.yu.myretrofitdownload.download;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017-11-28.
 * 文件下载、上传进度监听  最好仅用于提示进度
 *
 */

public class ProgressManager {

    /** 默认刷新进度的时间，防止高频率调用，减少压力 */
    private static final int DEFAULT_REFRESH_TIME = 150;
    /** 进度管理器 单例 */
    private static volatile ProgressManager mProgressManager;
    //重定向后的地址
    public static final String LOCATION_HEADER = "Location";

    /** 主线程 handler 用于分发进度事件，这样就不用再切换线程了*/
    private Handler mHandler;

    /** 定义拦截器，获取所拦截的链接，是否是需要监听的下载链接，如果是，则设置进度监听*/
    private Interceptor mProgressIntercept;

    private int mRefreshTime = DEFAULT_REFRESH_TIME; //进度刷新时间(单位ms),避免高频率调用

    /**
     *  可以对同一个下载链接 添加多个监听器，但是需要注意自己处理不要重复添加同一个监听器
     */
    private Map<String, List<ProgressListener>> mRequestListeners = new WeakHashMap<>();
    private Map<String, List<ProgressListener>> mResponseListeners = new WeakHashMap<>();

    private ProgressManager(){
        mHandler = new Handler(Looper.getMainLooper());
        mProgressIntercept = new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                return wrapResponseBody(chain.proceed(wrapRequestBody(chain.request())));
            }
        };
    }

    // 单例
    public static ProgressManager getInstance(){
        if (mProgressManager == null) {
            synchronized (ProgressManager.class) {
                if (mProgressManager == null) {
                    mProgressManager = new ProgressManager();
                }
            }
        }

        return mProgressManager;
    }

    /**
     * 从拦截的 Response 中取出请求的链接，检查该链接是否设置了下载进度监听器，
     * 如果是，则创建新的 ResponseBody ，进行下载进度的回调
     *
     */
    private Response wrapResponseBody(Response response) {
        if (response == null) {
            return null;
        }

        if (response.body() == null) {
            return response;
        }

        //取出该请求的链接，查看是否需要进行监听
        String key = response.request().url().toString();

        //判断链接是否被重定向了 ,是的话需要把原链接的监听器，设置给重定向后的链接
        if (haveRedirect(response)) {
            resolveRedirect(mRequestListeners, response, key);
            resolveRedirect(mResponseListeners, response, key);
        }

        if (mResponseListeners.containsKey(key)) { //设置了监听器，则取出该 url 对应的监听器，进行监听

            List<ProgressListener> downloadProgressListeners = mResponseListeners.get(key);
            if (downloadProgressListeners != null && downloadProgressListeners.size() > 0) {
                //对需要监听的链接在 ResponseBody 中进行监听回调
                return response.newBuilder()
                        .body(new ProgressResponseBody(mHandler, response.body(), downloadProgressListeners, mRefreshTime))
                        .build();
            }
        }

        return response;
    }

    /**
     * 从拦截的 Request 中，取出请求的 url， 判断该 url 是否添加了上传进度监听器，
     * 如果是，那么就创建新的 RequestBody ，对上传进度进行监听回调
     */
    public Request wrapRequestBody(Request request) {
        if (request == null){
            return null;
        }
        if (request.body() == null) {
            return request;
        }

        String key = request.url().toString();

        if (mRequestListeners.containsKey(key)) {
            List<ProgressListener> uploadProgressListeners = mRequestListeners.get(key);
            if (uploadProgressListeners != null && uploadProgressListeners.size() > 0) {
                return request.newBuilder()
                        .method(request.method(), new ProgressRequestBody(mHandler, request.body(), uploadProgressListeners, mRefreshTime))
                        .build();
            }
        }
        return request;
    }


    /**
     * 链接是否重定向了，是的话，需要把原链接的监听器，设置给重定向后的链接
     *
     * @param response 原始的 {@link Response}
     * @return 是否重定向了
     */
    private boolean haveRedirect(Response response) {
        String status = String.valueOf(response.code());
        if (TextUtils.isEmpty(status))
            return false;
        if (status.contains("301") || status.contains("302") || status.contains("303") || status.contains("307")) {
            return true;
        }
        return false;
    }

    /**
     * 处理重定向地址的监听
     * @param mapListeners 监听器
     * @param response 原始的 {@link Response}
     * @param key 原地址
     */
    private void resolveRedirect(Map<String, List<ProgressListener>> mapListeners, Response response, String key){
        List<ProgressListener> progressListeners = mapListeners.get(key); //获取原链接的监听器
        if (progressListeners != null && progressListeners.size() > 0) {
            String location = response.header(LOCATION_HEADER);//重定向后的地址
            if (!TextUtils.isEmpty(location)) {
                if (!mapListeners.containsKey(location)) {//把原链接的监听，添加给新的链接
                    mapListeners.put(location, progressListeners);
                }else { //已经存在重定向后的监听器，只添加新的
                    List<ProgressListener> locationListeners = mapListeners.get(location);
                    for (ProgressListener listener : progressListeners) {
                        if (!locationListeners.contains(listener)) {
                            locationListeners.add(listener);
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置 {@link ProgressListener#onProgress(ProgressInfo)} 每次被调用的间隔时间
     *
     * @param refreshTime 间隔时间,单位毫秒
     */
    public void setRefreshTime(int refreshTime) {
        this.mRefreshTime = refreshTime;
    }

    /**
     * 将需要被监听上传进度的 {@code url} 注册到管理器,此操作请在页面初始化时进行,切勿多次注册同一个(内容相同)监听器
     *
     * @param url      {@code url} 作为标识符
     * @param listener 当此 {@code url} 地址存在上传的动作时,此监听器将被调用
     */
    public void addRequestListener(String url, ProgressListener listener) {
        List<ProgressListener> uploadProgressListeners;
        synchronized (ProgressManager.class) {
            uploadProgressListeners = mRequestListeners.get(url);
            if (uploadProgressListeners == null) {
                uploadProgressListeners = new LinkedList<>();
                mRequestListeners.put(url, uploadProgressListeners);
            }
        }
        uploadProgressListeners.add(listener);
    }

    /**
     * 将需要被监听下载进度的 {@code url} 注册到管理器,此操作请在页面初始化时进行,切勿多次注册同一个(内容相同)监听器
     *
     * @param url      {@code url} 作为标识符
     * @param listener 当此 {@code url} 地址存在下载的动作时,此监听器将被调用
     */
    public void addResponseListener(String url, ProgressListener listener) {
        List<ProgressListener> progressListeners;
        synchronized (ProgressManager.class) {
            progressListeners = mResponseListeners.get(url);
            if (progressListeners == null) {
                progressListeners = new LinkedList<>();
                mResponseListeners.put(url, progressListeners);
            }
        }
        progressListeners.add(listener);
    }


    /**
     * 当在 {@link ProgressRequestBody} 和 {@link ProgressResponseBody} 内部处理二进制流时发生错误
     * 会主动调用 {@link ProgressListener#onError(long, Exception)},但是有些错误并不是在它们内部发生的
     * 但同样会引起网络请求的失败,所以向外面提供{@link ProgressManager#notifyOnErorr},当外部发生错误时
     * 手动调用此方法,以通知所有的监听器
     *
     * @param url {@code url} 作为标识符
     * @param e   错误
     */
    public void notifyOnErorr(String url, Exception e) {
        forEachListenersOnError(mRequestListeners, url, e);
        forEachListenersOnError(mResponseListeners, url, e);
    }

    /**
     * 对指定的 url 的监听器 发出错误通知
     */
    private void forEachListenersOnError(Map<String, List<ProgressListener>> map, String url, Exception e) {
        if (map.containsKey(url)) {
            List<ProgressListener> progressListeners = map.get(url);
            for (ProgressListener listener : progressListeners) {
                listener.onError(-1, e);
            }
        }
    }

}
