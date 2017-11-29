package com.yu.myretrofitdownload.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yu.myretrofitdownload.R;
import com.yu.myretrofitdownload.config.ApiService;
import com.yu.myretrofitdownload.config.RetrofitUtils;
import com.yu.myretrofitdownload.progress.ProgressInfo;
import com.yu.myretrofitdownload.progress.ProgressListener;
import com.yu.myretrofitdownload.progress.ProgressManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class TestProgressActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private TextView tvProgress;
    private Button btnDownload;
    private Button btnOther;

    private String url = new String("http://gslb.miaopai.com/stream/6XbmP4ue7WL1-qE0ccLQVAdDApMNs71pH35aZw__.mp4");
    private String fileName = "someone like you with heart.mp4";
    private OkHttpClient okHttpClient;
    private boolean isDownloading; //控制不重复下载
    private ProgressListener progressListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_progress);

        initView();
        initProgressListener();
    }

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        btnDownload = (Button) findViewById(R.id.btn_download);
        btnOther = (Button) findViewById(R.id.btn_another);
        btnDownload.setOnClickListener(this);
        btnOther.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnDownload) {
            if (!isDownloading) {
                startDownload();
            }else {
                Toast.makeText(this, "还是不要重复下载的好", Toast.LENGTH_SHORT).show();
            }
        }else if (view == btnOther) {
            Intent intent = new Intent(this, AnotherProgressActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }

    private void startDownload() {

        isDownloading = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //下载请求
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    //同步执行请求
                    Response response = okHttpClient.newCall(request).execute();

                    //写入文件
                    InputStream is = response.body().byteStream();
                    //为了方便就不动态申请权限了,直接将文件放到CacheDir()中
                    File file = new File(getCacheDir(), fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    bis.close();
                    is.close();

                    isDownloading = false;

                }catch (Exception e) {
                    e.printStackTrace();

                    // 当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法
                    ProgressManager.getInstance().notifyOnError(url, e);

                    isDownloading = false;
                }
            }
        }).start();

    }

    private void initProgressListener(){

        //添加拦截器
        okHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder()).build();


        progressListener = new ProgressListener() {
            @Override
            public void onError(long id, Exception e) {
                tvProgress.setText("下载失败");
            }

            @Override
            public void onProgress(ProgressInfo progressInfo) {
                if (progressInfo != null) {
                    int percent = progressInfo.getPercent();
                    progressBar.setProgress(percent);
                    tvProgress.setText(percent + " %");

                    if (progressInfo.isFinish()) {
                        tvProgress.setText("下载完成");
                    }
                }
            }
        };
        ProgressManager.getInstance().addResponseListener(url, progressListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressManager.getInstance().removeResponseListener(url, progressListener);
    }

}
