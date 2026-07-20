-- P2.13: Add per-user notification preferences table
CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id       VARCHAR(255) NOT NULL,
    tenant_id     VARCHAR(100) NOT NULL,
    email_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    types_enabled TEXT[]                DEFAULT '{}',
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, tenant_id)
);
