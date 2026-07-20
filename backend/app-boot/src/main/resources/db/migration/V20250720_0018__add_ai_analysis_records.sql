-- Migration: AI Analysis Records and Golden Dataset tables
-- Validates: P0.G.2 (golden_dataset), Requirement 4.8 (ai_analysis_records)

CREATE TABLE IF NOT EXISTS ai_analysis_records (
    id              VARCHAR(36)      NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(100)     NOT NULL,
    model           VARCHAR(100)     NOT NULL,
    prompt_version  VARCHAR(50)      NOT NULL,
    input_hash      VARCHAR(64)      NOT NULL,
    duration_ms     BIGINT           NOT NULL DEFAULT 0,
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fallback        BOOLEAN          NOT NULL DEFAULT FALSE,
    result          TEXT             NOT NULL,
    chunks_used     TEXT[]           NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_analysis_tenant ON ai_analysis_records(tenant_id);
CREATE INDEX IF NOT EXISTS idx_ai_analysis_created ON ai_analysis_records(tenant_id, created_at DESC);

CREATE TABLE IF NOT EXISTS golden_dataset (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(100)    NOT NULL,
    query           TEXT            NOT NULL,
    expected_answer TEXT            NOT NULL,
    category        VARCHAR(100),
    created_by      VARCHAR(100)    NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_golden_dataset_tenant ON golden_dataset(tenant_id);
