package com.lvqingyang.scancloud;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lvqingyang.mylibrary.base.BaseActivity;
import com.lvqingyang.scancloud.base.AppContact;
import com.lvqingyang.scancloud.easy_ar.GLView;
import com.lvqingyang.scancloud.easy_ar.OnTargetChangeListener;
import com.lvqingyang.scancloud.view.ScanView;

import cn.easyar.Engine;
import cn.easyar.Target;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

@RuntimePermissions
public class MainActivity extends BaseActivity {

    private android.widget.FrameLayout flpreview;
    private com.lvqingyang.scancloud.view.ScanView svmain;
    private GLView mGLView;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle paramBundle) {
        if (!Engine.initialize(this, AppContact.key)) {
            Log.e("HelloAR", "Initialization Failed.");
        }
        super.onCreate(paramBundle);
    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    @Override
    protected void initView() {
        this.svmain = (ScanView) findViewById(R.id.sv_main);
        svmain.post(new Runnable() {
            @Override
            public void run() {
                svmain.startScan();
            }
        });
        this.flpreview = (FrameLayout) findViewById(R.id.fl_preview);

        mGLView=new GLView(
                this,
                AppContact.cloud_server_address,
                AppContact.cloud_key,
                AppContact.cloud_secret);

        //有了权限再加
        flpreview.addView(mGLView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    protected void setListener() {
        mGLView.setOnTargetStatusChangeListener(new OnTargetChangeListener() {
            @Override
            public void targetChange(Target target) {
                if (BuildConfig.DEBUG) Log.d(TAG, "targetChange: "+target.name());
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
    protected void onResume()
    {
        super.onResume();
        if (mGLView != null) { mGLView.onResume(); }
    }

    @Override
    protected void onPause()
    {
        if (mGLView != null) { mGLView.onPause(); }
        super.onPause();
    }
}
