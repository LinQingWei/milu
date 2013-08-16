package com.krdavc.video.recorder.utils;

import android.content.Context;
import android.os.Vibrator;

public class VibratorUtils
{

    private static long[] START_PATTERN = {100, 100, 300};

    private static long[] STOP_PATTERN = {100, 100, 300, 100, 300};

    private static long[] SHUTDOWN_PATTERN = {50, 700};

    private static Vibrator mVibrator;

    public static void start(Context context)
    {
        if (mVibrator == null) {
            mVibrator = getSystemVibrator(context);
        }
        mVibrator.vibrate(START_PATTERN, -1);
    }

    public static void stop(Context context)
    {
        if (mVibrator == null) {
            mVibrator = getSystemVibrator(context);
        }
        mVibrator.vibrate(STOP_PATTERN, -1);
    }

    public static void shutdown(Context context)
    {
        if (mVibrator == null) {
            mVibrator = getSystemVibrator(context);
        }
        mVibrator.vibrate(SHUTDOWN_PATTERN, -1);
    }

    private static Vibrator getSystemVibrator(Context context)
    {
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

}
