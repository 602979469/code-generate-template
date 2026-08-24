package com.jakt.aiplatform.web.result;

import com.jakt.aiplatform.common.util.error.ErrorCode;
import com.jakt.aiplatform.common.util.error.CommonErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * web 层统一返回体：success + errorCode（字符串错误码）+ errorMessage + data。
 *
 * <p>独立于 core-model 的 Result，web 层接口统一使用；新增接口通过 ok/fail 静态工厂组装。
 */
@Data
public class ApiResult<T> implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否成功。 */
    private boolean success;

    /** 错误码：ErrorCodeEnum 的 code 数字值。 */
    private String errorCode;

    /** 错误消息。 */
    private String errorMessage;

    /** 业务数据。 */
    private T data;

    public static <T> ApiResult<T> ok() {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(true);
        return result;
    }

    public static <T> ApiResult<T> ok(T data) {
        ApiResult<T> result = ok();
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ApiResult<T> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }

    public static <T> ApiResult<T> fail(String errorCode, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(message);
        return result;
    }

    public static <T> ApiResult<T> fail(String message) {
        return fail(CommonErrorCode.SYSTEM_ERROR, message);
    }
}
