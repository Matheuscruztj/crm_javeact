-- V20250120_0007__create_search_index_table.sql
-- Create search_index table with full-text search capabilities using tsvector and GIN index

CREATE TABLE search_index (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID NOT NULL,
    content_vector  TSVECTOR NOT NULL,
    title           VARCHAR(500) NOT NULL,
    content_snippet TEXT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- GIN index for full-text search on content_vector
CREATE INDEX idx_search_content ON search_index USING GIN (content_vector);

-- B-tree index for tenant isolation queries
CREATE INDEX idx_search_index_tenant_id ON search_index (tenant_id);

-- Unique constraint to prevent duplicate entries per entity per tenant
CREATE UNIQUE INDEX idx_search_index_entity ON search_index (tenant_id, entity_type, entity_id);
