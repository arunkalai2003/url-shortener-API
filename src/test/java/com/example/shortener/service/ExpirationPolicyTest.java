package com.example.shortener.service;

import com.example.shortener.config.ShortenerProperties;
import com.example.shortener.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ExpirationPolicyTest {
    private final Instant now=Instant.parse("2026-08-17T19:00:00Z");
    private final ShortenerProperties p=new ShortenerProperties("http://x",8,5,Duration.ofSeconds(30),Duration.ofMinutes(30),Duration.ofSeconds(20),Duration.ofDays(365),60, Set.of());
    private final ExpirationPolicy policy=new ExpirationPolicy(Clock.fixed(now,ZoneOffset.UTC),p);
    @Test void nullMeansNoExplicitExpiry(){ assertNull(policy.validate(null)); }
    @Test void rejectsPast(){ assertThrows(BadRequestException.class,()->policy.validate(now.minusSeconds(1))); }
    @Test void acceptsFuture(){ assertEquals(now.plusSeconds(60),policy.validate(now.plusSeconds(60))); }
}
