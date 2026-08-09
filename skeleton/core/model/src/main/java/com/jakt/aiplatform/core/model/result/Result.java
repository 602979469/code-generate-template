package com.jakt.aiplatform.core.model.result;

import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回体（core-model 定义，供所有层使用）。
 * 预留：当前仅 BizTemplate 引用（BizTemplate 未接线）；web 层接口统一使用 AiPlatformResult。
 *
 * <p>成功：success=true + data；失败：success=false + errorCodeEnum + errorMessage。
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否成功。 */
    private boolean success;

    /** 失败时的错误码。 */
    private ErrorCodeEnum errorCodeEnum;

    /** 失败时的错误消息。 */
    private String errorMessage;

    /** 业务数据。 */
    private T data;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.success = true;
        return result;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = ok();
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(ErrorCodeEnum errorCodeEnum) {
        return fail(errorCodeEnum, errorCodeEnum.getMessage());
    }

    public static <T> Result<T> fail(ErrorCodeEnum errorCodeEnum, String message) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCodeEnum = errorCodeEnum;
        result.errorMessage = message;
        return result;
    }

    /** 兼容 ErrorCode 接口入参。 */
    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        if (errorCode instanceof ErrorCodeEnum errorCodeEnum) {
            return fail(errorCodeEnum, message);
        }
        return fail(ErrorCodeEnum.BIZ_ERROR, message);
    }
}
