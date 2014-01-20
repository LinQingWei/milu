package com.krdavc.video.recorder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class BackgroundVideoRecorder extends Service implements SurfaceHolder.Callback {

	private WindowManager windowManager;
	private SurfaceView surfaceView;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onCreate() {

		// Start foreground service to avoid unexpected kill
		Notification notification = null;
		Intent act = new Intent(this, VideoRecordActivity.class);
		act.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, act, 0);
		if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification = new Notification.Builder(this)
					.setContentTitle("Background Video Recorder").setContentText("")
					.setDeleteIntent(contentIntent).setSmallIcon(R.drawable.icon).build();
		} else {
			notification = new Notification(R.drawable.icon, "Background Video Recorder",
					System.currentTimeMillis());
			notification.setLatestEventInfo(this, "Background Video Recorder", "ss", contentIntent);
		}
		startForeground(1234, notification);

		// Create new SurfaceView, set its size to 1x1, move it to the top left
		// corner and set this service as a callback
		windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		surfaceView = new SurfaceView(this);
		LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		windowManager.addView(surfaceView, layoutParams);
		surfaceView.getHolder().addCallback(this);

	}

	// Method called right after Surface created (initializing and starting
	// MediaRecorder)
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {

		Log.e(VideoRecordActivity.TAG, "create from service");
		VideoApplication.sHolder = surfaceHolder;
		// camera = Camera.open();
		// mediaRecorder = new MediaRecorder();
		// camera.unlock();
		//
		// mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		// mediaRecorder.setCamera(camera);
		// mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		// mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		//
		// mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory()
		// + "/"
		// + DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime()) +
		// ".mp4");
		//
		// try {
		// mediaRecorder.prepare();
		// } catch (Exception e) {
		// }
		// mediaRecorder.start();

	}

	// Stop recording and remove SurfaceView
	@Override
	public void onDestroy() {

		// mediaRecorder.stop();
		// mediaRecorder.reset();
		// mediaRecorder.release();
		//
		// camera.lock();
		// camera.release();
		//
		windowManager.removeView(surfaceView);

	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		Log.e(VideoRecordActivity.TAG, "destory from service");

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}