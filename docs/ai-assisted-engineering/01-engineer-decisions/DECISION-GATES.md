# Engineer Decision Gates

Before implementing a material change, answer the relevant questions below.

## Product / business
- What is the logical identity of the resource?
- What behavior is expected for duplicate/retried requests?
- Is behavior tenant/user/campaign specific?
- What should happen on expiry, disable, delete, or restore?
- Which behavior is configurable versus fixed?

## API
- Is the change backward compatible?
- Which status codes and error contracts are expected?
- Is idempotency required?
- Are retry semantics documented?
- Does the change introduce versioning needs?

## Data
- What is the source of truth?
- Which uniqueness constraints enforce correctness?
- What data must be strongly consistent?
- Which data can be eventually consistent?
- What is the retention/deletion policy?
- Is a migration needed and can old/new versions coexist during rollout?

## Concurrency
- Can two pods execute the same operation concurrently?
- What prevents lost updates or duplicates?
- Is JVM synchronization incorrectly being used for distributed correctness?
- Is an atomic DB operation/constraint sufficient?
- Is optimistic/pessimistic locking actually justified?

## Security / privacy
- What inputs are trusted/untrusted?
- Does the system fetch user-supplied URLs or only redirect?
- Could the feature create SSRF, XSS, injection, enumeration, or abuse risk?
- Is any PII collected that can be minimized?
- Are authentication/authorization/rate limits required?

## Reliability
- Which dependencies are on the critical path?
- Should failure be fail-open or fail-closed?
- Which errors are retryable?
- Where are timeouts, retry budgets, jitter, circuit breakers, or fallbacks justified?
- What happens during partial outages?

## Scalability
- What is the read/write ratio?
- Can one resource become a hot key?
- Can one partition become hot because of partition-key selection?
- What can be cached safely?
- What is the cache invalidation strategy?

## Observability
- Which metrics prove the requirement works in production?
- Which logs/traces are needed without leaking sensitive data?
- What alert indicates abnormal behavior?

## AI usage
- What exact task is being delegated to AI?
- What constraints and acceptance criteria are supplied in the prompt?
- What output was accepted/edited/rejected?
- What evidence proves the final implementation is correct?
