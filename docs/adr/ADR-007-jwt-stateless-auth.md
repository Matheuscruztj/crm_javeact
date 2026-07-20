# ADR-007 — JWT Stateless Authentication with Refresh Token Rotation

**Status:** Accepted | **Date:** 2025-01-20

## Context

We need an authentication mechanism that is stateless (no server-side session), supports multi-tenant contexts, and allows token revocation without requiring shared session state.

## Decision

Use **JWT (JSON Web Tokens)** for access tokens + **Redis-backed refresh tokens** with rotation.

- Access token: short-lived (1h), signed HS256, contains `userId`, `tenantId`, `role`
- Refresh token: long-lived (7d), stored in Redis as `refresh:{token}` → `{userId}`; rotated on each use
- On refresh: old token deleted, new token issued (sliding window)
- On logout: refresh token deleted from Redis (effective revocation)

## Consequences

**Positive:** Stateless API, no DB lookup per request, revocation via Redis, multi-tenant claim propagation  
**Negative:** Access tokens irrevocable until expiry (mitigated by short TTL + rate limiting)  
**Mitigation:** Account lockout stored in Redis; rate limiter blocks brute force
