package com.burjkhalifacorp.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class DownloadConfig {
    @Value("${downloads.core-pool-size}")
    private int corePoolSize;

    @Value("${downloads.max-pool-size}")
    private int maxPoolSize;

    @Value("${downloads.queue-capacity}")
    private int queueCapacity;

    @Bean(name = "downloadsExecutor")
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("download-task-");
        executor.initialize();
        return executor;
    }
}
