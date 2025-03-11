package org.egov.im.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        log.info("Available processor: {}", availableProcessors);

        // Set core pool size to the number of available processors
        executor.setCorePoolSize(availableProcessors);

        executor.setMaxPoolSize(availableProcessors);

        executor.setQueueCapacity(availableProcessors);

        executor.setThreadNamePrefix("AsyncExecutor-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Initialize the executor
        executor.initialize();

        return executor;
    }
}
