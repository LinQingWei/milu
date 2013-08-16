package com.krdavc.video.recorder;

import java.io.File;
import java.io.IOException;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.krdavc.video.recorder.utils.SDUtils;
import com.krdavc.video.recorder.utils.UtilMethod;

public class VideoRecordService extends Service implements Callback
{
    SurfaceView surfaceViewForRecord;
    SurfaceHolder surfaceHolderForRecord;
    double recordStartTime, recordEndTime;
    int timeForPerVideo = 600000;
    Boolean recording = false;
    MediaRecorder mediaRecorder = new MediaRecorder();
    Camera camera;
    CamcorderProfile camcorderProfile;
    WindowManager windowManager;
    WindowManager.LayoutParams wmlLayoutParams;

    public static int SURFACE_WIDTH = 500 / 3 * 2;
    /**
     * 高度
     */
    public static int SURFACE_HEIGHT = 450 / 3 * 2;

    private NotificationManager mNM;

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i("tag", "serviceonCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.MODEL.equals("XT882")) {
            SURFACE_WIDTH = 450 * 9 / 10;
            SURFACE_HEIGHT = 445 * 9 / 10;
        }
        else if (android.os.Build.MODEL.equals("SCH-I939")) {
            SURFACE_WIDTH = 500 * 10 / 10;
            SURFACE_HEIGHT = 445 * 12 / 10;
        }
        else {
            SURFACE_WIDTH = 1280 / 2;
            SURFACE_HEIGHT = 720 / 2;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.krdavc.video.recorder.start");
        filter.addAction("com.krdavc.video.recorder.stop");
        filter.addAction("com.krdavc.video.recorder.hidden");
        filter.addAction("com.krdavc.video.recorder.show");
        registerReceiver(new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                if ("com.krdavc.video.recorder.start".equalsIgnoreCase(intent.getAction())) {
                    start();
                }
                else if ("com.krdavc.video.recorder.stop".equalsIgnoreCase(intent.getAction())) {
                    stop();
                }
                else if ("com.krdavc.video.recorder.show".equalsIgnoreCase(intent.getAction())) {
                    show();
                }
                else if ("com.krdavc.video.recorder.hidden".equalsIgnoreCase(intent.getAction())) {
                    hidden();
                }
            }
        }, filter);
    }

    public void start()
    {
        initializeVideo(0);
        try {
            mediaRecorder.prepare();
            recordStartTime = recordEndTime;
        }
        catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mediaRecorder.start();
        recording = true;


        if (UtilMethod.getAvailableStore() <= 3) {
            mediaRecorder.stop();
            recording = false;
            System.exit(0);
        }
    }

    public void stop()
    {
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    public void show()
    {
        wmlLayoutParams.width = SURFACE_WIDTH;
        wmlLayoutParams.height = SURFACE_HEIGHT;
        windowManager.updateViewLayout(surfaceViewForRecord, wmlLayoutParams);
    }

    public void hidden()
    {
        wmlLayoutParams.height = 1;
        wmlLayoutParams.width = 1;
        windowManager.updateViewLayout(surfaceViewForRecord, wmlLayoutParams);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        windowManager.removeView(surfaceViewForRecord);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        // setForeground(true);
        super.onStart(intent, startId);

        surfaceViewForRecord = new SurfaceView(getApplicationContext());
        windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        wmlLayoutParams = new WindowManager.LayoutParams();
        wmlLayoutParams.type = 2002;
        wmlLayoutParams.format = 1;
        wmlLayoutParams.flags = 40;
        wmlLayoutParams.width = 1;
        wmlLayoutParams.height = 1;
        wmlLayoutParams.x = 0;
        wmlLayoutParams.y = 85;
        wmlLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        windowManager.addView(surfaceViewForRecord, wmlLayoutParams);

        surfaceViewForRecord.setVisibility(View.VISIBLE);
        surfaceHolderForRecord = surfaceViewForRecord.getHolder();
        surfaceHolderForRecord.addCallback(this);
        surfaceHolderForRecord.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 禁止休眠
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.acquire();
        // 禁止锁屏
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();

    }


    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    private boolean initializeVideo(int deviceId)
    {
        if (camera != null) {
            camera.release();
            camera = null;
        }

        camera = Camera.open(deviceId);
        if (camera == null) {
            Toast.makeText(this, "连接不到摄像头设备!", 1000).show();
            return false;
        }
        Camera.Parameters camParams = camera.getParameters();
        //            if (camParams.isZoomSupported()) {
        //                camParams.setZoom(2);
        //            }
        //            else {
        //                Log.i(TAG, "不支持setZoom");
        //            }
        //            camParams.set("orientation", "portrait");
        camParams.set("rotation", 90);
        camera.lock();
        camera.setDisplayOrientation(90);
        camera.setParameters(camParams);
        camera.unlock();

        if (mediaRecorder == null)
            mediaRecorder = new MediaRecorder();
        else
            mediaRecorder.reset();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setPreviewDisplay(surfaceHolderForRecord.getSurface());
        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        camcorderProfile.duration = 60 * 60 * 24;
        mediaRecorder.setProfile(camcorderProfile);

        File videoFile = new File(SDUtils.makeOutputFileName());
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());

        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
    }
}
