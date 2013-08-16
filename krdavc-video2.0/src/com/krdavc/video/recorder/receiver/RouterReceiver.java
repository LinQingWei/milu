package com.krdavc.video.recorder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.krdavc.video.recorder.utils.UtilMethod;

/**
 * 监听SCREEN_ON事件, 电量低
 *
 * @author danielgaoxz
 */
public class RouterReceiver extends BroadcastReceiver
{

    private final String tag = "SctionOnAndOffReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            UtilMethod.disableKeyLight();

        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // VideorecorderActivity.screen_off = true;
        }
        else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            Log.i(tag, "电量过低,退出程序");
/*            try{
                RecordActivity.layoutForButton.type = 2003;
                RecordActivity.mWindowMgr.updateViewLayout(RecordActivity.toggleButton, RecordActivity.layoutForButton);
            }catch (Exception e) {
                // TODO: handle exception
                Log.i(tag, "电量过低处理发生错误");
            }*/
        }
    }
}
