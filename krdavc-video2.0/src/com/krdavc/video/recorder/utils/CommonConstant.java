package com.krdavc.video.recorder.utils;

import android.os.Environment;

/**
 * 公共的常量
 *
 * @author danielgaoxz
 */
public class CommonConstant
{

    /**
     * keydown促发多少次就是长按
     */
    public static final int LongClickRepeatCount = 10;


    private static String VIDEO_PATH = Environment.getExternalStorageDirectory().getPath()
        + "/router/";
}
