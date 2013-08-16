package com.krdavc.video.recorder;

import android.app.Application;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-10 下午04:45:23
 *          类说明
 */
public class VideoApplication extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        // 处理全局异常
        VideoCrashHandler crashHandler = VideoCrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(getApplicationContext());
    }
}
