# ADR-003 — Multi-Tenancy Strategy: Shared Database, Separate Schema Namespacing

**Status:** Accepted  
**Date:** 2025-01-20  
**Deciders:** Engineering team

---

## Context

AtlasOps AI serves multiple tenants (businesses) on a single infrastructure. We need to decide how to isolate tenant data: separate databases, separate schemas, or shared tables with tenant_id column.

## Decision

We use **shared database + shared tables** with a mandatory `tenant_id` column on every business entity table.

Isolation is enforced at multiple layers:

1. **Application layer:** All repository queries include `WHERE tenant_id = ?`
2. **Filter layer:** `TenantAuthorizationFilter` validates JWT claim vs request header
3. **Future:** PostgreSQL Row-Level Security (RLS) as defense-in-depth (P3.1.8)

## Consequences

**Positive:**

- Simple operations (single DB backup, single migration set)
- Lower infrastructure cost (no per-tenant database provisioning)
- Easier cross-tenant analytics for super-admin

**Negative:**

- A bug in query code could leak cross-tenant data (mitigated by filter + tests)
- Cannot offer per-tenant DB-level isolation (compliance risk for some sectors)

**Mitigations:**

- Integration tests verify cross-tenant isolation for every repository
- `CrossTenantDataIsolationPropertyTest` runs 100 property-based scenarios
- Future: RLS as database-level enforcement
