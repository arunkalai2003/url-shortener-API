package com.example.shortener.cache;

import java.time.Instant;

public record CachedUrl(String originalUrl, Instant expiresAt, String status) {
}
