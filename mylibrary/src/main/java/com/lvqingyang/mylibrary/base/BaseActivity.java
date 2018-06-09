package com.lvqingyang.mylibrary.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Activity基类
 *
 * @author Lv Qingyang
 * @date 2018/6/8
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @since
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle paramBundle) {
        super.onCreate(paramBundle);
        Bundle localBundle = getIntent().getExtras();
        if (localBundle != null) {
            getBundleExtras(localBundle);
        }

        setContentView(getLayoutId());
        initView();
        setListener();
        initData();
        bindData();
    }

    protected abstract void getBundleExtras(Bundle bundle);

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void setListener();

    protected abstract void initData();

    protected abstract void bindData();

    public void initToolbar(Toolbar toolbar, String title, boolean isDisplayHome) {
        setSupportActionBar(toolbar);
        ActionBar localActionBar = getSupportActionBar();
        if (localActionBar != null) {
            localActionBar.setTitle(title);
            localActionBar.setDisplayHomeAsUpEnabled(isDisplayHome);
        }
    }


    protected void initeActionbar(int titleId, boolean isDisplayHome) {
        initeActionbar(getString(titleId), isDisplayHome);
    }

    protected void initeActionbar(String title, boolean isDisplayHome) {
        ActionBar localActionBar = getSupportActionBar();
        if (localActionBar != null) {
            localActionBar.setTitle(title);
            localActionBar.setDisplayHomeAsUpEnabled(isDisplayHome);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        if (paramMenuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}