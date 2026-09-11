# API contract

## POST /api/v1/urls

Headers:
- `Content-Type: application/json`
- optional `Idempotency-Key` (max operational recommendation: 128 chars)

Body:
```json
{
  "url": "https://Example.com:443/product?id=100&utm_source=email",
  "customAlias": null,
  "expiresAt": "2026-12-31T23:59:59Z",
  "campaign": "SUMMER"
}
```

Selected business behavior:
- canonical redirect target becomes `https://example.com/product`;
- query and fragment are removed;
- same canonical URL returns same short code regardless of campaign;
- first canonical creation owns the expiry; later canonical creates return the existing mapping rather than mutating it.

Responses:
- `201 Created` success (including canonical existing result for this prototype API contract)
- `400` validation error
- `409` custom alias conflict or Idempotency-Key misuse
- `429` create rate limit
- `503` inability to persist/allocate mapping

## GET /{shortCode}
- `302 Found` active mapping
- `404 Not Found` unknown code
- `410 Gone` expired/disabled/deleted code

## GET /api/v1/urls/{shortCode}/analytics
Eventually consistent URL-level counters and country/region breakdown.

## DELETE /api/v1/urls/{shortCode}
Soft-disable/tombstone behavior. The short code is not reused.
