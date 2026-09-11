# Ambiguous Scenario — “URLs Should Expire”

## Ambiguous statement
“URLs should expire.”

## Questions that must be answered before implementation
- Is expiration mandatory or optional?
- Is there a system default?
- Is `expiresAt` supplied as absolute UTC or TTL duration?
- What is the maximum allowed lifetime?
- Is inactivity equivalent to expiry or only a retention signal?
- What response should an expired link return?
- Can an expired short code ever be reused?
- Should cached redirects remain available when the database is unavailable near expiry time?

## Approved baseline for this repository
- Expiration is an optional absolute UTC timestamp.
- Expired URLs return `410 Gone`.
- Expired short codes are never intentionally reassigned.
- Inactivity/retention is separate from explicit expiry.

## Acceptance criteria derived from the decision
- Missing `expiresAt` creates a non-expiring mapping under current policy.
- Past expiration is rejected.
- Future expiration is persisted in UTC.
- Cache TTL cannot outlive explicit business expiration.
- Resolution after expiry returns `410` even when a stale cache entry exists.
