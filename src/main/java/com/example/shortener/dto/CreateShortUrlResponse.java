package com.example.shortener.dto;
import java.time.Instant;
public record CreateShortUrlResponse(String shortCode, String shortUrl, String normalizedUrl, Instant createdAt, Instant expiresAt) {}
