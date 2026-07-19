-- V20250120_0004__create_requests_comments_documents_tables.sql
-- Create requests, comments, and documents tables for AtlasOps AI platform

-- ============================================================
-- Table: requests
-- ============================================================
CREATE TABLE requests (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    customer_id         UUID NOT NULL REFERENCES customers(id),
    assigned_analyst_id UUID REFERENCES users(id),
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    status              VARCHAR(50) NOT NULL,
    priority            VARCHAR(50) NOT NULL,
    assigned_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_requests_tenant_id ON requests(tenant_id);
CREATE INDEX idx_requests_status ON requests(status);
CREATE INDEX idx_requests_customer_id ON requests(customer_id);

-- ============================================================
-- Table: comments
-- ============================================================
CREATE TABLE comments (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_id  UUID NOT NULL REFERENCES requests(id),
    author_id   UUID NOT NULL REFERENCES users(id),
    content     TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Table: documents
-- ============================================================
CREATE TABLE documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    request_id      UUID REFERENCES requests(id),
    filename        VARCHAR(500) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size_bytes      BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    extracted_text  TEXT,
    analysis_result JSONB,
    storage_path    VARCHAR(1000) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_tenant_id ON documents(tenant_id);
CREATE INDEX idx_documents_status ON documents(status);
