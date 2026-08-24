package com.jakt.aiplatform.common.dal.config;

import com.jakt.aiplatform.common.util.template.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * common-dal 对 Spring TransactionTemplate 的适配实现。
 */
public class SpringTransactionTemplate implements TransactionTemplate {

    private final org.springframework.transaction.support.TransactionTemplate delegate;

    public SpringTransactionTemplate(PlatformTransactionManager transactionManager) {
        this.delegate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(TransactionCallback<T> action) {
        return delegate.execute(status -> action.execute());
    }

    @Override
    public void executeWithoutResult(TransactionCallbackWithoutResult action) {
        delegate.executeWithoutResult(status -> action.execute());
    }
}
