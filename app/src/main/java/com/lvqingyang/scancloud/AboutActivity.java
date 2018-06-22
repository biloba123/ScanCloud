package com.lvqingyang.scancloud;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.lvqingyang.mylibrary.base.BaseActivity;
import com.lvqingyang.scancloud.helper.IntentHelper;

public class AboutActivity extends BaseActivity {

    private android.widget.Button btnfork;

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initView() {
        initeActionbar(R.string.about_sc, true);
        this.btnfork = (Button) findViewById(R.id.btn_fork);
    }

    @Override
    protected void setListener() {
        btnfork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindData() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        switch (paramMenuItem.getItemId()) {
            case R.id.item_share:
                IntentHelper.shareText(AboutActivity.this, getString(R.string.text_share));
                break;
            default:
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }
}
