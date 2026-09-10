package com.example.shortener.cache;

import com.example.shortener.config.ShortenerProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Optional;

@Service
public class UrlCacheService {
    private static final String NOT_FOUND = "__NOT_FOUND__";
    private final StringRedisTemplate redis; private final ShortenerProperties p; private final Clock clock; private final ObjectMapper mapper;
    public UrlCacheService(StringRedisTemplate redis, ShortenerProperties p, Clock clock, ObjectMapper mapper){this.redis=redis;this.p=p;this.clock=clock;this.mapper=mapper;}
    private String key(String code){return "url:"+code;}

    @CircuitBreaker(name="redis", fallbackMethod="getFallback")
    public Optional<CachedUrl> get(String code){
        String v=redis.opsForValue().get(key(code));
        if(v==null)return Optional.empty();
        if(NOT_FOUND.equals(v)) return Optional.of(new CachedUrl("", null, "NOT_FOUND"));
        try { return Optional.of(mapper.readValue(v, CachedUrl.class)); }
        catch (JsonProcessingException malformed) { redis.delete(key(code)); return Optional.empty(); }
    }
    private Optional<CachedUrl> getFallback(String code, Throwable t){ return Optional.empty(); }

    @CircuitBreaker(name="redis", fallbackMethod="putFallback")
    public void put(String code, CachedUrl value){
        Duration ttl=p.redisMaxTtl();
        if(value.expiresAt()!=null){ Duration until=Duration.between(clock.instant(), value.expiresAt()); if(until.isNegative()||until.isZero())return; if(until.compareTo(ttl)<0)ttl=until; }
        try { redis.opsForValue().set(key(code), mapper.writeValueAsString(value), ttl); }
        catch (JsonProcessingException impossible) { throw new IllegalStateException(impossible); }
    }
    private void putFallback(String code, CachedUrl value, Throwable t) { }

    @CircuitBreaker(name="redis", fallbackMethod="oneArgFallback")
    public void putNegative(String code){ redis.opsForValue().set(key(code),NOT_FOUND,p.negativeCacheTtl()); }

    @CircuitBreaker(name="redis", fallbackMethod="oneArgFallback")
    public void evict(String code){ redis.delete(key(code)); }

    private void oneArgFallback(String code, Throwable t) { }
}
