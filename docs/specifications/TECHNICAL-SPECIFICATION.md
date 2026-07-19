# AtlasOps AI — Technical Specification

## 1. Technical goals

The implementation must be framework-agnostic at specification level, modular, tenant-safe, reproducible, observable, testable and compatible with human or agent-assisted engineering.

The default runtime remains intentionally simple:

```text
Admin Web
Client Web
Backend API
Worker
AI adapter or AI service
PostgreSQL
Redis or Valkey
MinIO or S3-compatible storage
```

Specialized databases are enabled only for focused use cases and must not replace the transactional core without an explicit architectural decision.

## 2. Architecture style

### 2.1 Modular monolith

The backend is one deployable API application organized by domain modules:

```text
modules/
  auth/
  tenants/
  users/
  customers/
  requests/
  documents/
  approvals/
  activities/
  notifications/
  integrations/
  search/
  imports/
  operations/
  audit/
  analytics/
```

Each module may contain:

```text
domain/
application/
infrastructure/
presentation/
tests/
```

Rules:

- domain code does not depend on the framework;
- domain code does not directly access databases, queues or HTTP clients;
- application services orchestrate use cases;
- infrastructure implements ports;
- presentation exposes REST, SSE or event-consumer adapters;
- cross-module access uses explicit contracts;
- cyclic dependencies are prohibited;
- `shared` cannot become an unowned utility collection.

### 2.2 Separate worker process

The worker is part of the same repository but runs as a separate process. It handles document processing, preview generation, AI analysis, retries, DLQ, notifications, imports, webhooks and specialized projections.

### 2.3 Service extraction rule

A module may become a separate service only when there is a justified independent scaling, ownership, security, deployment, runtime or fault-isolation requirement.

## 3. API conventions

### Base path

```text
/api/v1
```

### OpenAPI

- OpenAPI 3.1 is versioned with the repository;
- contract lint runs in CI;
- examples are mandatory for critical endpoints;
- generated frontend clients are deterministic;
- breaking-change detection runs before merge.

### Identifiers and dates

- opaque string identifiers;
- UTC persistence;
- ISO 8601 API dates;
- timezone conversion only in presentation;
- stable enums;
- no floating-point money.

### Error contract

```json
{
  "type": "https://atlasops/errors/resource-version-conflict",
  "title": "Resource version conflict",
  "status": 412,
  "code": "RESOURCE_VERSION_CONFLICT",
  "detail": "The resource changed after it was loaded.",
  "traceId": "trc_...",
  "violations": []
}
```

Stable error codes include:

```text
AUTHENTICATION_FAILED
SESSION_REVOKED
TENANT_READ_ONLY
PERMISSION_DENIED
RESOURCE_NOT_FOUND
RESOURCE_VERSION_CONFLICT
INVALID_STATE_TRANSITION
IDEMPOTENCY_CONFLICT
UPLOAD_SESSION_EXPIRED
DOCUMENT_NOT_READY
INTEGRATION_UNAVAILABLE
AI_PROVIDER_UNAVAILABLE
```

### Idempotency

Retry-sensitive commands accept:

```http
Idempotency-Key: <opaque-key>
```

Identity:

```text
tenant
+ actor or service identity
+ operation
+ idempotency key
```

Behavior:

```text
same key + same request hash
→ return stored result

same key + different request hash
→ return IDEMPOTENCY_CONFLICT
```

Apply to request creation, upload confirmation, approval commands, integration execution, import creation and replay requests.

### ETag and conditional requests

Read:

```http
GET /api/v1/requests/{id}
ETag: "v7"
```

Write:

```http
PATCH /api/v1/requests/{id}
If-Match: "v7"
```

Conflict:

```http
412 Precondition Failed
```

ETag is backed by a stable resource version.

## 4. Authentication and authorization

### Authentication

- strong password hashing;
- short-lived access token;
- refresh token stored as a hash;
- refresh rotation;
- token-family reuse detection;
- current-session revocation;
- all-session revocation;
- authentication rate limiting;
- authentication audit.

### Tenant context

Tenant authority comes from authenticated membership, trusted service identity or explicitly audited support context. Tenant ID from request body, query string or arbitrary header is never authoritative.

### Authorization

Authorization considers tenant, role, customer association, resource ownership, resource state and portal type. Frontend checks improve UX but never replace backend authorization.

## 5. Multi-tenancy

Initial strategy:

```text
shared PostgreSQL database
+ shared schema
+ tenant_id on business records
```

Required controls:

- tenant-aware repository abstraction;
- composite indexes;
- tenant-scoped uniqueness;
- cross-tenant tests;
- tenant-aware cache keys;
- tenant-aware stream payloads;
- tenant-aware search, graph and vector filters;
- optional PostgreSQL Row-Level Security as defense in depth.

## 6. Large-file upload

Flow:

```text
Create document metadata
→ create multipart upload session
→ issue signed URLs
→ browser uploads parts directly
→ browser completes upload
→ backend validates completion
→ backend confirms document
→ outbox stores document.uploaded.v1
→ worker processes document
```

Requirements:

- files larger than 500 MB supported;
- backend never proxies complete file bytes;
- configurable part size and parallelism;
- part-level retry;
- pause and resume;
- upload-session TTL;
- abandoned multipart cleanup;
- declared-size and checksum validation;
- non-predictable object key;
- tenant authorization;
- legal-hold awareness;
- duplicate completion is idempotent.

Object prefixes:

```text
tenants/{tenantId}/documents/{documentId}/original/
tenants/{tenantId}/documents/{documentId}/preview/
tenants/{tenantId}/imports/{importId}/
tenants/{tenantId}/exports/{exportId}/
```

## 7. Asynchronous processing

### Transactional outbox

Business state and event are committed in one PostgreSQL transaction.

### Redis Streams

Main stream:

```text
atlasops-events
```

Consumer groups:

```text
document-processing
notifications
search-index
vector-index
graph-projection
telemetry
analytics
integration-archive
```

Consumers support at-least-once delivery, idempotent effects, bounded retry, DLQ, poison-message handling, lag metrics, event versioning and correlation propagation.

### Job model

Statuses:

```text
PENDING
RUNNING
COMPLETED
FAILED
CANCELLED
DEAD_LETTER
```

A job contains ID, tenant, type, attempts, available-at, idempotency key, payload reference, correlation ID, normalized error and timestamps.

### Distributed locking

Locks may coordinate upload completion, projection rebuild, single-instance maintenance, tenant export and destructive replay. Locks require TTL, unique owner token, safe release and fencing token where stale writers are dangerous.

Locks do not replace database constraints, idempotency or optimistic concurrency.

## 8. Realtime delivery

SSE is the only browser realtime protocol in the MVP.

```http
GET /api/v1/events/stream
Accept: text/event-stream
```

Requirements:

- authenticated connection;
- tenant and user filtering;
- heartbeat;
- `Last-Event-ID`;
- reconnect support;
- bounded replay;
- disconnect cleanup;
- backpressure policy;
- active-connection metrics.

Persisted notifications remain the fallback.

## 9. Integrations

### REST API

Controls:

- HTTPS by default;
- scheme allowlist;
- loopback, link-local and private-network blocking unless approved;
- DNS validation;
- redirect limit;
- request/response size limits;
- timeout;
- bounded retry;
- secret reference outside code;
- redacted logs;
- normalized error.

### MCP

Initial scope is read-only.

Controls:

- allowlisted server;
- imported tool schema;
- per-tool policy;
- tenant-aware execution;
- timeout and step limit;
- result-size limit;
- prompt-injection review;
- sensitive-result redaction;
- full tool-call trace;
- no direct database credentials.

### Webhook

One outbound example includes HMAC signature, timestamp, delivery ID, bounded retry, DLQ, history and idempotent replay.

## 10. AI architecture

Port:

```text
DocumentAnalysisPort
```

Input includes tenant context, document reference, extracted text, prompt version and output schema.

Output includes summary, category, extracted fields, risks, missing information, confidence, provider metadata and fallback flag.

Requirements:

- provider behind adapter;
- deterministic fake for tests;
- deterministic fallback;
- output schema validation;
- prompt version and input hash;
- provider/model metadata;
- latency metric;
- human approval before sensitive actions;
- no direct database access.

## 11. Persistence responsibilities

| Technology | Responsibility |
|---|---|
| PostgreSQL | Transactional source of truth, outbox, read models and ledger |
| Redis/Valkey | Cache, rate limit, locks, temporary state and streams |
| MinIO/S3 | Original files, previews, imports and artifacts |
| pgvector | Semantic document index |
| PostGIS | Customer geospatial index |
| OpenSearch | Text search and autocomplete index |
| MongoDB | Raw REST and MCP execution archive |
| Neo4j | Relationship and impact projection |
| TimescaleDB | Selected time-series |
| ClickHouse | Historical analytical projection |
| DuckDB | Temporary CSV and Parquet processing |
| EventStoreDB | Approval event source only |

## 12. Projection lifecycle

Every specialized projection requires event version, tenant key, idempotent upsert, deletion propagation, lag metric, health check, reconciliation, rebuild command and degradation policy.

Statuses:

```text
DISABLED
PENDING
PROCESSING
READY
STALE
FAILED
```

Required commands:

```bash
make rebuild-search-index
make rebuild-vector-index
make rebuild-graph
make rebuild-analytics
make replay-projections
make verify-ledger
```

## 13. Observability

Structured logs include timestamp, level, service, environment, tenant ID, actor ID, correlation ID, trace ID, event, resource, duration and error code.

Never log passwords, tokens, raw secrets, unredacted headers or full document content by default.

Metrics include request latency, errors, SSE connections, upload bytes, job duration, retry, DLQ, stream lag, AI fallback, integration latency, search latency, projection lag and ledger verification.

Tracing:

```text
browser
→ API
→ PostgreSQL/outbox
→ Redis Stream
→ worker
→ AI or specialized database
```

## 14. Health and degradation

Core readiness may include PostgreSQL, migrations, Redis when required and object storage for upload operations.

Fallbacks:

```text
OpenSearch unavailable
→ PostgreSQL text-search fallback

MongoDB unavailable
→ execution metadata succeeds and archive retries

Neo4j unavailable
→ business write succeeds and graph becomes stale

TimescaleDB or ClickHouse unavailable
→ charts degrade and ingestion retries

AI unavailable
→ deterministic fallback

SMTP unavailable
→ in-app notification persists and email retries
```

## 15. Configuration and profiles

Baseline variables:

```text
APP_ENV
APP_PORT
DATABASE_URL
REDIS_URL
OBJECT_STORAGE_ENDPOINT
OBJECT_STORAGE_BUCKET
JWT_ISSUER
JWT_AUDIENCE
AI_PROVIDER
AI_BASE_URL
OTEL_ENDPOINT
LOG_LEVEL
```

Profiles:

```text
core
advanced
analytics
event-sourcing
observability
```

Commands:

```bash
make compose-core
make compose-advanced
make compose-analytics
make compose-event-sourcing
make compose-all
```

## 16. English-only engineering policy

Code, identifiers, files, tests, comments, logs, metrics, errors, migrations, OpenAPI, AsyncAPI, ADRs, commits, pull requests and documentation must be written in English. User-facing text must be internationalized.
