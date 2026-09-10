package com.example.shortener.analytics;
import java.time.Instant;
import java.util.UUID;
public record AnalyticsEvent(UUID eventId, String shortCode, Instant accessedAt, String country, String region, String referrerDomain, String userAgentFamily) {}
