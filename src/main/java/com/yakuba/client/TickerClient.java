package com.yakuba.client;

import com.yakuba.exception.TickerNotFoundException;
import com.yakuba.model.TickerResponseDto;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@Component
public class TickerClient {

    private final RestClient restClient;
    private final RateLimiter rateLimiter;
    private final Retry retry;

    public TickerClient(RestClient.Builder restClientBuilder,
                        RateLimiter rateLimiter,
                        Retry retry,
                        @Value("${crypto.api.base-url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.rateLimiter = rateLimiter;
        this.retry = retry;
    }

    public TickerResponseDto fetchTicker(String symbol) {
        Supplier<TickerResponseDto> baseSupplier = () -> {
            ResponseEntity<TickerResponseDto> responseEntity = restClient.get()
                    .uri("/api/v1/ticker?symbol={symbol}", symbol)
                    .retrieve()
                    .toEntity(TickerResponseDto.class);
            if (responseEntity.getStatusCode().value() == 204 || responseEntity.getBody() == null) {
                throw new TickerNotFoundException("Empty answer: " + symbol);
            }
            return responseEntity.getBody();
        };
        Supplier<TickerResponseDto> rateLimitedSupplier = RateLimiter.decorateSupplier(rateLimiter, baseSupplier);
        Supplier<TickerResponseDto> retryingAndRateLimitedSupplier  = Retry.decorateSupplier(retry, rateLimitedSupplier);
        return retryingAndRateLimitedSupplier.get();
    }
}
