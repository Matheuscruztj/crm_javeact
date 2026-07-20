# AtlasOps AI — System Architecture Overview

> **Validates:** P3.8.5 — Architecture diagrams refreshed

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                 │
│  Browser (Next.js 15)   │   Mobile (portal, responsive)             │
└──────────────────────┬──────────────────────────────────────────────┘
                       │ HTTPS (TLS)
                       │ JWT Bearer + X-Tenant-ID
┌──────────────────────▼──────────────────────────────────────────────┐
│                      API GATEWAY (app-boot)                          │
│  JwtAuthFilter → TenantAuthFilter → ResourceAuthFilter              │
│  CorrelationId → Idempotency → ETag → Rate Limit                    │
│                                                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐               │
│  │ Customers│ │ Requests │ │Documents │ │Approvals │               │
│  │ Tenants  │ │ Comments │ │ AI/RAG   │ │ Ledger   │               │
│  │ Users    │ │ SLA      │ │ Upload   │ │ Ledger   │               │
│  │ Search   │ │ Activity │ │ Notifs   │ │ Audit    │               │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘               │
│                                                                      │
│  SSE Events → Redis Streams → Outbox Events                         │
└──────────────────────┬──────────────────────────────────────────────┘
                       │ Async via Redis Streams
┌──────────────────────▼──────────────────────────────────────────────┐
│                     WORKER PROCESS                                   │
│  TextExtraction  │  AIAnalysis  │  Preview  │  Email  │  Webhooks   │
│  (Tika)          │  (Ollama)    │  (PDFBox) │  (SMTP) │  (HMAC)     │
└──────┬───────────┴──────────────┴──────────┴─────────┴─────────────┘
       │
┌──────▼──────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                        │
│                                                                      │
│  PostgreSQL 16 + pgvector  │  Redis 7     │  MinIO (S3)             │
│  ┌─────────────────────┐   │  ┌─────────┐ │  ┌──────────────────┐  │
│  │ customers           │   │  │ Sessions│ │  │ documents/       │  │
│  │ requests/comments   │   │  │ Cache   │ │  │ previews/        │  │
│  │ documents           │   │  │ Locks   │ │  │ exports/         │  │
│  │ approvals + ledger  │   │  │ Streams │ │  └──────────────────┘  │
│  │ notifications       │   │  │ Flags   │ │                         │
│  │ audit + ledger      │   │  └─────────┘ │                         │
│  │ outbox_events       │   │              │                         │
│  │ vector embeddings   │   │              │                         │
│  └─────────────────────┘   │              │                         │
│                             │              │                         │
│  Optional (feature flags):                                           │
│  OpenSearch │ Neo4j │ TimescaleDB │ ClickHouse │ EventStoreDB       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Module Dependencies

```
presentation → application → domain ← infrastructure
                                ↑
                          shared-kernel
```

All 19 modules follow this pattern. ArchUnit validates no violations at CI time.

---

## Key Data Flows

### Document Upload and AI Analysis

```
CLIENT → POST /documents (metadata)
       → POST /documents/{id}/upload-url
       → PUT presigned-url (direct to MinIO)
       → POST /documents/{id}/confirm-upload
API    → publish DocumentUploadedEvent to outbox
Outbox → publish to Redis Stream "documents.upload"
Worker → TextExtractionConsumer (Tika)
       → publish to "documents.ready_for_analysis"
Worker → AIAnalysisConsumer (Ollama + pgvector)
       → update document status = ANALYZED
       → create Approval (PENDING)
       → SSE notification to ADMIN/ANALYST
```

### Approval Lifecycle

```
ANALYST → POST /approvals/{id}/approve
API     → ApproveDocumentUseCase
        → Approval.approve() → domain event registered
        → ApprovalRepository.save()
        → EventPublisher.publish(ApprovalDecisionEvent)
        → AppendToLedgerUseCase → SHA-256 hash chain entry
        → SSE notification to CLIENT
Worker  → NotificationEventConsumer → email to CLIENT
```

---

## Security Layers

```
Request enters API
    │
    ▼ JwtAuthenticationFilter
    │   → validates Bearer token (HS256)
    │   → populates SecurityContext (userId, tenantId, role)
    │
    ▼ TenantAuthorizationFilter
    │   → validates X-Tenant-ID header == JWT claim tenantId
    │   → 403 if mismatch (prevents tenant escalation)
    │
    ▼ ResourceAuthorizationFilter
    │   → CLIENT role: sets request attribute for downstream use
    │   → Controllers use UserCustomerAssociationRepository to filter
    │
    ▼ IdempotencyFilter (POST only)
    │   → Redis-backed deduplication (24h TTL)
    │
    ▼ ETagFilter (GET only)
        → SHA-256 ETag on response body
        → 304 Not Modified if If-None-Match matches
```

---

## Infrastructure Profiles (Docker Compose)

| Profile          | Services                                             |
| ---------------- | ---------------------------------------------------- |
| `core`           | PostgreSQL + Redis + MinIO + Backend API + Worker    |
| `observability`  | core + Prometheus + Grafana + Loki + Tempo + MailHog |
| `advanced`       | core + OpenSearch + MongoDB + Neo4j                  |
| `analytics`      | core + TimescaleDB + ClickHouse                      |
| `event-sourcing` | core + EventStoreDB                                  |
| `all`            | All of the above                                     |
