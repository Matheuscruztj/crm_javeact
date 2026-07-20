-- Migration: Rename tables to match JPA entity @Table annotations
-- JPA entities use 'service_requests' and 'request_comments' but migrations created 'requests' and 'comments'
-- This migration fixes the mismatch that would cause TableNotFoundException at startup.
--
-- Validates: Bug fix — table name mismatch between Flyway migrations and JPA entities

-- Rename requests → service_requests
ALTER TABLE requests RENAME TO service_requests;

-- Rename associated indexes for service_requests
ALTER INDEX IF EXISTS idx_requests_tenant_id RENAME TO idx_service_requests_tenant_id;
ALTER INDEX IF EXISTS idx_requests_status RENAME TO idx_service_requests_status;
ALTER INDEX IF EXISTS idx_requests_customer_id RENAME TO idx_service_requests_customer_id;
ALTER INDEX IF EXISTS idx_requests_analyst RENAME TO idx_service_requests_analyst;

-- Rename comments → request_comments
ALTER TABLE comments RENAME TO request_comments;

-- Rename associated indexes for request_comments
ALTER INDEX IF EXISTS idx_comments_request_id RENAME TO idx_request_comments_request_id;
ALTER INDEX IF EXISTS idx_comments_tenant_id RENAME TO idx_request_comments_tenant_id;
