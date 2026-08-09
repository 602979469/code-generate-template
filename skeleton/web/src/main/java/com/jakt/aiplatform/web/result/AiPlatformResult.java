package com.jakt.aiplatform.web.result;

import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * web 层统一返回体：success + errorCode（数字错误码）+ errorMessage + data。
 *
 * <p>独立于 core-model 的 Result，web 层接口统一使用；新增接口通过 ok/fail 静态工厂组装。
 */
@Data
public class AiPlatformResult<T> implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否成功。 */
    private boolean success;

    /** 错误码：ErrorCodeEnum 的 code 数字值。 */
    private int errorCode;

    /** 错误消息。 */
    private String errorMessage;

    /** 业务数据。 */
    private T data;

    public static <T> AiPlatformResult<T> ok() {
        AiPlatformResult<T> result = new AiPlatformResult<>();
        result.setSuccess(true);
        return result;
    }

    public static <T> AiPlatformResult<T> ok(T data) {
        AiPlatformResult<T> result = ok();
        result.setData(data);
        return result;
    }

    public static <T> AiPlatformResult<T> fail(ErrorCodeEnum errorCodeEnum) {
        return fail(errorCodeEnum, errorCodeEnum.getMessage());
    }

    public static <T> AiPlatformResult<T> fail(ErrorCodeEnum errorCodeEnum, String message) {
        AiPlatformResult<T> result = new AiPlatformResult<>();
        result.setSuccess(false);
        result.setErrorCode(errorCodeEnum.getCode());
        result.setErrorMessage(message);
        return result;
    }

    /** 兼容 ErrorCode 接口入参。 */
    public static <T> AiPlatformResult<T> fail(ErrorCode errorCode, String message) {
        if (errorCode instanceof ErrorCodeEnum errorCodeEnum) {
            return fail(errorCodeEnum, message);
        }
        return fail(ErrorCodeEnum.BIZ_ERROR, message);
    }
}
