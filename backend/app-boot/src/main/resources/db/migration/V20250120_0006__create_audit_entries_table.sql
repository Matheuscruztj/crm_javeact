-- V20250120_0006__create_audit_entries_table.sql
-- Create audit_entries table as append-only with immutability trigger

CREATE TABLE audit_entries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    action_type     VARCHAR(100) NOT NULL,
    actor_id        UUID NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID NOT NULL,
    correlation_id  UUID,
    details         JSONB,
    "timestamp"     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_audit_details_size CHECK (
        details IS NULL OR octet_length(details::text) <= 10240
    )
);

-- Index for time range queries on audit entries
CREATE INDEX idx_audit_timestamp ON audit_entries ("timestamp");

-- Trigger function to enforce immutability (prevent UPDATE and DELETE)
CREATE OR REPLACE FUNCTION prevent_audit_entry_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit entries are immutable: UPDATE and DELETE operations are not allowed';
END;
$$ LANGUAGE plpgsql;

-- Attach trigger to audit_entries table
CREATE TRIGGER trg_audit_entries_immutable
    BEFORE UPDATE OR DELETE ON audit_entries
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_entry_mutation();
