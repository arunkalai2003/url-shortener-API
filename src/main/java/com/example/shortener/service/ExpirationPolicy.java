package com.example.shortener.service;

import com.example.shortener.config.ShortenerProperties;
import com.example.shortener.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class ExpirationPolicy {
    private final Clock clock;
    private final ShortenerProperties properties;

    public ExpirationPolicy(Clock clock, ShortenerProperties properties) {
        this.clock = clock;
        this.properties = properties;
    }

    public Instant validate(Instant expiresAt) {
        if (expiresAt == null) {
            return null; // approved default: no expiry unless explicitly requested
        }
        Instant now = clock.instant();
        if (!expiresAt.isAfter(now)) {
            throw new BadRequestException("expiresAt must be in the future");}

        if (expiresAt.isAfter(now.plus(properties.maxExpiration()))) {
            throw new BadRequestException("expiresAt exceeds maximum allowed lifetime");
        }
        return expiresAt;
    }
}
