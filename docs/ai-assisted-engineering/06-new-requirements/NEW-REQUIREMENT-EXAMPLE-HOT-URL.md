# New Requirement Example — Protect Against Hot URLs

## Requirement
A single short URL may suddenly receive extremely high traffic without overloading one database partition or one analytics partition.

## Engineer decisions
- Database sharding distributes **different keys** but does not solve one hot key.
- Redirect read amplification is handled through layered caching: CDN/edge (production), pod-local Caffeine, Redis, then PostgreSQL.
- PostgreSQL remains authoritative.
- Local request coalescing reduces cache stampedes inside a pod.
- Negative caching briefly protects the DB from repeated nonexistent-code probes.
- Analytics does not synchronously update the mapping row per redirect.
- Analytics partitioning may salt a hot URL across partitions because strict ordering is not required for aggregate click metrics.

## Acceptance criteria
- Repeated L1 hits do not query Redis or DB.
- Redis hits do not query DB.
- Concurrent L1 misses for one key coalesce to a bounded number of downstream loads.
- Redis outage has a controlled DB fallback path.
- Analytics events for a hot URL can distribute across configured salt buckets.
- Final aggregate remains correct/idempotent despite duplicate event delivery.
