package com.krdavc.video.recorder;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ToggleButton;
import com.krdavc.video.recorder.utils.UtilMethod;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-9 上午12:21:11
 *          类说明
 */
public class VideoService extends Service
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        //禁止休眠
        UtilMethod.acquireWakeLock(getApplicationContext());

        // 禁止锁屏
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();

//        WindowManager mWindowMgr = (WindowManager) getSystemService("window");
//        ToggleButton toggleButton = new ToggleButton(this);
//        toggleButton.setBackgroundDrawable(null);
//        toggleButton.setTextOn("");
//        toggleButton.setTextOff("");
//        toggleButton.setText("Test");
//        WindowManager.LayoutParams   layoutForButton = new WindowManager.LayoutParams(100, 100, 0, 0, 2002, 32, 1);
//        layoutForButton.gravity = Gravity.LEFT | Gravity.TOP;
//        mWindowMgr.addView(toggleButton, layoutForButton);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);

        //关闭键盘灯
        UtilMethod.disableKeyLight();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        UtilMethod.releaseWakeLock();
    }

}
