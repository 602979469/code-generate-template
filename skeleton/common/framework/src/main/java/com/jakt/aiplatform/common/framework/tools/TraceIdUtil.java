package com.jakt.aiplatform.common.framework.tools;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * traceId 工具：负责 MDC 读写，配合日志模板实现全链路追踪。
 */
public final class TraceIdUtil {

    /** 请求头名称，网关/前端可通过此头透传。 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC key，日志模板中通过 %X{traceId} 输出。 */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    private TraceIdUtil() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    public static void putTraceId(String traceId) {
        MDC.put(TRACE_ID_MDC_KEY, traceId);
    }

    public static void removeTraceId() {
        MDC.remove(TRACE_ID_MDC_KEY);
    }

    /** 生成 32 位无横线 traceId。 */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
