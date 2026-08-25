package com.jakt.aiplatform.common.dal.config;

import com.jakt.aiplatform.common.framework.template.TransactionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * common-dal 事务配置。
 */
@Configuration
public class DalTransactionConfig {

    @Bean
    @Primary
    public TransactionTemplate dalTransactionTemplate(PlatformTransactionManager transactionManager) {
        return new SpringTransactionTemplate(transactionManager);
    }
}
