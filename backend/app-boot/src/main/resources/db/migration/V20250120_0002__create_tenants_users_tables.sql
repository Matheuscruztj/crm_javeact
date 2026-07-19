-- V20250120_0002__create_tenants_users_tables.sql
-- Create tenants, users, and user_customer_associations tables

-- =============================================================================
-- TENANTS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS tenants (
    id         UUID        NOT NULL DEFAULT uuid_generate_v4(),
    name       VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uk_tenants_name UNIQUE (name)
);

-- =============================================================================
-- USERS TABLE
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id            UUID         NOT NULL DEFAULT uuid_generate_v4(),
    tenant_id     UUID         NOT NULL,
    email         VARCHAR(255) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users (tenant_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_tenant ON users (email, tenant_id);

-- =============================================================================
-- USER_CUSTOMER_ASSOCIATIONS (junction table)
-- FK to customers will be added in a later migration after customers table exists.
-- =============================================================================
CREATE TABLE IF NOT EXISTS user_customer_associations (
    user_id     UUID        NOT NULL,
    customer_id UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_user_customer_associations PRIMARY KEY (user_id, customer_id),
    CONSTRAINT fk_uca_user FOREIGN KEY (user_id) REFERENCES users (id)
);
