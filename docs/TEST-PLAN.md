# Test plan

## Unit
- URL normalization removes default ports, queries and fragments.
- Reject unsupported schemes/malformed URLs.
- Base58-style codes are correct length/alphabet.
- Expiry accepts null/future and rejects past/excessive values.
- Alias validation and reserved names.

## Concurrency/integration to run against PostgreSQL
- 100 concurrent requests for the same URL -> exactly one row, same short code to all callers.
- Forced short-code collision for different URLs -> second request regenerates.
- 50 concurrent requests for same custom alias -> one 201, others 409 unless same canonical URL winner is returned per business rule.
- Same Idempotency-Key + same request -> same response.
- Same Idempotency-Key + different request -> 409.
- Pod/process failure after DB commit before HTTP response -> retry returns canonical row.

## Cache/resilience
- L1 hit avoids Redis/DB.
- Redis hit avoids DB.
- Redis outage falls back to DB.
- Missing code negative cached briefly.
- Disable invalidates L1 and Redis.
- Expired cached entry returns 410.

## Analytics
- Redirect publishes event asynchronously.
- Same event delivered twice counts once.
- Concurrent analytics events do not lose increments.
- `last_accessed_at` remains greatest timestamp.
- country/region aggregates update.
- Kafka outage does not make redirect fail.

## Security
- `javascript:`, `file:`, `data:` rejected.
- URL length > 4096 rejected.
- invalid/reserved aliases rejected.
- distributed create limit returns 429.
- raw IP never persisted.

## Performance acceptance targets (prototype targets, not universal SLA)
- cached redirect p95 < 50 ms under local load.
- create p95 < 250 ms under moderate local load.
- zero duplicate short codes under concurrency test.
