package com.jakt.aiplatform.common.framework.context;

/**
 * 用户上下文：web 层过滤器写入当前请求用户，业务层统一从此读取。
 *
 * <p>纯 ThreadLocal 实现，不依赖 Spring；后续接入真实登录体系时只改写入方。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    private UserContext() {
    }

    /** 写入当前用户。 */
    public static void set(Long userId, String userName) {
        USER_ID.set(userId);
        USER_NAME.set(userName);
    }

    /** 当前用户ID（未设置时为 null）。 */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 当前用户名（未设置时为 null）。 */
    public static String getUserName() {
        return USER_NAME.get();
    }

    /** 清理当前线程上下文。 */
    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
    }
}
