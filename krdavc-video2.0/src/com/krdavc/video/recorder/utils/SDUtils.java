package com.krdavc.video.recorder.utils;

import java.io.File;

import android.os.Environment;
import android.text.format.Time;

/**
 * SD卡工具类
 * 
 * @author dannysun
 */
public class SDUtils {

	/**
	 * 默认的广告在SD卡上的存放路径
	 * 注意：此路径不包括广告名
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
		if (android.os.Build.MODEL.equals("SCH-I939") || android.os.Build.MODEL.equals("GT-I9300")
				|| android.os.Build.MODEL.equals("SCH-I939D")
				|| android.os.Build.MODEL.equals("SCH-I959")) {
			return "/mnt/extSdCard";
		}

		if (android.os.Build.MODEL.contains("SCH") || android.os.Build.MODEL.contains("GT")) {
			return "/mnt/extSdCard";
		}
		File xt882Path = new File(XT882_EX_SDCARD_PATH);
		File xt3Xath = new File(XT3X_EX_SDCARD_PATH);
		if (xt882Path.exists() && xt882Path.isDirectory()) {
			return XT882_EX_SDCARD_PATH;
		} else if (xt3Xath.exists() && xt3Xath.isDirectory()) {
			return XT3X_EX_SDCARD_PATH;
		} else {
			boolean sdCardExist = sdCardExists();
			if (sdCardExist) {
				File sdDir = Environment.getExternalStorageDirectory();
				return sdDir.getPath();
			} else {
				return null;
			}
		}
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
		if (!sdCardExists())
			return false;

		if (routePathExists())
			return false;

		File adDir = new File(routePath());

		return adDir.mkdirs();
	}

	/**
	 * 获取本地广告文件在Android文件系统上的完整存放路径，如/mnt/sdcard/route/
	 */
	public static String routePath() {
		return sdcardRootPath() + routeSDcardPath();
	}

	/**
	 * 
	 * @return
	 */
	public static String makeOutputFileName() {
		Time time = new Time();
		time.setToNow();
		return String.format("%s%s%s", SDUtils.routePath(), time.format2445(), ".rar");
	}

}
