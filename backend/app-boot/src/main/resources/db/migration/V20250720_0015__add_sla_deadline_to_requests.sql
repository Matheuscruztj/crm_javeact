-- P2.10: Add SLA deadline tracking to requests table
ALTER TABLE requests ADD COLUMN IF NOT EXISTS sla_deadline TIMESTAMPTZ;
ALTER TABLE requests ADD COLUMN IF NOT EXISTS sla_breached BOOLEAN DEFAULT FALSE;
