package com.jakt.aiplatform.core.model.exception;

/**
 * 错误码接口
 */
public interface ErrorCode {

    /** 错误码。 */
    int getCode();

    /** 默认错误消息。 */
    String getMessage();
}
