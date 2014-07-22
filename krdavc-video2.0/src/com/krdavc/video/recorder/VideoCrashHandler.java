package com.krdavc.video.recorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;

import com.krdavc.video.recorder.utils.LogUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-10 下午04:41:32 类说明
 */
public class VideoCrashHandler implements UncaughtExceptionHandler {

	private static final File DIR = new File(
			Environment.getExternalStorageDirectory() + "/router/log/");

	private static final String VERSION_NAME = "versionName";

	private static final String VERSION_CODE = "versionCode";

	private static final String STACK_TRACE = "STACK_TRACE";

	/**
	 * 错误报告文件的扩展名
	 */
	private static final String CRASH_REPORTER_EXTENSION = ".txt";

	private static final long SLEEP_TIME = 1500;

	private static final int FILE_NUM = 10;

	private static SimpleDateFormat format = new SimpleDateFormat(
			"yyyyMMdd_HHmmss");

	/**
	 * CrashHandler实例
	 */
	private static VideoCrashHandler instance;

	/**
	 * 系统默认的UncaughtException处理类
	 */
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	/**
	 * 程序的Context对象
	 */
	private Context mContext;

	/**
	 * 使用Properties来保存设备的信息和错误堆栈信息
	 */
	private Properties mDeviceCrashInfo = new Properties();

	/**
	 * 是否弹出提示
	 */
	private boolean isToast = false;

	/**
	 * 保证只有一个CrashHandler实例
	 */
	private VideoCrashHandler() {
	}

	/**
	 * 获取CrashHandler实例 ,单例模式
	 * 
	 * @return CrashHandler
	 * 
	 * @see [类、类#方法、类#成员]
	 */
	public static synchronized VideoCrashHandler getInstance() {
		if (instance == null) {
			instance = new VideoCrashHandler();
		}
		return instance;
	}

	/**
	 * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
	 * 
	 * @param ctx
	 *            Context
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 * 
	 * @param thread
	 *            Thread
	 * @param ex
	 *            Throwable
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		ex.printStackTrace();
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// Sleep一会后结束程序
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			// System.exit(10);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
	 * 
	 * @param ex
	 * 
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		}

		ex.printStackTrace();

		// final String msg = ex.getLocalizedMessage();

		// 使用Toast来显示异常信息
		if (isToast) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					Looper.prepare();
					Toast.makeText(mContext, "程序发生异常", Toast.LENGTH_LONG)
							.show();
					Looper.loop();
				}
			};
			thread.start();
		}

		// 收集设备信息
		collectCrashDeviceInfo(mContext);

		// 保存错误报告文件
		String crashFileName = saveCrashInfoToFile(ex);
		LogUtils.error(crashFileName);

		// 发送错误报告到服务器
		// sendCrashReportsToServer(mContext);
		return true;
	}

	/**
	 * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
	 */
	public void sendPreviousReportsToServer() {
		sendCrashReportsToServer(mContext);
	}

	/**
	 * 把错误报告发送给服务器,包含新产生的和以前没发送的.
	 * 
	 * @param ctx
	 */
	private void sendCrashReportsToServer(Context ctx) {
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));

			for (String fileName : sortedFiles) {
				File cr = new File(ctx.getFilesDir(), fileName);
				if (postReport(cr)) {
					// 删除已发送的报告
					cr.delete();
				}
			}
		}
	}

	private boolean postReport(File file) {
		// TODO 使用HTTP Post 发送错误报告到服务器
		// 这里不再详述,开发者可以根据OPhoneSDN上的其他网络操作
		// 教程来提交错误报告

		return false;
	}

	/**
	 * 获取错误报告文件名
	 * 
	 * @param ctx
	 */
	private String[] getCrashReportFiles(Context ctx) {
		// File filesDir = ctx.getFilesDir();
		File filesDir = DIR;

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};

		return filesDir.list(filter);
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 */
	private String saveCrashInfoToFile(Throwable ex) {
		removeCache();

		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info);
		ex.printStackTrace(printWriter);

		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}

		String result = info.toString();
		printWriter.close();
		mDeviceCrashInfo.put(STACK_TRACE, result);

		try {
			// long timestamp = System.currentTimeMillis();
			String fileName = "crash_" + format.format(new Date())
					+ CRASH_REPORTER_EXTENSION;

			// FileOutputStream trace = mContext.openFileOutput(fileName,
			// Context.MODE_PRIVATE);
			if (!DIR.exists()) {
				if (!DIR.mkdirs()) {
					return null;
				}
			}

			FileOutputStream trace = new FileOutputStream(new File(DIR,
					fileName));

			mDeviceCrashInfo.store(trace, "");
			trace.flush();
			trace.close();
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 收集程序崩溃的设备信息
	 * 
	 * @param ctx
	 *            Context
	 */
	private void collectCrashDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				mDeviceCrashInfo.put(VERSION_NAME,
						pi.versionName == null ? "not set" : pi.versionName);
				mDeviceCrashInfo.put(VERSION_CODE, pi.versionCode + "");
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		// 使用反射来收集设备信息.在Build类中包含各种设备信息,
		// 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
		// 具体信息请参考后面的截图
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				mDeviceCrashInfo.put(field.getName(), field.get(null) + "");

				LogUtils.error(field.getName() + " : " + field.get(null));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除日志文件，控制在10个
	 * 
	 * @param dirPath
	 */
	private void removeCache() {

		File[] files = DIR.listFiles();

		if (files == null || files.length == 0) {
			return;
		}

		if (files.length > FILE_NUM) {
			int removeFactor = files.length - FILE_NUM;

			Arrays.sort(files, new FileLastModifSort());

			for (int i = 0; i < removeFactor; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				}
			}
		}
	}

	/**
	 * 根据文件的最后修改时间进行排序 *
	 */
	private class FileLastModifSort implements Comparator<File> {
		public int compare(File arg0, File arg1) {
			if (arg0.lastModified() > arg1.lastModified()) {
				return 1;
			} else if (arg0.lastModified() == arg1.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	public static void save2Log(String log, String fileName) {
		Log.i("TAG", log);
		if (TextUtils.isEmpty(fileName)) {
			fileName = "log.txt";
		}
		if (!DIR.exists()) {
			if (!DIR.mkdirs()) {
				return;
			}
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(DIR, fileName));
			fos.write(log.getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
