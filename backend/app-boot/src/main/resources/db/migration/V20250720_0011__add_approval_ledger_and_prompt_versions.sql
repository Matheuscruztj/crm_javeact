-- ============================================================
-- P0.F.1 — Approval Ledger (append-only hash chain)
-- P0.F.2 — Prompt Version Registry
-- ============================================================

-- Approval Ledger: immutable, append-only table with hash chain
CREATE TABLE IF NOT EXISTS approval_ledger (
    id             BIGSERIAL         PRIMARY KEY,
    sequence_number BIGINT           NOT NULL,
    approval_id    VARCHAR(36)       NOT NULL,
    status         VARCHAR(20)       NOT NULL,
    decision_by    VARCHAR(255)      NOT NULL,
    occurred_at    TIMESTAMPTZ       NOT NULL,
    tenant_id      VARCHAR(100)      NOT NULL,
    previous_hash  VARCHAR(64)       NOT NULL,
    entry_hash     VARCHAR(64)       NOT NULL UNIQUE,
    created_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_approval_ledger_tenant_seq
    ON approval_ledger (tenant_id, sequence_number);

CREATE INDEX IF NOT EXISTS idx_approval_ledger_approval_id
    ON approval_ledger (approval_id, tenant_id);

-- Prompt Version Registry: versioned prompt templates with A/B support
CREATE TABLE IF NOT EXISTS prompt_versions (
    id            VARCHAR(36)   PRIMARY KEY,
    tenant_id     VARCHAR(100)  NOT NULL,
    name          VARCHAR(100)  NOT NULL,
    version       VARCHAR(50)   NOT NULL,
    tag           VARCHAR(50),
    template      TEXT          NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT FALSE,
    ab_weight     SMALLINT      NOT NULL DEFAULT 100,
    created_by    VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_prompt_versions_tenant_name_version
    ON prompt_versions (tenant_id, name, version);

CREATE INDEX IF NOT EXISTS idx_prompt_versions_tenant_active
    ON prompt_versions (tenant_id, active);
