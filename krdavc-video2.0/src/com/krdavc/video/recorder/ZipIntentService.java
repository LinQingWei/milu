package com.krdavc.video.recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nochump.util.zip.EncryptZipEntry;
import nochump.util.zip.EncryptZipOutput;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ZipIntentService extends IntentService {
	// TODO: Rename actions, choose action names that describe tasks that this
	// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
	private static final String ACTION_FOO = "com.krdavc.video.recorder.action.FOO";

	// TODO: Rename parameters
	private static final String EXTRA_PARAM1 = "com.krdavc.video.recorder.extra.PARAM1";
	private static final String EXTRA_PARAM2 = "com.krdavc.video.recorder.extra.PARAM2";

	/**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	// TODO: Customize helper method
	public static void startZip(Context context, String aviPath, String pwd) {
		Intent intent = new Intent(context, ZipIntentService.class);
		intent.setAction(ACTION_FOO);
		intent.putExtra(EXTRA_PARAM1, aviPath);
		intent.putExtra(EXTRA_PARAM2, pwd);
		context.startService(intent);
	}

	private EncryptZipOutput mZipOutput;

	public ZipIntentService() {
		super("ZipIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_FOO.equals(action)) {
				final String param1 = intent.getStringExtra(EXTRA_PARAM1);
				final String param2 = intent.getStringExtra(EXTRA_PARAM2);
				handleActionFoo(param1, param2);
			}
		}
	}

	/**
	 * Handle action Foo in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionFoo(String path, String pwd) {
		FileInputStream fis = null;
		try {
			String zipPath = path.replace(".avi", ".zip");
			mZipOutput = new EncryptZipOutput(new FileOutputStream(zipPath), pwd);
			mZipOutput.putNextEntry(new EncryptZipEntry(new File(path).getName()));

			fis = new FileInputStream(path);
			byte[] arr = new byte[1024 * 10];
			int len = 0;
			while ((len = fis.read(arr)) != -1) {
				mZipOutput.write(arr, 0, len);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mZipOutput != null) {
				try {
					mZipOutput.flush();
					mZipOutput.closeEntry();
					mZipOutput.close();
					mZipOutput = null;
					new File(path).delete();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
