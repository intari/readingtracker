package com.viorsan.readingtracker;

/**
 * Created by dkzm on 23.05.14.
 * based on https://github.com/fpillet/NSLogger/tree/master/Client%20Logger/Android
 *
 */
import android.content.Context;

import android.util.Log;
import com.NSLogger.NSLoggerClient;
import java.util.Formatter;


public final class DroidLogger extends NSLoggerClient
{
    public static final boolean ALLOW_NSLOGGER=false; //sometimes true=freezes (usually if we switch too much between WiFi and LTE)
    private static String TAG="ReadingTracker";
    public static int LOGLEVEL_CRIT=0;
    public static int LOGLEVEL_FATAL=1;
    public static int LOGLEVEL_ERROR=2;
    public static int LOGLEVEL_WARN=3;
    public static int LOGLEVEL_INFO=4;
    public static int LOGLEVEL_NOTICE=5;
    public static int LOGLEVEL_TRACE=6;

    public DroidLogger(Context ctx)
    {
        super(ctx);
    }

    private void taggedLog(final int level, final String tag, final String message)
    {
        final StackTraceElement[] st = Thread.currentThread().getStackTrace();
        if (st != null && st.length > 4)
        {
            // log with originating source code info
            final StackTraceElement e = st[4];
            String msg=e.getFileName()+":"+e.getLineNumber()+" "+e.getClassName()+"."+e.getMethodName()+"()."+tag+new Integer(level).toString()+" "+message;
            Log.d("ReadingTracker:",msg);
            if (ALLOW_NSLOGGER) {
                log(e.getFileName(), e.getLineNumber(), e.getClassName() + "." + e.getMethodName() + "()", tag, level, message);
            }
        }
        else
        {
            // couldn't obtain stack trace? log without source code info
            if (ALLOW_NSLOGGER) {
                log(tag, level, message);
            }

            String msg= tag+" "+new Integer(level).toString()+" "+message;
            Log.d("ReadingTracker:",msg);

        }
    }

    public void LOG_MARK(String mark)
    {
        logMark(mark);
    }

    public void LOG_EXCEPTION(Exception exc)
    {
        final StackTraceElement[] st = exc.getStackTrace();
        if (st != null && st.length != 0)
        {
            // a stack trace was attached to exception, report the most recent callsite in file/line/function
            // information, and append the full stack trace to the message
            StringBuilder sb = new StringBuilder(256);
            sb.append("Exception: ");
            sb.append(exc.toString());
            sb.append("\n\nStack trace:\n");
            for (int i=0, n=st.length; i < n; i++)
            {
                final StackTraceElement e = st[i];
                sb.append(e.getFileName());
                if (e.getLineNumber() < 0)
                    sb.append(" (native)");
                else
                {
                    sb.append(":");
                    sb.append(Integer.toString(e.getLineNumber()));
                }
                sb.append(" ");
                sb.append(e.getClassName());
                sb.append(".");
                sb.append(e.getMethodName());
                sb.append("\n");
            }
            final StackTraceElement e = st[0];
            String msg=""+e.getFileName()+" "+e.getLineNumber()+e.getClassName() + "." + e.getMethodName() + "()"+TAG+":exception"+" "+sb.toString();
            Log.d("ReadingTracker:Exception",msg);
            if (ALLOW_NSLOGGER) {
                log(e.getFileName(), e.getLineNumber(), e.getClassName() + "." + e.getMethodName() + "()", TAG+":exception", 0, sb.toString());
            }

        }
        else
        {
            // no stack trace attached to exception (should not happen)
            if (ALLOW_NSLOGGER) {
                taggedLog(0, TAG+":exception", exc.toString());
            }
        }
    }

    public void LOG_APP(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":app", new Formatter().format(format, args).toString());
        }
    }

    public void LOG_DYNAMIC_IMAGES(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":images", new Formatter().format(format, args).toString());
        }
    }

    public void LOG_WEBVIEW(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":webview", new Formatter().format(format, args).toString());
        }
    }

    public void LOG_CACHE(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":cache", new Formatter().format(format, args).toString());
        }
    }

    public void LOG_NETWORK(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":network", new Formatter().format(format, args).toString());
        }
    }

    public void LOG_SERVICE(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":service", new Formatter().format(format, args).toString());
        }
    }

    public void LOG_ACCESSIBILITY_SERVICE(int level, String format, Object ... args)
    {
        if (Debug.D) {
            taggedLog(level, TAG+":accessibility_service", new Formatter().format(format, args).toString());
        }
    }
    public void LOG_UI(int level, String format, Object ... args)
    {
        if (Debug.D) {
          taggedLog(level, TAG+":ui", new Formatter().format(format, args).toString());
        }
    }
}