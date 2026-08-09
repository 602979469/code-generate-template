package com.jakt.aiplatform.app.web.support;

import com.jakt.aiplatform.core.model.exception.AiPlatformException;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：业务代码禁止 try-catch 处理异常，统一在这里转换。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AiPlatformException.class)
    public Result<Void> handleBizException(AiPlatformException e) {
        log.warn("业务异常 code={} message={}", e.getErrorCode().getCode(), e.getMessage());
        Result<Void> result = new Result<>();
        result.setSuccess(false);
        if (e.getErrorCode() instanceof ErrorCodeEnum errorCodeEnum) {
            result.setErrorCodeEnum(errorCodeEnum);
        } else {
            result.setErrorCodeEnum(ErrorCodeEnum.BIZ_ERROR);
        }
        result.setErrorMessage(e.getMessage());
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 {}", message);
        Result<Void> result = new Result<>();
        result.setSuccess(false);
        result.setErrorCodeEnum(ErrorCodeEnum.PARAM_INVALID);
        result.setErrorMessage(message);
        return result;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 {}", message);
        Result<Void> result = new Result<>();
        result.setSuccess(false);
        result.setErrorCodeEnum(ErrorCodeEnum.PARAM_INVALID);
        result.setErrorMessage(message);
        return result;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误 {}", e.getMessage());
        Result<Void> result = new Result<>();
        result.setSuccess(false);
        result.setErrorCodeEnum(ErrorCodeEnum.PARAM_INVALID);
        result.setErrorMessage("请求体格式错误");
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        Result<Void> result = new Result<>();
        result.setSuccess(false);
        result.setErrorCodeEnum(ErrorCodeEnum.SYSTEM_ERROR);
        return result;
    }
}
