# AtlasOps AI — Current Status

> **Updated:** 2026-07-27  
> **Phase:** P3 (Hardening) — P0+P1 Complete, P2 ~40%, P3 ~60%  
> **Version:** v1.0.0-rc.1 (pending final validation)

---

## ✅ What's Working

### Authentication & Security

- JWT auth + refresh token rotation
- Account lockout after 5 failed attempts
- Cross-tenant isolation (TenantAuthorizationFilter)
- CLIENT role scoped to own customer data
- SSRF protection on webhook dispatch
- CORS configured for frontend

### Core Business Features

- **Customers** — CRUD, activate/deactivate, geospatial radius (PostGIS migration ready)
- **Requests** — lifecycle (OPEN→CLOSED), comments, status history, SLA deadline
- **Documents** — upload via presigned S3 URLs, text extraction, AI analysis
- **Approvals** — PENDING→APPROVED/REJECTED/CANCELLED with immutable hash-chain ledger
- **Activities** — tenant feed with role filtering, real-time via SSE
- **Notifications** — in-app + email, per-user preferences, SSE push
- **Search** — PostgreSQL full-text with tenant isolation, command palette (Ctrl+K)
- **Operations** — job monitoring, retry/cancel, projection status

### AI/RAG Pipeline

- Spring AI + Ollama integration
- pgvector embeddings (chunking, overlap)
- Deterministic fallback when Ollama unavailable
- Prompt versioning + A/B testing support
- Golden dataset evaluation framework

### Infrastructure

- Redis Streams event backbone (at-least-once delivery, DLQ)
- Transactional outbox pattern
- Distributed locking for concurrent operations
- Feature-flagged adapters: OpenSearch, Neo4j, TimescaleDB, ClickHouse, EventStoreDB

### Frontend

- Next.js 15 + React 19 with App Router
- Admin portal (customers, requests, documents, approvals, search, analytics, operations)
- Client portal (home, requests, documents, notifications)
- SSE real-time updates (notifications, document progress, activity feed)
- Upload Manager with pause/resume/retry
- i18n: English + Portuguese

### Quality & Observability

- ≥75% unit test coverage (P0 target met)
- 10 Playwright E2E journeys
- k6 smoke + average + stress test scenarios
- Prometheus alerts (job failures, AI fallback, ledger tampering, SLOs)
- Grafana dashboards (AI metrics, job health, request latency)
- Structured JSON logging with tenantId + actorId in MDC
- OpenAPI docs at /swagger-ui.html (local profile)
- AsyncAPI spec at docs/asyncapi.yaml

---

## ⚠️ Known Limitations

| Area    | Limitation                                       | Planned Fix                    |
| ------- | ------------------------------------------------ | ------------------------------ |
| Auth    | OAuth2/SSO not implemented                       | P3 or beyond                   |
| AI      | Prompt injection not fully mitigated             | P3.1.7                         |
| Storage | Large file uploads (>500MB) tested manually only | P3.4.2                         |
| Search  | OpenSearch not active (feature flag off)         | P1.3 pending production config |
| Graph   | Neo4j projection disabled (feature flag off)     | P2.2 pending infra             |
| Ledger  | Periodic verification job not scheduled          | P3 hardening                   |
| DAST    | Dynamic security testing not yet run             | P3.1.4                         |
| RLS     | PostgreSQL Row-Level Security not configured     | P3.1.8                         |

---

## 🔄 W5/W6 Evidência Não Funcional

### W5 — Runtime Resilience

- `backend/app-boot/build.gradle.kts` already defines `resilienceTest` as a separate verification task.
- `backend/app-boot/src/main/java/com/atlasops/boot/config/ResilienceConfig.java` registers circuit breaker metrics.
- `backend/app-boot/src/test/java/com/atlasops/boot/config/ResilienceConfigResilienceTest.java` covers the baseline behavior.
- `docs/runbooks/RESILIENCE-TESTING.md` documents the dependency-failure matrix and expected degradation.
- `ResilienceConfigResilienceTest` now exercises open/recover state transitions for the circuit breaker baseline.
- `.github/workflows/ci.yml` now routes `resilienceTest` through a dedicated `resilience-tests` job on the main quality pipeline.
- `backend/app-boot/src/test/java/com/atlasops/boot/resilience/MinioResilienceIntegrationTest.java` and `backend/ai/src/test/java/com/atlasops/ai/infrastructure/OllamaAIAdapterResilienceTest.java` add fault-injection and deterministic fallback coverage, with the Ollama path validated and the MinIO/Testcontainers path still being stabilized in this environment.
- `backend/search/src/test/java/com/atlasops/search/infrastructure/OpenSearchAdapterTest.java` and `backend/auth/src/test/java/com/atlasops/auth/infrastructure/RedisRateLimiterTest.java` cover fail-closed behavior for dependency outages.

### W6 — Performance and Operational Evidence

- `tests/load/smoke.js`, `tests/load/average.js`, `tests/load/stress.js` and `tests/load/five-users.js` already exist.
- `Makefile` exposes `test-load-smoke`, `test-load-5vu`, `test-load`, `test-load-report`, `test-load-5vu-report` and `test-load-stress`.
- `tests/load/generate-report.mjs` now emits a lightweight HTML report from k6 JSON summary artifacts.
- `Makefile` report targets now emit JSON raw output, k6 `summary-export` artifacts, and HTML summaries.
- The k6 scripts accept threshold overrides through env vars, which makes baseline tuning reproducible.
- `.github/workflows/ci.yml` runs the smoke load test on push to `main`.
- `.github/workflows/nightly.yml` runs the average load test and stores JSON + HTML artifacts.
- `docs/BUILD-PERFORMANCE.md` records the scenario metadata, threshold inputs and artifact layout used for comparability.
- `docs/RELEASE-v1.0.0-rc.1.md` now references the load-test report targets as release evidence.

### Remaining Gaps

- Fault injection with Toxiproxy + Testcontainers is present for MinIO and Ollama, but PostgreSQL, Redis and OpenSearch outage suites still need dedicated coverage.
- Frontend critical-path load evidence is still not documented in a dedicated flow.

---

## 🔧 Environment Requirements

| Component  | Version       | Notes                                   |
| ---------- | ------------- | --------------------------------------- |
| Java       | 21+           | Eclipse Temurin recommended             |
| Docker     | 24+           | Required for compose                    |
| Node.js    | 20+           | For frontend                            |
| pnpm       | 9+            | Package manager                         |
| PostgreSQL | 16 (pgvector) | Via Docker                              |
| Redis      | 7             | Via Docker                              |
| MinIO      | Latest        | S3-compatible                           |
| Ollama     | Latest        | Local AI (optional, fallback available) |

---

## 🚀 Quick Start

```bash
git clone <repo> && cd atlasops-ai
cp .env.example .env
make bootstrap        # Full setup (Java + Docker + migrations + seed)
make verify           # Run all quality gates
make compose-up       # Start infrastructure
# Backend: http://localhost:8080/swagger-ui.html
# Frontend: cd frontend && pnpm dev → http://localhost:3000
```

---

## 📋 Sprint Status

| Phase               | Status      | Tasks Done | Tasks Total |
| ------------------- | ----------- | ---------- | ----------- |
| P0 Foundation       | ✅ COMPLETE | 55         | 55          |
| P1 Experience       | ✅ COMPLETE | 30         | 30          |
| P2 Specialized Data | 🔄 40%      | 14         | 35          |
| P3 Hardening        | 🔄 60%      | 12         | 20          |
