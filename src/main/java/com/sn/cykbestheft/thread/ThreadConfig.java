package com.sn.cykbestheft.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: songning
 * @date: 2020/2/19 22:49
 */
@Configuration
public class ThreadConfig {

    /**
     * 获取 当前在线blogger 的线程池
     *
     * @return
     */
    @Bean(name = "CategoryExecutor")
    public Executor categoryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(17);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(3600);
        executor.setThreadNamePrefix("CategoryAsync_");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(3600);
        return executor;
    }
}
