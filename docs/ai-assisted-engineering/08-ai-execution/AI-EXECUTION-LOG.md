# AI Execution Log — URL Shortener

This log demonstrates traceability without pretending that raw AI output is authoritative.

| Task | AI role | Engineer review | Disposition | Validation |
|---|---|---|---|---|
| Initial Spring Boot structure | generate scaffolding | checked package boundaries/dependencies | Edited | build/tests |
| Short-code algorithm | compare sequence/hash/random approaches | rejected predictable sequential IDs | Edited | generator + collision tests |
| Same-URL concurrency | propose synchronization approaches | rejected JVM `synchronized` as cross-pod correctness mechanism | Edited | concurrent DB integration test |
| Persistence conflict handling | propose duplicate-key catch/retry | refined to atomic `INSERT ... ON CONFLICT DO NOTHING` to avoid poisoned transaction behavior | Edited | concurrency/integration tests |
| URL normalization | propose conservative normalization | engineer/business explicitly chose stripping all query params | Edited | normalization tests + ADR risk |
| Hot URL strategy | propose Redis cache | expanded to CDN/L1/Redis/DB and request coalescing | Edited | cache/hot-key tests |
| Analytics | synchronous counter baseline | rejected per-request primary-row writes | Rejected | async analytics tests |
| Analytics partitioning | key Kafka by short code | refined with salt for extreme hot keys where ordering is unnecessary | Edited | distribution + aggregation tests |
| IP analytics | persist IP for location | rejected persistent raw IP | Rejected | privacy test/review |
| Circuit breaker | apply broadly | constrained to appropriate remote/cache dependencies | Edited | failure-injection tests |

## Engineer ownership statement
No generated implementation is accepted because it “looks correct.” Correctness is established through review of invariants, data constraints, failure behavior, automated tests, and explicit production limitations.
