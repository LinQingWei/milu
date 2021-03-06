package com.krdavc.video.recorder.utils;

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.format.Time;

import com.krdavc.video.recorder.VideoCrashHandler;

/**
 * SD卡工具类
 * 
 * @author dannysun
 */
public class SDUtils {

	/**
	 * 默认的广告在SD卡上的存放路径 注意：此路径不包括广告名
	 */
	public static final String ROUTE_SDCARD_PATH = "/route/";

	public static final String XT882_EX_SDCARD_PATH = "/sdcard-ext/";
	public static final String XT3X_EX_SDCARD_PATH = "/sdcard/external_sd/";

	private static String sRout;

	/**
	 * 获取Android文件系统挂载SD卡的目录，一般为/mnt/sdcard或/sdcard
	 * 
	 * @return 以字符串形式返回SD卡的根目录，如果SD卡无法使用，则返回null
	 */
	// public static String sdcardRootPath(Context c) {
	// String sdcardPath = "";
	// if (Build.MODEL.equals("SM-G9006V")) {
	// sdcardPath = "/storage/extSdCard";
	// }
	// Log.d("SDUtils", "sdcardRootPath paths : " + StorageOptions.paths);
	// if (StorageOptions.paths != null && StorageOptions.paths.length != 0) {
	// sdcardPath = StorageOptions.paths[0];
	// }
	//
	// sdcardPath += "/android/data/com.krdavc.video.recorder";
	// return sdcardPath;
	// }

	// public static String sdcardRootPath() {
	// boolean sdCardExist = sdCardExists();
	// if (sdCardExist) {
	// File sdDir = Environment.getExternalStorageDirectory();
	// return sdDir.getPath();
	// }
	//
	// return null;
	// }

	/**
	 * SD卡是否可用，如果可用，就可以对其读写
	 */
	public static boolean sdCardExists() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取本地录像文件在SD卡上的存放路径，如/route/
	 */
	public static String routeSDcardPath() {
		return ROUTE_SDCARD_PATH;
	}

	private static final String DIR_ANDROID = "Android";
	private static final String DIR_DATA = "data";
	private static final String DIR_FILES = "files";

	private static File buildPath(File base, String... segments) {
		File cur = base;
		for (String segment : segments) {
			if (cur == null) {
				cur = new File(segment);
			} else if (segment != null) {
				cur = new File(cur, segment);
			}
		}
		return cur;
	}

	/**
	 * 获取本地广告文件在Android文件系统上的完整存放路径，如/mnt/sdcard/route/
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String routePath(Context c) {
		// File f = c.getExternalFilesDir("router");
		// ContextCompat.getObbDirs(c);
		// ContextCompat.getObbDirs(arg0)

		if (sRout != null) {
			return sRout;
		}
		final int version = Build.VERSION.SDK_INT;
		File[] fs = null;
		if (version >= 19) {
			fs = c.getExternalFilesDirs(null);
			// getExternalFilesDir(String).
		} else {
			StorageOptions.determineStorageOptions();
			String[] paths = StorageOptions.paths;
			String thePath = null;
			for (String path : paths) {
				if (path.contains("ex")) {
					thePath = path;
					break;
				}
			}
			if (thePath == null && paths != null && paths.length > 0) {
				thePath = paths[0];
			}
			if (thePath != null) {
				fs = new File[] { buildPath(null, thePath, DIR_ANDROID,
						DIR_DATA, c.getPackageName(), DIR_FILES) };
			}
		}

		if (fs == null || fs.length == 0) {
			return null;
		}
		for (File f : fs) {
			VideoCrashHandler.save2Log(f.getPath(), null);
		}
		String path = null;
		if (fs.length > 1) {
			for (File f : fs) {
				f.delete();
				if (f.getPath().contains("ex")) { // samsung consider this is
													// the external card
					path = f.getPath();
					break;
				}
			}
			if (path == null) {
				path = fs[0].getPath();
			}
		} else {
			fs[0].delete();
			path = fs[0].getPath();
		}
		if (path != null) {
			path = path.replace("/files", "/router");
			new File(path).mkdirs();
		}
		// return fs[0].getPath();
		sRout = path + "/";
		return sRout;
	}

	/**
	 * 
	 * @return
	 */
	public static String makeOutputFileName(Context c) {
		Time time = new Time();
		time.setToNow();
		return String.format("%s%s%s", SDUtils.routePath(c), time.format2445(),
				".rar");
	}

}
