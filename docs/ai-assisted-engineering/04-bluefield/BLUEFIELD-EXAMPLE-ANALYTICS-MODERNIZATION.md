# Bluefield Scenario — Modernize Analytics Without Rewriting Redirect Core

## Situation
The redirect service is already production-critical. Synchronous analytics writes are causing contention, but the mapping/redirect path is stable and should not be rewritten merely to adopt a new design.

## Bluefield decision
Keep the existing mapping and redirect contracts, but introduce an asynchronous analytics boundary using `AnalyticsEventPublisher` and Kafka-compatible event semantics.

## Preserved contracts
- Redirect API behavior and status codes.
- PostgreSQL remains mapping source of truth.
- Existing short codes remain valid.
- Existing analytics API response shape remains backward compatible unless versioned.

## New components
- Event publisher abstraction.
- Analytics event schema with stable `eventId`.
- Consumer with idempotent processing.
- Salted partition key for hot URLs where strict per-URL ordering is not required.
- DLQ/retry policy.

## Trade-off
Analytics becomes eventually consistent. This is accepted because redirect latency/availability has higher priority than immediate dashboard freshness.

## Validation
Run old redirect regression suite plus new event idempotency, Kafka outage, duplicate delivery, hot-key distribution, and eventual aggregation tests.
