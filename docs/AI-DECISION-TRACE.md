# AI decision trace

This repository intentionally demonstrates engineer-led AI use rather than autonomous code generation.

| Decision | AI-proposed baseline | Engineer decision | Rationale |
|---|---|---|---|
| Same URL behavior | multiple links or idempotency-only | canonical link across campaigns | user/business requirement |
| Distributed synchronization | JVM locks possible in simple sample | DB uniqueness, no JVM lock for correctness | multiple pods do not share memory |
| URL normalization | preserve query by default | remove all query params | explicit business decision; risk documented |
| Code generation | Base62 random | human-friendly Base58-style SecureRandom | unpredictable and readable |
| Expiry | configurable default | optional explicit UTC timestamp | avoid silently breaking old links |
| Hot URL | Redis | CDN + L1 + Redis + DB | Redis alone can become a hot key |
| Analytics | synchronous counter | async Kafka + salted hot-key fanout | protect redirect latency and partition hotspots |
| IP analytics | persist IP | do not persist raw IP | privacy minimization |
| Circuit breaker | apply broadly | Redis/remote dependency only | circuit breakers do not replace DB correctness |
