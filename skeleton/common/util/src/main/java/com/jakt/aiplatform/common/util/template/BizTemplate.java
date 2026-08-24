package com.jakt.aiplatform.common.util.template;

import com.jakt.aiplatform.common.util.error.CommonErrorCode;
import com.jakt.aiplatform.common.util.error.CommonException;
import com.jakt.aiplatform.common.util.enums.LogFileEnum;
import com.jakt.aiplatform.common.util.result.Result;
import com.jakt.aiplatform.common.util.tools.LoggerUtil;

/**
 * common-util 层业务执行模板：callback + 异常分类 + Result 包装。
 */
public final class BizTemplate {

    private BizTemplate() {
    }

    public static <T> Result<T> execute(Callback<T> callback) {
        try {
            return Result.ok(callback.execute());
        } catch (CommonException e) {
            return Result.fail(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            LoggerUtil.error(LogFileEnum.COMMON_ERROR, e, "业务执行失败");
            return Result.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), CommonErrorCode.SYSTEM_ERROR.getMessage());
        }
    }

    public static <T> Result<T> execute(TransactionTemplate transactionTemplate, Callback<T> callback) {
        try {
            return Result.ok(transactionTemplate.execute(callback::execute));
        } catch (CommonException e) {
            return Result.fail(e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            LoggerUtil.error(LogFileEnum.COMMON_ERROR, e, "业务执行失败");
            return Result.fail(CommonErrorCode.SYSTEM_ERROR.getCode(), CommonErrorCode.SYSTEM_ERROR.getMessage());
        }
    }

    public static Result<Void> executeWithoutResult(CallbackWithoutResult callback) {
        return execute(() -> {
            callback.execute();
            return null;
        });
    }

    public static Result<Void> executeWithoutResult(
            TransactionTemplate transactionTemplate,
            CallbackWithoutResult callback) {
        return execute(transactionTemplate, () -> {
            callback.execute();
            return null;
        });
    }

    @FunctionalInterface
    public interface Callback<T> {
        T execute();
    }

    @FunctionalInterface
    public interface CallbackWithoutResult {
        void execute();
    }
}
