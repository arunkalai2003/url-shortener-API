# Required scenarios

## Greenfield
Build URL create/redirect from zero. Decompose into API contract, canonicalization, short-code generation, persistence, cache, security validation, tests, and observability. Validate with DB constraints and API tests.

## Brownfield
Enhance working shortener with asynchronous analytics and hot-link protection. Impacted flow: redirect -> cache -> event publisher -> Kafka -> analytics consumer/store. Preserve redirect availability when analytics fails. Add idempotent event processing and salted event keys.

## Ambiguous requirement
"Same URL should be shortened and analytics should support campaigns." Decision: same canonical URL returns the same code regardless of campaign. Consequence: campaign-specific click attribution cannot be inferred from the short code alone. Campaign creation metadata may be recorded, but accurate click attribution requires a separate signal (distinct short link, request token, or referrer convention). This trade-off is deliberately documented rather than silently fabricated.
