package com.krdavc.video.recorder.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import android.os.Environment;

public class StorageOptions {
	public static final String MNT_SDCARD = Environment.getExternalStorageDirectory().getPath();
	public static ArrayList<String> mMounts = new ArrayList<String>();
	public static ArrayList<String> mVold = new ArrayList<String>();

	/**
	 * 由外部存储到内部存储排序
	 */
	public static String[] paths;

	public static void determineStorageOptions() {
		readMountsFile();
		readVoldFile();
		compareMountsWithVold();
		testAndCleanMountsList();
		setProperties();
	}

	public static void readMountsFile() {
		/*
		 * Scan the /proc/mounts file and look for lines like this:
		 * /dev/block/vold/179:1 /mnt/sdcard vfat
		 * rw,dirsync,nosuid,nodev,noexec,
		 * relatime,uid=1000,gid=1015,fmask=0602,dmask
		 * =0602,allow_utime=0020,codepage
		 * =cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
		 * 
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 */

		try {
			Scanner scanner = new Scanner(new File("/proc/mounts"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("/dev/block/vold/")) {
					String[] lineElements = line.split(" ");
					String path = lineElements[1];
					path.replaceAll(":.*$", "");
					mMounts.add(path);
				}
			}
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readVoldFile() {
		/*
		 * Scan the /system/etc/vold.fstab file and look for lines like this:
		 * dev_mount sdcard /mnt/sdcard 1
		 * /devices/platform/s3c-sdhci.0/mmc_host/mmc0
		 * 
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 */

		try {
			Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("dev_mount")) {
					String[] lineElements = line.split(" ");
					lineElements[2] = lineElements[2].replaceAll(":.*$", "");
					mVold.add(lineElements[2]);
				}
			}
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void compareMountsWithVold() {
		/*
		 * Sometimes the two lists of mount points will be different. We only
		 * want those mount points that are in both list.
		 * 
		 * Compare the two lists together and remove items that are not in both
		 * lists.
		 */

		for (int i = 0; i < mMounts.size(); i++) {
			String mount = mMounts.get(i);
			if (!mVold.contains(mount) && !"/mnt/sdcard2".equals(mount)) // 有些手机上(huawei
																			// U9509)/mnt/sdcard2会不在Void里！！！因此屏蔽掉
				mMounts.remove(i--);
		}

		// don't need this anymore, clear the vold list to reduce memory
		// use and to prepare it for the next time it's needed.
		mVold.clear();
	}

	public static void testAndCleanMountsList() {
		/*
		 * Now that we have a cleaned list of mount paths Test each one to make
		 * sure it's a valid and available path. If it is not, remove it from
		 * the list.
		 */

		for (int i = 0; i < mMounts.size(); i++) {
			String mount = mMounts.get(i);
			File root = new File(mount);
			if (!root.exists() || !root.isDirectory() || !root.canWrite())
				mMounts.remove(i--);
		}
	}

	public static void setProperties() {
		/*
		 * At this point all the paths in the list should be valid. Build the
		 * public properties.
		 */

		ArrayList<String> temp = new ArrayList<String>();
		Iterator<String> it = mMounts.iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			if (MNT_SDCARD.equalsIgnoreCase(path)) {
				it.remove();
				temp.add(path);
			}
		}
		if (temp.isEmpty()) {
			temp.add(MNT_SDCARD);
		}
		mMounts.addAll(temp);
		paths = new String[mMounts.size()];
		mMounts.toArray(paths);

		// don't need this anymore, clear the mounts list to reduce memory
		// use and to prepare it for the next time it's needed.
		mMounts.clear();
	}
}