package com.lvqingyang.scancloud.base;

import android.app.Application;
import android.content.Context;

/**
 * @author Lv Qingyang
 * @date 2018/6/11
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @see
 * @since
 */
public class MyApplication extends Application {
    private static Context sMyApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sMyApplication=this;
    }

    public static Context getContext() {
        return sMyApplication;
    }
}
