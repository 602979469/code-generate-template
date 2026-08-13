package com.jakt.aiplatform.core.model.enums;

import com.jakt.aiplatform.core.model.exception.ErrorCode;

/**
 * 全局错误码枚举
 *
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

    /** 更新失败：记录不存在或已被修改。 */
    UPDATE_FAILED(30002, "更新失败"),

    /** 删除失败：记录不存在或已被删除。 */
    DELETE_FAILED(30003, "删除失败"),

    /** 枚举值未匹配。 */
    ENUM_NOT_MATCHED(30004, "枚举值未匹配"),

    /** 查询结果不唯一（预期 1 条，实际多条）。 */
    RESULT_NOT_UNIQUE(30005, "查询结果不唯一"),

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
