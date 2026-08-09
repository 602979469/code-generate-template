package com.jakt.aiplatform.core.model.exception;

import lombok.Getter;

/**
 * 业务异常。领域服务发现规则被违反时抛出，由 web 全局异常处理器统一转换。
 */
@Getter
public class AiPlatformException extends RuntimeException {

    private final ErrorCode errorCode;

    private final String errorMessage;

    public AiPlatformException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errorMessage=errorCode.getMessage();
    }

    public AiPlatformException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage=message;
    }

    public AiPlatformException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage=message;
    }
}
