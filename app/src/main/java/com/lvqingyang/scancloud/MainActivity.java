package com.lvqingyang.scancloud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lvqingyang.mylibrary.base.BaseActivity;
import com.lvqingyang.mylibrary.tool.MyToast;
import com.lvqingyang.scancloud.base.AppContact;
import com.lvqingyang.scancloud.bean.TargetMeta;
import com.lvqingyang.scancloud.ar.GLView;
import com.lvqingyang.scancloud.ar.OnTargetChangeListener;
import com.lvqingyang.scancloud.helper.IntentHelper;
import com.lvqingyang.scancloud.view.ScanView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.easyar.Engine;
import cn.easyar.FunctorOfVoidFromRecordStatusAndString;
import cn.easyar.RecordStatus;
import cn.easyar.Target;
import io.reactivex.functions.Consumer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private android.widget.FrameLayout flpreview;
    private GLView mGLView;
    private com.lvqingyang.scancloud.view.ScanView sv;
    private boolean mIsFlashOn = false;
    private android.widget.TextView tvfindar;
    private View viewclick;
    private android.widget.ImageView ivstartrecord;
    private android.widget.ImageView ivstoprecord;
    private android.widget.LinearLayout llvideo;
    private boolean mIsRecording = false;
    private TargetMeta mCurTargetMeta;
    private DateFormat mDateFormat=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private String mUrl = "";

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
        this.llvideo = (LinearLayout) findViewById(R.id.ll_video);
        this.ivstoprecord = (ImageView) findViewById(R.id.iv_stop_record);
        this.ivstartrecord = (ImageView) findViewById(R.id.iv_start_record);
        this.viewclick = (View) findViewById(R.id.view_click);
        this.tvfindar = (TextView) findViewById(R.id.tv_find_ar);
        this.flpreview = (FrameLayout) findViewById(R.id.fl_preview);
        this.sv = (ScanView) findViewById(R.id.sv);

        mGLView = new GLView(
                this,
                AppContact.cloud_server_address,
                AppContact.cloud_key,
                AppContact.cloud_secret);

        addGLView();
    }

    @Override
    protected void setListener() {
        ivstartrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });

        ivstoprecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });

        viewclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurTargetMeta != null && !mIsRecording) {
                    String url=mCurTargetMeta.getUrl();
                    if (!TextUtils.isEmpty(url)) {
                        IntentHelper.openBrower(MainActivity.this, url);
                    }
                }
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
            case R.id.item_switch:
                mGLView.switchCamera();
                return true;
            case R.id.item_flashlight:
                if (mIsFlashOn) {
                    paramMenuItem.setIcon(R.drawable.ic_flash_on);
                    mGLView.setFlashState(false);
                } else {
                    paramMenuItem.setIcon(R.drawable.ic_flash_off);
                    mGLView.setFlashState(true);
                }
                mIsFlashOn = !mIsFlashOn;
                return true;
            default:
        }
        return super.onOptionsItemSelected(paramMenuItem);
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
                                public void targetChange(final Target target, final TargetMeta meta) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mCurTargetMeta = meta;
                                            Toast.makeText(MainActivity.this, "target改变：" + target.name(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void targetLost() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "lost", Toast.LENGTH_SHORT).show();
                                            startScan();
                                        }
                                    });
                                }

                                @Override
                                public void targetTrack() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "track", Toast.LENGTH_SHORT).show();
                                            stopScan();
                                        }
                                    });
                                }
                            });
                        } else {
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
    protected void onPause() {
        if (mGLView != null) {
            mGLView.onPause();
        }
        sv.stopScan();
        if (mIsRecording) {
            stopRecord();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void startScan() {
        if (mIsRecording) {
            stopRecord();
        }
        llvideo.setVisibility(View.GONE);

        tvfindar.setVisibility(View.VISIBLE);
        sv.setVisibility(View.VISIBLE);
        sv.startScan();
    }

    private void stopScan() {
        sv.stopScan();
        sv.setVisibility(View.GONE);
        tvfindar.setVisibility(View.GONE);

        llvideo.setVisibility(View.VISIBLE);
    }

    private void startRecord() {
        mUrl = prepareUrl();

        mGLView.startRecording(mUrl, new FunctorOfVoidFromRecordStatusAndString() {
            @Override
            public void invoke(int status, String value) {
                switch (status) {
                    case RecordStatus.OnStarted:
                        mIsRecording = true;
                        ivstartrecord.setVisibility(View.GONE);
                        ivstoprecord.setVisibility(View.VISIBLE);
                        MyToast.info(MainActivity.this, R.string.recording);
                        break;
                    case RecordStatus.OnStopped:
                        break;
                    case RecordStatus.FileFailed:
                        MyToast.error(MainActivity.this, R.string.error);
                        break;
                    default:
                        break;

                }
                Log.i("HelloAR", "Recorder Callback status: " + Integer.toString(status) + ", MSG: " + value);
            }
        });
    }

    private void stopRecord() {
        mIsRecording = false;
        ivstoprecord.setVisibility(View.GONE);
        ivstartrecord.setVisibility(View.VISIBLE);
        mGLView.stopRecording();
        Toast.makeText(MainActivity.this, "Recorded at " + mUrl, Toast.LENGTH_LONG).show();
    }

    private String prepareUrl() {
        String timeStamp = mDateFormat.format(new Date());
        String FileName = timeStamp + ".mp4";
        return AppContact.FOLDER.getPath()+"/" + FileName;
    }
}
