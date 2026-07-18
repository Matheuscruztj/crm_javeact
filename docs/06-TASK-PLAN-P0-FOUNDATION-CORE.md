# AtlasOps AI — Task Plan P0: Foundation and Core Vertical

## 1. Objective

P0 delivers a complete, secure and testable product vertical using the core platform:

```text
PostgreSQL
Redis or Valkey
Redis Streams
MinIO or S3-compatible storage
Admin Web
Client Web
Backend API
Worker
AI fake and deterministic fallback
```

P0 ends only when the complete Playwright journey passes.

---

## 2. Workstreams

### Track A — Backend Core

- modules;
- domain rules;
- REST API;
- PostgreSQL;
- authorization;
- ETag and idempotency.

### Track B — Frontend

- design system;
- admin shell;
- client shell;
- pages;
- generated client;
- Playwright page objects.

### Track C — Platform and Quality

- Docker;
- CI;
- commands;
- migrations;
- seeds;
- test isolation;
- observability;
- security scanning.

### Track D — Async and AI

- outbox;
- Redis Streams;
- worker;
- fake AI;
- fallback;
- evaluation skeleton.

### Track E — Documentation

- specifications;
- OpenAPI;
- AsyncAPI;
- ADRs;
- diagrams;
- current status.

Tracks can run in parallel when their contracts are stable.

---

## 3. Wave P0.0 — Repository and harness

### Tasks

- initialize repository;
- create module skeleton;
- create `AGENTS.md`;
- create `docs/00-current-status.md`;
- add ADR, issue and PR templates;
- configure CODEOWNERS;
- define English-only policy;
- add Makefile or Taskfile;
- add configuration validation;
- add health and readiness;
- add structured logging and correlation ID;
- create admin and client shells;
- configure Docker Compose core profile;
- add PostgreSQL, Redis, MinIO and MailHog;
- configure format, lint, typecheck, unit runner and build;
- add architecture dependency rule;
- add secret, dependency and container scans;
- initialize OpenAPI and AsyncAPI;
- initialize Playwright and k6;
- create seed framework.

### Required commands

```bash
make doctor
make bootstrap
make dev
make verify
make verify-full
make compose-core
make compose-down
make reset
make seed
make seed-reset
make seed-demo
make seed-tests
```

### Parallelization

Can run concurrently:

- backend shell;
- frontend shells;
- Docker and CI;
- documentation;
- test harness.

### Exit criteria

On a clean machine:

```bash
make doctor
make bootstrap
make compose-core
make seed
make verify
```

All pass.

---

## 4. Wave P0.1 — Tenant, user and authentication

### Dependency

P0.0.

### Required order

```text
Tenant
→ User
→ Session
→ Login
→ Refresh rotation
→ Logout
→ Role policy
→ Tenant-aware repositories
→ Cross-tenant tests
```

### Backend tasks

- tenant migration;
- user migration;
- session migration;
- password hash;
- access token;
- refresh-token hash and family;
- refresh rotation;
- replay detection;
- session revocation;
- tenant context middleware;
- code-defined role policies;
- authentication audit;
- rate limiting;
- stable auth errors.

### Frontend tasks

- login page;
- secure token/session handling;
- protected routes;
- role-aware navigation;
- expired-session flow;
- logout.

### Test tasks

- valid and invalid login;
- inactive user;
- suspended tenant;
- expired token;
- refresh reuse;
- revoked session;
- cross-tenant repository;
- cross-tenant HTTP;
- rate limit.

### Seed tasks

Create Tenant Alpha and Tenant Beta with baseline users.

### Exit criteria

Both portals authenticate and tenant isolation is proven.

---

## 5. Wave P0.2 — Customer management

### Dependency

P0.1.

### Backend tasks

- Customer entity and status;
- create/update/get/list use cases;
- tenant-aware repository;
- pagination, filter and sort;
- version field;
- audit;
- OpenAPI.

### Frontend tasks

- customer table;
- create form;
- edit form;
- detail page;
- loading, empty and error states;
- responsive behavior.

### Tests

- domain validation;
- repository integration;
- authorization;
- pagination;
- tenant isolation;
- optimistic concurrency.

### Parallelization

API and UI can progress from an OpenAPI mock. Geospatial fields are included now; PostGIS queries wait for P2.

### Exit criteria

ADMIN creates and edits a customer.

---

## 6. Wave P0.3 — Request management

### Dependencies

P0.1 and P0.2.

### Backend tasks

- Request entity;
- priority;
- state-transition policy;
- analyst assignment;
- comments;
- SLA deadline;
- status history;
- version and ETag;
- idempotent create;
- activity event.

### Frontend tasks

- admin list and detail;
- client list and detail;
- create-request form;
- analyst assignment;
- status update;
- comments;
- ETag conflict dialog.

### Tests

- valid and invalid transitions;
- assignment authorization;
- fake-clock SLA;
- duplicate idempotency key;
- ETag conflict;
- tenant and customer isolation.

### Exit criteria

CLIENT creates a request and ANALYST processes it.

---

## 7. Wave P0.4 — Documents and multipart upload

### Dependencies

P0.1 and customer/request foundations.

### Backend tasks

- Document migration;
- UploadSession migration;
- storage port;
- MinIO adapter;
- upload-session creation;
- signed part URLs;
- completion;
- checksum;
- declared/actual size validation;
- expiration;
- abandoned-upload cleanup;
- authorized download;
- legal-hold flag;
- outbox event.

### Frontend tasks

- file picker;
- multipart client;
- global upload manager;
- part parallelism;
- pause and resume;
- retry;
- cancel;
- document list and detail;
- processing placeholder.

### Tests

- storage integration;
- large-file fixture;
- duplicate completion;
- incomplete upload;
- expired session;
- wrong tenant;
- key safety;
- size mismatch;
- checksum mismatch;
- concurrent completion.

### Parallelization

- storage adapter;
- API;
- upload UI;
- test fixtures;
- document state policy.

### Exit criteria

A large file is uploaded directly to MinIO and confirmed idempotently.

---

## 8. Wave P0.5 — Outbox, streams and worker

### Dependencies

P0.3 and P0.4.

### Required order

```text
Domain event
→ Outbox
→ Dispatcher
→ Redis Stream
→ Consumer group
→ Job
→ Retry
→ DLQ
```

### Tasks

- OutboxEvent table;
- dispatcher;
- event envelope;
- main Redis Stream;
- consumer groups;
- Job table;
- worker bootstrap;
- idempotent consumer base;
- bounded retry;
- DLQ;
- normalized error;
- correlation propagation;
- stream-lag metric;
- operations API.

### Tests

- rollback does not publish;
- duplicate event is harmless;
- failure increments attempts;
- poison event reaches DLQ;
- correlation is preserved;
- worker restart recovers pending work.

### Exit criteria

`document.uploaded.v1` is processed asynchronously and failure is visible.

---

## 9. Wave P0.6 — Document processing and AI fallback

### Dependency

P0.5.

### Tasks

- document-extraction port;
- PDF/image metadata extraction;
- preview generation;
- DocumentAnalysisPort;
- deterministic fake;
- optional real adapter;
- deterministic fallback;
- output schema validation;
- prompt version;
- model metadata;
- analysis persistence;
- processing metrics;
- processing activities;
- preview UI.

### Evaluation skeleton

- golden dataset directory;
- structured-output validator;
- baseline evaluation command;
- prompt registry;
- initial judge rubric.

### Tests

- successful processing;
- provider unavailable;
- invalid AI output;
- fallback;
- duplicate job;
- preview unavailable;
- no cross-tenant context.

### Exit criteria

A document reaches `READY` with preview and structured analysis.

---

## 10. Wave P0.7 — Approval and ledger

### Dependency

P0.6.

### Tasks

- conventional PostgreSQL Approval model for P0;
- request approval;
- assign;
- approve;
- reject;
- cancel;
- approval list;
- analyst review UI;
- decision reason;
- append-only ledger;
- canonical payload;
- hash chain;
- verification command;
- decision activity;
- client notification.

### Tests

- role authorization;
- optimistic concurrency;
- duplicate decision;
- ledger verification;
- tamper detection;
- client isolation.

### Exit criteria

ANALYST decides a document and the decision is verifiable.

---

## 11. Wave P0.8 — SSE, activities and notifications

### Dependencies

P0.5 and P0.7.

### Tasks

- SSE endpoint;
- authenticated connection;
- heartbeat;
- `Last-Event-ID`;
- notification consumer;
- Activity persistence;
- Notification persistence;
- activity feed;
- notification center;
- unread count;
- mark read and mark all;
- email through MailHog;
- reconnect;
- connection metric.

### Tests

- own notification;
- no cross-tenant delivery;
- reconnect;
- duplicate event;
- disconnected persisted fallback;
- accessibility announcement.

### Exit criteria

CLIENT observes progress and final decision without polling.

---

## 12. Wave P0.9 — Dashboard, operations and developer docs

### Tasks

- dashboard cards;
- operations page;
- job detail;
- retry;
- cancellation;
- correlation search;
- developer page;
- Swagger UI;
- AsyncAPI artifact;
- SSE example;
- webhook contract example;
- MCP example placeholder;
- ledger verification page.

### Exit criteria

ADMIN can demonstrate, diagnose and access technical contracts.

---

## 13. Wave P0.10 — Seed, Playwright and k6 smoke

### Seed

```bash
make seed
make seed-reset
make seed-demo
make seed-tests
```

### Playwright

```bash
make test-functional
make test-functional-headed
make test-functional-report
```

Cover:

- ADMIN login and customer creation;
- CLIENT login and request creation;
- multipart upload;
- SSE processing;
- ANALYST approval;
- notification;
- activity;
- operations;
- ledger;
- tenant denial.

### k6 smoke

```bash
make test-load-smoke
```

Cover:

- authentication;
- customer list;
- request create;
- upload-session create;
- SSE connection;
- job read.

### P0 completion gate

```bash
make seed-reset
make seed-demo
make verify
make test-functional
make test-load-smoke
```
