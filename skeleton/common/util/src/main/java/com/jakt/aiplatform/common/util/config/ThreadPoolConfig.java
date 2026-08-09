package com.jakt.aiplatform.common.util.config;

import com.jakt.aiplatform.common.util.enums.ThreadPoolEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置：统一提供系统/异步线程池，业务方通过 ThreadPoolUtil 调用。
 */
@Configuration
public class ThreadPoolConfig {

    /** 系统业务线程池，Bean 名称与 {@link ThreadPoolEnum#SYS_THREAD_POOL} 对应。 */
    @Bean(name = "sysThreadPool")
    public ThreadPoolTaskExecutor sysThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("sys-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /** 异步任务线程池（线程池名称枚举后续补充）。 */
    @Bean(name = "asyncThreadPool")
    public ThreadPoolTaskExecutor asyncThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
