package com.yu.myretrofitdownload.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yu.myretrofitdownload.R;
import com.yu.myretrofitdownload.progress.ProgressInfo;
import com.yu.myretrofitdownload.progress.ProgressListener;
import com.yu.myretrofitdownload.progress.ProgressManager;
import com.yu.myretrofitdownload.util.SpeedUtils;

public class AnotherProgressActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView tvSpeed;

    private String url;
    private ProgressListener progressListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_progress);

        initView();

        initListener();
    }

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.another_progress_bar);
        tvProgress = (TextView) findViewById(R.id.tv_another_progress);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
    }

    private void initListener() {
        url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            progressListener = new ProgressListener() {
                @Override
                public void onError(long id, Exception e) {
                    tvProgress.setText("下载失败");
                    tvSpeed.setText("");
                }

                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    if (progressInfo != null) {
                        int percent = progressInfo.getPercent();
                        progressBar.setProgress(percent);
                        tvProgress.setText(percent + " %");
                        tvSpeed.setText(SpeedUtils.bytes2kb(progressInfo.getSpeed()));

                        if (progressInfo.isFinish()) {
                            tvProgress.setText("下载完成");
                        }
                    }
                }
            };

            ProgressManager.getInstance().addResponseListener(url, progressListener);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressListener != null) {
            ProgressManager.getInstance().removeResponseListener(url, progressListener);
        }
    }
}
