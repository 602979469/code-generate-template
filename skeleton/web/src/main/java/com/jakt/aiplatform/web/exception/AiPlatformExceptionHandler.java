package com.jakt.aiplatform.web.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.jakt.aiplatform.common.util.error.CommonErrorCode;
import com.jakt.aiplatform.common.util.error.CommonException;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.common.util.enums.LogFileEnum;
import com.jakt.aiplatform.common.util.tools.LoggerUtil;
import com.jakt.aiplatform.web.result.ApiResult;
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
 * <p>业务异常（{@link CommonException}）沿 cause 链解包后返回 HTTP 200 + 业务错误码；
 * 框架级异常按 HTTP 语义分类（404/405/415/400），细节只进日志不返回前端。
 */
@RestControllerAdvice
public class AiPlatformExceptionHandler {

    /** 未登录异常：HTTP 401，前端跳登录。 */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResult<Void>> handleNotLogin(NotLoginException e) {
        LoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "未登录访问 类型={}", e.getType());
        return fail(HttpStatus.UNAUTHORIZED, ErrorCodeEnum.NOT_LOGIN, "未登录或登录已过期");
    }

    /** 权限/角色不足异常：HTTP 403。 */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public ResponseEntity<ApiResult<Void>> handleNoPermission(RuntimeException e) {
        LoggerUtil.warn(LogFileEnum.BIZ_SERVICE, "无权限访问 原因={}", e.getMessage());
        return fail(HttpStatus.FORBIDDEN, ErrorCodeEnum.NO_PERMISSION, "无权限访问");
    }

    /** 单一入口：按异常类型分类转换。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        LoggerUtil.error(LogFileEnum.COMMON_ERROR, e, "系统异常");
        // 框架层可能包装业务异常（如 Jackson 反序列化包 AiPlatformException），沿 cause 链解包
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof CommonException bizException) {
                return ResponseEntity.ok(ApiResult.fail(bizException.getErrorCode(), bizException.getMessage()));
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

    /**
     * 组装失败响应：HTTP 状态码 + 统一返回体。
     *
     * @param status   HTTP 状态码
     * @param errorCode 错误码
     * @param message  错误消息，可为 null
     * @return 统一返回体
     */
    private ResponseEntity<ApiResult<Void>> fail(HttpStatus status, ErrorCodeEnum errorCode, String message) {
        ApiResult<Void> result = message == null
                ? ApiResult.fail(errorCode)
                : ApiResult.fail(errorCode, message);
        return ResponseEntity.status(status).body(result);
    }
}
