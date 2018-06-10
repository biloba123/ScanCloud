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

    public static boolean hasRelateApp(Activity activity, Intent intent){
        return intent.resolveActivity(activity.getPackageManager())!=null;
    }


}
