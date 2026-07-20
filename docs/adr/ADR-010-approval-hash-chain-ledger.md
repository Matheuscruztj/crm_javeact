# ADR-010 — Append-Only Hash Chain Ledger for Audit Trail

**Status:** Accepted | **Date:** 2026-07-20

## Context

Approval decisions (approve/reject/cancel) must be tamper-evident. Once recorded, no one — not even a DBA — should be able to silently modify a decision without detection.

## Decision

Implement an **append-only hash chain ledger** using SHA-256:

- Each `LedgerEntry` stores: `sequenceNumber`, `eventType`, `payloadHash` (SHA-256 of audit JSON), `previousHash`, `currentHash`, `occurredAt`, `tenantId`
- `currentHash = SHA256(previousHash | seqNum | eventType | payloadHash | occurredAt | tenantId)`
- First entry uses `GENESIS_HASH = "GENESIS"` as previousHash
- Verification: recompute each entry's hash and verify the chain

Two ledgers exist:

1. `approval_ledger` — per-approval decisions (existing, P0.F.1)
2. `audit_ledger` — global audit entries (new, P2.5)

## Consequences

**Positive:** Tamper-evident audit trail; cryptographically verifiable; detects both data tampering and record deletion  
**Negative:** Append-only (no deletes); performance impact per write (mitigated by async ledger writes)
