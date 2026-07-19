-- V20250720_0010__add_request_status_history_and_jobs_tables.sql
-- Add tables for: request status history (P0.N.2) and operations jobs (P0.I.1)

-- ============================================================
-- Table: request_status_history
-- Immutable audit trail of every status transition on a request
-- ============================================================
CREATE TABLE IF NOT EXISTS request_status_history (
    id          VARCHAR(36) PRIMARY KEY,
    request_id  VARCHAR(36) NOT NULL,
    tenant_id   VARCHAR(36) NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50) NOT NULL,
    reason      VARCHAR(500),
    actor_id    VARCHAR(36),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_request_status_history_request_id
    ON request_status_history(request_id);
CREATE INDEX IF NOT EXISTS idx_request_status_history_tenant_id
    ON request_status_history(tenant_id);
CREATE INDEX IF NOT EXISTS idx_request_status_history_occurred_at
    ON request_status_history(occurred_at);

-- ============================================================
-- Table: jobs
-- Tracks asynchronous background jobs (document processing, imports, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS jobs (
    id               VARCHAR(36) PRIMARY KEY,
    type             VARCHAR(100) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'QUEUED',
    tenant_id        VARCHAR(36)  NOT NULL,
    reference_id     VARCHAR(36),
    progress_percent SMALLINT     NOT NULL DEFAULT 0
                         CHECK (progress_percent >= 0 AND progress_percent <= 100),
    error_message    VARCHAR(2000),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_jobs_tenant_id       ON jobs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status          ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at      ON jobs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_jobs_tenant_status   ON jobs(tenant_id, status);
