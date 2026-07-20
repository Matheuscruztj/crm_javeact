-- P2.9: Add legal hold support to documents table
ALTER TABLE documents ADD COLUMN IF NOT EXISTS legal_hold BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE documents ADD COLUMN IF NOT EXISTS legal_hold_activated_at TIMESTAMPTZ;
