# Test-First Matrix — URL Shortener

Tests are defined before implementation changes. A feature is not complete until relevant new tests and all regression tests pass.

| Area | Test | Expected result |
|---|---|---|
| Normalization | default HTTPS port + query + fragment | canonical URL has default port/query/fragment removed per approved policy |
| Validation | `javascript:` / `file:` / `data:` | rejected |
| Creation | valid URL | one persisted mapping + short response |
| Concurrency | 100 simultaneous creates for same URL | exactly one canonical mapping; all callers resolve to same short code |
| Collision | forced same random code for different canonical URLs | second operation regenerates; mappings remain unique |
| Idempotency | same key + same payload | original result returned |
| Idempotency | same key + changed payload | `409 Conflict` |
| Alias | 50 concurrent same custom alias | one winner; conflicts return `409` |
| Expiry | past `expiresAt` | rejected |
| Expiry | redirect after expiry | `410 Gone` |
| Cache | L1 hit | Redis/DB not called |
| Cache | Redis hit | DB not called |
| Cache | Redis unavailable | controlled DB fallback |
| Cache | disable mapping | L1 and Redis invalidated |
| Cache | missing code repeated | short negative-cache protection |
| Hot key | many concurrent same-code misses | local load coalescing prevents one DB call per request |
| Analytics | successful redirect | async event emitted |
| Analytics | duplicate `eventId` | counted once |
| Analytics | older event after newer event | `lastAccessedAt` does not move backward |
| Analytics | Kafka unavailable | redirect still succeeds |
| Analytics | salted hot URL | events can spread across salt buckets |
| Privacy | analytics processing | raw IP is not persisted |
| Security | create flood | distributed rate limit returns `429` according to policy |
| Failure | DB unavailable during create | `503`; no cache-only mapping is created |
| Failure | process dies after commit before response | retry resolves canonical mapping; no duplicate |
| Regression | all existing tests | all pass after every brownfield/bluefield change |
