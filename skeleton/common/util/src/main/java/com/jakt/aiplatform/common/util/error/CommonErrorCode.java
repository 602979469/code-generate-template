package com.jakt.aiplatform.common.util.error;

/**
 * common-util 层通用错误码。
 */
public enum CommonErrorCode implements ErrorCode {

    /** 系统内部错误。 */
    SYSTEM_ERROR("系统内部错误"),

    /** 认证失败。 */
    AUTH_ERROR("认证失败"),

    /** 参数校验失败。 */
    PARAM_INVALID("参数校验失败");

    private final String message;

    CommonErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
