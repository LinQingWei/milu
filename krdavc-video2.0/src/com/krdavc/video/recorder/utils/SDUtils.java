package com.krdavc.video.recorder.utils;

import java.io.File;

import android.os.Build;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

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

	/**
	 * 获取Android文件系统挂载SD卡的目录，一般为/mnt/sdcard或/sdcard
	 * 
	 * @return 以字符串形式返回SD卡的根目录，如果SD卡无法使用，则返回null
	 */
	public static String sdcardRootPath() {
		if (Build.MODEL.equals("SM-G9006V")) {
			return "/storage/extSdCard";
		}
		Log.d("SDUtils", "sdcardRootPath paths : " + StorageOptions.paths);
		if (StorageOptions.paths != null && StorageOptions.paths.length != 0) {
			return StorageOptions.paths[0];
		}
		return null;
	}

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
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断SD卡上录像文件存放目录是否存在
	 */
	public static boolean routePathExists() {
		File adFile = new File(sdcardRootPath() + routeSDcardPath());

		if (adFile.exists() && adFile.isDirectory()) {
			return true;
		}

		return false;
	}

	/**
	 * 获取本地录像文件在SD卡上的存放路径，如/route/
	 */
	public static String routeSDcardPath() {
		return ROUTE_SDCARD_PATH;
	}

	/**
	 * 创建广告存放路径
	 * 
	 * @return 如果已经存在，返回false；如果不存在，则创建所需的目录，并返回true
	 */
	public static boolean createRoutePath() {
		Log.e("SDUtils", "createRoutePath");
		if (!sdCardExists()) {
			Log.e("SDUtils", "sdCardExists no");
			return false;
		}

		if (routePathExists()) {
			Log.e("SDUtils", "routePathExists no");
			return false;
		}
		String path = routePath();
		Log.e("SDUtils", "mkdirs : " + path);
		File adDir = new File(path);
		return adDir.mkdirs();
	}

	/**
	 * 获取本地广告文件在Android文件系统上的完整存放路径，如/mnt/sdcard/route/
	 */
	public static String routePath() {
		String path = sdcardRootPath() + routeSDcardPath();
		Log.d("SDUtils", path);
		return path;
	}

	/**
	 * 
	 * @return
	 */
	public static String makeOutputFileName() {
		Time time = new Time();
		time.setToNow();
		return String.format("%s%s%s", SDUtils.routePath(), time.format2445(), ".avi");
	}

}
