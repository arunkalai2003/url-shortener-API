# Trade-offs and limitations

1. **All query parameters are removed.** This is the selected business policy, but it can collapse distinct functional URLs. A general-purpose service should normally preserve functional query parameters.
2. **Canonical URL across campaigns.** Campaign-specific click attribution is impossible from the short code alone. A separate attribution signal is required if the business later needs per-campaign clicks.
3. **Optional expiration.** No silent default expiry prevents unexpected broken links; storage lifecycle/archival should be handled separately.
4. **Caffeine L1 improves hot-link resilience but can be stale.** TTL is deliberately short. Production should add an invalidation topic/pub-sub event for immediate disable propagation.
5. **Cached redirect during DB outage favors availability.** A recently disabled URL could remain reachable until bounded cache TTL expires. High-risk domains may choose fail-closed instead.
6. **Rate limiting fails open if Redis is unavailable.** Production should enforce a second independent layer at API Gateway/WAF; application Redis limiting is defense-in-depth.
7. **Analytics is eventually consistent and may be dropped if the local async publisher queue is saturated.** Redirect availability is prioritized. Production can use a local durable outbox if click accounting must be lossless.
8. **Processed-event table provides simple idempotency but grows indefinitely.** Production needs TTL/partitioned retention or a streaming deduplication strategy.
9. **No server-side URL fetching.** This deliberately avoids SSRF surface. Preview/safety scanning should run in a separately hardened component if introduced.
10. **Authentication is not implemented in the prototype.** Public creation is rate-limited. Production enterprise management/analytics APIs should sit behind OIDC/OAuth2 and tenant authorization.
