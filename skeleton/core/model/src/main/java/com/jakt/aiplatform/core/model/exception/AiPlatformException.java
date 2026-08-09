package com.jakt.aiplatform.core.model.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class AiPlatformException extends RuntimeException {

    private final ErrorCode errorCode;

    public AiPlatformException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AiPlatformException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiPlatformException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
