package com.lvqingyang.scancloud;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lvqingyang.mylibrary.base.BaseActivity;
import com.lvqingyang.mylibrary.tool.MyOkHttp;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindARActivity extends BaseActivity {
    private static final String TAG = "FindARActivity";
    private com.youth.banner.Banner banner;
    private android.widget.LinearLayout llcase;
    private android.widget.Button btnmorecases;
    private android.widget.Button btnaboutsc;

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_find_ar;
    }

    @Override
    protected void initView() {
        initeActionbar(R.string.find_ar, true);

        this.btnaboutsc = (Button) findViewById(R.id.btn_about_sc);
        this.btnmorecases = (Button) findViewById(R.id.btn_more_cases);
        this.llcase = (LinearLayout) findViewById(R.id.ll_case);
        this.banner = (Banner) findViewById(R.id.banner);
    }

    @Override
    protected void setListener() {
        btnaboutsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FindARActivity.this, AboutActivity.class));
            }
        });
    }

    @Override
    protected void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MyOkHttp.NameValuePair> pairs = new ArrayList<>();
                pairs.add(new MyOkHttp.NameValuePair("count", "2"));
                pairs.add(new MyOkHttp.NameValuePair("first", "0"));
                try {
                    String s = MyOkHttp.getInstance().postForm("http://111.230.247.95/AR_CLOUD/series.php", pairs);
                    if (BuildConfig.DEBUG) Log.d(TAG, "run: " + s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void bindData() {
        Integer[] images={
                R.mipmap.sea, R.mipmap.forests, R.mipmap.jungle
        };
        String[] titles={
                "sea", "forests", "jungle"
        };
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE)
                .setImageLoader(new GlideImageLoader())
                .setImages(Arrays.asList(images))
                .setBannerTitles(Arrays.asList(titles))
                .setBannerAnimation(Transformer.Stack)
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }
}
