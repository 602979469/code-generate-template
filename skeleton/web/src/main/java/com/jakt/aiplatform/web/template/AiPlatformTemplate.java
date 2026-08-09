package com.jakt.aiplatform.web.template;

import com.jakt.aiplatform.web.result.AiPlatformResult;
import com.jakt.aiplatform.common.util.enums.LogFileEnum;
import com.jakt.aiplatform.common.util.tools.AiPlatformLoggerUtil;
import com.jakt.aiplatform.common.util.tools.AiPlatformParamValidator;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.exception.AiPlatformException;
import jakarta.validation.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * web 层业务模板：统一请求日志、参数校验、异常封装与 Result 包装，Controller 只提供业务回调。
 *
 * <p>执行流程：请求日志 → beforeService（参数校验）→ execute（业务）→ afterService（finally）→ 结果日志。
 */
public final class AiPlatformTemplate {

    /** 日志时间格式。 */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private AiPlatformTemplate() {
    }

    /**
     * 执行 web 用例：请求日志 → beforeService → execute → afterService（finally），统一包装返回结果。
     *
     * @param param    入参（DTO 或路径参数）
     * @param callback 业务回调
     * @param <P>      入参类型
     * @param <R>      出参类型
     * @return 统一返回体
     */
    public static <P, R> AiPlatformResult<R> execute(P param, Callback<P, R> callback) {
        long start = System.currentTimeMillis();
        String caller = resolveCaller();
        String startTime = LocalDateTime.now().format(TIME_FORMATTER);
        AiPlatformResult<R> result = null;
        R data = null;

        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "请求开始 接口信息={} 时间={} 请求参数={}", caller, startTime, param);

        try {
            try {
                callback.beforeService(param);
            } catch (AiPlatformException | ValidationException e) {
                AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "参数校验失败 接口信息={} 原因={}", caller, e.getMessage());
                result = AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, e.getMessage());
            } catch (Exception e) {
                AiPlatformLoggerUtil.error(LogFileEnum.COMMON_ERROR, "执行" + caller + "校验逻辑时抛出异常", e);
                result = AiPlatformResult.fail(ErrorCodeEnum.SYSTEM_ERROR);
            }

            if (result == null) {
                try {
                    data = callback.execute(param);
                    result = AiPlatformResult.ok(data);
                } catch (AiPlatformException e) {
                    AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "业务异常 接口信息={} errorCode={} message={}",
                            caller, e.getErrorCode().getCode(), e.getMessage());
                    result = AiPlatformResult.fail(e.getErrorCode(), e.getMessage());
                } catch (Exception e) {
                    AiPlatformLoggerUtil.error(LogFileEnum.COMMON_ERROR, "执行" + caller + "业务逻辑时抛出异常", e);
                    result = AiPlatformResult.fail(ErrorCodeEnum.SYSTEM_ERROR);
                }
            }
        } finally {
            try {
                callback.afterService(param, data);
            } catch (Exception e) {
                AiPlatformLoggerUtil.error(LogFileEnum.COMMON_ERROR, "afterService 执行异常 caller=" + caller, e);
            }
            boolean success = result != null && result.isSuccess();
            long cost = System.currentTimeMillis() - start;
            AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE,
                    "请求结束 接口信息={} 时间={} 耗时={}ms 是否成功={} 返回值={}",
                    caller, startTime, cost, success, result);
        }
        return result;
    }

    /**
     * 执行无返回值的 web 用例（如删除），回调无需 return。
     *
     * @param param    入参（DTO 或路径参数）
     * @param callback 无返回值业务回调
     * @param <P>      入参类型
     * @return 统一返回体
     */
    public static <P> AiPlatformResult<Void> executeWithoutResult(P param, CallbackWithoutResult<P> callback) {
        return execute(param, new Callback<P, Void>() {

            @Override
            public void beforeService(P p) {
                callback.beforeService(p);
            }

            @Override
            public Void execute(P p) {
                callback.execute(p);
                return null;
            }

            @Override
            public void afterService(P p, Void result) {
                callback.afterService(p);
            }
        });
    }

    /**
     * 业务回调：Controller 用匿名类实现三个钩子。
     *
     * @param <P> 入参类型
     * @param <R> 出参类型
     */
    public interface Callback<P, R> {

        /** 业务执行前钩子：统一在此调用 {@link AiPlatformParamValidator#validate(Object, Class[])} 做参数校验。 */
        void beforeService(P param);

        /** 核心业务逻辑。 */
        R execute(P param);

        /** 业务执行后钩子：留空即可，如需清理/日志在此实现。 */
        void afterService(P param, R result);
    }

    /**
     * 无返回值业务回调：用于 {@link #executeWithoutResult(Object, CallbackWithoutResult)}。
     *
     * @param <P> 入参类型
     */
    public interface CallbackWithoutResult<P> {

        /** 业务执行前钩子：统一在此调用 {@link AiPlatformParamValidator#validate(Object, Class[])} 做参数校验。 */
        void beforeService(P param);

        /** 核心业务逻辑（无返回值）。 */
        void execute(P param);

        /** 业务执行后钩子：留空即可，如需清理/日志在此实现。 */
        void afterService(P param);
    }

    /** 解析调用方（Controller 类名.方法名），用于接口信息日志。
     *  每次请求遍历堆栈，当前量级可接受；如需精确接口名可改为显式传参。 */
    private static String resolveCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (!className.startsWith(AiPlatformTemplate.class.getName())
                    && !className.startsWith("java.")
                    && !className.startsWith("jdk.")) {
                return className.substring(className.lastIndexOf('.') + 1) + "." + element.getMethodName();
            }
        }
        return "unknown";
    }
}
