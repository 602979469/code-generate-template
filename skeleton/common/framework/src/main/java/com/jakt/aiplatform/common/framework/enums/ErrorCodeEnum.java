package com.jakt.aiplatform.common.framework.enums;

import com.jakt.aiplatform.common.framework.error.ErrorCode;

/**
 * 全局错误码枚举
 * 只保留三个通用异常码：系统异常 / 认证异常 / 参数异常；
 * findOne 由 Mapper selectOne（LIMIT 1）实现，多条取第一条不报错；业务错误码由业务模块自行扩展。
 */
public enum ErrorCodeEnum implements ErrorCode {

    /** 系统内部错误。 */
    SYSTEM_ERROR("系统内部错误"),

    /** 外部服务认证失败。 */
    AUTH_ERROR("外部服务认证失败"),

    /** 参数校验失败。 */
    PARAM_INVALID("参数校验失败");

    private final String message;

    ErrorCodeEnum(String message) {
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
