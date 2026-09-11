# Greenfield Scenario — Initial URL Shortener

## Overview
Design and implement a small, production-ready URL shortener service that can create canonical short URLs for arbitrary HTTP/HTTPS targets and resolve them with high correctness, safety, and observability. The scenario focuses on a single microservice with PostgreSQL as the authoritative store and Redis as a cache/fast lookup layer.

## Goals
- Provide canonical mapping: identical canonical target URLs map to a single short-code.
- Safe, collision-resistant, non-sequential short-codes suitable for human reading.
- Resolve short-codes with minimal latency and graceful degradation when caches fail.
- Support expiry, soft-deletion, and analytics without impacting redirect correctness.
- Be design-ready for horizontal scale and multi-region read replicas.

## Non-goals
- Full-featured user accounts or multi-tenant billing. (Keep single-tenant MVP.)
- Advanced anti-abuse (beyond rate-limiting and simple heuristics).

## Stakeholders
- Product: short links for marketing and sharing
- Platform/Infra: ops and scaling
- Security/Privacy: abuse detection and content policies
- Analytics: click/geo/time insights

## Key Requirements (Functional)
- POST /shorten -> create short-code for provided URL (accepts optional ttl, custom code)
- GET /r/{code} -> 302/307 redirect to target or 404/410
- Idempotent canonical creation: concurrent creates for same canonical target yield one mapping
- Analytics published asynchronously (click events, user-agent, IP geolocation optional)

## Non-functional Requirements
- P95 redirect latency < 50ms (cache hit), P95 < 200ms (cache miss)
- Availability target: 99.95% for resolution
- Durable mappings (Postgres is authoritative)
- Reasonable cost for small-to-medium traffic (10k–100k daily redirects)

## API Sketch
- POST /api/v1/shorten
  - body: { "url": "https://example.com/path?utm=1", "expiry": "2026-12-31T23:59:59Z"?, "custom_code": "promo1"? }
  - responses: 201 { code, short_url, expires_at } | 400 | 409 (custom code conflict)
- GET /r/{code}
  - behavior: 302 (or 307) redirect to target; 404 if missing; 410 if expired/deleted
- GET /api/v1/metadata/{code}
  - returns mapping metadata (no analytics heavy load)

## Data model (high-level)
Postgres table: url_mapping (
- id BIGSERIAL PRIMARY KEY,
- canonical_target TEXT NOT NULL UNIQUE (hash indexed),
- code VARCHAR(16) NOT NULL UNIQUE,
- created_at TIMESTAMP, expires_at TIMESTAMP NULLABLE,
- metadata JSONB NULL,
- deleted BOOLEAN DEFAULT FALSE
)

- Store a canonical fingerprint (SHA256 or normalized URL hash) as an indexed column to enforce uniqueness.
- Redis: code -> serialized mapping (with TTL aligned to expiry). Cache is advisory only.

## URL Canonicalization
- Normalize by: scheme lowercasing, default port removal, percent-encode normalization, remove tracking query parameters (configurable), trailing slash policy defined in spec.
- Produce canonical string, then compute fingerprint (e.g., SHA256) for uniqueness checks.
- Document canonicalization rules clearly; engineers must approve them before rollout.

## Short-code Generation
- Use cryptographically-random bytes then encode using a human-friendly alphabet (e.g., base62 without ambiguous characters) to produce fixed-length codes (configurable length, minimum 6 chars).
- For custom codes: validate pattern, enforce uniqueness with DB constraint and handle conflict (return 409).
- Avoid sequential IDs to prevent enumeration.

## Concurrency & Correctness
- Creation flow for canonical mapping:
  1. Compute canonical fingerprint.
  2. Attempt INSERT into Postgres ON CONFLICT (fingerprint) DO NOTHING returning id; if inserted, generate code and update row atomically (use a transaction or two-phase upsert).
  3. If conflict (existing mapping), return existing mapping (idempotency).
- Short-code assignment must be unique; create with a DB-unique code field. If code collision occurs, retry generation.
- Use short transactions and optimistic retries. Prefer SERIALIZABLE or explicit row-level locking sparingly — benchmark.

## Cache Layer
- Redis stores code -> mapping for fast resolution. On miss, fall back to Postgres and populate Redis.
- Cache is non-authoritative; Postgres is source-of-truth.
- Handle Redis errors by falling back gracefully and logging metrics.

## Analytics & Asynchronous Work
- Redirects should enqueue a light event (Kafka/SQS) or write to a local append-only buffer processed asynchronously.
- Analytics failures (queue full/down) must not block the redirect.

## Security, Abuse & Rate Limits
- Rate-limit creation endpoints per IP/API key.
- Validation: only accept http/https schemes; limit target length and total payload size.
- Sanitize metadata; avoid SSRF during creation (do not ping target URL during creation as default).
- Provide an ops endpoint to mark codes as blocked/unsafe.

## Observability & Metrics
- Metrics: creations/sec, redirects/sec, cache hit rate, DB errors, code collision retries, latency percentiles.
- Tracing: attach trace-id to operations; propagate to async analytics events.
- Logs: structured logs with enough context to debug mapping and creation flows.
- Health checks: readiness/liveness that validate DB and (optionally) Redis connectivity.

## Testing Strategy
- Unit tests: canonicalization rules, code generation edge cases
- Integration tests: end-to-end create -> resolve, concurrent canonical creation (race tests)
- Load tests: simulate high read and write patterns to validate caching and DB contention
- Chaos testing: Redis outage, DB failover, message queue backpressure

## Rollout & Migration
- Start with single-region service behind load balancer.
- Run in blue-green or canary deployment for production.
- Migration: if importing existing links, compute canonical fingerprint and insert with upsert logic; handle duplicates by preferring newest or oldest per policy.

## AI-assisted engineering guidance
- Use AI to scaffold handlers, propose canonicalization implementations, and produce test cases — but require human review for correctness of canonicalization, security, and concurrency semantics.
- Use generated code as a basis for pair-programming and accelerate boilerplate tasks (API stubs, DTOs, simple tests).

## Acceptance criteria (concrete)
- Creating a short URL for a valid HTTP/HTTPS link returns a code and short_url.
- Concurrent requests to shorten the same canonical URL produce one logical mapping (idempotent response).
- Unknown codes return HTTP 404.
- Expired mappings return HTTP 410.
- Custom code conflicts return 409 and do not corrupt data.
- Redis failures do not lose mappings; read path falls back to Postgres and repopulates cache.
- Analytics issues never prevent successful redirects.

## Deliverables / Required evidence
- Implementation repo with automated tests and CI passing.
- Test matrix and quality gates: see `07-test-first/URL-SHORTENER-TEST-MATRIX.md` and `09-quality-gates/QUALITY-GATES.md`.
- Load test report and metrics dashboard screenshots.

---

Document authoring note: canonicalization rules are a product decision and must be captured in a separate spec file before wide rollout.