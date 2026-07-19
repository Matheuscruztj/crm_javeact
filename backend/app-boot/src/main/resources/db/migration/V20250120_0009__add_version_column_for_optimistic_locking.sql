-- V20250120_0009__add_version_column_for_optimistic_locking.sql
-- Adds a version column to key entities for ETag-based optimistic concurrency control.
-- The version is incremented on each update and used as ETag value in HTTP responses.

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Comment
COMMENT ON COLUMN tenants.version IS 'Optimistic locking version, used as ETag';
COMMENT ON COLUMN users.version IS 'Optimistic locking version, used as ETag';
COMMENT ON COLUMN customers.version IS 'Optimistic locking version, used as ETag';
