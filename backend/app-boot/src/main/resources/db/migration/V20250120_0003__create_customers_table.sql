-- V20250120_0003__create_customers_table.sql
-- Create customers table with PostGIS geometry column and add FK from user_customer_associations

CREATE TABLE customers (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID        NOT NULL REFERENCES tenants(id),
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    street          VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    postal_code     VARCHAR(20),
    country         VARCHAR(100),
    location        geometry(Point, 4326),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- B-tree index for tenant isolation queries
CREATE INDEX idx_customers_tenant_id ON customers(tenant_id);

-- Unique index enforcing email uniqueness per tenant
CREATE UNIQUE INDEX idx_customers_email_tenant ON customers(email, tenant_id);

-- GIST index for geospatial radius queries
CREATE INDEX idx_customers_location ON customers USING GIST (location);

-- Add FK from user_customer_associations to customers (table created in V20250120_0002)
ALTER TABLE user_customer_associations
    ADD CONSTRAINT fk_user_customer_associations_customer_id
    FOREIGN KEY (customer_id) REFERENCES customers(id);
