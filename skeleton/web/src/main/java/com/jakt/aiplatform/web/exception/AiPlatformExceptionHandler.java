package com.jakt.aiplatform.web.exception;

import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.enums.LogFileEnum;
import com.jakt.aiplatform.core.model.exception.AiPlatformException;
import com.jakt.aiplatform.core.model.util.AiPlatformLoggerUtil;
import com.jakt.aiplatform.web.result.AiPlatformResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * <p>业务异常（{@link AiPlatformException}）沿 cause 链解包后返回 HTTP 200 + 业务错误码；
 * 框架级异常按 HTTP 语义分类（404/405/415/400），细节只进日志不返回前端。
 */
@RestControllerAdvice
public class AiPlatformExceptionHandler {

    /** 单一入口：按异常类型分类转换。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AiPlatformResult<Void>> handleException(Exception e) {
        AiPlatformLoggerUtil.error(LogFileEnum.COMMON_ERROR, "系统异常", e);
        // 框架层可能包装业务异常（如 Jackson 反序列化包 AiPlatformException），沿 cause 链解包
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof AiPlatformException bizException) {
                return ResponseEntity.ok(AiPlatformResult.fail(bizException.getErrorCode(), bizException.getMessage()));
            }
            cause = cause.getCause();
        }
        if (e instanceof NoResourceFoundException || e instanceof NoHandlerFoundException) {
            return fail(HttpStatus.NOT_FOUND, ErrorCodeEnum.RESOURCE_NOT_FOUND, "接口或资源不存在");
        }
        if (e instanceof HttpRequestMethodNotSupportedException) {
            return fail(HttpStatus.METHOD_NOT_ALLOWED, ErrorCodeEnum.PARAM_INVALID, "请求方法不支持");
        }
        if (e instanceof HttpMediaTypeNotSupportedException) {
            return fail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodeEnum.PARAM_INVALID, "不支持的媒体类型");
        }
        if (e instanceof HttpMessageNotReadableException) {
            return fail(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PARAM_INVALID, "请求体格式错误");
        }
        if (e instanceof MissingServletRequestParameterException) {
            return fail(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PARAM_INVALID, "缺少请求参数");
        }
        if (e instanceof MethodArgumentTypeMismatchException) {
            return fail(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PARAM_INVALID, "请求参数类型不匹配");
        }
        if (e instanceof MethodArgumentNotValidException) {
            return fail(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PARAM_INVALID, "参数校验失败");
        }
        if (e instanceof DataIntegrityViolationException) {
            return fail(HttpStatus.OK, ErrorCodeEnum.PARAM_INVALID, "数据不合法：必填字段缺失或违反数据约束");
        }
        return fail(HttpStatus.OK, ErrorCodeEnum.SYSTEM_ERROR, null);
    }

    private ResponseEntity<AiPlatformResult<Void>> fail(HttpStatus status, ErrorCodeEnum errorCode, String message) {
        AiPlatformResult<Void> result = message == null
                ? AiPlatformResult.fail(errorCode)
                : AiPlatformResult.fail(errorCode, message);
        return ResponseEntity.status(status).body(result);
    }
}
