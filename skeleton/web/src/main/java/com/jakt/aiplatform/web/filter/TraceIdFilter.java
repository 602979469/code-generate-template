package com.jakt.aiplatform.web.filter;

import cn.hutool.core.util.StrUtil;
import com.jakt.aiplatform.common.framework.tools.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * traceId 过滤器：从请求头读取或生成 traceId，写入 MDC，响应头回写。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TraceIdUtil.TRACE_ID_HEADER);
        if (StrUtil.isBlank(traceId)) {
            traceId = TraceIdUtil.generateTraceId();
        }
        TraceIdUtil.putTraceId(traceId);
        response.setHeader(TraceIdUtil.TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceIdUtil.removeTraceId();
        }
    }
}
