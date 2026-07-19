-- =============================================================================
-- AtlasOps AI — Minimal Test Seed Data (Idempotent)
-- =============================================================================
-- Populates only tenants and users for automated test scenarios.
-- Does NOT include customers or documents (tests create their own).
--
-- Usage: make seed-tests
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Tenants (test-specific)
-- ---------------------------------------------------------------------------
INSERT INTO tenants (id, name, status) VALUES
    ('t1000000-0000-0000-0000-000000000001', 'Test Tenant Alpha', 'ACTIVE'),
    ('t2000000-0000-0000-0000-000000000002', 'Test Tenant Beta', 'ACTIVE'),
    ('t3000000-0000-0000-0000-000000000003', 'Test Tenant Inactive', 'INACTIVE')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Users (test-specific, password: "Test1234!")
-- BCrypt hash of "Test1234!" with cost 10
-- ---------------------------------------------------------------------------
INSERT INTO users (id, tenant_id, email, name, password_hash, role, status) VALUES
    -- Alpha tenant users
    ('ut100000-0000-0000-0000-000000000001', 't1000000-0000-0000-0000-000000000001', 'owner@test-alpha.local', 'Test Alpha Owner', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'OWNER', 'ACTIVE'),
    ('ut100000-0000-0000-0000-000000000002', 't1000000-0000-0000-0000-000000000001', 'admin@test-alpha.local', 'Test Alpha Admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE'),
    ('ut100000-0000-0000-0000-000000000003', 't1000000-0000-0000-0000-000000000001', 'viewer@test-alpha.local', 'Test Alpha Viewer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'VIEWER', 'ACTIVE'),
    ('ut100000-0000-0000-0000-000000000004', 't1000000-0000-0000-0000-000000000001', 'inactive@test-alpha.local', 'Test Alpha Inactive', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'VIEWER', 'INACTIVE'),
    -- Beta tenant users
    ('ut200000-0000-0000-0000-000000000001', 't2000000-0000-0000-0000-000000000002', 'admin@test-beta.local', 'Test Beta Admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE'),
    ('ut200000-0000-0000-0000-000000000002', 't2000000-0000-0000-0000-000000000002', 'viewer@test-beta.local', 'Test Beta Viewer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'VIEWER', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Verification
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    RAISE NOTICE 'Test seed data applied successfully.';
    RAISE NOTICE 'Tenants: %', (SELECT COUNT(*) FROM tenants WHERE id LIKE 't%');
    RAISE NOTICE 'Users: %', (SELECT COUNT(*) FROM users WHERE id LIKE 'ut%');
END $$;
