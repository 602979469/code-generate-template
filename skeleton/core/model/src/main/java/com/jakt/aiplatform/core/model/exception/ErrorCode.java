package com.jakt.aiplatform.core.model.exception;

/**
 * 错误码接口。所有错误码枚举实现此接口，保证错误码与消息成对出现。
 */
public interface ErrorCode {

    /** 错误码，5 位数字，按段分配（1xxxx 系统 / 2xxxx 参数 / 3xxxx 业务 / 4xxxx 外部依赖）。 */
    int getCode();

    /** 默认错误消息。 */
    String getMessage();
}
