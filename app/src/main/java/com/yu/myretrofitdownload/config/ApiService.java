package com.yu.myretrofitdownload.config;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by yu on 2017/11/29.
 * api
 */

public interface ApiService {

    @Streaming
    @GET
    Call<ResponseBody> download(@Url String url);
}
