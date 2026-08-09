package com.jakt.aiplatform.core.model.enums;

import com.jakt.aiplatform.core.model.exception.ErrorCode;

/**
 * 全局错误码枚举。新增错误码必须在此登记，禁止在业务代码中使用魔法数字或字符串。
 *
 * <p>分段规则：
 * <ul>
 *     <li>1xxxx 系统级</li>
 *     <li>2xxxx 参数级</li>
 *     <li>3xxxx 业务级</li>
 *     <li>4xxxx 外部依赖</li>
 * </ul>
 */
public enum ErrorCodeEnum implements ErrorCode {

    /** 系统内部错误。 */
    SYSTEM_ERROR(10000, "系统内部错误"),

    /** 参数校验失败。 */
    PARAM_INVALID(20000, "参数校验失败"),

    /** 业务处理失败（业务错误码的基类）。 */
    BIZ_ERROR(30000, "业务处理失败"),

    /** 资源不存在。 */
    RESOURCE_NOT_FOUND(30001, "资源不存在"),

    /** 外部服务调用失败。 */
    EXTERNAL_ERROR(40000, "外部服务调用失败");

    private final int code;

    private final String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
