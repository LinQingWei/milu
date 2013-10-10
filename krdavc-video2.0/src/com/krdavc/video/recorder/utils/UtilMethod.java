package com.krdavc.video.recorder.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.krdavc.video.recorder.R;
import com.krdavc.video.recorder.VideoRecordActivity;
import com.krdavc.video.recorder.VideoStore;

public class UtilMethod {

	private static String tag = "UtilMethod";
	private static WakeLock mWakeLock;
	String path = Environment.getExternalStorageDirectory().getPath() + "/router/";

	/**
	 * 获取可用空间百分比
	 */
	public static int getAvailableStore() {
		int result = 0;
		try {
			// 取得sdcard文件路径
			StatFs statFs = new StatFs(SDUtils.routePath());
			// 获取BLOCK数量
			float totalBlocks = statFs.getBlockCount();
			// 可使用的Block的数量
			float availaBlock = statFs.getAvailableBlocks();
			float s = availaBlock / totalBlocks;
			s *= 100;
			result = (int) s;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	/**
	 * 显示Dialog
	 * 
	 * @param context
	 */
	public static void showDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("错误");
		builder.setMessage("存储卡不存在,或者可用空间不够");
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setPositiveButton("退出", null);
		AlertDialog dlg = builder.create();
		dlg.show();
	}

	/**
	 * 设置静音
	 */
	public static void setSilent(Context context) {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
	}

	/**
	 * 没有sdcard提示
	 */
	public static void noSdcardTip(final Activity activity) {
		// 不存在sdcard
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("错误");
		builder.setMessage("存储卡不存在,或者可用空间不够");
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				activity.finish();
				System.exit(0);
			}
		});
		AlertDialog dlg = builder.create();
		dlg.show();
	}

	/**
	 * sdcard异常
	 */
	public static boolean checkSdcardInfo(final Activity activity) {

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// 存在sdcard
			/*
			 * File sdcardRoot = Environment.getExternalStorageDirectory();
			 * StatFs stat = new StatFs(sdcardRoot.getPath()); long blockSize
			 * = stat.getBlockSize(); long availableBlocks =
			 * stat.getAvailableBlocks(); File f = new File(path);
			 * if(!f.exists()) { f.mkdirs(); }
			 */
			// 计算可用空间
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 更新sdcard信息
	 * 
	 * @param tv
	 */
	public static void updateSdcardPercent(final TextView tv) {
		if (tv == null) {
			return;
		}
		final int result = VideoStore.getAvailableStore();
		tv.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				tv.setText(result + "%");
				if (result >= 60) {
					tv.setTextColor(Color.GREEN);
				} else if (result <= 20) {
					tv.setTextColor(Color.RED);
				} else {
					tv.setTextColor(Color.LTGRAY);
				}
			}
		});
	}

	/**
	 * 禁止休眠
	 * 
	 * @param context
	 */
	public static void acquireWakeLock(Context context) {
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UtilMethod");
		}
		mWakeLock.acquire();
	}

	/**
	 * 释放电源锁
	 */
	public static void releaseWakeLock() {
		try {
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
				mWakeLock = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public static void setAirplaneMode(Context context, boolean setAirPlane) {

		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON,
				setAirPlane ? 1 : 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		context.sendBroadcast(intent);
	}

	/**
	 * 屏幕亮
	 */
	public static void enableLCDLight(final Activity context) {
		View view = context.findViewById(R.id.videoBg);
		view.setVisibility(View.GONE);
		return;
	}

	/**
	 * 屏幕关闭
	 */
	public static void disableLCDLight(Activity context) {
		View view = context.findViewById(R.id.videoBg);
		view.setVisibility(View.VISIBLE);

		if (context instanceof VideoRecordActivity) {
			VideoRecordActivity videoRecordActivity = (VideoRecordActivity) context;
			videoRecordActivity.hiddenVideoView();
		}
	}

	/**
	 * 键盘不可亮
	 */
	public static void disableKeyLight() {
		// TODO Auto-generated method stub

		try {
			OutputStream outputStream = Runtime.getRuntime().exec("su").getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			// 2.2
			dataOutputStream
					.writeBytes("echo 0 > /sys/class/leds/button-backlight/brightness\nchmod 444 /sys/class/leds/button-backlight/brightness\n");
			// dataOutputStream.writeBytes("echo 0 > /sys/class/leds/lcd-backlight/brightness\nchmod 444 /sys/class/leds/lcd-backlight/brightness\n");
			// dataOutputStream.writeBytes("chmod 644 /sys/class/leds/lcd-backlight/brightness\necho 2 > /sys/class/leds/lcd-backlight/brightness\n chmod 444 /sys/class/leds/lcd-backlight/brightness\n");
			// /sys/class/leds/lcd-backlight/
			// dataOutputStream.flush();
			Log.i("aaa", "修改成功");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("aaa", "修改键盘灯亮度出错");
			e.printStackTrace();
		}
	}

	/*
	 * public static int toggleButtonBacklight(SharedPreferences
	 * paramSharedPreferences, boolean paramBoolean, String paramString) { int
	 * i = 1; String str2 = ""; str2 = String.valueOf("chmod 644 " +
	 * paramString + "\n" + "echo " + i + " > " + paramString + "\n"); String
	 * str5; String str3 = str2 + "chmod 444 " + paramString + "\n"; str3 =
	 * str5 + "echo " + i + " > " + paramString + "\n"; Log.i("aaa", "开始执行");
	 * }
	 */

	/**
	 * 屏幕键盘亮
	 */
	public static void enableKeyLight() {
		try {
			OutputStream outputStream = Runtime.getRuntime().exec("su").getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			dataOutputStream.writeBytes("chmod 644 /sys/class/leds/button-backlight/brightness\n");
			dataOutputStream.writeBytes("echo 48 > /sys/class/leds/button-backlight/brightness\n");

			// dataOutputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 检查sdcard状态
	 * 
	 * @param context
	 */
	public static boolean sdcardCheck(Context context) {
		// 检查SDCARD状态
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// SDCARD正常
			if (VideoStore.getAvailableStore() > 3) {
				return true;
				// alert("SD卡已满，请清理！");
			} else {
				Toast.makeText(context, "SD卡已满，请清理", 0).show();
			}
		} else {
			Toast.makeText(context, "请检查存储卡", 0).show();
		}
		return false;
	}

	private static String toHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String x16 = Integer.toHexString(bytes[i]);
			if (x16.length() < 2) {
				sb.append("0" + x16);
			} else if (x16.length() > 2) {
				sb.append(x16.substring(x16.length() - 2));
			} else
				sb.append(x16);
		}
		return sb.toString();
	}

	public static void checkMD5(Context context) {
		// 创建文件夹
		File sdcard = Environment.getExternalStorageDirectory();
		String path = sdcard.getAbsolutePath() + File.separator + "aMPU" + File.separator;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
			String path1 = dir.getAbsolutePath() + File.separator + "accredit" + File.separator;
			File dir1 = new File(path1);
			if (!dir1.exists()) {
				dir1.mkdir();
			}
		}
		// 读取授权文件
		try {
			OutputStream outputStream = Runtime.getRuntime().exec("su").getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			dataOutputStream.writeBytes(" cp /data/data/CreditToken.txt /sdcard/aMPU/accredit/\n");
			dataOutputStream.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String filenameString = "sdcard/aMPU/accredit/CreditToken.txt";
		// String filenameString = "data/data/CreditToken.txt";

		try {
			FileInputStream fileInputStream = new FileInputStream(filenameString);
			try {
				byte[] buffer = new byte[fileInputStream.available()];
				fileInputStream.read(buffer);
				// 文件内容
				byte[] credittoken = buffer;

				String str2 = ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
				String str3 = String.valueOf(str2);
				byte[] arrayOfByte1 = MD5.encrypt((str3 + "mygod").getBytes());
				byte[] arrayOfByte3 = "godsaveme".getBytes();
				int j = arrayOfByte1.length;
				int l = arrayOfByte3.length;
				byte[] arrayOfByte4 = new byte[j + l];
				System.arraycopy(arrayOfByte1, 0, arrayOfByte4, 0, 16);
				System.arraycopy(arrayOfByte3, 0, arrayOfByte4, 16, 9);
				byte[] s3 = MD5.encrypt(arrayOfByte4);
				if (!toHex(s3).equalsIgnoreCase(toHex(credittoken))) {
					// alert("未授权，非法安装！");
					Toast.makeText(context, "未授权，非法安装", 0).show();
				}

				Log.i("tag", toHex(credittoken));
				Log.i("tag", toHex(s3));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// alert("找不到授权文件!");
			Toast.makeText(context, "找不到授权文件", 0).show();
		}

	}
}
