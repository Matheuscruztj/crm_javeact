-- =============================================================================
-- AtlasOps AI — Seed Data (Idempotent)
-- =============================================================================
-- This script populates demonstration data for local development.
-- It is fully idempotent: multiple executions produce the same final state
-- without creating duplicate records.
--
-- Data includes:
--   - 2 tenants (Alpha, Beta)
--   - 1 user per role (OWNER, ADMIN, MANAGER, ANALYST, OPERATOR, VIEWER) per tenant
--   - 3+ customers per tenant
--   - 2+ documents per customer
--
-- Usage: make seed
-- =============================================================================

-- Ensure tables exist (create if needed for seed data)
-- These tables use the 'app' schema created by init-db.

-- ---------------------------------------------------------------------------
-- Tenants
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app.tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO app.tenants (id, name, slug) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Alpha', 'alpha'),
    ('b0000000-0000-0000-0000-000000000002', 'Beta', 'beta')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Roles (lookup table)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app.roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO app.roles (id, name) VALUES
    ('r0000000-0000-0000-0000-000000000001', 'OWNER'),
    ('r0000000-0000-0000-0000-000000000002', 'ADMIN'),
    ('r0000000-0000-0000-0000-000000000003', 'MANAGER'),
    ('r0000000-0000-0000-0000-000000000004', 'ANALYST'),
    ('r0000000-0000-0000-0000-000000000005', 'OPERATOR'),
    ('r0000000-0000-0000-0000-000000000006', 'VIEWER')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Users (one per role per tenant = 12 total)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES app.tenants(id),
    role_id UUID NOT NULL REFERENCES app.roles(id),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255) NOT NULL DEFAULT '$2a$10$seedhashplaceholder000000000000000000000000000000',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Alpha tenant users
INSERT INTO app.users (id, tenant_id, role_id, email, name) VALUES
    ('u1000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'r0000000-0000-0000-0000-000000000001', 'owner@alpha.local', 'Alpha Owner'),
    ('u1000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'r0000000-0000-0000-0000-000000000002', 'admin@alpha.local', 'Alpha Admin'),
    ('u1000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'r0000000-0000-0000-0000-000000000003', 'manager@alpha.local', 'Alpha Manager'),
    ('u1000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'r0000000-0000-0000-0000-000000000004', 'analyst@alpha.local', 'Alpha Analyst'),
    ('u1000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001', 'r0000000-0000-0000-0000-000000000005', 'operator@alpha.local', 'Alpha Operator'),
    ('u1000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000001', 'r0000000-0000-0000-0000-000000000006', 'viewer@alpha.local', 'Alpha Viewer')
ON CONFLICT (id) DO NOTHING;

-- Beta tenant users
INSERT INTO app.users (id, tenant_id, role_id, email, name) VALUES
    ('u2000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'r0000000-0000-0000-0000-000000000001', 'owner@beta.local', 'Beta Owner'),
    ('u2000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'r0000000-0000-0000-0000-000000000002', 'admin@beta.local', 'Beta Admin'),
    ('u2000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', 'r0000000-0000-0000-0000-000000000003', 'manager@beta.local', 'Beta Manager'),
    ('u2000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', 'r0000000-0000-0000-0000-000000000004', 'analyst@beta.local', 'Beta Analyst'),
    ('u2000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002', 'r0000000-0000-0000-0000-000000000005', 'operator@beta.local', 'Beta Operator'),
    ('u2000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', 'r0000000-0000-0000-0000-000000000006', 'viewer@beta.local', 'Beta Viewer')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Customers (3+ per tenant = 8 total)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app.customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES app.tenants(id),
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    company VARCHAR(200),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, email)
);

-- Alpha tenant customers
INSERT INTO app.customers (id, tenant_id, name, email, phone, company) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'Acme Corporation', 'contact@acme.example', '+55-11-1111-0001', 'Acme Corp'),
    ('c1000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'Globex Industries', 'info@globex.example', '+55-11-1111-0002', 'Globex Inc'),
    ('c1000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'Initech Solutions', 'hello@initech.example', '+55-11-1111-0003', 'Initech LLC'),
    ('c1000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'Umbrella Corp', 'sales@umbrella.example', '+55-11-1111-0004', 'Umbrella Corp')
ON CONFLICT (id) DO NOTHING;

-- Beta tenant customers
INSERT INTO app.customers (id, tenant_id, name, email, phone, company) VALUES
    ('c2000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'Wayne Enterprises', 'info@wayne.example', '+55-21-2222-0001', 'Wayne Ent'),
    ('c2000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'Stark Industries', 'contact@stark.example', '+55-21-2222-0002', 'Stark Ind'),
    ('c2000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', 'Oscorp Research', 'lab@oscorp.example', '+55-21-2222-0003', 'Oscorp'),
    ('c2000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', 'LexCorp Holdings', 'biz@lexcorp.example', '+55-21-2222-0004', 'LexCorp')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Documents (2+ per customer = 16 total)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app.documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES app.tenants(id),
    customer_id UUID NOT NULL REFERENCES app.customers(id),
    title VARCHAR(300) NOT NULL,
    content_type VARCHAR(100) NOT NULL DEFAULT 'text/plain',
    storage_key VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID REFERENCES app.users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Documents for Alpha tenant customers
INSERT INTO app.documents (id, tenant_id, customer_id, title, content_type, storage_key, status, created_by) VALUES
    -- Acme Corporation documents
    ('d1000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Acme - Service Contract 2024', 'application/pdf', 'alpha/acme/contract-2024.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000003'),
    ('d1000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Acme - Technical Proposal', 'application/pdf', 'alpha/acme/tech-proposal.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000004'),
    -- Globex Industries documents
    ('d1000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000002', 'Globex - NDA Agreement', 'application/pdf', 'alpha/globex/nda.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000002'),
    ('d1000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000002', 'Globex - Meeting Notes Q1', 'text/plain', 'alpha/globex/meeting-q1.txt', 'ACTIVE', 'u1000000-0000-0000-0000-000000000005'),
    -- Initech Solutions documents
    ('d1000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000003', 'Initech - Project Scope', 'application/pdf', 'alpha/initech/scope.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000003'),
    ('d1000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000003', 'Initech - Budget Estimate', 'application/pdf', 'alpha/initech/budget.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000004'),
    -- Umbrella Corp documents
    ('d1000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000004', 'Umbrella - Lab Report', 'application/pdf', 'alpha/umbrella/lab-report.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000004'),
    ('d1000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000004', 'Umbrella - Safety Audit', 'application/pdf', 'alpha/umbrella/safety-audit.pdf', 'ACTIVE', 'u1000000-0000-0000-0000-000000000002')
ON CONFLICT (id) DO NOTHING;

-- Documents for Beta tenant customers
INSERT INTO app.documents (id, tenant_id, customer_id, title, content_type, storage_key, status, created_by) VALUES
    -- Wayne Enterprises documents
    ('d2000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000001', 'Wayne - R&D Partnership', 'application/pdf', 'beta/wayne/rd-partnership.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000003'),
    ('d2000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000001', 'Wayne - Annual Review', 'application/pdf', 'beta/wayne/annual-review.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000004'),
    -- Stark Industries documents
    ('d2000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000002', 'Stark - Technology License', 'application/pdf', 'beta/stark/tech-license.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000002'),
    ('d2000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000002', 'Stark - Integration Spec', 'text/plain', 'beta/stark/integration-spec.txt', 'ACTIVE', 'u2000000-0000-0000-0000-000000000005'),
    -- Oscorp Research documents
    ('d2000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000003', 'Oscorp - Research Agreement', 'application/pdf', 'beta/oscorp/research-agreement.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000003'),
    ('d2000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000003', 'Oscorp - Lab Certification', 'application/pdf', 'beta/oscorp/lab-cert.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000004'),
    -- LexCorp Holdings documents
    ('d2000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000004', 'LexCorp - Acquisition Memo', 'application/pdf', 'beta/lexcorp/acquisition-memo.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000002'),
    ('d2000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000002', 'c2000000-0000-0000-0000-000000000004', 'LexCorp - Due Diligence', 'application/pdf', 'beta/lexcorp/due-diligence.pdf', 'ACTIVE', 'u2000000-0000-0000-0000-000000000003')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Verification (optional output for debugging)
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    RAISE NOTICE 'Seed data applied successfully.';
    RAISE NOTICE 'Tenants: %', (SELECT COUNT(*) FROM app.tenants);
    RAISE NOTICE 'Roles: %', (SELECT COUNT(*) FROM app.roles);
    RAISE NOTICE 'Users: %', (SELECT COUNT(*) FROM app.users);
    RAISE NOTICE 'Customers: %', (SELECT COUNT(*) FROM app.customers);
    RAISE NOTICE 'Documents: %', (SELECT COUNT(*) FROM app.documents);
END $$;
