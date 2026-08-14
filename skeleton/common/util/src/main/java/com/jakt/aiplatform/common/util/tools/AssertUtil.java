package com.jakt.aiplatform.common.util.tools;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jakt.aiplatform.common.util.error.CommonException;
import com.jakt.aiplatform.common.util.error.ErrorCode;

import java.util.Collection;
import java.util.Map;

/**
 * common-util 层条件断言工具。
 */
public final class AssertUtil {

    private AssertUtil() {
    }

    public static void throwErrWhenTrue(boolean condition, ErrorCode errorCode) {
        throwErrWhenTrue(condition, errorCode, errorCode.getMessage());
    }

    public static void throwErrWhenTrue(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw CommonException.of(errorCode, message);
        }
    }

    public static void throwErrWhenFalse(boolean condition, ErrorCode errorCode) {
        throwErrWhenTrue(!condition, errorCode);
    }

    public static void throwErrWhenFalse(boolean condition, ErrorCode errorCode, String message) {
        throwErrWhenTrue(!condition, errorCode, message);
    }

    public static void throwErrWhenNull(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(ObjectUtil.isNull(value), errorCode);
    }

    public static void throwErrWhenNull(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(ObjectUtil.isNull(value), errorCode, message);
    }

    public static void throwErrWhenNotNull(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(ObjectUtil.isNotNull(value), errorCode);
    }

    public static void throwErrWhenNotNull(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(ObjectUtil.isNotNull(value), errorCode, message);
    }

    public static void throwErrWhenBlank(CharSequence value, ErrorCode errorCode) {
        throwErrWhenTrue(StrUtil.isBlank(value), errorCode);
    }

    public static void throwErrWhenBlank(CharSequence value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(StrUtil.isBlank(value), errorCode, message);
    }

    public static void throwErrWhenNotBlank(CharSequence value, ErrorCode errorCode) {
        throwErrWhenTrue(StrUtil.isNotBlank(value), errorCode);
    }

    public static void throwErrWhenNotBlank(CharSequence value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(StrUtil.isNotBlank(value), errorCode, message);
    }

    public static void throwErrWhenEmpty(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(isEmpty(value), errorCode);
    }

    public static void throwErrWhenEmpty(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(isEmpty(value), errorCode, message);
    }

    public static void throwErrWhenNotEmpty(Object value, ErrorCode errorCode) {
        throwErrWhenTrue(!isEmpty(value), errorCode);
    }

    public static void throwErrWhenNotEmpty(Object value, ErrorCode errorCode, String message) {
        throwErrWhenTrue(!isEmpty(value), errorCode, message);
    }

    /**
     * 空值判定：null / 空字符串 / 空集合 / 空 Map / 空数组。
     *
     * @param value 待判定对象
     * @return 是否为空
     */
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
