package com.jakt.aiplatform.app.web.support;

import com.jakt.aiplatform.core.model.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 请求日志切面：自动记录 Controller 调用、耗时与返回码，业务代码无需自行打请求日志。
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    private static final int MAX_ARG_LENGTH = 300;

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            long cost = System.currentTimeMillis() - start;
            String code = "?";
            if (result instanceof Result<?> r) {
                code = String.valueOf(r.getCode());
            } else if (error != null) {
                code = "exception";
            }
            log.info("请求日志 class={} method={} cost={}ms code={} args={}",
                    className, methodName, cost, code, truncate(Arrays.toString(args)));
        }
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= MAX_ARG_LENGTH ? text : text.substring(0, MAX_ARG_LENGTH) + "...(truncated)";
    }
}
