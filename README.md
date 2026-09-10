# AI-Assisted URL Shortener — Senior Software Engineer Assignment

Production-oriented Java 21 / Spring Boot prototype designed to demonstrate engineer-led AI-assisted execution, explicit trade-offs, concurrency correctness, scalability, reliability, security, testability, and traceability.

## Key engineering decisions

- **Canonical URL:** the same normalized URL maps to one short code, regardless of campaign.
- **Normalization:** lowercase scheme/host, remove default port, normalize path, **remove all query parameters and fragments** (business decision; see ADR-001).
- **Short codes:** 8-character cryptographically strong random Base58-style alphabet, no sequential IDs, DB uniqueness + bounded collision retry.
- **Concurrency:** database unique constraints are the cross-pod correctness boundary; no JVM `synchronized` for distributed correctness.
- **Idempotency:** `Idempotency-Key` returns the original result for the same request and `409` for key reuse with a different request.
- **Expiry:** optional absolute UTC `expiresAt`; no implicit default expiry. Expired links return `410 Gone`. Codes are never intentionally reused.
- **Caching/hot links:** pod-local Caffeine L1 + Redis L2 + PostgreSQL source of truth. Per-key Caffeine load coalesces local cache misses. CDN/edge caching is the production front layer documented in architecture.
- **Analytics:** Kafka asynchronous events, salted Kafka keys for hot URLs, idempotent event processing, URL-level exact counters and country/region aggregates. Raw IP is not stored.
- **Security:** http/https only, strict input size/alias validation, unpredictable identifiers, Redis-backed distributed create rate limit, security headers, no server-side target fetching.
- **Resilience:** Redis circuit breakers with DB fallback for resolution; analytics never blocks redirect; PostgreSQL remains authoritative for writes.

## Run

```bash
docker compose up --build
```

Create:

```bash
curl -i -X POST http://localhost:8080/api/v1/urls \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-123' \
  -d '{"url":"https://Example.com:443/product?id=100&utm_source=email","expiresAt":"2026-12-31T23:59:59Z","campaign":"SUMMER"}'
```

Redirect:

```bash
curl -i http://localhost:8080/<shortCode>
```

Analytics:

```bash
curl http://localhost:8080/api/v1/urls/<shortCode>/analytics
```

Delete/disable:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/urls/<shortCode>
```

## Tests

```bash
mvn test
```

See `docs/` for architecture, risk register, AI decision trace, scenarios, and trade-offs.

## AI-assisted engineering playbook

The reusable workflow for future AI-assisted changes is under [`docs/ai-assisted-engineering/`](docs/ai-assisted-engineering/README.md). It includes Greenfield, Brownfield, Bluefield, Ambiguous/New Requirement, test-first, AI execution traceability, quality-gate, and engineer sign-off templates.

For every new requirement, start with:
1. `00-intake/` / `06-new-requirements/`
2. `01-engineer-decisions/DECISION-GATES.md`
3. `07-test-first/CHANGE-TEST-TEMPLATE.md`
4. `08-ai-execution/PROMPT-CONTRACT-TEMPLATE.md`
5. `09-quality-gates/QUALITY-GATES.md`
6. `10-review-signoff/ENGINEER-SIGNOFF.md`
