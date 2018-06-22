package com.lvqingyang.scancloud.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * @author Lv Qingyang
 * @date 2018/6/10
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @see
 * @since
 */
public class IntentHelper {
    public static void openBrower(Activity activity, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (hasRelateApp(activity, intent)) {
            activity.startActivity(intent);
        }
    }

    public static void shareText(Activity activity, String text){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setType("text/plain");
        //设置分享列表的标题，并且每次都显示分享列表
        activity.startActivity(Intent.createChooser(intent, "分享到"));
    }

    public static boolean hasRelateApp(Activity activity, Intent intent){
        return intent.resolveActivity(activity.getPackageManager())!=null;
    }

}
