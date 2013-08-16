package com.krdavc.video.recorder.receiver;

import com.krdavc.video.recorder.utils.UtilMethod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author E-mail:383781299@qq.com
 * @version data：2012-3-17 下午12:45:45
 * @explain explain：
 */

public class BootReceiver extends BroadcastReceiver
{

    private String tag = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        UtilMethod.enableKeyLight();
//        UtilMethod.enableLCDLight(context);
        Log.i(tag, "BootReceiver onReceive enableLCDLight");
    }

}
