package com.jakt.aiplatform.common.framework.template;

/**
 * common-util 层事务执行器抽象。
 */
public interface TransactionTemplate {

    <T> T execute(TransactionCallback<T> action);

    void executeWithoutResult(TransactionCallbackWithoutResult action);

    @FunctionalInterface
    interface TransactionCallback<T> {
        T execute();
    }

    @FunctionalInterface
    interface TransactionCallbackWithoutResult {
        void execute();
    }
}
