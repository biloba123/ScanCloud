package com.lvqingyang.scancloud.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lvqingyang.scancloud.R;

/**
 * @author Lv Qingyang
 * @date 2018/6/9
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @see
 * @since
 */
public class ScanView extends FrameLayout {
    private android.widget.ImageView ivscan;
    private android.widget.TextView tvscaninfo;
    private AnimationDrawable mAnimationDrawable;


    public ScanView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_scan, this);
        this.tvscaninfo = (TextView) findViewById(R.id.tv_scan_info);
        this.ivscan = (ImageView) findViewById(R.id.iv_scan);


        mAnimationDrawable = (AnimationDrawable) ivscan.getDrawable();
    }


    public void startScan() {
        mAnimationDrawable.start();
    }

    public void stopScan() {
        mAnimationDrawable.stop();
    }

}
