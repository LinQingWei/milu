package com.krdavc.video.recorder;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import com.krdavc.video.recorder.utils.SDUtils;

import java.io.File;

public class VideoStore
{
    public static int getAvailableStore()
    {
        // 取得sdcard文件路径
        StatFs statFs;
        statFs = new StatFs(Environment.getExternalStorageDirectory()
            .getAbsolutePath());
        // 获取block的SIZE
        long blocSize = statFs.getBlockSize();
        // 获取BLOCK数量
        float totalBlocks = statFs.getBlockCount();
        // 可使用的Block的数量
        float availaBlock = statFs.getAvailableBlocks();
        float result = availaBlock / totalBlocks;
        result *= 100;

        Log.i("tag", "sdcard" + result);
        return (int) result;

    }

    static final int ERROR = -1;

    /**
     * 外部存储是否可用
     */
    static public boolean externalMemoryAvailable()
    {
        return android.os.Environment.getExternalStorageState().equals(
            android.os.Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取手机外部可用空间大小
     */
    static public float getAvailableExternalMemorySize()
    {
        if (externalMemoryAvailable()) {

            File path = new File(SDUtils.sdcardRootPath());

//            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
//            long blockSize = stat.getBlockSize();
//            long availableBlocks = stat.getAvailableBlocks();

            // 获取block的SIZE
            long blocSize = stat.getBlockSize();
            // 获取BLOCK数量
            float totalBlocks = stat.getBlockCount();
            // 可使用的Block的数量
            float availaBlock = stat.getAvailableBlocks();
            float result = (float) availaBlock / totalBlocks;
            result *= 100;

            return result;
        }
        else {
            return ERROR;
        }
    }

}
