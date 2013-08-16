package com.krdavc.video.recorder.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.R.string;

public class MD5
{
    public static byte[] encrypt(byte[] paramArrayOfByte)
    {
        MessageDigest localMessageDigest = null;

        Object localObject = "MD5";
        try {
            localMessageDigest = MessageDigest.getInstance((String) localObject);
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        localMessageDigest.update(paramArrayOfByte);
        localObject = localMessageDigest.digest();


        return (byte[]) localObject;
    }
}