package com.lvqingyang.mylibrary.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment基类
 * @author Lv Qingyang
 * @since
 * @date 2018/6/8
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 */


public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        是否保留Fragment
//        setRetainInstance(true);

//        是否有menu，然后重写onCreateOptionsMenu, onOptionsItemSelected
        setHasOptionsMenu(true);
    }


    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (null != getArguments()) {
            getBundleExtras(getArguments());
        }
        View view = initContentView(inflater, container, savedInstanceState);
        initView(view);
        setListener();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        bindData();
    }

    // 初始化UI
    protected abstract View initContentView(LayoutInflater inflater, @Nullable ViewGroup container,
                                            @Nullable Bundle savedInstanceState);

    // 初始化控件
    protected abstract void initView(View view);


     //设置监听器
    protected abstract void setListener();

    // 数据
    protected abstract void initData();

    //为View设置数据
    protected abstract void bindData();

    /**
     * 获取bundle信息
     *
     * @param bundle
     */
    protected abstract void getBundleExtras(Bundle bundle);


    //初始化toolbar
    public void initToolbar(Toolbar toolbar, String title, boolean isDisplayHomeAsUp) {
        Activity activity=getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity)activity).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity)activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(title);
                actionBar.setDisplayHomeAsUpEnabled(isDisplayHomeAsUp);
            }
        }
    }

}
