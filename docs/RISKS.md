# Risk register

| Risk | Impact | Control |
|---|---|---|
| Two pods create same canonical URL | duplicate mapping | SHA-256 fingerprint + DB unique constraint; loser returns winner |
| Random code collision | wrong/failed mapping | SecureRandom + DB unique constraint + bounded retry |
| Idempotency key reused with different body | semantic corruption | request hash + 409 |
| Viral/hot URL | cache/DB hot key | CDN + Caffeine + Redis + request coalescing |
| Viral analytics key | one Kafka partition hot | salted event key + aggregation |
| Redis outage | increased DB load | circuit breaker + DB fallback + gateway/load shedding recommendation |
| Cache stale after disable | unsafe redirect | explicit eviction + short L1 TTL + bounded Redis TTL; production invalidation channel |
| Analytics outage | delayed/lost analytics | async Kafka; redirect independent; production DLQ/monitoring |
| Duplicate Kafka delivery | overcount | unique event ID / processed-event table |
| URL enumeration | information exposure | unpredictable codes + rate limit |
| Dangerous URL scheme | XSS/unsafe redirects | only HTTP/HTTPS |
| Sensitive IP collection | privacy risk | IP only transient for rate limiting; hash key; country/region headers stored, raw IP not persisted |
| Query stripping changes meaning | incorrect redirect | explicit ADR/business decision |
| AI-generated defect | production defect | engineer-owned review, unit/integration/concurrency/security gates |
