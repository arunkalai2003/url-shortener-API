# ADR-001 — Canonical URL normalization

## Decision

Canonicalization lowercases scheme/host, removes default port, normalizes the path, and removes **all query parameters and fragments**. Same canonical URL returns the same short code regardless of campaign.

## Consequence / explicit risk

Removing all query parameters can change destination semantics (`/product?id=100` and `/product?id=200` collapse to the same canonical target). This was selected as a business rule for this assignment and is intentionally visible rather than hidden in code.

## Production recommendation

For a general public URL shortener, preserve functional query parameters and strip only explicitly configured tracking parameters such as `utm_*` after product/security review.
