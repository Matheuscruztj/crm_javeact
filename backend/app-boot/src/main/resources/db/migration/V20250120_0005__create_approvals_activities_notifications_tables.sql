-- V20250120_0005__create_approvals_activities_notifications_tables.sql
-- Create approvals, activities, and notifications tables for AtlasOps AI platform

-- ============================================================
-- Table: approvals
-- ============================================================
CREATE TABLE approvals (
    id               UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id        UUID        NOT NULL REFERENCES tenants(id),
    document_id      UUID        NOT NULL REFERENCES documents(id),
    status           VARCHAR(50) NOT NULL,
    decision_by      UUID,
    rejection_reason VARCHAR(1000),
    decision_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_approvals_status ON approvals(status);

-- ============================================================
-- Table: activities
-- ============================================================
CREATE TABLE activities (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID         NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    actor_id    UUID         NOT NULL,
    summary     VARCHAR(500),
    event_id    VARCHAR(255) NOT NULL,
    timestamp   TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id, tenant_id);
CREATE UNIQUE INDEX idx_activities_event_id ON activities(event_id);

-- ============================================================
-- Table: notifications
-- ============================================================
CREATE TABLE notifications (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id         UUID         NOT NULL REFERENCES tenants(id),
    recipient_user_id UUID         NOT NULL REFERENCES users(id),
    title             VARCHAR(150) NOT NULL,
    message           VARCHAR(500) NOT NULL,
    read              BOOLEAN      NOT NULL DEFAULT false,
    link              VARCHAR(1000),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_unread ON notifications(recipient_user_id, tenant_id, read) WHERE read = false;
