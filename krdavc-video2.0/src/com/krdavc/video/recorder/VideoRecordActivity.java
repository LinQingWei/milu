package com.krdavc.video.recorder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
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
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.krdavc.video.recorder.utils.SDUtils;
import com.krdavc.video.recorder.utils.UtilMethod;
import com.krdavc.video.recorder.utils.VibratorUtils;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-8 下午08:14:30 类说明
 */
public class VideoRecordActivity extends Activity implements SurfaceHolder.Callback {
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
	 * 当前activity是否处于活动状态
	 */
	TextView text;
	Timer availableStoreTimer;
	BatteryReceiver batteryReceiver;
	Timer timer;
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
	private SurfaceView mSurfaceView;// 显示视频的控件
	private static String mOutputFileName;
	/**
	 * 按键点击次数(达到一定次数即为长按)
	 */
	private int keyDownTimes = 0;
	private static int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
	// ToggleButton toggleButton;
	LayoutParams layoutForButton;
	/**
	 * 视频宽度
	 */
	private static int mVideoWidth;
	/**
	 * 视频高度
	 */
	private static int mVideoHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.videomain);
		mContext = this;

		initView();

		// sdcard不存在或者可用空间太小
		if (!UtilMethod.checkSdcardInfo(this) || UtilMethod.getAvailableStore() < 3) {
			UtilMethod.noSdcardTip(this);
		} else {

			// 设置键盘灯不可亮
			// UtilMethod.disableKeyLight();

			// 更新SDCARD最新状态
			updateSdcardPercent();
			sdcardStateUpdate();
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
	private void initView() {

		View toggle = findViewById(R.id.toggle);

		int widthPixel = getResources().getDisplayMetrics().widthPixels;
		int heightPixel = getResources().getDisplayMetrics().heightPixels;

		final int toTop = 106;
		final int toLeft = 960;
		// final int toTop = 50; // 939d
		// final int toLeft = 920; // 939d

		float wRatio = widthPixel / 1080f;
		float hRatio = heightPixel / 1920f;
		android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
				(int) (84 * wRatio), (int) (80 * hRatio));
		params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
		params.leftMargin = (int) (toLeft * wRatio);
		toggle.setLayoutParams(params);

		View cameraType = findViewById(R.id.cameraType);
		params = new android.widget.FrameLayout.LayoutParams((int) (84 * wRatio),
				(int) (80 * hRatio));
		params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
		params.leftMargin = (int) ((toLeft - 160) * wRatio);
		cameraType.setLayoutParams(params);

		params = new android.widget.FrameLayout.LayoutParams((int) (84 * wRatio),
				(int) (80 * hRatio));
		params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
		params.leftMargin = (int) ((toLeft - 360) * wRatio);
		findViewById(R.id.set_param).setLayoutParams(params);

		text = (TextView) findViewById(R.id.textView);
		text.setText("80.7%");
		text.setTextColor(Color.BLUE);

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

		if (android.os.Build.MODEL.equals("XT882")) {
			SURFACE_WIDTH = 450 * 9 / 10;
			SURFACE_HEIGHT = 445 * 9 / 10;
		} else if (isGT93()) {
			SURFACE_WIDTH = 500 * 10 / 10;
			SURFACE_HEIGHT = 445 * 12 / 10;
			cameraType.setVisibility(View.VISIBLE);
		} else {
			SURFACE_WIDTH = 1280 / 2;
			SURFACE_HEIGHT = 720 / 2;
		}

		// LinearLayout.LayoutParams layout = new
		// LinearLayout.LayoutParams(SURFACE_WIDTH, SURFACE_HEIGHT);
		// layout.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		// mSurfaceView.setLayoutParams(layout);

		// mSurfaceView.setClickable(true);
		SurfaceHolder h = mSurfaceView.getHolder();
		h.addCallback(this);
		h.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
		findViewById(R.id.videoView).setVisibility(View.VISIBLE);
	}

	// 按钮
	// toggleButton.setOnCheckedChangeListener(checkChangeListener);

	public void onSetParam(View v) {
		final Camera sCamera = VideoApplication.sCamera;
		final Parameters p = sCamera.getParameters();
		List<String> li = p.getSupportedSceneModes();
		if (li == null) {
			Toast.makeText(this, "不支持模式切换", Toast.LENGTH_SHORT).show();
			return;
		}
		Button btn = (Button) v;
		String mode = p.getSceneMode();
		if (Camera.Parameters.SCENE_MODE_NIGHT.equals(mode)) {
			btn.setText("自动");
			p.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
		} else {
			btn.setText("夜间");
			p.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
		}
		sCamera.setParameters(p);
	}

	@Override
	protected void onPause() {
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		flag_ActivityIsOn = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		UtilMethod.enableLCDLight(mContext);
		flag_ActivityIsOn = false;
		// 回到home桌面后设置toggleButton不能点击
		// toggleButton.setOnCheckedChangeListener(null);
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");

		stopMediaRecordTask();

		UtilMethod.releaseWakeLock();
		// by john
		// UtilMethod.enableKeyLight();

		if (batteryReceiver != null) {
			unregisterReceiver(batteryReceiver);
		}

		UtilMethod.enableLCDLight(mContext);

		// by john
		// Intent intent1 = new Intent();
		// intent1.setClass(mContext, VideoService.class);
		// stopService(intent1);
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static Camera initCamera(int deviceId) {
		try {
			Camera mCamera = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				mCamera = Camera.open(deviceId);
			} else {
				mCamera = Camera.open();
			}
			Camera.Parameters camParams = mCamera.getParameters();
			// if (camParams.isZoomSupported()) {
			// camParams.setZoom(2);
			// }
			// else {
			// Log.i(TAG, "不支持setZoom");
			// }
			// camParams.set("orientation", "portrait");

			// camParams.set("rotation", 90);
			mCamera.lock();
			mCamera.setDisplayOrientation(90);
			mCamera.setParameters(camParams);
			return mCamera;
		} catch (RuntimeException re) {
			Log.v(TAG, "Could not initialize the Camera");
			re.printStackTrace();
			return null;
		}
	}

	private void startVideoRecordTask() {
		timer = new Timer();
		Log.i(TAG, "timer.schedule");
		timer.schedule(new RepeatTimerTask(), RECORD_TIME, RECORD_TIME);
		VideoApplication.sCamera = initCamera(cameraId);
		if (VideoApplication.sCamera == null) {
			return;
		}
		VideoApplication.sRecorder = new MediaRecorder();
		startVideoRecord(this, VideoApplication.sCamera, VideoApplication.sRecorder);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static void startVideoRecord(Activity activity, Camera c, MediaRecorder r) {

		// mWindowMgr.addView(mSurfaceView, layoutForSurfaceView);
		Log.d(TAG, "startVideoRecord before mk file path");
		mOutputFileName = SDUtils.makeOutputFileName();
		SurfaceHolder sHolder = VideoApplication.sHolder;
		if (sHolder == null) {
			return;
		}
		File outFile = new File(mOutputFileName);
		if (outFile.exists()) {
			outFile.delete();
		} else {
			SDUtils.createRoutePath();
		}
		try {
			c.unlock();
			r.setOnErrorListener(new MediaRecorder.OnErrorListener() {
				@Override
				public void onError(MediaRecorder mr, int what, int extra) {
					Log.d(TAG, "onError=" + what + " " + extra);
				}
			});
			// int rotation =
			// activity.getWindowManager().getDefaultDisplay().getRotation();

			// CameraInfo info = new CameraInfo();
			// Camera.getCameraInfo(cameraId, info);
			// if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			// rotation = (info.orientation - rotation + 360) % 360;
			// } else { // back-facing camera
			// rotation = (info.orientation + rotation) % 360;
			// }
			// if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
			// r.setOrientationHint(rotation);
			// } else {
			// r.setOrientationHint(rotation);
			// }
			// mRecorder.setMaxDuration(RECORD_TIME);
			r.setCamera(c);
			r.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			r.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			r.setPreviewDisplay(sHolder.getSurface());
			CamcorderProfile camcorderProfile = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				camcorderProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
			} else {
				camcorderProfile = CamcorderProfile.get(0);
			}
			mVideoWidth = camcorderProfile.videoFrameWidth;
			mVideoHeight = camcorderProfile.videoFrameHeight;

			// 三星相机获取的分辨率和实际支持的不符，通过下面的方法强制设置
			if (cameraId == 1 && isGT93()) {
				// setProfile(mRecorder, camcorderProfile, cameraId);
				r.setProfile(camcorderProfile);
			} else {
				r.setProfile(camcorderProfile);
			}
			r.setOutputFile(mOutputFileName);

			Log.d(TAG, "Video Path " + mOutputFileName);
			r.prepare();
			r.start();
		} catch (Exception e) {
			Log.d(TAG, "MediaRecorder failed to initialize" + e.getMessage());
			e.printStackTrace();
			if (c != null) {
				c.release();
			}
		}
	}

	private static boolean isGT93() {
		return android.os.Build.MODEL.equals("SCH-I939")
				|| android.os.Build.MODEL.equals("GT-I9300")
				|| android.os.Build.MODEL.equals("SCH-I939D");
	}

	public void onChangeCamera(View view) {
		if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		stopMediaRecordTask();
		startVideoRecordTask();
	}

	public void setProfile(MediaRecorder mRecorder, CamcorderProfile profile, int deviceId) {
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

		// mRecorder.setOutputFormat(profile.fileFormat);
		// mRecorder.setVideoFrameRate(profile.videoFrameRate);
		// mRecorder.setVideoSize(profile.videoFrameWidth,
		// profile.videoFrameHeight);
		// mRecorder.setVideoEncodingBitRate(profile.videoBitRate);
		// mRecorder.setAudioEncodingBitRate(profile.audioBitRate);
		// mRecorder.setAudioChannels(profile.audioChannels);
		// mRecorder.setAudioSamplingRate(profile.audioSampleRate);
		// mRecorder.setVideoEncoder(profile.videoCodec);
		// mRecorder.setAudioEncoder(profile.audioCodec);
	}

	public void onToggleVideoView(View view) {
		ViewGroup viewGroup = (ViewGroup) findViewById(R.id.videoView);
		if (viewGroup.getVisibility() == View.INVISIBLE) {
			viewGroup.setVisibility(View.VISIBLE);
		} else {
			viewGroup.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 停止录制
	 */
	private static void stopMediaRecorder(Context context, MediaRecorder r, Camera c) {
		try {
			if (r != null) {
				r.stop();
				r.release();
				r = null;
			}

		} catch (Exception e) {
		}
		try {
			if (c != null) {
				try {
					c.reconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				c.stopPreview();
				c.release();
				c = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mOutputFileName != null) {
			ZipIntentService.startZip(context, mOutputFileName, "123");
		}
	}

	private void stopMediaRecordTask() {
		stopMediaRecorder(this, VideoApplication.sRecorder, VideoApplication.sCamera);
		VideoApplication.sRecorder = null;
		VideoApplication.sCamera = null;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (flag_ActivityIsOn && !screen_off) {
				Log.i(TAG, "点击back键,退出程序");
				finish();
				return true;
			}
		}

		// 只监听几个键
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				keyDownTimes += 1;
				if (keyDownTimes < 5) {
					if (keyCode == KeyEvent.KEYCODE_CAMERA) {
						UtilMethod.showDialog(mContext);
					}
					// 短按,直接退出
					return true;
				} else if (keyDownTimes == 5) {
					// 第一次长按,执行长按操作
					Log.i(TAG, "第一次长按,执行长按操作");
					switch (keyCode) {
					case KeyEvent.KEYCODE_VOLUME_DOWN:
						Log.i(TAG, "长按VOLUME_DOWN事件,发送ManualRecord消息");
						if (timer != null) {
							VibratorUtils.stop(mContext);
							stopMediaRecordTask();
						} else {
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
						} else {
							// 非锁屏状态
							VibratorUtils.stop(mContext);
							UtilMethod.setAirplaneMode(mContext, true);
							UtilMethod.disableLCDLight(mContext);
							findViewById(R.id.videoView).setVisibility(View.INVISIBLE);
							screen_off = true;
						}
						break;

					case KeyEvent.KEYCODE_SEARCH:

						if (screen_off) {
							// 锁屏状态
							UtilMethod.enableLCDLight(mContext);
							screen_off = false;
							Log.i(TAG, "点亮屏幕");
						} else if (flag_ActivityIsOn) {
							// 非锁屏状态
							Log.i(TAG, "弹出框提示关机");
							// smallSurface();
							// toggleButton.setChecked(false);
							shutDownAlertDialog = showShutDownDialog(VideoRecordActivity.this);
							shutDownAlertDialog.getWindow().setType(2003);
							// shutDownAlertDialog.getWindow().setFlags(flags,
							// mask)
							// shutDownAlertDialog.getWindow().addFlags(32);
							shutDownAlertDialog.getWindow().setFlags(
									WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
									WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
							shutDownAlertDialog
									.setOnDismissListener(new DialogInterface.OnDismissListener() {
										public void onDismiss(DialogInterface dialog) {
											// shutdownScreenAndKeyboard();
											UtilMethod.setSilent(mContext);
										}
									});
							shutDownAlertDialog
									.setOnShowListener(new DialogInterface.OnShowListener() {
										public void onShow(DialogInterface dialog) {
										}
									});
							shutDownAlertDialog.show();

						} else {
							Log.i(TAG, "当前activity未处于活动状态, 什么都不做");
						}
						break;

					default:
						break;
					}
					return true;
				}
			} else if (event.getAction() == KeyEvent.ACTION_UP) {
				keyDownTimes = 0;
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		keyDownTimes = 0;
		return true;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(VideoRecordActivity.TAG, "create from activity");
		SurfaceHolder h = VideoApplication.sHolder;
		VideoApplication.sHolder = holder;
		if (h == null) {// 首次创建
			stopMediaRecordTask();
			startVideoRecordTask();
		} else { // 后台转前台
			Intent service = new Intent(this, BackgroundVideoRecorder.class);
			stopService(service);
			stopMediaRecordTask();
			startVideoRecordTask();
		}
		LinearLayout.LayoutParams layout = (android.widget.LinearLayout.LayoutParams) mSurfaceView
				.getLayoutParams();

		if (mVideoWidth == 0 || mVideoHeight == 0 || isGT93()) {
			layout.height = SURFACE_HEIGHT;
			layout.width = SURFACE_WIDTH;
		} else {
			int width = getResources().getDisplayMetrics().widthPixels / 2;
			int height = mVideoWidth * width / mVideoHeight;
			layout.width = width;
			layout.height = height;
		}
		mSurfaceView.requestLayout();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(VideoRecordActivity.TAG, "destory from activity");
		// stopMediaRecordTask();
		if (!isFinishing()) {
			Intent service = new Intent(this, BackgroundVideoRecorder.class);
			startService(service);
		}
	}

	/**
	 * 显示关机AlertDialog
	 * 
	 * @param context
	 */
	public AlertDialog showShutDownDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setAdapter(new ShutDownAdapter(context), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				UtilMethod.setSilent(context);
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
	private void shutdownScreenAndKeyboard() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setTitle("关机");
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setMessage("正在关机...");
		progressDialog.getWindow().setType(2002);
		progressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		progressDialog.show();

		TimerTask progressTimerTask = new TimerTask() {
			@Override
			public void run() {
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

	private void sdcardStateUpdate() {
		Timer sdcardStateUpdateTimer = new Timer();
		TimerTask sdcardStateUpdateTimerTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateSdcardPercent();
			}
		};
		sdcardStateUpdateTimer.schedule(sdcardStateUpdateTimerTask, 500, sdcardStateUpdateTime_ms);
	}

	protected void updateSdcardPercent() {
		final float result = VideoStore.getAvailableExternalMemorySize();
		Log.i(TAG, "getAvailableExternalMemorySize " + result);

		// final int result = GetAvilibleStore.getAvailableStore();
		if (result < 1) {
			Toast.makeText(this, "没有有效的存储空间", 2000).show();
			stopMediaRecordTask();
			System.exit(0);
		}
		text.post(new Runnable() {
			public void run() {
				text.setText(result + "%");
				if (result >= 60) {
					text.setTextColor(Color.GREEN);
				} else if (result <= 20) {
					text.setTextColor(Color.RED);
				} else {
					text.setTextColor(Color.LTGRAY);
				}
				Log.i("tag", "sdcardPercenTextView" + result);
			}
		});
	}

	public void showMessage(String msg) {

		Handler handler = new Handler(getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(VideoRecordActivity.this, "电量/存储空间不足，程序不予运行", 5000).show();
			}
		});
	}

	private static final String FROM_BK_GRD = "FROM_BK_GRD";

	public class RepeatTimerTask extends TimerTask {

		/**
		 * The task to run should be specified in the implementation of the
		 * {@code run()} method.
		 */
		@Override
		public void run() {
			Log.i(TAG, "RepeatTimerTask .run");
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					stopMediaRecorder(getApplication(), VideoApplication.sRecorder,
							VideoApplication.sCamera);
					try {
						VideoApplication.sCamera = initCamera(cameraId);
						if (VideoApplication.sCamera == null) {
							return;
						}
						VideoApplication.sRecorder = new MediaRecorder();
						startVideoRecord(VideoRecordActivity.this, VideoApplication.sCamera,
								VideoApplication.sRecorder);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		}

	}

	public class AvailableStoreTimerTask extends TimerTask {

		@Override
		public void run() {
			Log.i(TAG,
					"AvailableStoreTimerTask .run  current available store = "
							+ UtilMethod.getAvailableStore());

			boolean isFull = UtilMethod.getAvailableStore() < 5;
			if (isFull) {
				stopMediaRecordTask();
				showMessage("电量/存储空间不足，程序不予运行");
				// UtilMethod.noSdcardTip(VideoRecordActivity.this);
				// VideoRecordActivity.this.finish();
			}
		}
	}

	public class BatteryReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra("level", 0);// 电量百分比
			Log.i("BatteryReceiver", "Battery level = " + String.valueOf(level) + "%");

			if (level <= 5) {
				showMessage("电量/存储空间不足，程序不予运行");
				stopMediaRecordTask();
				// VideoRecordActivity.this.finish();
			}
		}
	}
}
