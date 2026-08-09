package com.jakt.aiplatform.common.util.tools;

import com.jakt.aiplatform.common.util.enums.LogFileEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志工具：按 {@link LogFileEnum} 获取对应 Logger，统一日志输出。
 */
public final class AiPlatformLoggerUtil {

    /** Logger 缓存：枚举 → 对应日志文件 Logger。 */
    private static final Map<LogFileEnum, Logger> LOGGER_CACHE = new ConcurrentHashMap<>();

    private AiPlatformLoggerUtil() {
    }

    /** 记录 info 日志。 */
    public static void info(LogFileEnum logFile, String message) {
        logger(logFile).info(message);
    }

    /** 记录带占位符的 info 日志。 */
    public static void info(LogFileEnum logFile, String format, Object... args) {
        logger(logFile).info(format, args);
    }

    /** 记录 warn 日志。 */
    public static void warn(LogFileEnum logFile, String message) {
        logger(logFile).warn(message);
    }

    /** 记录带占位符的 warn 日志。 */
    public static void warn(LogFileEnum logFile, String format, Object... args) {
        logger(logFile).warn(format, args);
    }

    /** 记录 error 日志（带异常堆栈）。 */
    public static void error(LogFileEnum logFile, String message, Throwable throwable) {
        logger(logFile).error(message, throwable);
    }

    /** 记录 error 日志（不带异常）。 */
    public static void error(LogFileEnum logFile, String message) {
        logger(logFile).error(message);
    }

    private static Logger logger(LogFileEnum logFile) {
        return LOGGER_CACHE.computeIfAbsent(logFile, file -> LoggerFactory.getLogger(file.getFileName()));
    }
}
