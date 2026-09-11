# Architecture

## Request path

```text
Client
  -> CDN / Edge (production)
  -> API Gateway / WAF / rate limiting (production)
  -> stateless Spring Boot pods
       -> Caffeine L1
       -> Redis L2 (circuit breaker)
       -> PostgreSQL source of truth
       -> Kafka analytics events
            -> idempotent analytics consumer
            -> analytics summary + geo aggregates
```

## Hot URL strategy

Database partitioning spreads **different URLs** but does not solve one viral key. The hot-key design is hierarchical:

1. CDN/edge absorbs global repeated redirects.
2. Caffeine L1 absorbs repeated requests per pod and prevents one Redis key from becoming the only hot dependency.
3. Redis L2 protects PostgreSQL across pods.
4. Caffeine atomic `get(key, loader)` provides per-pod request coalescing after expiry/miss.
5. Negative Redis caching protects PostgreSQL from repeated invalid-code enumeration.
6. Analytics uses `shortCode:salt` Kafka keys so one viral URL can use multiple Kafka partitions because click-counter ordering is unnecessary.

Future datastore partitioning should hash the random short code for even aggregate distribution. It is not the single-hot-key solution.

## Consistency boundaries

Strong correctness:
- short-code uniqueness
- canonical normalized-URL uniqueness
- custom-alias uniqueness
- idempotency key semantics
- status/expiration checks

Eventual consistency:
- access count
- last-accessed timestamp
- geo analytics

## Failure behavior

- Redis down: circuit breaker opens; read falls through to PostgreSQL.
- PostgreSQL down during create: create fails; Redis is never treated as durable storage.
- PostgreSQL down during redirect with L1/Redis hit: cached active URL may continue until bounded TTL; documented availability/consistency trade-off.
- Kafka/analytics down: redirect succeeds; analytics delivery is decoupled from the primary path.
