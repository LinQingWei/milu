package com.krdavc.video.recorder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static final byte[] encrypt(byte[] src) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		m.update(src);
		return m.digest();
	}

	/**
	 * 将MD5结果转换成大写的16进制的字符串
	 * 
	 * @return
	 */
	public static String byteArray2String(byte[] array) {
		if (array.length != 16) {
			throw new RuntimeException("数组长度不为16：" + array.length);
		}
		// 把byte转成Byte，方便format
		Byte[] arrBytes = new Byte[16];
		for (int i = 0; i < arrBytes.length; i++) {
			arrBytes[i] = array[i];
		}
		return String
				.format("%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
						(Object[]) arrBytes);
	}

	public static byte[] string2ByteArray(String value) {
		if (value.length() != 32) {
			throw new RuntimeException("字符串长度不为32：" + value.length());
		}
		byte[] bValue = new byte[16];
		for (int i = 0; i < 16; i++) {
			String part = value.substring(i * 2, i * 2 + 2);
			bValue[i] = Byte.parseByte(part, 16);
		}
		return bValue;
	}
}
