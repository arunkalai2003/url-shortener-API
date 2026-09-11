package com.example.shortener.dto;

import java.time.Instant;
import java.util.List;

public record AnalyticsResponse(String shortCode, long accessCount, Instant lastAccessedAt, List<GeoCount> byLocation) {

    public record GeoCount(String country, String region, long accessCount) {
    }
}
