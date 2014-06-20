package com.krdavc.video.recorder.utils;

import android.util.Log;


/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-10 下午04:43:28 类说明
 */
public class LogUtils
{
    /**
     * 日志级别
     */
    private static final int LOG_LEVEL = Log.ERROR;

    /**
     * 异常栈位移
     */
    private static final int EXCEPTION_STACK_INDEX = 2;

    /**
     * verbose级别的日志
     *
     * @param msg 打印内容
     *
     * @see [类、类#方法、类#成员]
     */
    public static void verbose(String msg)
    {
        if (Log.VERBOSE >= LOG_LEVEL) {
            Log.v(getTag(), msg);
        }
    }

    /**
     * debug级别的日志
     *
     * @param msg 打印内容
     *
     * @see [类、类#方法、类#成员]
     */
    public static void debug(String msg)
    {
        if (Log.DEBUG >= LOG_LEVEL) {
            Log.d(getTag(), msg);
        }
    }

    /**
     * info级别的日志
     *
     * @param msg 打印内容
     *
     * @see [类、类#方法、类#成员]
     */
    public static void info(String msg)
    {
        if (Log.INFO >= LOG_LEVEL) {
            Log.i(getTag(), msg);
        }
    }

    /**
     * warn级别的日志
     *
     * @param msg 打印内容
     *
     * @see [类、类#方法、类#成员]
     */
    public static void warn(String msg)
    {
        if (Log.WARN >= LOG_LEVEL) {
            Log.w(getTag(), msg);
        }
    }

    /**
     * error级别的日志
     *
     * @param msg 打印内容
     *
     * @see [类、类#方法、类#成员]
     */
    public static void error(String msg)
    {
        if (Log.ERROR >= LOG_LEVEL) {
            Log.e(getTag(), msg);
        }
    }

    /**
     * 获取日志的标签 格式：类名_方法名_行号 （需要权限：android.permission.GET_TASKS）
     *
     * @return tag
     *
     * @see [类、类#方法、类#成员]
     */
    private static String getTag() throws StackOverflowError
    {
        StackTraceElement element = new LogException().getStackTrace()[EXCEPTION_STACK_INDEX];

        String className = element.getClassName();

        int index = className.lastIndexOf(".");
        if (index > 0) {
            className = className.substring(index + 1);
        }

        return className + "_" + element.getMethodName() + "_"
            + element.getLineNumber();
    }

    /**
     * 取日志标签用的的异常类，只是用于取得日志标签
     */
    private static class LogException extends Exception
    {
        /**
         * 注释内容
         */
        private static final long serialVersionUID = 1L;
    }
}
