CREATE TABLE short_url (
    id UUID PRIMARY KEY,
    short_code VARCHAR(32) NOT NULL,
    original_url VARCHAR(4096) NOT NULL,
    normalized_url VARCHAR(4096) NOT NULL,
    url_fingerprint CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NULL,
    status VARCHAR(16) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_short_url_code UNIQUE(short_code),
    CONSTRAINT uk_short_url_fingerprint UNIQUE(url_fingerprint)
);
CREATE INDEX idx_short_url_expires_at ON short_url(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_short_url_created_at ON short_url(created_at);

CREATE TABLE idempotency_record (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash CHAR(64) NOT NULL,
    short_code VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE analytics_summary (
    short_code VARCHAR(32) PRIMARY KEY,
    access_count BIGINT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMPTZ NULL
);

CREATE TABLE analytics_geo (
    short_code VARCHAR(32) NOT NULL,
    country VARCHAR(8) NOT NULL,
    region VARCHAR(64) NOT NULL,
    access_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY(short_code, country, region)
);

CREATE TABLE processed_analytics_event (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);
