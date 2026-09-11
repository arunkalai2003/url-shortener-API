# Concurrency and idempotency

## Same URL, same time, different pods

Both requests compute the same SHA-256 fingerprint of the canonical URL. PostgreSQL has `UNIQUE(url_fingerprint)`. Creation uses `INSERT ... ON CONFLICT DO NOTHING`, not an exception-driven retry inside a failed transaction.

- winner inserts the row;
- loser sees `0 rows inserted` and loads the winner by fingerprint;
- both return the same short code;
- no JVM `synchronized` or Redis lock is required for distributed correctness.

## Different URLs, random code collision

`UNIQUE(short_code)` rejects the candidate through the same `ON CONFLICT DO NOTHING` path. If no canonical winner exists for the request fingerprint, the service knows this is a code collision and generates another code, bounded by configuration.

## Idempotency-Key

`idempotency_record.idempotency_key` is the primary key. Insert is also `ON CONFLICT DO NOTHING`.

- same key + same request hash -> return original mapping;
- same key + different request hash -> `409 Conflict`.

## Why not `synchronized`?

A Java monitor only coordinates threads inside one JVM. Multiple Kubernetes pods have independent heaps and locks. PostgreSQL uniqueness is the shared atomic boundary.
