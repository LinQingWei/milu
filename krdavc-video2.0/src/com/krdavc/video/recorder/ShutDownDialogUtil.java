package com.krdavc.video.recorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-8 下午10:05:34
 *          类说明
 */
public class ShutDownDialogUtil
{

    public static boolean isShowing = false;


    public static void showShutDownDialog(Context context)
    {
        new AlertDialog.Builder(context).setTitle("关机").setIcon(
            R.drawable.stat_sys_warning).setMessage("您的手机会关机。")
            .setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {                        /*tabHostRequestFocus();
						// 假关机的处理部分
						shutdownScreenAndKeyboard();*/
                }
            }).setNegativeButton("取消",
            new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog,
                                    int which)
                {
                    // TODO Auto-generated method stub
								/*tabHostRequestFocus();
								flag_powerOffAlertWindowIsOn = false;*/
                }
            }).setOnCancelListener(
            new DialogInterface.OnCancelListener()
            {

                @Override
                public void onCancel(DialogInterface dialog)
                {
								/*tabHostRequestFocus();
								flag_powerOffAlertWindowIsOn = false;*/
                }
            }).create().show();

    }
}
