package com.jakt.aiplatform.web.exception;

import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.enums.LogFileEnum;
import com.jakt.aiplatform.core.model.util.AiPlatformLoggerUtil;
import com.jakt.aiplatform.web.result.AiPlatformResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器：单一入口兜住所有逃逸到框架层的异常。
 *
 * <p>Controller 正常路径不抛异常（结果统一包成 {@link AiPlatformResult}，业务异常由 AiPlatformTemplate 处理），
 * 这里只处理框架级异常，按类型分支转换，细节只进日志不返回前端。
 */
@RestControllerAdvice
public class AiPlatformExceptionHandler {

    /** 单一入口：按异常类型分类转换。 */
    @ExceptionHandler(Exception.class)
    public AiPlatformResult<Void> handleException(Exception e) {
        if (e instanceof HttpMessageNotReadableException) {
            AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "请求体格式错误 {}", e.getMessage());
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "请求体格式错误");
        }
        if (e instanceof MethodArgumentNotValidException) {
            AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "参数校验失败 {}", e.getMessage());
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "参数校验失败");
        }
        if (e instanceof MethodArgumentTypeMismatchException typeMismatch) {
            AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "参数类型错误 name={}", typeMismatch.getName());
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "参数类型错误: " + typeMismatch.getName());
        }
        if (e instanceof MissingServletRequestParameterException missingParam) {
            AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "缺少参数 name={}", missingParam.getParameterName());
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "缺少参数: " + missingParam.getParameterName());
        }
        if (e instanceof DataIntegrityViolationException) {
            AiPlatformLoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "数据完整性冲突 {}", e.getMessage());
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "数据冲突：违反唯一约束或非空约束");
        }
        if (e instanceof NoResourceFoundException || e instanceof NoHandlerFoundException) {
            return AiPlatformResult.fail(ErrorCodeEnum.RESOURCE_NOT_FOUND, "接口不存在");
        }
        if (e instanceof HttpRequestMethodNotSupportedException) {
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "请求方法不支持");
        }
        if (e instanceof HttpMediaTypeNotSupportedException) {
            return AiPlatformResult.fail(ErrorCodeEnum.PARAM_INVALID, "请求媒体类型不支持");
        }
        AiPlatformLoggerUtil.error(LogFileEnum.COMMON_ERROR, "系统异常", e);
        return AiPlatformResult.fail(ErrorCodeEnum.SYSTEM_ERROR);
    }
}
