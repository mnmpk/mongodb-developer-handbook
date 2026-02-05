package com.mongodb.javabasic.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${settings.corePoolsize}")
    private int corePoolsize;

    @Value("${settings.maxPoolSize}")
    private int maxPoolSize;

    @Value("${settings.queueSize}")
    private int queueSize;

    @Bean
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolsize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("th-");
        executor.initialize();
        return executor;
    }


}
