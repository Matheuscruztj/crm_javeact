# ADR-019: Tenant Authorization Filter Design

**Status:** Accepted  
**Date:** 2026-07-20  
**Deciders:** Engineering Team

---

## Context

Multi-tenant systems are vulnerable to **tenant escalation attacks**: an authenticated user from Tenant A sends `X-Tenant-ID: tenant-B` to access another tenant's data. Row-level data filters in repositories are a second line of defense, but should not be the only protection.

## Decision

Implement a **`TenantAuthorizationFilter`** that runs after `JwtAuthenticationFilter` and validates that the `X-Tenant-ID` request header matches the `tenantId` claim in the JWT.

## Filter Logic

```
1. If not authenticated → skip (handled by Spring Security)
2. If public path (login, actuator, swagger) → skip
3. If role = SUPER_ADMIN → allow any tenant header
4. Extract tenantId from JWT claims
5. Compare with X-Tenant-ID header
6. If mismatch → return 403 FORBIDDEN with code TENANT_ACCESS_DENIED
7. Populate MDC: tenantId + actorId for structured logging
```

## Bypass Rules

- Unauthenticated requests: Spring Security handles 401 before filter runs
- Public endpoints: `/actuator/**`, `/api/v1/auth/**`, `/swagger-ui/**`
- SUPER_ADMIN role: cross-tenant operations (e.g., admin tooling)

## Consequences

**Positive:**

- Defense in depth: tenant isolation enforced at HTTP layer before reaching use cases
- MDC population provides traceability in all downstream logs

**Negative:**

- All authenticated requests require `X-Tenant-ID` header — clients must include it
- SUPER_ADMIN bypass must be audited carefully to prevent escalation via role claims
