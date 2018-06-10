package com.lvqingyang.mylibrary.tool;

import com.google.gson.Gson;

/**
 * @author Lv Qingyang
 * @date 2018/6/10
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @see
 * @since
 */
public class MyJson {
    public static Gson mGson=new Gson();

    public static <T> T fromJson(String json, Class<T> tClass){
        return mGson.fromJson(json, tClass);
    }
}
