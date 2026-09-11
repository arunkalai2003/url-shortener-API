package com.example.shortener.service;

import com.example.shortener.cache.CachedUrl;
import com.example.shortener.cache.UrlCacheService;
import com.example.shortener.config.ShortenerProperties;
import com.example.shortener.domain.ShortUrlEntity;
import com.example.shortener.domain.UrlStatus;
import com.example.shortener.exception.GoneException;
import com.example.shortener.exception.NotFoundException;
import com.example.shortener.repository.IdempotencyRepository;
import com.example.shortener.repository.ShortUrlRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlServiceResolveTest {
    private ShortUrlRepository repo;
    private IdempotencyRepository idem;
    private UrlCacheService redis;
    private ShortCodeGenerator generator;
    private UrlService service;
    private com.github.benmanes.caffeine.cache.Cache<String,Object> l1;
    private final Instant now = Instant.parse("2026-08-17T19:00:00Z");

    @BeforeEach
    void setup() {
        repo = mock(ShortUrlRepository.class);
        idem = mock(IdempotencyRepository.class);
        redis = mock(UrlCacheService.class);
        generator = mock(ShortCodeGenerator.class);
        var p = new ShortenerProperties("https://sho.rt", 8, 5, Duration.ofSeconds(30), Duration.ofMinutes(30), Duration.ofSeconds(20), Duration.ofDays(1825), 60, java.util.Set.of("api"));
        l1 = Caffeine.newBuilder().build();
        service = new UrlService(new UrlNormalizer(), generator, new ExpirationPolicy(Clock.fixed(now, ZoneOffset.UTC), p), repo, idem, p, Clock.fixed(now, ZoneOffset.UTC), redis, l1);
    }

    @Test
    void resolveReturnsResolvedUrlWhenRedisHasActive() {
        when(redis.get("CODE1")).thenReturn(Optional.of(new CachedUrl("https://example.com/", null, "ACTIVE")));
        var r = service.resolve("CODE1");
        assertEquals("CODE1", r.shortCode());
        assertEquals("https://example.com/", r.destination());
    }

    @Test
    void resolveThrowsNotFoundWhenRedisHasNegative() {
        when(redis.get("MISSING")).thenReturn(Optional.of(new CachedUrl("", null, "NOT_FOUND")));
        assertThrows(NotFoundException.class, () -> service.resolve("MISSING"));
    }

    @Test
    void resolveThrowsGoneWhenRedisEntryExpired() {
        when(redis.get("OLD")).thenReturn(Optional.of(new CachedUrl("https://old.example/", Instant.parse("2026-08-17T18:59:00Z"), "ACTIVE")));
        assertThrows(GoneException.class, () -> service.resolve("OLD"));
    }

    @Test
    void resolveFallsBackToDbAndPopulatesRedis() {
        when(redis.get("DB1")).thenReturn(Optional.empty());
        ShortUrlEntity e = ShortUrlEntity.create("DB1", "https://db.example/", "https://db.example/", "fp", now, null);
        when(repo.findByShortCode("DB1")).thenReturn(Optional.of(e));
        var r = service.resolve("DB1");
        assertEquals("https://db.example/", r.destination());
        verify(redis, times(1)).put(eq("DB1"), any(CachedUrl.class));
    }

    @Test
    void disableEvictsCachesAndMarksDeleted() {
        ShortUrlEntity e = ShortUrlEntity.create("DEL1", "https://del.example/", "https://del.example/", "fp2", now, null);
        when(repo.findByShortCode("DEL1")).thenReturn(Optional.of(e));
        // Put into local L1
        l1.put("DEL1", new CachedUrl(e.getOriginalUrl(), e.getExpiresAt(), e.getStatus().name()));
        // Now disable
        service.disable("DEL1");
        // Local cache invalidated
        assertNull(l1.getIfPresent("DEL1"));
        // Redis evicted
        verify(redis, times(1)).evict("DEL1");
    }
}
