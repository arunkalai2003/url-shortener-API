# ADR-002 — Short-code generation

Use 8 cryptographically strong random characters from a human-friendly Base58-style alphabet. Sequential IDs are rejected because they are predictable/enumerable. Correctness is enforced by `UNIQUE(short_code)` and bounded retry (5 attempts). `UNIQUE(url_fingerprint)` independently handles concurrent canonical duplicates.
