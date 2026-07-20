# Release Notes — v1.0.0-rc.1

> **Release Date:** 2026-07-20  
> **Type:** Release Candidate  
> **Previous:** 0.1.0-SNAPSHOT  
> **Status:** Pending final validation (`make test-functional && make test-load`)

---

## Overview

AtlasOps AI v1.0.0-rc.1 is the first release candidate for the production-ready CRM with local AI document analysis. This release completes the P0 (Foundation), P1 (Experience), and initiates P2/P3 hardening.

---

## What's New in v1.0.0-rc.1

### Core Platform

- **Multi-tenant CRM** — Customer, Request, Document, Approval lifecycle management
- **Local AI Analysis** — Document classification and extraction via Ollama + pgvector RAG
- **Real-time Updates** — SSE push for notifications, document progress, activity feed
- **Role-Based Access** — ADMIN / ANALYST / CLIENT with resource-scoped authorization
- **Multipart Upload** — Files >500MB via presigned S3/MinIO URLs with pause/resume/retry

### Security

- JWT auth + refresh token rotation (1h access, 7d refresh)
- Cross-tenant isolation via header validation (prevents tenant spoofing)
- SSRF protection on webhook dispatch
- Idempotency-Key deduplication (24h Redis cache)
- SAST via CodeQL + Semgrep in CI
- OWASP Dependency-Check (blocks CVSS ≥ 9.0)

### Event Infrastructure

- Redis Streams consumer groups (at-least-once delivery)
- Transactional Outbox (no event loss on API crash)
- Dead Letter Queue after 5 retry failures
- HMAC-SHA256 signed outbound webhooks

### Observability

- Prometheus alerts: job failures, AI fallback, search latency, ledger tampering, SLOs
- Grafana dashboards: AI metrics, job health, request latency
- Structured JSON logging with tenantId + actorId in MDC
- k6 smoke + average + stress test scenarios

### Frontend

- Next.js 15 App Router, React 19, Tailwind CSS v4
- Admin portal + Client portal with responsive layouts
- Command Palette (Ctrl+K), Notification Center, Upload Manager
- i18n: English + Portuguese (pt-BR)
- Storybook 8 component catalog with accessibility addon

---

## Breaking Changes

None (first production-ready release).

---

## Known Issues

See [docs/00-current-status.md](00-current-status.md#known-limitations) for full list.

Key items:

- OAuth2/SSO not yet implemented
- OpenSearch disabled by default (feature flag)
- DAST not yet run against production-like environment

---

## Upgrade from 0.1.0-SNAPSHOT

```bash
# 1. Pull latest code
git pull origin main

# 2. Apply new migrations (V0013 through V0017)
make migrate

# 3. Rebuild and restart
make build
make compose-up

# 4. Validate
make verify
make test-functional
```

---

## Validation Checklist (P3.13)

- [ ] `make seed-reset && make seed-demo` — seed completes without errors
- [ ] `make verify` — all quality gates pass
- [ ] `make test-functional` — all Playwright journeys pass
- [ ] `make test-load-smoke` — p95 < 500ms, error rate 0%
- [ ] `make verify-ledger` — no tampering detected
- [ ] All 12 PROJECT-SCOPE success criteria verified (see PROJECT-SCOPE.md)

---

## Contributors

AtlasOps Engineering Team — Architecture, implementation, and quality.
