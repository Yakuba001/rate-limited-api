package com.yakuba.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class ResilienceConfig {

    public static final String SERVICE_KEY = "tickerClient";

    @Bean
    public RateLimiterRegistry customRateLimiterRegistry() {
        RateLimiterConfig  defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(2)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    public RateLimiter customRateLimiter(RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter(SERVICE_KEY);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryOnException(throwable -> {
                    if (throwable instanceof IOException || throwable instanceof TimeoutException) {
                        return true;
                    }
                    if (throwable instanceof RestClientResponseException restException) {
                        int status = restException.getStatusCode().value();
                        return status == 429 || (status >= 500 && status < 600);
                    }
                    return false;
                })
                .build();
        return RetryRegistry.of(config);
    }

    @Bean
    public Retry customRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry(SERVICE_KEY);
    }
}
