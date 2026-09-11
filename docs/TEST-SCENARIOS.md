# Test Scenarios

This document enumerates the automated test scenarios implemented in the repository and the intent behind them.

## Unit / Service tests (src/test/java)

- UrlNormalizerTest
  - Ensures canonicalization rules: default port removal, query/fragment handling, path root normalization, and rejecting unsafe schemes.

- HashingTest
  - Verifies SHA-256 fingerprint stability and fixed width for canonical identity.

- ShortCodeGeneratorTest
  - Verifies generated short codes are human-friendly and unpredictable-looking (entropy/format checks).

- ExpirationPolicyTest
  - Validates expiry acceptance/rejection rules (null => no expiry, rejects past timestamps, accepts future timestamps).

- UrlServiceTest
  - Covers creation flows including:
    - Existing canonical mapping detection (returns existing short code)
    - Random short-code collision recovery / retry logic
    - Idempotency-key conflict detection and 409 behavior

- UrlServiceResolveTest (new)
  - Covers resolution and cache behavior including:
    - Successful resolve when Redis returns an ACTIVE cached entry
    - 404/NotFound when Redis negative cache is present
    - 410/Gone when a cached entry is expired
    - DB fallback resolution when Redis has no entry and subsequent cache population
    - disable() invalidates local L1 and evicts Redis entry

## Missing / out-of-scope tests

These are intentionally left for integration/acceptance levels (can be added later):

- Full end-to-end integration with Docker Compose services (Postgres, Redis, Kafka) using Testcontainers.
- Kafka analytics delivery correctness and idempotent consumers.
- Full concurrency stress tests that exercise real DB constraints under load.
- Network-level fault injection / chaos testing.

## Running tests

Run the unit test suite locally:

```bash
mvn test
```

For resolved dependency versions prior to running integration suites, use:

```bash
mvn help:effective-pom
mvn dependency:tree
```

## Notes

- Most service-level behaviors are exercised using mocks to keep the unit tests fast and hermetic.
- Integration tests (with Testcontainers) are present as a future step and can be added to validate the full production stack behavior.
