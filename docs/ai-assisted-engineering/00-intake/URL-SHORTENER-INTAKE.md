# Requirement Intake — URL Shortener

## Original objective
Build a production-oriented URL shortener with core APIs, analytics, reliability, security, scalability, testing, and explicit engineer-led AI assistance.

## Current business decisions already approved

- Same normalized URL maps to one canonical short URL regardless of campaign.
- An `Idempotency-Key` is supported; same key + different payload returns `409 Conflict`.
- URL normalization lowercases scheme/host, removes default ports, query parameters, and fragments.
- Short codes must be readable, unpredictable, non-sequential, and collision-resistant.
- Expiration is represented using an absolute UTC timestamp when supplied; no implicit expiration is assumed unless policy changes.
- Expired codes are not reused.
- Raw client IP is not persisted for analytics.
- Analytics must not block redirects.

## Explicitly documented risk
Removing **all** query parameters is a business policy, not a universally safe URL-normalization rule. Functional query parameters can change destination semantics. This repository preserves the approved policy and documents the risk in ADR-001.
