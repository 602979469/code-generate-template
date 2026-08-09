package com.jakt.aiplatform.core.model.template;

import com.jakt.aiplatform.core.model.exception.AiPlatformException;
import com.jakt.aiplatform.core.model.exception.ErrorCode;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务模板：统一捕获异常并组装返回结果。
 *
 * <p>约定：
 * <ul>
 *     <li>业务异常（{@link AiPlatformException}）→ success=false + 错误码 + message</li>
 *     <li>系统异常 → success=false + SYSTEM_ERROR（并记录 error 日志）</li>
 *     <li>正常返回 → success=true + data</li>
 * </ul>
 */
public class BizTemplate {

    private static final Logger log = LoggerFactory.getLogger(BizTemplate.class);

    private BizTemplate() {
    }

    /**
     * 执行业务逻辑，自动捕获异常并组装返回结果。
     *
     * @param params   业务入参
     * @param callback 业务回调
     */
    public static <T, R> Result<R> execute(T params, BizTemplateCallBack<T, R> callback) {
        try {
            R data = callback.process(params);
            Result<R> result = new Result<>();
            result.setSuccess(true);
            result.setData(data);
            return result;
        } catch (AiPlatformException e) {
            // 业务异常：success=false + 错误码 + message
            ErrorCode errorCode = e.getErrorCode();
            log.warn("业务异常 code={} message={}", errorCode.getCode(), e.getMessage());
            Result<R> result = new Result<>();
            result.setSuccess(false);
            if (errorCode instanceof ErrorCodeEnum errorCodeEnum) {
                result.setErrorCodeEnum(errorCodeEnum);
            } else {
                result.setErrorCodeEnum(ErrorCodeEnum.BIZ_ERROR);
            }
            result.setErrorMessage(e.getMessage());
            return result;
        } catch (Exception e) {
            // 系统异常：记录 error 日志，返回系统错误
            log.error("系统异常", e);
            Result<R> result = new Result<>();
            result.setSuccess(false);
            result.setErrorCodeEnum(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
    }
}
