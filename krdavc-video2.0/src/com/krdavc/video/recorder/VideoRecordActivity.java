package com.krdavc.video.recorder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
public class VideoRecordActivity extends Activity
    implements SurfaceHolder.Callback
{
    public static final String TAG = VideoRecordActivity.class.getSimpleName();
    public static final int RECORD_TIME = 5 * 60 * 1000;
    public static final int DEVICE_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    /**
     * 预览视图高度(横屏为宽度)
     */
    public static int SURFACE_WIDTH = 500 / 3 * 2;
    /**
     * 高度
     */
    public static int SURFACE_HEIGHT = 450 / 3 * 2;
    /**
     * 屏幕是否亮着
     */
    public static boolean screen_off = false;
    /**
     * 是否正在录制
     */
    public boolean isRecording = false;
    /**
     * 当前activity是否处于活动状态
     */
    TextView text;
    Timer availableStoreTimer;
    BatteryReceiver batteryReceiver;
    Camera camera;
    Timer timer;
    WindowManager mWindowMgr = null;
    boolean flag_ActivityIsOn = true;
    /**
     * todo shigang add
     */

    int sdcardStateUpdateTime_ms = 30000;
    private AlertDialog shutDownAlertDialog;
    /**
     * ApplicationContext
     */
    private Activity mContext;
    private MediaRecorder mRecorder;// 录制视频类
    private SurfaceView mSurfaceView;// 显示视频的控件
    private LayoutParams layoutForSurfaceView;
    private String mOutputFileName;
    /**
     * 按键点击次数(达到一定次数即为长按)
     */
    private int keyDownTimes = 0;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceHolder mHolder = null;
    ToggleButton toggleButton;
    LayoutParams layoutForButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.videomain);
        mContext = this;

        initView();

        // sdcard不存在或者可用空间太小
        if (!UtilMethod.checkSdcardInfo(this)
            || UtilMethod.getAvailableStore() < 3) {
            UtilMethod.noSdcardTip(this);
        }
        else {

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

        availableStoreTimer.schedule(new AvailableStoreTimerTask(), 10 * 1000, 60 * 1000);
    }

    /**
     * 初始化view
     */
    private void initView()
    {
        text = (TextView) findViewById(R.id.textView);
        text.setText("80.7%");
        text.setTextColor(Color.BLUE);

//        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        if (android.os.Build.MODEL.equals("XT882")) {
            SURFACE_WIDTH = 450 * 9 / 10;
            SURFACE_HEIGHT = 445 * 9 / 10;
        }
        else if (android.os.Build.MODEL.equals("SCH-I939")) {
            SURFACE_WIDTH = 500 * 10 / 10;
            SURFACE_HEIGHT = 445 * 12 / 10;

            View view = findViewById(R.id.cameraType);
            view.setVisibility(View.VISIBLE);
        }
        else if (android.os.Build.MODEL.equals("GT-I9300")) {
            SURFACE_WIDTH = 500;
            SURFACE_HEIGHT = 445 * 12 / 10;
            View view = findViewById(R.id.cameraType);
            view.setVisibility(View.VISIBLE);
        }
        else {
            SURFACE_WIDTH = 1280 / 2;
            SURFACE_HEIGHT = 720 / 2;
        }

//        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(SURFACE_WIDTH, SURFACE_HEIGHT);
//        layout.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
//        mSurfaceView.setLayoutParams(layout);

        mWindowMgr = (WindowManager) mContext.getSystemService("window");
        mSurfaceView = new SurfaceView(mContext);
        layoutForSurfaceView = new LayoutParams(1, 1, 0, 85, LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, 1);
        layoutForSurfaceView.type = 2002;// LayoutParams.TYPE_SYSTEM_ERROR;
        layoutForSurfaceView.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutForSurfaceView.gravity = Gravity.RIGHT | Gravity.TOP;
        mWindowMgr.addView(mSurfaceView, layoutForSurfaceView);
        mSurfaceView.setClickable(true);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 按钮
        toggleButton = new ToggleButton(mContext);
        toggleButton.setBackgroundDrawable(null);
        toggleButton.setTextOn("");
        toggleButton.setTextOff("");
        layoutForButton = new LayoutParams(100, 100, 0, 0, 2002, 32, 1);
        layoutForButton.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowMgr.addView(toggleButton, layoutForButton);
        toggleButton.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                return onKeyDown1(keyCode, event);
            }
        });
        toggleButton.setChecked(false);
//        toggleButton.setOnCheckedChangeListener(checkChangeListener);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

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

        hiddenVideoView();
        flag_ActivityIsOn = false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        flag_ActivityIsOn = true;
//        if (toggleButton != null) {
//            toggleButton.setOnCheckedChangeListener(checkChangeListener);
//        }

    }


    @Override
    protected void onStop()
    {
        super.onStop();
        UtilMethod.enableLCDLight(mContext);
        flag_ActivityIsOn = false;
        // 回到home桌面后设置toggleButton不能点击
//        toggleButton.setOnCheckedChangeListener(null);

        if (android.os.Build.MODEL.equals("SCH-I939") || android.os.Build.MODEL.equals("GT-I9300")) {
            if (mWindowMgr != null) {
                mWindowMgr.removeView(mSurfaceView);
                mWindowMgr.removeView(toggleButton);
            }
        }
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

        stopMediaRecordTask();

        UtilMethod.releaseWakeLock();
        UtilMethod.enableKeyLight();

        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }

        if (availableStoreTimer != null) {
            availableStoreTimer.cancel();
        }

        UtilMethod.enableLCDLight(mContext);

        // 停止service
        Intent intent1 = new Intent();
        intent1.setClass(mContext, VideoService.class);
        stopService(intent1);
        System.exit(0);
        super.onDestroy();
    }

    private Camera initCamera(int deviceId)
    {
        try {
            Camera mCamera = Camera.open(deviceId);
            Camera.Parameters camParams = mCamera.getParameters();
//            if (camParams.isZoomSupported()) {
//                camParams.setZoom(2);
//            }
//            else {
//                Log.i(TAG, "不支持setZoom");
//            }
//            camParams.set("orientation", "portrait");

            camParams.set("rotation", 90);
            mCamera.lock();
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(camParams);
            return mCamera;
        }
        catch (RuntimeException re) {
            Log.v(TAG, "Could not initialize the Camera");
            re.printStackTrace();
            return null;
        }
    }

    private void releaseFocus()
    {
        try {
            layoutForButton.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
            mWindowMgr.updateViewLayout(toggleButton, layoutForButton);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "释放焦点异常");
        }

    }

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

    private void startVideoRecordTask()
    {
        timer = new Timer();
        Log.i(TAG, "timer.schedule");
        timer.schedule(new RepeatTimerTask(), RECORD_TIME, RECORD_TIME);
        startVideoRecord();
    }

    private void startVideoRecord()
    {
        if (isRecording)
            return;

//        mWindowMgr.addView(mSurfaceView, layoutForSurfaceView);

        mOutputFileName = SDUtils.makeOutputFileName();

        File outFile = new File(mOutputFileName);
        if (outFile.exists()) {
            outFile.delete();
        }
        else {
            SDUtils.createRoutePath();
        }

        try {
            camera = initCamera(cameraId);
            if (camera == null) {
                Toast.makeText(this, "连接不到摄像头设备!", 1000).show();
                return;
            }
            camera.unlock();
            mRecorder = new MediaRecorder();

            mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener()
            {
                @Override
                public void onError(MediaRecorder mr, int what, int extra)
                {
                    Log.d(TAG, "onError=" + what + " " + extra);
                }
            });
//            mRecorder.setMaxDuration(RECORD_TIME);
            mRecorder.setCamera(camera);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setPreviewDisplay(mHolder.getSurface());
            CamcorderProfile camcorderProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            //三星相机获取的分辨率和实际支持的不符，通过下面的方法强制设置
            if (cameraId == 1 && (android.os.Build.MODEL.equals("SCH-I939") || android.os.Build.MODEL.equals("GT-I9300"))) {
//                setProfile(mRecorder, camcorderProfile, cameraId);
                mRecorder.setProfile(camcorderProfile);
            }
            else {
                mRecorder.setProfile(camcorderProfile);
            }
            mRecorder.setOutputFile(mOutputFileName);
            Log.d(TAG, "Video Path " + mOutputFileName);
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
        }
        catch (Exception e) {
            Toast.makeText(this, "媒体设备初始化失败" + (e.getMessage() == null ? "" : e.getMessage()), 2000).show();
            Log.d(TAG, "MediaRecorder failed to initialize" + e.getMessage());
            e.printStackTrace();
            if (camera != null) {
                camera.release();
            }
        }
    }

    public void onChangeCamera(View view)
    {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopMediaRecordTask();
        startVideoRecordTask();
    }

    public void setProfile(MediaRecorder mRecorder, CamcorderProfile profile, int deviceId)
    {
        Log.d(TAG, "" + deviceId);
        Log.d(TAG, "" + profile.fileFormat);
        Log.d(TAG, "" + profile.videoFrameRate);
        Log.d(TAG, "" + profile.videoFrameWidth + "/" + profile.videoFrameHeight);
        Log.d(TAG, "" + profile.videoBitRate);
        Log.d(TAG, "" + profile.audioBitRate);
        Log.d(TAG, "" + profile.audioChannels);
        Log.d(TAG, "" + profile.audioSampleRate);
        Log.d(TAG, "" + profile.videoCodec);
        Log.d(TAG, "" + profile.audioCodec);


        mRecorder.setOutputFormat(2);
        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoSize(1280, 720);
        mRecorder.setVideoEncodingBitRate(12000000);
        mRecorder.setAudioEncodingBitRate(128000);
        mRecorder.setAudioChannels(2);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setVideoEncoder(2);
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

    public void onToggleVideoView(View view)
    {
        Object obj = view.getTag();
        if (obj == null) {
            view.setTag(new Object());
            showVideoView();
        }
        else {
            view.setTag(null);
            hiddenVideoView();
        }
    }

    public void showVideoView()
    {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.videoView);
        viewGroup.setVisibility(View.VISIBLE);
//        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(SURFACE_WIDTH, SURFACE_HEIGHT);
//        layout.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
//        mSurfaceView.setLayoutParams(layout);

        layoutForSurfaceView.height = SURFACE_HEIGHT;
        layoutForSurfaceView.width = SURFACE_WIDTH;
        mWindowMgr.updateViewLayout(mSurfaceView, layoutForSurfaceView);
    }

    public void hiddenVideoView()
    {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.videoView);
        viewGroup.setVisibility(View.INVISIBLE);

        if (mSurfaceView != null && layoutForSurfaceView != null
            && mWindowMgr != null) {
            layoutForSurfaceView.height = 1;
            layoutForSurfaceView.width = 1;
            mWindowMgr.updateViewLayout(mSurfaceView, layoutForSurfaceView);
        }

//        mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
    }

    /**
     * 停止录制
     */
    private void stopMediaRecorder()
    {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                isRecording = false;
            }

            if (android.os.Build.MODEL.equals("SCH-I939") || android.os.Build.MODEL.equals("GT-I9300")) {
                if (mSurfaceView != null) {
                    Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            hiddenVideoView();
                        }
                    });
                }
            }


        }
        catch (Exception e) {
            isRecording = false;
        }

        try {
            if (camera != null) {
                try {
                    camera.reconnect();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                camera.stopPreview();
                camera.release();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            isRecording = false;
        }
    }

    private void stopMediaRecordTask()
    {
        stopMediaRecorder();
        if (timer != null) {
            timer.cancel();
        }
    }

    public boolean onKeyDown1(int keyCode, KeyEvent event)
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
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                keyDownTimes += 1;
                if (keyDownTimes < 5) {
                    if (keyCode == KeyEvent.KEYCODE_CAMERA) {
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
                            Log.i(TAG, "长按VOLUME_DOWN事件,发送ManualRecord消息");
                            if (isRecording) {
                                VibratorUtils.stop(mContext);
                                stopMediaRecordTask();
                            }
                            else {
                                VibratorUtils.start(mContext);
                                startVideoRecordTask();
                            }
                            break;

                        case KeyEvent.KEYCODE_VOLUME_UP:


                            if (screen_off) {
                                // 锁屏状态
                                VibratorUtils.start(mContext);
                                UtilMethod.enableLCDLight(mContext);
                                UtilMethod.setAirplaneMode(mContext, false);
                                screen_off = false;
                            }
                            else {
                                // 非锁屏状态
                                VibratorUtils.stop(mContext);
                                UtilMethod.setAirplaneMode(mContext, true);
                                UtilMethod.disableLCDLight(mContext);
                                screen_off = true;
                            }
                            break;

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
//                                toggleButton.setChecked(false);
                                shutDownAlertDialog = showShutDownDialog(VideoRecordActivity.this);
                                shutDownAlertDialog.getWindow().setType(2003);
                                // shutDownAlertDialog.getWindow().setFlags(flags,
                                // mask)
                                // shutDownAlertDialog.getWindow().addFlags(32);
                                shutDownAlertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                                shutDownAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                                {
                                    public void onDismiss(DialogInterface dialog)
                                    {
                                        acquireFocus();
//                                         shutdownScreenAndKeyboard();
                                        UtilMethod.setSilent(mContext);
                                    }
                                });
                                shutDownAlertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                                {
                                    public void onShow(DialogInterface dialog)
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        keyDownTimes = 0;
        return true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        mHolder = holder;
//        stopMediaRecordTask();
//        startVideoRecordTask();
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        mHolder = holder;
        stopMediaRecordTask();
        startVideoRecordTask();
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        stopMediaRecordTask();
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
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
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
            stopMediaRecordTask();
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

    public void showMessage(String msg)
    {

        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(VideoRecordActivity.this, "电量/存储空间不足，程序不予运行", 5000).show();
            }
        });
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
            stopMediaRecorder();
            try {
                startVideoRecord();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
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

            stopMediaRecorder();

            return super.cancel();
        }
    }

    public class AvailableStoreTimerTask extends TimerTask
    {

        @Override
        public void run()
        {
            Log.i(TAG, "AvailableStoreTimerTask .run  current available store = " + UtilMethod.getAvailableStore());

            boolean isFull = UtilMethod.getAvailableStore() < 5;
            if (isFull) {
                stopMediaRecordTask();
                showMessage("电量/存储空间不足，程序不予运行");
//                UtilMethod.noSdcardTip(VideoRecordActivity.this);
//                VideoRecordActivity.this.finish();
            }
        }
    }

    public class BatteryReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            int level = intent.getIntExtra("level", 0);// 电量百分比
            Log.i("BatteryReceiver", "Battery level = " + String.valueOf(level) + "%");

            if (level <= 5) {
                showMessage("电量/存储空间不足，程序不予运行");
                stopMediaRecordTask();
                //  VideoRecordActivity.this.finish();
            }
        }
    }
}
