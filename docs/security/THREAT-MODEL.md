# AtlasOps AI — Threat Model

> **Version:** 1.0 | **Updated:** 2026-07-20  
> **Methodology:** STRIDE per component  
> **Validates:** P3.1.1 — Threat Model

---

## 1. System Overview

AtlasOps AI is a multi-tenant CRM with AI document analysis. Key assets:

- Customer PII (names, emails, addresses)
- Documents (contracts, invoices — potentially sensitive)
- AI analysis results (business intelligence)
- Authentication tokens (JWT, refresh tokens)
- Tenant configuration and branding

**Trust boundaries:** Browser → API (TLS) → Backend → PostgreSQL/Redis/MinIO/Ollama

---

## 2. STRIDE Analysis

### 2.1 Authentication and Session (Auth Module)

| Threat                    | Category    | Mitigation                                                | Status         |
| ------------------------- | ----------- | --------------------------------------------------------- | -------------- |
| JWT token theft           | Spoofing    | Short-lived access tokens (1h), httpOnly refresh rotation | ✅ Implemented |
| Token replay after logout | Repudiation | Refresh token revocation in Redis                         | ✅ Implemented |
| Brute force login         | Denial      | Account lockout after 5 failures (Redis-backed)           | ✅ Implemented |
| Credential stuffing       | Spoofing    | Rate limiting per IP + per email                          | ✅ Implemented |

### 2.2 Multi-Tenancy (Tenant Isolation)

| Threat                               | Category  | Mitigation                                                          | Status         |
| ------------------------------------ | --------- | ------------------------------------------------------------------- | -------------- |
| Cross-tenant data access via header  | Elevation | TenantAuthorizationFilter validates JWT claim vs X-Tenant-ID header | ✅ Implemented |
| IDOR on resource IDs                 | Elevation | All repository queries include `AND tenant_id = ?`                  | ✅ Implemented |
| CLIENT accessing other customer data | Elevation | ResourceAuthorizationFilter + UserCustomerAssociation               | ✅ Implemented |
| Super-admin impersonation            | Spoofing  | SUPER_ADMIN role requires specific claim, not user-assignable       | ✅ Implemented |

### 2.3 Document Upload (MinIO/S3)

| Threat                           | Category  | Mitigation                                  | Status         |
| -------------------------------- | --------- | ------------------------------------------- | -------------- |
| Path traversal in filename       | Elevation | Filename sanitized, storage path uses UUID  | ✅ Implemented |
| Malicious file upload (polyglot) | Tampering | Content-type validation + SHA-256 checksum  | ✅ Implemented |
| Unsigned URL bypass              | Spoofing  | Presigned URLs expire in 15min, single-use  | ✅ Implemented |
| Large file DoS                   | Denial    | Upload session TTL 24h, size limit enforced | ✅ Implemented |

### 2.4 AI/Prompt Injection (Ollama)

| Threat                                | Category  | Mitigation                                                 | Status                 |
| ------------------------------------- | --------- | ---------------------------------------------------------- | ---------------------- |
| Prompt injection via document content | Tampering | Document text is data, not prompt; LLM output not executed | ✅ Partially mitigated |
| Cross-tenant data leakage via RAG     | Elevation | Vector store queries scoped by tenant_id                   | ✅ Implemented         |
| Ollama endpoint exposed externally    | Elevation | Ollama runs on internal network only, not exposed          | ✅ Implemented         |
| Prompt version tampering              | Tampering | Prompts stored in DB, version-locked per analysis          | ✅ Implemented         |

### 2.5 API Layer

| Threat                | Category  | Mitigation                                                     | Status         |
| --------------------- | --------- | -------------------------------------------------------------- | -------------- |
| SSRF via webhook URL  | SSRF      | SSRFValidator blocks loopback, private, cloud metadata         | ✅ Implemented |
| SQL injection         | Tampering | JPA parameterized queries only                                 | ✅ Implemented |
| Mass assignment       | Tampering | Command objects with explicit fields, no `@RequestBody Entity` | ✅ Implemented |
| CORS misconfiguration | Spoofing  | Explicit allowedOrigins from config, no wildcard               | ✅ Implemented |
| Rate limiting bypass  | Denial    | Per-IP + per-user rate limit via Redis                         | ✅ Implemented |

### 2.6 Infrastructure

| Threat                            | Category    | Mitigation                                               | Status           |
| --------------------------------- | ----------- | -------------------------------------------------------- | ---------------- |
| Redis key collision in tests      | Tampering   | TestRunConfig isolates keys under `test:{runId}:*`       | ✅ Implemented   |
| Secret in logs                    | Information | MDC filters exclude sensitive values; log review in P3.8 | ⚠️ Review needed |
| Dependency with known CVE         | Tampering   | OWASP Dependency-Check blocks CVSS ≥ 9.0                 | ✅ Implemented   |
| Docker image with vulnerabilities | Tampering   | Multi-stage build, non-root user, pinned base images     | ✅ Implemented   |

---

## 3. Residual Risks

| Risk                                        | Likelihood | Impact | Mitigation Plan                                      |
| ------------------------------------------- | ---------- | ------ | ---------------------------------------------------- |
| Prompt injection in document text           | Medium     | Medium | Content filtering layer (P3.1.7 — future)            |
| Row-Level Security not enforced at DB level | Low        | High   | PostgreSQL RLS as defense-in-depth (P3.1.8 — future) |
| DAST scan not run in CI                     | Medium     | Medium | DAST against running API (P3.1.4 — future)           |

---

## 4. Authorization Matrix

| Role        | Customers    | Requests     | Documents    | Approvals    | Admin |
| ----------- | ------------ | ------------ | ------------ | ------------ | ----- |
| ADMIN       | Full CRUD    | Full CRUD    | Full CRUD    | Full CRUD    | Full  |
| ANALYST     | Read         | Read+Assign  | Read+Approve | Read+Decide  | None  |
| CLIENT      | Own only     | Own only     | Own only     | None         | None  |
| SUPER_ADMIN | Cross-tenant | Cross-tenant | Cross-tenant | Cross-tenant | Full  |

---

## 5. Next Steps (P3.1 remaining)

- [ ] P3.1.4: DAST scan (OWASP ZAP) against running dev instance
- [ ] P3.1.5: Authorization abuse test suite (role escalation attempts)
- [ ] P3.1.7: Prompt injection review with LLM safety guidelines
- [ ] P3.1.8: PostgreSQL RLS for additional defense in depth
