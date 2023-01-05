package com.maicard.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskConfig {

    /*
    如果不强行配置一个，可能会报这个错
    NoUniqueBeanDefinitionException: No qualifying bean of type 'org.springframework.core.task.TaskExecutor' available: expected single matching bean but found 2: applicationTaskExecutor,taskScheduler
     */
    @Primary
    @Bean
    public TaskExecutor primaryTaskExecutor(){
        return new ThreadPoolTaskExecutor();
    }
}
