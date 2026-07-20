-- =============================================================================
-- AtlasOps AI — Minimal Test Seed Data (Idempotent)
-- =============================================================================
-- Populates tenants, users, customers, and service requests for automated tests.
-- Each entity uses deterministic UUIDs for reliable cross-test references.
--
-- Validates: P0.Q.1.1 — make seed-tests
-- Usage: make seed-tests
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Tenants (test-specific)
-- ---------------------------------------------------------------------------
INSERT INTO tenants (id, name, status, created_at, updated_at) VALUES
    ('t1000000-0000-0000-0000-000000000001', 'Test Tenant Alpha', 'ACTIVE', NOW(), NOW()),
    ('t2000000-0000-0000-0000-000000000002', 'Test Tenant Beta',  'ACTIVE', NOW(), NOW()),
    ('t3000000-0000-0000-0000-000000000003', 'Test Tenant Inactive', 'INACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Users (test-specific, password: "Test1234!")
-- BCrypt hash of "Test1234!" with cost 10
-- ---------------------------------------------------------------------------
INSERT INTO users (id, tenant_id, email, name, password_hash, role, status, created_at, updated_at) VALUES
    -- Alpha tenant: one per role
    ('ut100000-0000-0000-0000-000000000001', 't1000000-0000-0000-0000-000000000001', 'admin@test-alpha.local',    'Test Admin Alpha',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',    'ACTIVE', NOW(), NOW()),
    ('ut100000-0000-0000-0000-000000000002', 't1000000-0000-0000-0000-000000000001', 'analyst@test-alpha.local',  'Test Analyst Alpha',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ANALYST',  'ACTIVE', NOW(), NOW()),
    ('ut100000-0000-0000-0000-000000000003', 't1000000-0000-0000-0000-000000000001', 'client@test-alpha.local',   'Test Client Alpha',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT',   'ACTIVE', NOW(), NOW()),
    ('ut100000-0000-0000-0000-000000000004', 't1000000-0000-0000-0000-000000000001', 'inactive@test-alpha.local', 'Test Inactive Alpha', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT',   'INACTIVE', NOW(), NOW()),
    -- Beta tenant
    ('ut200000-0000-0000-0000-000000000001', 't2000000-0000-0000-0000-000000000002', 'admin@test-beta.local',    'Test Admin Beta',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',    'ACTIVE', NOW(), NOW()),
    ('ut200000-0000-0000-0000-000000000002', 't2000000-0000-0000-0000-000000000002', 'analyst@test-beta.local',  'Test Analyst Beta',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ANALYST',  'ACTIVE', NOW(), NOW()),
    ('ut200000-0000-0000-0000-000000000003', 't2000000-0000-0000-0000-000000000002', 'client@test-beta.local',   'Test Client Beta',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT',   'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Customers (test-specific — 2 per tenant for isolation tests)
-- ---------------------------------------------------------------------------
INSERT INTO customers (id, tenant_id, name, email, status, created_at, updated_at) VALUES
    ('cc100000-0000-0000-0000-000000000001', 't1000000-0000-0000-0000-000000000001', 'Alpha Customer One',   'cust1@test-alpha.local', 'ACTIVE',   NOW(), NOW()),
    ('cc100000-0000-0000-0000-000000000002', 't1000000-0000-0000-0000-000000000001', 'Alpha Customer Two',   'cust2@test-alpha.local', 'ACTIVE',   NOW(), NOW()),
    ('cc200000-0000-0000-0000-000000000001', 't2000000-0000-0000-0000-000000000002', 'Beta Customer One',    'cust1@test-beta.local',  'ACTIVE',   NOW(), NOW()),
    ('cc200000-0000-0000-0000-000000000002', 't2000000-0000-0000-0000-000000000002', 'Beta Customer Two',    'cust2@test-beta.local',  'INACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- User-Customer Associations (CLIENT users linked to customers)
-- ---------------------------------------------------------------------------
INSERT INTO user_customer_associations (user_id, customer_id, tenant_id, created_at) VALUES
    ('ut100000-0000-0000-0000-000000000003', 'cc100000-0000-0000-0000-000000000001', 't1000000-0000-0000-0000-000000000001', NOW()),
    ('ut200000-0000-0000-0000-000000000003', 'cc200000-0000-0000-0000-000000000001', 't2000000-0000-0000-0000-000000000002', NOW())
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Verification
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    RAISE NOTICE 'Test seed data applied successfully.';
    RAISE NOTICE 'Tenants: %', (SELECT COUNT(*) FROM tenants WHERE id LIKE 't%000000-0000-0000-0000-000000000%');
    RAISE NOTICE 'Users: %', (SELECT COUNT(*) FROM users WHERE email LIKE '%@test-%');
    RAISE NOTICE 'Customers: %', (SELECT COUNT(*) FROM customers WHERE email LIKE '%@test-%');
END $$;
