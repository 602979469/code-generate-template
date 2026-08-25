package com.jakt.aiplatform.common.framework.result;

/**
 * common-util 层通用业务执行结果。
 */
public class Result<T> {

    private boolean success;

    private String errorCode;

    private String errorMessage;

    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.success = true;
        result.data = data;
        return result;
    }

    public static Result<Void> ok() {
        Result<Void> result = new Result<>();
        result.success = true;
        return result;
    }

    public static <T> Result<T> fail(String errorCode, String errorMessage) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCode = errorCode;
        result.errorMessage = errorMessage;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }
}
