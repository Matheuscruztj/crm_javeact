# Changelog

All notable changes to AtlasOps AI are documented here.  
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).  
This project uses [Semantic Versioning](https://semver.org/).

---

## [Unreleased] — v1.0.0-rc.1

### Added — P0: Foundation

- **JWT Authentication Filter** (`JwtAuthenticationFilter`) — validates Bearer tokens on all protected endpoints
- **Tenant Authorization Filter** (`TenantAuthorizationFilter`) — prevents cross-tenant header spoofing
- **CORS Configuration** — explicit allowedOrigins from config, Idempotency-Key and Last-Event-ID headers allowed
- **Resource Authorization Filter** (`ResourceAuthorizationFilter`) — CLIENT role scoped to own customer data
- **Distributed Locking** (`RedisDistributedLockAdapter`) — prevents concurrent upload completion race conditions
- **Idempotency-Key header** (`IdempotencyFilter`) — Redis-backed deduplication for POST endpoints (24h TTL)
- **ETag filter** (`ETagFilter`) — SHA-256 ETags on GET responses, 304 Not Modified support
- **SSE Event Store** (`RedisSSEEventStore`) — Redis Sorted Set for Last-Event-ID replay (24h TTL)
- **SSE Heartbeat** (`SSEHeartbeatScheduler`) — 30s heartbeat to keep connections alive
- **Outbox cleanup** (`OutboxCleanupJob`) — removes PUBLISHED events older than 7 days
- **Circuit Breakers** (`ResilienceConfig`) — Resilience4j for Ollama, MinIO, webhook-dispatch
- **Multipart Upload** — presigned URL flow (initiate → upload → confirm), no backend proxying
- **Upload Manager** frontend — pause/resume/retry/cancel with XHR progress

### Added — P1: Experience

- **Feature Flag Framework** (`FeatureFlagPort`) — property-based + Redis runtime toggle
- **OpenSearch Adapter stub** — feature-flagged, falls back to PostgreSQL FTS
- **HMAC-SHA256 Webhook signing** (`WebhookSignatureUtils`) — X-Hub-Signature header
- **Webhook retry consumer** (`WebhookDispatchConsumer`) — exponential backoff (1s→600s), DLQ after 5 failures
- **Command Palette** (`CommandPalette`, `useCommandPalette`) — Ctrl+K global, route navigation
- **DataTable component** — server-side pagination, skeleton loading, keyboard navigation, URL-synced page
- **NotificationCenter** — badge, dropdown, mark-read, real-time via SSE
- **ErrorBoundary** — per-section fallback UI with reset
- **UploadManager** — chunked multipart with pause/resume/retry/cancel
- **Accessibility utilities** — SkipNav, VisuallyHidden, focus trap, WCAG_AA constants
- **i18n** — next-intl with en and pt-BR locales
- **Confirmation Dialog** — SENSITIVE/DESTRUCTIVE variants with affected-resource summary
- **UI States** — MaintenanceBanner, DegradedBanner, StaleProjectionBanner, PermissionDeniedState

### Added — P2: Specialized Data

- **Feature-flagged adapters** — Neo4j, TimescaleDB, ClickHouse, EventStoreDB (stubs, inactive by default)
- **PostGIS migration** — `geography(Point, 4326)` column + GIST index on customers
- **Verifiable Ledger** (`LedgerEntry`, `AppendToGlobalLedgerUseCase`) — SHA-256 hash chain
- **ProjectionStatus domain** — lifecycle DISABLED→READY→STALE→FAILED + rebuild commands
- **Document Legal Hold** — prevents archive/delete while hold active
- **Request SLA** — configurable deadline, breach notification
- **Maintenance Mode Filter** — Redis-backed read-only mode per tenant
- **Tenant Branding** — logo_url + primary_color endpoints
- **Notification Preferences** — per-user email + type preferences
- **Cross-Store Deletion Service** — legal hold check + distributed lock + domain events

### Added — P3: Hardening

- **SAST** — CodeQL + Semgrep in CI (P3.1.3)
- **Threat Model** — STRIDE analysis (P3.1.1)
- **Backup/Restore scripts** — pg_dump + MinIO mirror, manifest validation (P3.3)
- **k6 Stress Test** — 200 VUs, 10min ramp (P3.4.1)
- **Prometheus Alerts** — job failures, AI fallback, search latency, ledger tampering, SLOs (P3.6.3-P3.6.8)
- **SBOM generation** — CycloneDX via Syft in nightly pipeline (P3.7.1)

### Fixed

- `DashboardSummaryAggregationTest`: incorrect `TOTAL_CUSTOMERS`/`TOTAL_REQUESTS` MetricName references
- `GetDashboardUseCaseTest`: same MetricName fix
- `CancelJobUseCase`: wrong Clock import path
- `app-boot/build.gradle.kts`: duplicate `integrationTest` task — changed to `tasks.named`

---

## [0.1.0-SNAPSHOT] — 2025-01-20

### Added

- Initial monorepo structure (19 modules, hexagonal architecture)
- Spring Boot 3.2 + Java 21 baseline
- PostgreSQL + pgvector + Redis + MinIO infrastructure
- Spring AI + Ollama integration
- Docker Compose development environment
- Basic auth module (JWT, refresh tokens, account lockout)
