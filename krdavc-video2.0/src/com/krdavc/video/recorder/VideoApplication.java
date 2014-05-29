package com.krdavc.video.recorder;

import com.krdavc.video.recorder.utils.StorageOptions;

import android.app.Application;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-10 下午04:45:23 类说明
 */
public class VideoApplication extends Application {

	public static MediaRecorder sRecorder;
	public static Camera sCamera;
	/**
	 * 摄像头预览的holder，在后台或前台的时候，这个holder会赋不同的值
	 */
	public static SurfaceHolder sHolder;

	@Override
	public void onCreate() {
		super.onCreate();
		StorageOptions.determineStorageOptions();
		// 处理全局异常
		VideoCrashHandler crashHandler = VideoCrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());
	}
}
