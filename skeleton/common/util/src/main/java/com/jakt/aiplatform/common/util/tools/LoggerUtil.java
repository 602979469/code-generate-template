package com.jakt.aiplatform.common.util.tools;

import com.jakt.aiplatform.common.util.enums.LogFileEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * common-util 层统一日志工具。
 */
public final class LoggerUtil {

    private static final Map<LogFileEnum, Logger> LOGGER_CACHE = new ConcurrentHashMap<>();

    private LoggerUtil() {
    }

    public static void info(LogFileEnum logFile, String message, Object... args) {
        logger(logFile).info(message, args);
    }

    public static void warn(LogFileEnum logFile, String message, Object... args) {
        logger(logFile).warn(message, args);
    }

    public static void error(LogFileEnum logFile, String message, Object... args) {
        logger(logFile).error(message, args);
    }

    public static void error(LogFileEnum logFile, Throwable throwable, String message, Object... args) {
        logger(logFile).error(message, appendThrowable(args, throwable));
    }

    private static Object[] appendThrowable(Object[] args, Throwable throwable) {
        Object[] result = new Object[args.length + 1];
        System.arraycopy(args, 0, result, 0, args.length);
        result[args.length] = throwable;
        return result;
    }

    private static Logger logger(LogFileEnum logFile) {
        return LOGGER_CACHE.computeIfAbsent(logFile, file -> LoggerFactory.getLogger(file.getFileName()));
    }
}
