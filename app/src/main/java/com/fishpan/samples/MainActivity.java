package com.fishpan.samples;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.fishpan.widget.XRecyclerView;

public class MainActivity extends AppCompatActivity {
    private XRecyclerView xRecyclerView;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        xRecyclerView = (XRecyclerView) findViewById(R.id.xrecyclerview);
        xRecyclerView.addHeader(getLayoutInflater().inflate(R.layout.layout_header, xRecyclerView, false));
        xRecyclerView.addFooter(getLayoutInflater().inflate(R.layout.layout_header, xRecyclerView, false));
        xRecyclerView.setAdapter(new CustomerAdapter(this));
        xRecyclerView.setEnablePullRefresh(true);
        xRecyclerView.setListener(new XRecyclerView.IRecycleViewListener() {
            @Override
            public void onRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xRecyclerView.stopRefresh();
                    }
                }, 3000);
            }

            @Override
            public void onLoadMore() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        xRecyclerView.stopLoading();
                    }
                }, 3000);
            }
        });
    }
}
