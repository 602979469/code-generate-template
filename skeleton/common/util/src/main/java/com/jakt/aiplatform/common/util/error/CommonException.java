package com.jakt.aiplatform.common.util.error;

/**
 * common-util 层通用业务异常。
 */
public class CommonException extends RuntimeException {

    private final String errorCode;

    private final String errorMessage;

    public CommonException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static CommonException of(ErrorCode errorCode, String message) {
        return new CommonException(errorCode.getCode(), message);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
