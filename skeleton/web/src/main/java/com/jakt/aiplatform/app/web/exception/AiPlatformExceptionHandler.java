package com.jakt.aiplatform.app.web.exception;

import com.jakt.aiplatform.app.web.result.AiPlatformResult;
import com.jakt.aiplatform.common.util.enums.LogFileEnum;
import com.jakt.aiplatform.common.util.tools.AiPlatformLoggerUtil;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器：兜住框架级异常（未进入 AiPlatformTemplate 前抛出的，如 JSON 解析失败、参数类型错误）。
 * 业务异常由 AiPlatformTemplate 处理，这里只处理框架异常，统一转 AiPlatformResult。
 */
@RestControllerAdvice
public class AiPlatformExceptionHandler {

    /** 请求体格式错误（如 malformed JSON）。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AiPlatformResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "请求体格式错误 {}", e.getMessage());
        return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "请求体格式错误");
    }

    /** 路径/查询参数类型错误（如 GET /users/abc）。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AiPlatformResult<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "参数类型错误 name={}", e.getName());
        return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "参数类型错误: " + e.getName());
    }

    /** 缺少必填请求参数。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public AiPlatformResult<Void> handleMissingParam(MissingServletRequestParameterException e) {
        AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "缺少参数 name={}", e.getParameterName());
        return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "缺少参数: " + e.getParameterName());
    }

    /** 兜底：未匹配到的其他异常统一转系统错误（细节只进日志，不返回前端）。 */
    @ExceptionHandler(Exception.class)
    public AiPlatformResult<Void> handleException(Exception e) {
        AiPlatformLoggerUtil.error(LogFileEnum.COMMON_ERROR, "系统异常", e);
        return AiPlatformResult.fail(ErrorCodeEnum.SYSTEM_ERROR);
    }
}
