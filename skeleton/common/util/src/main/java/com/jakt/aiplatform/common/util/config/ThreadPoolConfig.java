package com.jakt.aiplatform.common.util.config;

import com.jakt.aiplatform.common.util.enums.ThreadPoolEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置：统一提供系统/异步线程池，业务方通过 ThreadPoolUtil 调用。
 * 注意：MDC 不跨线程，异步任务中无 traceId；后续用 TaskDecorator 在提交时复制 MDC。
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
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }

    /** 异步任务线程池，与 {@link ThreadPoolEnum#ASYNC_THREAD_POOL} 对应。 */
    @Bean(name = "asyncThreadPool")
    public ThreadPoolTaskExecutor asyncThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }

    /** 镜像下载任务线程池，与 {@link ThreadPoolEnum#MIRROR_DOWNLOAD} 对应。 */
    @Bean(name = "mirrorDownloadThreadPool")
    public ThreadPoolTaskExecutor mirrorDownloadThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mirror-download-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }
}
