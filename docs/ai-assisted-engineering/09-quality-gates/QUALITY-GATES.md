# Quality Gates

A change cannot be marked complete until all applicable required gates pass or an exception is explicitly documented and approved.

## Gate 1 — Requirement / decision completeness
- [ ] Requirement captured verbatim.
- [ ] Ambiguities resolved or explicitly assumed.
- [ ] High-impact decisions have rationale/trade-offs.
- [ ] Acceptance criteria are testable.

## Gate 2 — Build / static quality
- [ ] Project compiles.
- [ ] Formatting/lint/static analysis passes when configured.
- [ ] No new compiler warnings of concern.

## Gate 3 — Automated tests
- [ ] New requirement tests pass.
- [ ] Unit tests pass.
- [ ] Integration tests pass.
- [ ] Regression suite passes.
- [ ] Concurrency tests pass when shared state is changed.

## Gate 4 — Security / privacy
- [ ] Input validation reviewed.
- [ ] Authentication/authorization impact reviewed.
- [ ] Injection/SSRF/XSS/enumeration/abuse impact reviewed as applicable.
- [ ] Sensitive data/logging reviewed.
- [ ] Dependency/secret scanning passes when configured.

## Gate 5 — Reliability / resilience
- [ ] Dependency timeouts are explicit.
- [ ] Retry behavior is bounded and only for retryable failures.
- [ ] Circuit breaker/fallback behavior tested where applicable.
- [ ] Partial outage behavior is documented.

## Gate 6 — Scalability / performance
- [ ] Hot-key/partition behavior considered.
- [ ] Cache correctness/invalidation reviewed.
- [ ] Critical path does not contain unnecessary synchronous work.
- [ ] Performance acceptance target or relative benchmark validated when applicable.

## Gate 7 — Operability
- [ ] Metrics/logs/traces cover new failure modes.
- [ ] Config defaults are safe.
- [ ] Deployment/migration compatibility reviewed.

## Gate 8 — AI traceability
- [ ] AI task contract captured for significant AI-generated work.
- [ ] Material AI suggestions are marked accepted/edited/rejected.
- [ ] High-impact output received explicit human approval.

## Gate 9 — Final sign-off
- [ ] Engineer confirms correctness.
- [ ] Known limitations documented.
- [ ] Rollback/disable strategy documented for high-impact changes.
