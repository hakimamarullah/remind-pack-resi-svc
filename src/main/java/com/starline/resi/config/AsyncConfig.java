package com.starline.resi.config;

import io.opentelemetry.context.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncConfig implements AsyncUncaughtExceptionHandler {

    @Bean
    @Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-custom-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return Context.taskWrapping(executor);
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... ignored) {
        log.warn("Uncaught exception in async task -> Method: {}, Class: {}, Message: {}", method.getName(),
                method.getDeclaringClass().getName(),
                ex.getMessage());
    }
}
