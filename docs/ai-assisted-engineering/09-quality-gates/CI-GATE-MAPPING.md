# CI Gate Mapping

## Automated in current GitHub workflow
- Java 21 setup
- Maven test execution
- Maven package execution

## Recommended additions for a production repository
- formatter/style check
- SpotBugs/PMD/Checkstyle or organization equivalent
- OWASP dependency scanning / SCA
- secret scanning
- container image scanning
- integration profile with PostgreSQL/Redis/Kafka Testcontainers
- concurrency test suite
- API contract tests
- lightweight load/performance smoke test

## Principle
CI automation is evidence, not the entire quality process. Architectural correctness, business assumptions, threat decisions, and production trade-offs still require engineer review.
