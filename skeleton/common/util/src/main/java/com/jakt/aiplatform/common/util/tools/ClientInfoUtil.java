package com.jakt.aiplatform.common.util.tools;

import cn.dev33.satoken.context.SaHolder;
import cn.hutool.core.util.StrUtil;

/**
 * 客户端信息工具：统一从 Sa-Token 请求上下文获取 IP 与 User-Agent。
 */
public final class ClientInfoUtil {

    /** User-Agent 存储长度上限。 */
    private static final int USER_AGENT_MAX_LENGTH = 255;

    private ClientInfoUtil() {
    }

    /**
     * 获取客户端 IP（优先 X-Forwarded-For，缺省 127.0.0.1）。
     *
     * @return 客户端 IP
     */
    public static String getClientIp() {
        String ip = SaHolder.getRequest().getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip)) {
            return "127.0.0.1";
        }
        // X-Forwarded-For 可能是逗号分隔的多级代理列表，取最左侧真实客户端 IP
        int commaIndex = ip.indexOf(',');
        if (commaIndex >= 0) {
            ip = ip.substring(0, commaIndex);
        }
        ip = ip.trim();
        // 代理以 IPv6 映射格式上报 IPv4（如 ::ffff:192.168.3.212），归一化为纯 IPv4
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring("::ffff:".length());
        }
        return ip;
    }

    /**
     * 获取 User-Agent（截断至 255 字符）。
     *
     * @return User-Agent
     */
    public static String getUserAgent() {
        return StrUtil.maxLength(SaHolder.getRequest().getHeader("User-Agent"), USER_AGENT_MAX_LENGTH);
    }
}
