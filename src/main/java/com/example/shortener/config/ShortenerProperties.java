package com.example.shortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Set;

@ConfigurationProperties(prefix = "shortener")
public record ShortenerProperties(
        String baseUrl,
        int codeLength,
        int maxCollisionRetries,
        Duration l1Ttl,
        Duration redisMaxTtl,
        Duration negativeCacheTtl,
        Duration maxExpiration,
        int createRequestsPerMinute,
        Set<String> reservedAliases
) {}
