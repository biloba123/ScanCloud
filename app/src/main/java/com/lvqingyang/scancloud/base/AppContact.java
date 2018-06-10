package com.lvqingyang.scancloud.base;

import android.os.Environment;

import com.lvqingyang.mylibrary.tool.ExternalStorageUtils;

import java.io.File;

/**
 * @author Lv Qingyang
 * @date 2018/6/9
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @see
 * @since
 */
public class AppContact {
    public static String key = "brn8rOmcjkfQNtipCZpWD5vN9l30oLhsvjy53dfLoeho0Uqx7zBTyaf7iviixbdKp5Y2EYwKzKP6gYMr6eJJYYeFD1VpnnvjPvJmESvDRjE8ZNxt3bdHrKJJPN6de6JjHJstikoAKNxdOzl2E0qvvuXqFMyZ3whXed4hCcANh3bxO2Wcc8R8ZMdVIXpgA6dMn5Fzt3dF";
    public static String cloud_server_address = "214b8b66307aa425ec6eb172a9345d87.cn1.crs.easyar.com:8080";
    public static String cloud_key = "a1a17e9711edf6aded13a6d44f19888b";
    public static String cloud_secret = "jB9DzCEkWWOy0hkmZrFeTmbjW3DCtxTEvUXTJirevlUhSuw2zB6sVWjCJvlqg7cJHZKPdfcTwFZ5F6qNIVw1x100QZZAdYV74i0MgL9B4fjgeQSWDswFpDcIQApMeB7l";

    public static File FOLDER= ExternalStorageUtils.getStorageDir("扫云", Environment.DIRECTORY_MOVIES);
}
