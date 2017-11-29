package com.yu.myretrofitdownload.config;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * Created by yu on 2017/11/29.
 * Retrofit 获取
 */

public class RetrofitUtils {

    private static Retrofit mRetrofit;
    private static OkHttpClient.Builder mBuilder;

    public static Retrofit getRetrofit(){

        if (mRetrofit == null) {
            synchronized (RetrofitUtils.class) {
                if (mRetrofit == null) {
                    mRetrofit = new Retrofit.Builder()
                            .baseUrl("http:aaabbbccc//")
                            .client(getOkHttpClientBuilder().build())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .build();
                }
            }
        }

        return mRetrofit;
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        if (mBuilder == null) {
            synchronized (RetrofitUtils.class){
                if (mBuilder == null) {
                    mBuilder = new OkHttpClient.Builder();
                }
            }
        }
        return mBuilder;
    }

}
