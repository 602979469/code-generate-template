package com.jakt.aiplatform.common.util.tools;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.exception.AiPlatformException;
import com.jakt.aiplatform.core.model.exception.ErrorCode;

import java.util.Collection;
import java.util.Map;

/**
 * 条件断言工具：所有 {@code if (条件) { throw ... }} 的场景统一走这里，业务代码不再手写 if + throw。
 *
 * <p>方法命名统一为 {@code throwErrWhenXxx}，所有方法最终都委托给
 * {@link #throwErrWhenTrue(boolean, ErrorCode, String)}；
 * 入参可以只传条件，也可以带 ErrorCode，或 ErrorCode + message（缺省错误码为 {@link ErrorCodeEnum#BIZ_ERROR}）。
 */
public final class AiPlatformInvoker {

    private static final ErrorCode DEFAULT_ERROR_CODE = ErrorCodeEnum.BIZ_ERROR;

    private AiPlatformInvoker() {
    }

    // ---------- 核心 ----------

    public static void throwErrWhenTrue(boolean condition) {
        throwErrWhenTrue(condition, DEFAULT_ERROR_CODE, null);
    }

    public static void throwErrWhenTrue(boolean condition, ErrorCode errorCode) {
        throwErrWhenTrue(condition, errorCode, null);
    }

    public static void throwErrWhenTrue(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            ErrorCode code = errorCode == null ? DEFAULT_ERROR_CODE : errorCode;
            throw new AiPlatformException(code, message == null ? code.getMessage() : message);
        }
    }

    public static void throwErrWhenFalse(boolean condition) {
        throwErrWhenTrue(!condition);
    }

    public static void throwErrWhenFalse(boolean condition, ErrorCode errorCode) {
        throwErrWhenTrue(!condition, errorCode);
    }

    public static void throwErrWhenFalse(boolean condition, ErrorCode errorCode, String message) {
        throwErrWhenTrue(!condition, errorCode, message);
    }

    // ---------- 判空 ----------

    public static void throwErrWhenNull(Object value) {
        throwErrWhenTrue(ObjectUtil.isNull(value));
    }

    public static void throwErrWhenNull(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(ObjectUtil.isNull(value), errorCode);
    }

    public static void throwErrWhenNull(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(ObjectUtil.isNull(value), errorCode, message);
    }

    public static void throwErrWhenNotNull(Object value) {
        throwErrWhenTrue(ObjectUtil.isNotNull(value));
    }

    public static void throwErrWhenNotNull(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(ObjectUtil.isNotNull(value), errorCode);
    }

    public static void throwErrWhenNotNull(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(ObjectUtil.isNotNull(value), errorCode, message);
    }

    // ---------- 判 Blank（字符串）----------

    public static void throwErrWhenBlank(CharSequence value) {
        throwErrWhenTrue(StrUtil.isBlank(value));
    }

    public static void throwErrWhenBlank(CharSequence value, ErrorCode errorCode) {
        throwErrWhenTrue(StrUtil.isBlank(value), errorCode);
    }

    public static void throwErrWhenBlank(CharSequence value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(StrUtil.isBlank(value), errorCode, message);
    }

    public static void throwErrWhenNotBlank(CharSequence value) {
        throwErrWhenTrue(StrUtil.isNotBlank(value));
    }

    public static void throwErrWhenNotBlank(CharSequence value, ErrorCode errorCode) {
        throwErrWhenTrue(StrUtil.isNotBlank(value), errorCode);
    }

    public static void throwErrWhenNotBlank(CharSequence value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(StrUtil.isNotBlank(value), errorCode, message);
    }

    // ---------- 判 Empty（字符串/集合/Map/数组通用）----------

    public static void throwErrWhenEmpty(Object value) {
        throwErrWhenTrue(isEmpty(value));
    }

    public static void throwErrWhenEmpty(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(isEmpty(value), errorCode);
    }

    public static void throwErrWhenEmpty(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(isEmpty(value), errorCode, message);
    }

    public static void throwErrWhenNotEmpty(Object value) {
        throwErrWhenTrue(!isEmpty(value));
    }

    public static void throwErrWhenNotEmpty(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(!isEmpty(value), errorCode);
    }

    public static void throwErrWhenNotEmpty(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(!isEmpty(value), errorCode, message);
    }

    /** 统一判空：null、空字符串、空集合、空 Map、空数组都算 empty。 */
    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence text) {
            return StrUtil.isEmpty(text);
        }
        if (value instanceof Collection<?> collection) {
            return CollUtil.isEmpty(collection);
        }
        if (value instanceof Map<?, ?> map) {
            return MapUtil.isEmpty(map);
        }
        if (value.getClass().isArray()) {
            return ArrayUtil.isEmpty(value);
        }
        return false;
    }
}
