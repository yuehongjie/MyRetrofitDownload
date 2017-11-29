package com.yu.myretrofitdownload;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.yu.myretrofitdownload.test.TestActivity;
import com.yu.myretrofitdownload.test.TestProgressActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView(){
        findViewById(R.id.btn_progress).setOnClickListener(this);
        findViewById(R.id.btn_test).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_progress:
                go2Activity(TestProgressActivity.class);
                break;
            case R.id.btn_test:
                go2Activity(TestActivity.class);
        }
    }

    private void go2Activity(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }

}
