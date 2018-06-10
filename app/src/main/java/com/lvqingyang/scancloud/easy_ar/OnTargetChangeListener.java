package com.lvqingyang.scancloud.easy_ar;

import cn.easyar.Target;

/**
 * @author Lv Qingyang
 * @date 2018/6/9
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 * @see
 * @since
 */
public interface OnTargetChangeListener {
    void targetChange(Target target);
    void targetLost();
    void targetTrack();
}
