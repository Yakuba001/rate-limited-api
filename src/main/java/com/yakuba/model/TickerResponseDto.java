package com.yakuba.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TickerResponseDto(
        String symbol,
        BigDecimal price,
        Long timestamp
) {
    public Instant toInstant() {
        return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
    }
}
