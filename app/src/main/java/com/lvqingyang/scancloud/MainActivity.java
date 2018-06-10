package com.lvqingyang.scancloud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lvqingyang.mylibrary.base.BaseActivity;
import com.lvqingyang.scancloud.base.AppContact;
import com.lvqingyang.scancloud.easy_ar.GLView;
import com.lvqingyang.scancloud.easy_ar.OnTargetChangeListener;
import com.lvqingyang.scancloud.view.ScanView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.easyar.Engine;
import cn.easyar.Target;
import io.reactivex.functions.Consumer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private android.widget.FrameLayout flpreview;
    private GLView mGLView;
    private com.lvqingyang.scancloud.view.ScanView sv;

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

    @Override
    protected void initView() {
        this.flpreview = (FrameLayout) findViewById(R.id.fl_preview);
        this.sv = (ScanView) findViewById(R.id.sv);

        mGLView = new GLView(
                this,
                AppContact.cloud_server_address,
                AppContact.cloud_key,
                AppContact.cloud_secret);

        addGLView();
    }

    @SuppressLint("CheckResult")
    private void addGLView() {
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isGranted) throws Exception {
                        if (isGranted) {
                            flpreview.addView(mGLView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                            mGLView.setOnTargetStatusChangeListener(new OnTargetChangeListener() {
                                @Override
                                public void targetChange(final Target target) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "target改变："+target.name(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void targetLost() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "lost", Toast.LENGTH_SHORT).show();
                                            sv.startScan();
                                        }
                                    });
                                }

                                @Override
                                public void targetTrack() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "track", Toast.LENGTH_SHORT).show();
                                            sv.stopScan();
                                        }
                                    });
                                }
                            });
                        }else {
                            showPermissionInfoHint();
                        }
                    }
                });
    }

    private void showPermissionInfoHint() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.hint)
                .setMessage(R.string.need_permission)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addGLView();
                    }
                })
                .show();
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
    }

    @Override
    protected void bindData() {

    }

    @Override
    protected void onPause() {
        if (mGLView != null) {
            mGLView.onPause();
        }
        sv.stopScan();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLView != null) {
            mGLView.onResume();
        }
        sv.startScan();
    }
}
