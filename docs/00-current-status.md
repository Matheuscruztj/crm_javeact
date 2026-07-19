# AtlasOps AI — Current Status

> **Last updated:** 2025-07-19  
> **Version:** 0.1.0-SNAPSHOT  
> **Phase:** P0 (MVP Foundation)

---

## Overview

AtlasOps AI is a multi-tenant CRM with local AI-powered document analysis (Ollama + pgvector). The project is in active development with the P0 foundation layer largely complete.

---

## Architecture Summary

- **Backend:** Java 21, Spring Boot 3.2.5, Gradle multi-project (18 modules)
- **Frontend:** React 19, Next.js 15, TypeScript, Tailwind CSS v4, shadcn/ui
- **Infrastructure:** Docker Compose (PostgreSQL 16 + pgvector, Redis 7, MinIO, Ollama)
- **Observability:** Prometheus, Grafana, Loki, Micrometer
- **Testing:** JUnit 5, jqwik (PBT), Mockito, ArchUnit, Testcontainers

---

## Module Status

| Module        | Domain | Application | Infrastructure | Presentation | Tests |
| ------------- | :----: | :---------: | :------------: | :----------: | :---: |
| shared-kernel |   ✅   |     N/A     |      N/A       |     N/A      |  ✅   |
| auth          |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| tenants       |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| users         |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| customers     |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| documents     |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| requests      |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| approvals     |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |
| activities    |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |
| notifications |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |
| integrations  |   ✅   |     ✅      |       ⚠️       |      ⚠️      |  ⚠️   |
| search        |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |
| imports       |   ✅   |     ✅      |       ⚠️       |      ⚠️      |  ⚠️   |
| operations    |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |
| ai            |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |
| analytics     |   ✅   |     ✅      |       ⚠️       |      ✅      |  ⚠️   |
| audit         |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |
| app-boot      |  N/A   |     N/A     |       ✅       |      ✅      |  ✅   |
| worker        |  N/A   |     ✅      |       ✅       |     N/A      |  ⚠️   |

Legend: ✅ Complete | ⚠️ Partial/Needs improvement | ❌ Not started

---

## Recently Completed

- [x] Transactional Outbox Pattern (migration + dispatcher + TransactionalEventPublisher)
- [x] Refresh Token Rotation with replay detection (token families)
- [x] Session Revocation endpoint (POST /api/v1/auth/revoke-all-sessions)
- [x] Rate Limiting (Redis-backed, tiered per endpoint type)
- [x] Seed commands (`make seed`, `make seed-reset`, `make seed-demo`, `make seed-tests`)
- [x] Apache Tika text extraction for documents (PDF, DOCX, 1000+ formats)
- [x] ETag/Optimistic Concurrency (version columns + Versioned interface)
- [x] OpenAPI/Swagger documentation (SpringDoc)

---

## In Progress / Next Steps

### High Priority (MVP critical path)

- [ ] Integration tests with Testcontainers
- [ ] Playwright E2E journeys (login, CRUD flows)
- [ ] CI Pipeline (GitHub Actions)
- [ ] Cross-Tenant Isolation Tests

### Medium Priority (quality)

- [ ] Idempotency-Key header for POST endpoints
- [ ] Approval Ledger (append-only hash chain)
- [ ] SSE heartbeat and Last-Event-ID validation
- [ ] Processing metrics (Micrometer counters/timers for AI)
- [ ] Prompt Version Registry

### Lower Priority (nice-to-have for P0)

- [ ] Multipart upload (signed part URLs, pause/resume)
- [ ] Operations Job UI (detail, retry, cancel)
- [ ] Golden Dataset + AI Evaluation framework
- [ ] AsyncAPI documentation

---

## Infrastructure

| Service    | Status | Version               |
| ---------- | :----: | --------------------- |
| PostgreSQL |   ✅   | 16 (pgvector enabled) |
| Redis      |   ✅   | 7-alpine              |
| MinIO      |   ✅   | RELEASE.2024-01-01    |
| Ollama     |   ✅   | 0.3.4                 |
| Prometheus |   ✅   | v2.48.0               |
| Grafana    |   ✅   | 10.2.0                |
| Loki       |   ✅   | 2.9.0                 |
| MailHog    |   ✅   | v1.0.1                |

---

## Quality Metrics (Target)

| Metric            | Target     | Current |
| ----------------- | ---------- | ------- |
| Line Coverage     | ≥ 75%      | ~60%    |
| Branch Coverage   | ≥ 65%      | ~50%    |
| Domain Coverage   | ≥ 85%      | ~75%    |
| Compilation       | 0 errors   | ✅      |
| Lint (Checkstyle) | 0 warnings | ⚠️      |
| SpotBugs          | 0 bugs     | ⚠️      |
| ArchUnit          | Pass       | ✅      |

---

## Commands Quick Reference

```bash
make bootstrap      # Full setup for new developer
make verify         # All quality gates
make test-unit      # Unit tests only
make compose-up     # Start infrastructure
make seed           # Demo data
make doctor         # Environment diagnostic
```

---

## Related Documents

- [AGENTS.md](../AGENTS.md) — Project governance and agent roles
- [PENDING-TASKS-AND-CLEANUP.md](./PENDING-TASKS-AND-CLEANUP.md) — Detailed task backlog
- [Architecture](./architecture/) — Architecture documentation
- [Task Plans](./task-plans/) — P0/P1/P2 planning
