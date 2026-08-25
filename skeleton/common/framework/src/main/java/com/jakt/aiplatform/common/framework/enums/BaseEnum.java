package com.jakt.aiplatform.common.framework.enums;

import com.jakt.aiplatform.common.framework.exception.AiPlatformException;

/**
 * 业务枚举基座。
 *
 * <p>T 为 code 类型（Integer / String / Long…），数据库只存 code；
 * name 为枚举常量名（如 SYSTEM_USER）；desc 为枚举描述（如 系统用户）。
 *
 * <p>DO 使用 code 原始类型，Model 使用枚举，由仓储 Convertor 互转。
 */
public interface BaseEnum<T> {

    /** 枚举 code（数据库存储值）。 */
    T getCode();

    /** 枚举常量名（如 SYSTEM_USER）。 */
    String getName();

    /** 枚举描述（如 系统用户）。 */
    String getDesc();

    /**
     * 按 Integer code 反查枚举；null 返回 null，未匹配抛 ENUM_NOT_MATCHED。
     *
     * @param enumClass 枚举类型
     * @param code code
     * @param <E> 枚举类型
     * @return 枚举
     */
    static <E extends BaseEnum<Integer>> E fromCode(Class<E> enumClass, Integer code) {
        if (code == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw AiPlatformException.ofThrow(ErrorCodeEnum.ENUM_NOT_MATCHED, "枚举值未匹配: " + code);
    }

    /**
     * 按 String code 反查枚举；null 返回 null，未匹配抛 ENUM_NOT_MATCHED。
     *
     * @param enumClass 枚举类型
     * @param code code
     * @param <E> 枚举类型
     * @return 枚举
     */
    static <E extends BaseEnum<String>> E fromCode(Class<E> enumClass, String code) {
        if (code == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw AiPlatformException.ofThrow(ErrorCodeEnum.ENUM_NOT_MATCHED, "枚举值未匹配: " + code);
    }

    /**
     * 按 Long code 反查枚举；null 返回 null，未匹配抛 ENUM_NOT_MATCHED。
     *
     * @param enumClass 枚举类型
     * @param code code
     * @param <E> 枚举类型
     * @return 枚举
     */
    static <E extends BaseEnum<Long>> E fromCode(Class<E> enumClass, Long code) {
        if (code == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw AiPlatformException.ofThrow(ErrorCodeEnum.ENUM_NOT_MATCHED, "枚举值未匹配: " + code);
    }

    /** 枚举是否等于指定 code。 */
    default boolean is(T code) {
        return getCode() != null && getCode().equals(code);
    }
}
