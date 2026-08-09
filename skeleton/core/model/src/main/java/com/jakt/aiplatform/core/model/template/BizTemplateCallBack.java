package com.jakt.aiplatform.core.model.template;

/**
 * 业务模板回调：调用方通过 lambda 提供具体业务逻辑。
 *
 * @param <T> 入参类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface BizTemplateCallBack<T, R> {

    /** 执行业务逻辑。 */
    R process(T params);
}
