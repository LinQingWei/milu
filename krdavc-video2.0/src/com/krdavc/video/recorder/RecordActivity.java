package com.krdavc.video.recorder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.krdavc.video.recorder.receiver.RouterReceiver;
import com.krdavc.video.recorder.utils.SDUtils;
import com.krdavc.video.recorder.utils.UtilMethod;
import com.krdavc.video.recorder.utils.VibratorUtils;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-8 下午08:14:30 类说明
 */
public class RecordActivity extends Activity implements SurfaceHolder.Callback
{

    public static final String TAG = RecordActivity.class.getSimpleName();

    private AlertDialog shutDownAlertDialog;

    /**
     * ApplicationContext
     */
    private Activity mContext;

    /**
     * 预览视图高度(横屏为宽度)
     */
    public static final int SURFACE_WIDTH = 500;
    /**
     * 高度
     */
    public static final int SURFACE_HEIGHT = 450;
    /**
     * 是否正在录制
     */
    public static boolean isRecording = false;

    private MediaRecorder mRecorder;// 录制视频类

    private SurfaceView mSurfaceView;// 显示视频的控件

    public static WindowManager mWindowMgr;

    private LayoutParams layoutForSurfaceView;
    public static LayoutParams layoutForButton;

    private String mOutputFileName;

    /**
     * 放大缩小SurfaceView的按钮
     */
    public static ToggleButton toggleButton;

    /**
     * 按键点击次数(达到一定次数即为长按)
     */
    private int keyDownTimes = 0;

    /**
     * 屏幕是否亮着
     */
    public static boolean screen_off = false;
    /**
     * 当前activity是否处于活动状态
     */

    TextView text;

    private Camera mCamera = null;

    private SurfaceHolder mHolder = null;

    /*
      * 初始化Camera
      */
    private boolean initCamera()
    {
        try {
            mCamera = Camera.open();
            Camera.Parameters camParams = mCamera.getParameters();
            if (camParams.isZoomSupported()) {
                camParams.setZoom(2);
            }
            else {
                Log.i(TAG, "不支持setZoom");
            }
            mCamera.lock();
            mCamera.setParameters(camParams);
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        catch (RuntimeException re) {
            Log.v(TAG, "Could not initialize the Camera");
            re.printStackTrace();
            return false;
        }
        return true;
    }

    private void initRecorder()
    {
        if (isRecording)
            return;

        if (mRecorder != null || mCamera == null)
            return;

        mOutputFileName = SDUtils.makeOutputFileName();

        File outFile = new File(mOutputFileName);
        if (outFile.exists()) {
            outFile.delete();
        }
        else {
            SDUtils.createRoutePath();
        }

        try {
            mCamera.stopPreview();
            mCamera.unlock();

            Log.d(TAG, "Zoom: " + mCamera.getParameters().getZoom());

            // Camera.CameraInfo info = new Camera.CameraInfo();
            //Camera.getCameraInfo(0, info);

            mRecorder = new MediaRecorder();
            mRecorder.setCamera(mCamera);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setPreviewDisplay(mHolder.getSurface());
            CamcorderProfile camcorderProfile = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
            camcorderProfile.duration = 60 * 60 * 24;
            setProfile(mRecorder, camcorderProfile);
            mRecorder.setOutputFile(mOutputFileName);

            mRecorder.prepare();
            Log.v(TAG, "MediaRecorder initialized");
        }
        catch (Exception e) {
            Log.v(TAG, "MediaRecorder failed to initialize");
            e.printStackTrace();
        }
    }

    public void setProfile(MediaRecorder mRecorder, CamcorderProfile profile)
    {
        Log.d(TAG, "" + profile.fileFormat);
        Log.d(TAG, "" + profile.videoFrameRate);
        Log.d(TAG, "" + profile.videoFrameWidth + "/" + profile.videoFrameHeight);
        Log.d(TAG, "" + profile.videoBitRate);
        Log.d(TAG, "" + profile.audioBitRate);
        Log.d(TAG, "" + profile.audioChannels);
        Log.d(TAG, "" + profile.audioSampleRate);
        Log.d(TAG, "" + profile.videoCodec);
        Log.d(TAG, "" + profile.audioCodec);

        mRecorder.setOutputFormat(1);
        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoSize(1280, 720);
        mRecorder.setVideoEncodingBitRate(10000000);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setAudioChannels(2);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setVideoEncoder(3);
        mRecorder.setAudioEncoder(3);

        //        mRecorder.setOutputFormat(profile.fileFormat);
        //        mRecorder.setVideoFrameRate(profile.videoFrameRate);
        //        mRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        //        mRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        //        mRecorder.setAudioEncodingBitRate(profile.audioBitRate);
        //        mRecorder.setAudioChannels(profile.audioChannels);
        //        mRecorder.setAudioSamplingRate(profile.audioSampleRate);
        //        mRecorder.setVideoEncoder(profile.videoCodec);
        //        mRecorder.setAudioEncoder(profile.audioCodec);
    }

    private void releaseRecorder()
    {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void releaseCamera()
    {
        if (mCamera != null) {
            try {
                mCamera.reconnect();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 初始化view
     */
    private void initView()
    {
        mWindowMgr = (WindowManager) mContext.getSystemService("window");
        mSurfaceView = new SurfaceView(mContext);
        layoutForSurfaceView = new LayoutParams(1, 1, 0, 0, LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_FOCUSABLE, 1);
        layoutForSurfaceView.type = 2002;// LayoutParams.TYPE_SYSTEM_ERROR;
        layoutForSurfaceView.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutForSurfaceView.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowMgr.addView(mSurfaceView, layoutForSurfaceView);

        text = new TextView(this);
        text.setText("80.7%");
        text.setTextColor(Color.BLUE);

        LayoutParams textLayout = new LayoutParams();
        textLayout.type = 2002;
        textLayout.format = 1;
        textLayout.flags = 32;
        textLayout.width = 100;
        textLayout.height = 100;
        textLayout.x = SURFACE_WIDTH - 50;
        textLayout.y = SURFACE_HEIGHT - 30;
        textLayout.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowMgr.addView(text, textLayout);

        // 按钮
        toggleButton = new ToggleButton(mContext);
        toggleButton.setBackgroundDrawable(null);
        toggleButton.setTextOn("");
        toggleButton.setTextOff("");
        layoutForButton = new LayoutParams(100, 100, 0, 0, 2002, 32, 1);
        layoutForButton.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowMgr.addView(toggleButton, layoutForButton);
        toggleButton.setOnKeyListener(keyListener);
        toggleButton.setChecked(false);
        toggleButton.setOnCheckedChangeListener(checkChangeListener);

        initCamera();
    }

    BatteryReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON, LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);
        mContext = this;

        initView();

        // sdcard不存在或者可用空间太小
        if (!UtilMethod.checkSdcardInfo(this)
            || UtilMethod.getAvailableStore() < 3) {
            releaseFocus();
            UtilMethod.noSdcardTip(this);
        }
        else {
            new Thread()
            {
                @Override
                public void run()
                {
                    try {
                        // 等待n秒再prepare,不然prepare失败
                        Thread.sleep(3 * 1000);
//                        initCamera();
//                        initRecorder();

//                        mRecorder.start();
//                        isRecording = true;

//                        startMediarecorder();
                        /**
                         * todo shigang add
                         */


                        timer = new RepeatTimer();
                        Log.i(TAG, "timer.schedule");
                        timer.schedule(new RepeatTimerTask(), 0, 5 * 60 * 1000);

                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ;
            }.start();

            // 添加view组件，显示级别为最高

            // 设置键盘灯不可亮
            UtilMethod.disableKeyLight();

            // 更新SDCARD最新状态
            updateSdcardPercent();
            sdcardStateUpdate();

            Intent intent1 = new Intent();
            intent1.setClass(mContext, VideoService.class);
            startService(intent1);

            // 设置静音
            UtilMethod.setSilent(mContext);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.BATTERY_LOW");

            registerReceiver(new RouterReceiver(), intentFilter);

        }


        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();

        registerReceiver(batteryReceiver, batteryFilter);

        availableStoreTimer = new Timer();

        availableStoreTimer.schedule(new AvailableStoreTimerTask(), 0, 5 * 60 * 1000);

    }

    Timer availableStoreTimer;


    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause mOutputFileName" + mOutputFileName);
        super.onPause();

        if (mOutputFileName == null) {
            return;
        }

        File outFile = new File(mOutputFileName);
        if (outFile.exists() && outFile.length() < 10) {
            outFile.delete();
        }

        flag_ActivityIsOn = false;
        smallSurface();
    }

    boolean flag_ActivityIsOn = true;

    @Override
    protected void onResume()
    {
        super.onResume();
        flag_ActivityIsOn = true;
        if (toggleButton != null) {
            toggleButton.setOnCheckedChangeListener(checkChangeListener);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        flag_ActivityIsOn = false;
        // 回到home桌面后设置toggleButton不能点击
        toggleButton.setOnCheckedChangeListener(null);
    }

    @Override
    public void finish()
    {
        super.finish();
        Log.i(TAG, "finish");
        UtilMethod.releaseWakeLock();
    }

    @Override
    protected void onDestroy()
    {
        Log.i(TAG, "onDestroy");
        UtilMethod.releaseWakeLock();
        UtilMethod.enableKeyLight();

        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }

        if (availableStoreTimer != null) {
            availableStoreTimer.cancel();
        }

        // 停止service
        Intent intent1 = new Intent();
        intent1.setClass(mContext, VideoService.class);
        stopService(intent1);
        System.exit(0);
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 放大
     */
    private void bigSurface()
    {
        Log.i(TAG, "bigSurface");
        layoutForSurfaceView.height = SURFACE_HEIGHT;
        layoutForSurfaceView.width = RecordActivity.SURFACE_WIDTH;
        mWindowMgr.updateViewLayout(mSurfaceView, layoutForSurfaceView);
    }

    /**
     * 缩小
     */
    private void smallSurface()
    {
        try {
            Log.i(TAG, "smallSurface");
            if (mSurfaceView != null && layoutForSurfaceView != null
                && mWindowMgr != null) {
                layoutForSurfaceView.height = 1;
                layoutForSurfaceView.width = 1;
                mWindowMgr.updateViewLayout(mSurfaceView, layoutForSurfaceView);
            }
        }
        catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * 开始录制
     */
    private void startMediarecorder()
    {
        try {
            initCamera();
            initRecorder();

            mRecorder.start();
            isRecording = true;

            VibratorUtils.start(mContext);
        }
        catch (Exception e) {
            isRecording = false;
            // prepareMediarecorder();
            initRecorder();
        }

    }

    /**
     * 停止录制
     */
    private void stopMediarecorder()
    {
        try {
            // mRecorder.stop();
            // mRecorder.release();
            // mRecorder = null;
            isRecording = false;

            VibratorUtils.stop(mContext);
        }
        catch (Exception e) {
            isRecording = false;
        }
        // prepareMediarecorder();
        releaseRecorder();
        releaseCamera();
        // 停止播放后就关闭预览

    }

    RepeatTimer timer;

    /**
     * 监听键盘
     */
    private final OnKeyListener keyListener = new OnKeyListener()
    {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (flag_ActivityIsOn && !screen_off) {
                    Log.i(TAG, "点击back键,退出程序");
                    finish();
                    return true;
                }
            }
            // 只监听几个键
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_SEARCH
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    keyDownTimes += 1;
                    if (keyDownTimes < 5) {
                        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
                            /*
                                    * Intent i = new Intent(Intent.ACTION_MAIN);
                                    * i.addCategory(Intent.CATEGORY_LAUNCHER);
                                    * i.setAction(Intent.ACTION_BATTERY_LOW);
                                    * sendBroadcast(i);
                                    */
                            UtilMethod.showDialog(mContext);
                        }

                        // 短按,直接退出
                        return true;
                    }
                    else if (keyDownTimes == 5) {
                        // 第一次长按,执行长按操作
                        Log.i(TAG, "第一次长按,执行长按操作");
                        switch (keyCode) {

                            case KeyEvent.KEYCODE_VOLUME_DOWN:
                                // 发送ManualRecord Broadcast,至于是开始还是停止,
                                // 在Broadcast里判断
                                Log.i(TAG, "长按VOLUME_DOWN事件,发送ManualRecord消息");
                                // UtilMethod.updateSdcardPercent(sdcardInfoTextView);
//                                if (isRecording) {
//                                    // 正在录制,停止
//                                    stopMediarecorder();
//
//                                    // initRecorder();
//                                } else {
//                                    // 不在录制,开始录制
//                                    startMediarecorder();
//                                }

                                if (isRecording) {
                                    // 正在录制,停止
//                                    stopMediarecorder();
                                    if (timer != null) {
                                        Log.i(TAG, "timer .cancel");
                                        timer.cancel();
                                    }
                                    else {
                                        Log.i(TAG, "timer == null  .stopMediarecorder");
                                        stopMediarecorder();
                                    }

                                    // initRecorder();
                                }
                                else {
                                    // 不在录制,开始录制
//                                    startMediarecorder();

                                    timer = new RepeatTimer();
                                    Log.i(TAG, "timer.schedule");
                                    timer.schedule(new RepeatTimerTask(), 0, 5 * 60 * 1000);
                                }

                                break;

                            case KeyEvent.KEYCODE_VOLUME_UP:
                                UtilMethod.setAirplaneMode(mContext, true);

                                if (screen_off) {
                                    // 锁屏状态
                                    UtilMethod.enableLCDLight(mContext);
                                    screen_off = false;
                                }
                                else {
                                    // 非锁屏状态
                                    UtilMethod.disableLCDLight(mContext);
                                    screen_off = true;
                                }
                                break;

//                            case KeyEvent.KEYCODE_SEARCH:
                            case KeyEvent.KEYCODE_SEARCH:

                                if (screen_off) {
                                    // 锁屏状态
                                    UtilMethod.enableLCDLight(mContext);
                                    screen_off = false;
                                    Log.i(TAG, "点亮屏幕");
                                }
                                else if (flag_ActivityIsOn) {
                                    // 非锁屏状态
                                    Log.i(TAG, "弹出框提示关机");
                                    // smallSurface();
                                    toggleButton.setChecked(false);
                                    shutDownAlertDialog = showShutDownDialog(RecordActivity.this);
                                    shutDownAlertDialog.getWindow()
                                        .setType(2003/*
                                                     * WindowManager.LayoutParams
													 * .TYPE_SYSTEM_DIALOG
													 */);
                                    // shutDownAlertDialog.getWindow().setFlags(flags,
                                    // mask)
                                    // shutDownAlertDialog.getWindow().addFlags(32);
                                    shutDownAlertDialog
                                        .getWindow()
                                        .setFlags(
                                            LayoutParams.FLAG_BLUR_BEHIND,
                                            LayoutParams.FLAG_BLUR_BEHIND);
                                    shutDownAlertDialog
                                        .setOnDismissListener(new DialogInterface.OnDismissListener()
                                        {
                                            public void onDismiss(
                                                DialogInterface dialog)
                                            {
                                                acquireFocus();
                                                // shutdownScreenAndKeyboard();
                                                UtilMethod.setSilent(mContext);
                                            }
                                        });
                                    shutDownAlertDialog
                                        .setOnShowListener(new DialogInterface.OnShowListener()
                                        {
                                            public void onShow(
                                                DialogInterface dialog)
                                            {
                                                releaseFocus();
                                            }
                                        });
                                    shutDownAlertDialog.show();

                                }
                                else {
                                    Log.i(TAG, "当前activity未处于活动状态, 什么都不做");
                                }
                                break;

                            default:
                                break;
                        }
                        return true;
                    }
                    else {
                        return true;
                    }
                }
                else if (event.getAction() == KeyEvent.ACTION_UP) {
                    keyDownTimes = 0;
                }
            }
            else {
                return false;
            }
            return true;
        }

    };

    /**
     * 获得焦点
     */
    private void acquireFocus()
    {
        try {
            layoutForButton.flags = 32;
            mWindowMgr.updateViewLayout(toggleButton, layoutForButton);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "获取焦点异常");
        }
    }

    /**
     * 释放焦点
     */
    private void releaseFocus()
    {
        try {
            layoutForButton.flags |= LayoutParams.FLAG_NOT_FOCUSABLE; /*
                                                                     * LayoutParams.
																	 * FLAG_NOT_TOUCH_MODAL
																	 * |
																	 * LayoutParams
																	 * .
																	 * FLAG_NOT_FOCUSABLE
																	 * |
																	 * LayoutParams
																	 * .
																	 * FLAG_NOT_TOUCHABLE
																	 */
            ;
            mWindowMgr.updateViewLayout(toggleButton, layoutForButton);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "释放焦点异常");
        }

    }

    /**
     * button切换预览视图大小
     */
    private final CompoundButton.OnCheckedChangeListener checkChangeListener = new CompoundButton.OnCheckedChangeListener()
    {

        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked)
        {
            try {
                if (isChecked) {
                    if (!isFinishing()) {
                        // 大图
                        bigSurface();
                        text.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    // 小图
                    smallSurface();
                    text.setVisibility(View.INVISIBLE);
                }
            }
            catch (Exception e) {
                Log.i(TAG, "切换视图大小发生错误");
            }

        }
    };

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        Log.d(TAG, "surfaceChanged");
        mHolder = holder;
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        Log.d(TAG, "surfaceCreated");

        mHolder = holder;
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch (IOException e) {
            Log.v(TAG, "Could not start the preview");
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // surfaceDestroyed的时候同时对象设置为null
        Log.d(TAG, "surfaceDestroyed");
        mSurfaceView = null;
        mHolder = null;
        mRecorder = null;
    }


    /**
     * 显示关机AlertDialog
     *
     * @param context
     */
    public AlertDialog showShutDownDialog(final Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setAdapter(new ShutDownAdapter(context),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    UtilMethod.setSilent(context);
                    acquireFocus();
                    switch (which) {

                        case 2:
                            // 关机
                            shutdownScreenAndKeyboard();
                            // UtilMethod.disableLCDLight(getApplicationContext());
                            break;

                        default:
                            break;
                    }
                }
            });
        builder.setTitle("手机选项");
        return builder.create();
    }

    /**
     * 关机框
     */
    private void shutdownScreenAndKeyboard()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("关机");
        progressDialog.setIcon(android.R.drawable.ic_dialog_info);
        progressDialog.setMessage("正在关机...");
        progressDialog.getWindow().setType(2002);
        progressDialog.getWindow().setFlags(
            LayoutParams.FLAG_BLUR_BEHIND,
            LayoutParams.FLAG_BLUR_BEHIND);
        progressDialog.show();

        TimerTask progressTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                UtilMethod.disableKeyLight();
                UtilMethod.disableLCDLight(mContext);
                screen_off = true;

                progressDialog.cancel();
                VibratorUtils.shutdown(mContext);
            }
        };
        Timer progressDialogTimer = new Timer();
        progressDialogTimer.schedule(progressTimerTask, 5000);
    }


    /**
     * todo shigang add
     */

    int sdcardStateUpdateTime_ms = 30000;


    private void sdcardStateUpdate()
    {
        Timer sdcardStateUpdateTimer = new Timer();
        TimerTask sdcardStateUpdateTimerTask = new TimerTask()
        {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                updateSdcardPercent();
            }
        };
        sdcardStateUpdateTimer.schedule(sdcardStateUpdateTimerTask, 500, sdcardStateUpdateTime_ms);
    }

    protected void updateSdcardPercent()
    {
        final float result = VideoStore.getAvailableExternalMemorySize();
        Log.i(TAG, "getAvailableExternalMemorySize " + result);


//        final int result = GetAvilibleStore.getAvailableStore();
        if (result < 1) {
            Toast.makeText(this, "没有有效的存储空间", 2000).show();
            stopMediarecorder();
            System.exit(0);
        }
        text.post(new Runnable()
        {
            public void run()
            {
                text.setText(result + "%");
                if (result >= 60) {
                    text.setTextColor(Color.GREEN);
                }
                else if (result <= 20) {
                    text.setTextColor(Color.RED);
                }
                else {
                    text.setTextColor(Color.LTGRAY);
                }
                Log.i("tag", "sdcardPercenTextView" + result);
            }
        });
    }


    public class RepeatTimer extends Timer
    {
        /**
         * Cancels the {@code Timer} and all scheduled tasks. If there is a
         * currently running task it is not affected. No more tasks may be scheduled
         * on this {@code Timer}. Subsequent calls do nothing.
         */
        @Override
        public void cancel()
        {
            super.cancel();
            Log.i(TAG, "RepeatTimer .cancel");

            stopMediarecorder();
        }
    }

    public class RepeatTimerTask extends TimerTask
    {
        /**
         * The task to run should be specified in the implementation of the {@code run()}
         * method.
         */
        @Override
        public void run()
        {
            Log.i(TAG, "RepeatTimerTask .run");
            stopMediarecorder();
            startMediarecorder();
        }

        /**
         * Cancels the {@code TimerTask} and removes it from the {@code Timer}'s queue. Generally, it
         * returns {@code false} if the call did not prevent a {@code TimerTask} from running at
         * least once. Subsequent calls have no effect.
         *
         * @return {@code true} if the call prevented a scheduled execution
         *         from taking place, {@code false} otherwise.
         */
        @Override
        public boolean cancel()
        {
            Log.i(TAG, "RepeatTimerTask .cancel");

            stopMediarecorder();

            return super.cancel();
        }
    }

    public class AvailableStoreTimerTask extends TimerTask
    {

        @Override
        public void run()
        {
            Log.i(TAG, "AvailableStoreTimerTask .run  current available store = " + UtilMethod.getAvailableStore());
            if (UtilMethod.getAvailableStore() < 3) {
                UtilMethod.noSdcardTip(RecordActivity.this);
                if (timer != null) {
                    timer.cancel();
                }

                stopMediarecorder();
            }
        }
    }

    public class BatteryReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            int level = intent.getIntExtra("level", 0);// 电量百分比
            Log.i("BatteryReceiver", "Battery level = " + String.valueOf(level) + "%");

            if (level <= 2) {
                RecordActivity.this.finish();
            }
        }
    }
}
