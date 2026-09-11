# Brownfield Scenario — Add Custom Alias to Existing URL Shortener

## Existing behavior
The service generates random short codes and supports canonical deduplication.

## New requirement
Allow eligible clients to request a readable custom alias such as `/summer-sale`.

## Impact analysis before coding
- API request DTO gains optional `customAlias`.
- Validation rules and reserved-name policy are required.
- Existing `short_code` uniqueness constraint must also protect aliases.
- Collision behavior differs from random-code collision: a custom alias conflict returns `409`; it must not silently regenerate.
- Cache keys do not need schema change because alias occupies the same short-code namespace.
- Rate-limit/abuse controls may need stricter policy for alias creation.
- Existing clients with no alias must retain unchanged behavior.

## New acceptance criteria
- Valid unused alias creates successfully.
- Invalid/reserved alias is rejected before persistence.
- Two concurrent requests for the same alias produce exactly one winner.
- Alias conflict returns `409`, not a different generated code.
- Existing random-code flow still passes regression tests.

## Brownfield regression requirement
No change is complete until existing create, redirect, expiry, cache, analytics, and concurrency tests still pass.
