package com.example.shortener.security;

import com.example.shortener.config.ShortenerProperties;
import com.example.shortener.service.Hashing;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DistributedRateLimiter {

    private final StringRedisTemplate redis;
    private final ShortenerProperties p;

    public DistributedRateLimiter(StringRedisTemplate redis, ShortenerProperties p) {
        this.redis = redis;
        this.p = p;
    }

    @CircuitBreaker(name = "redisRateLimit", fallbackMethod = "allowFallback")
    public boolean allow(String clientIp) {
        String anonymized = Hashing.sha256(clientIp).substring(0, 24);
        String key = "rl:create:" + anonymized;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) redis.expire(key, Duration.ofMinutes(1));
        return count == null || count <= p.createRequestsPerMinute();
    }

    // Availability choice for prototype: fail open if Redis is unavailable; production gateway/WAF should enforce a second layer.
    private boolean allowFallback(String clientIp, Throwable t) {
        return true;
    }
}
