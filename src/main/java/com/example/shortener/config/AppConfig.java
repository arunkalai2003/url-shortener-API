package com.example.shortener.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(ShortenerProperties.class)
public class AppConfig {
    @Bean public Clock clock() { return Clock.systemUTC(); }

    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(10_000);
        executor.setThreadNamePrefix("analytics-publisher-");
        // Redirect availability wins over analytics completeness if the local queue is saturated.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public Cache<String, Object> l1UrlCache(ShortenerProperties p) {
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(p.l1Ttl())
                .build();
    }
}
