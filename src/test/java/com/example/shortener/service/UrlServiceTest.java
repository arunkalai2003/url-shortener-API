package com.example.shortener.service;

import com.example.shortener.cache.UrlCacheService;
import com.example.shortener.config.ShortenerProperties;
import com.example.shortener.domain.*;
import com.example.shortener.dto.CreateShortUrlRequest;
import com.example.shortener.exception.ConflictException;
import com.example.shortener.repository.*;
import com.example.shortener.util.HashingUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UrlServiceTest {
    private ShortUrlRepository repo;
    private IdempotencyRepository idem;
    private UrlCacheService redis;
    private ShortCodeGenerator generator;
    private UrlService service;
    private final Instant now=Instant.parse("2026-08-17T19:00:00Z");

    @BeforeEach void setup(){
        repo=mock(ShortUrlRepository.class); idem=mock(IdempotencyRepository.class); redis=mock(UrlCacheService.class); generator=mock(ShortCodeGenerator.class);
        var p=new ShortenerProperties("https://sho.rt",8,5,Duration.ofSeconds(30),Duration.ofMinutes(30),Duration.ofSeconds(20),Duration.ofDays(1825),60,Set.of("api"));
        service=new UrlService(new UrlNormalizer(),generator,new ExpirationPolicy(Clock.fixed(now,ZoneOffset.UTC),p),repo,idem,p,Clock.fixed(now,ZoneOffset.UTC),redis,Caffeine.newBuilder().build());
    }

    @Test void sameCanonicalUrlReturnsExistingMappingWithoutGeneratingNewCode(){
        ShortUrlEntity existing=ShortUrlEntity.create("K7mP4xQa","https://example.com/product","https://example.com/product", HashingUtil.sha256("https://example.com/product"),now,null);
        when(repo.findByUrlFingerprint(existing.getUrlFingerprint())).thenReturn(Optional.of(existing));
        var r=service.create(new CreateShortUrlRequest("HTTPS://Example.com:443/product?id=100",null,null,"SUMMER"),null);
        assertEquals("K7mP4xQa",r.shortCode()); verifyNoInteractions(generator);
    }

    @Test void randomCodeCollisionRetriesWhenNoFingerprintWinnerExists(){
        when(repo.findByUrlFingerprint(anyString())).thenReturn(Optional.empty());
        when(generator.generate()).thenReturn("AAAA2222","BBBB3333");
        when(repo.insertIfAbsent(any(),eq("AAAA2222"),anyString(),anyString(),anyString(),any(),isNull())).thenReturn(0);
        when(repo.insertIfAbsent(any(),eq("BBBB3333"),anyString(),anyString(),anyString(),any(),isNull())).thenReturn(1);
        ShortUrlEntity winner=ShortUrlEntity.create("BBBB3333","https://example.com/","https://example.com/", HashingUtil.sha256("https://example.com/"),now,null);
        when(repo.findByShortCode("BBBB3333")).thenReturn(Optional.of(winner));
        var r=service.create(new CreateShortUrlRequest("https://example.com",null,null,null),null);
        assertEquals("BBBB3333",r.shortCode()); verify(generator,times(2)).generate();
    }

    @Test void sameIdempotencyKeyWithDifferentRequestReturns409(){
        when(idem.findById("key-1")).thenReturn(Optional.of(new IdempotencyRecord("key-1","different-hash","OLD12345",now)));
        assertThrows(ConflictException.class,()->service.create(new CreateShortUrlRequest("https://example.com",null,null,null),"key-1"));
    }
}
