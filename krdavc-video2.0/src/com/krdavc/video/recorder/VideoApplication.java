package com.krdavc.video.recorder;

import java.lang.reflect.Field;

import com.krdavc.video.recorder.utils.LogUtils;
import com.krdavc.video.recorder.utils.StorageOptions;

import android.app.Application;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
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
		// 使用反射来收集设备信息.在Build类中包含各种设备信息,
		// 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
		// 具体信息请参考后面的截图
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);

				LogUtils.error(field.getName() + " : " + field.get(null));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		StorageOptions.determineStorageOptions();
		// 处理全局异常
		VideoCrashHandler crashHandler = VideoCrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());
	}
}
