-- ============================================================
-- P0.E.1 — Idempotency-Key Header support
-- P0.H.2 — SSE Event Replay (Last-Event-ID)
-- ============================================================

-- Idempotency keys: deduplicate POST requests using client-provided key
CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key   VARCHAR(255)    PRIMARY KEY,
    tenant_id         VARCHAR(100)    NOT NULL,
    endpoint          VARCHAR(500)    NOT NULL,
    response_status   INTEGER         NOT NULL,
    response_body     TEXT,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    expires_at        TIMESTAMPTZ     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_expires_at
    ON idempotency_keys (expires_at);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_tenant
    ON idempotency_keys (tenant_id, idempotency_key);

-- SSE Events: store events for Last-Event-ID replay
CREATE TABLE IF NOT EXISTS sse_events (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        VARCHAR(36)     NOT NULL UNIQUE,
    user_id         VARCHAR(255)    NOT NULL,
    tenant_id       VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ     NOT NULL DEFAULT (NOW() + INTERVAL '24 hours')
);

CREATE INDEX IF NOT EXISTS idx_sse_events_user_tenant
    ON sse_events (user_id, tenant_id, id);

CREATE INDEX IF NOT EXISTS idx_sse_events_expires_at
    ON sse_events (expires_at);
