-- V20250120_0008__create_outbox_events_table.sql
-- Transactional Outbox Pattern: stores domain events in the same transaction
-- as the business operation, ensuring at-least-once delivery to Redis Streams.

CREATE TABLE IF NOT EXISTS outbox_events (
    id            UUID         NOT NULL DEFAULT uuid_generate_v4(),
    event_type    VARCHAR(255) NOT NULL,
    event_id      VARCHAR(255) NOT NULL,
    tenant_id     VARCHAR(255),
    correlation_id VARCHAR(255),
    payload       JSONB        NOT NULL,
    stream_name   VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at  TIMESTAMPTZ,
    retry_count   INTEGER      NOT NULL DEFAULT 0,
    last_error    TEXT,

    CONSTRAINT pk_outbox_events PRIMARY KEY (id),
    CONSTRAINT ck_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

-- Index for the dispatcher to find pending events efficiently
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_created
    ON outbox_events (status, created_at)
    WHERE status = 'PENDING';

-- Index for deduplication and lookup by event_id
CREATE UNIQUE INDEX IF NOT EXISTS idx_outbox_events_event_id
    ON outbox_events (event_id);

-- Comment
COMMENT ON TABLE outbox_events IS 'Transactional outbox for domain events - ensures at-least-once delivery to message broker';
