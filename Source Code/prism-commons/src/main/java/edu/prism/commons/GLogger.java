package edu.prism.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangwei
 */
public class GLogger {

    private static final Logger debugLog = LoggerFactory.getLogger("debugLog");
    private static final Logger infoLog = LoggerFactory.getLogger("info");
    private static final Logger warnLog = LoggerFactory.getLogger("warn");
    private static final Logger errorLog = LoggerFactory.getLogger("error");
    private static final Logger testLog = LoggerFactory.getLogger("testlog");

    static {
        //TODO: to test if this code below is necessary
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            }
        });
    }

    public static boolean isTraceEnabled() {
        return debugLog.isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return debugLog.isDebugEnabled();
    }

    public static void trace(String format, Object... arguments) {
        format = formatLogString(format);
        debugLog.trace(String.format(format, arguments), arguments);
    }

    public static void debug(String format, Object... arguments) {
        if (debugLog.isDebugEnabled()) {
            format = formatLogString(format);
            debugLog.debug(String.format(format, arguments), arguments);
        }
    }

    public static void info(String format, Object... arguments) {

        if (infoLog.isInfoEnabled()) {
            format = formatLogString(format);
            infoLog.info(String.format(format, arguments), arguments);
        }
    }

    public static void warn(Throwable t, String format, Object... arguments) {
        format = formatLogString(format);
        warnLog.warn(String.format(format, arguments), t);
    }

    public static void warn(String format, Object... arguments) {
        format = formatLogString(format);
        warnLog.warn(String.format(format, arguments), arguments);
    }

    public static void error(Throwable t, String format, Object... arguments) {
        format = formatLogString(format);
        errorLog.error(String.format(format, arguments), t);
    }

    public static void error(String format, Object... arguments) {
        format = formatLogString(format);
        errorLog.error(String.format(format, arguments), arguments);
    }

    public static void errorForTest(Throwable t, String format, Object... arguments) {
        format = formatLogString(format);
        testLog.error(String.format(format, arguments), t);
    }

    public static void infoForTest(String format, Object... arguments) {
        format = formatLogString(format);
        testLog.info(String.format(format, arguments), arguments);
    }

    public static String formatLogString(String format) {
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        StackTraceElement s = null;
        int state = 0;
        for (int i = 0; i < stack.length; i++) {
            s = stack[i];
            if (s.getClassName().indexOf("org.gmd.commons.utils.GLogger") != -1) {
                state = 1;
                continue;
            } else {
                if (state == 1) {
                    state = 2;
                }
            }
            if (state == 2) {
                break;
            }
        }
        String[] classNames = s.getClassName().split("\\.");
        String className = classNames[classNames.length - 1];
        String methodName = s.getMethodName();
        int lineNumber = s.getLineNumber();
        String wrapFormat = "%s [%s.java:%d -> %s(...)]";
        return String.format(wrapFormat, format, className, lineNumber, methodName);
    }

}
