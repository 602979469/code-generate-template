package com.jakt.aiplatform.common.framework.error;

/**
 * 通用错误码契约：对外 code 为字符串，且与实现枚举名一致。
 */
public interface ErrorCode {

    /**
     * 错误码。
     *
     * @return 错误码字符串
     */
    String getCode();

    /**
     * 错误默认消息。
     *
     * @return 错误消息
     */
    String getMessage();
}
