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

        int availableProcessors = 2;
        log.info("Available processors: {}", availableProcessors);

        executor.setCorePoolSize(availableProcessors);

        executor.setMaxPoolSize(availableProcessors * 2);

        executor.setQueueCapacity(availableProcessors * 5);

        executor.setThreadNamePrefix("AsyncIOExecutor-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setTaskDecorator(runnable -> () -> {
            log.info("Task started on thread: {}", Thread.currentThread().getName());

            try {
                runnable.run();
            } catch (Exception ex) {
                log.error("Error executing task on thread: {}", Thread.currentThread().getName(), ex);
                throw ex;
            } finally {
                log.info("Task completed on thread: {}", Thread.currentThread().getName());
            }
        });

        executor.initialize();

        return executor;
    }
}
