# AtlasOps AI — Project Scope

## 1. Product definition

AtlasOps AI is a multi-tenant B2B operational platform designed to validate software architecture practices, protocols, storage models, testing strategies and AI-assisted engineering techniques without turning the product into an unnecessarily large business system.

The application contains:

- an administrative backoffice;
- a client portal;
- customer and request management;
- large-file upload and document processing;
- AI-assisted document analysis;
- approval workflows;
- real-time activities and notifications;
- integrations through REST, webhooks and MCP;
- textual, semantic and geospatial search;
- operational analytics;
- immutable audit records.

The application must remain small in business scope and broad in technical coverage.

## 2. Primary end-to-end journey

```text
Administrator creates a customer
→ client creates a request
→ client uploads a large document
→ worker validates and processes the document
→ AI analyzes the content or executes a deterministic fallback
→ analyst approves or rejects the result
→ client receives a real-time notification
→ activity feed and dashboard are updated
→ critical actions are written to the immutable audit ledger
```

## 3. Roles

```text
ADMIN
ANALYST
CLIENT
```

### ADMIN

- manages customers and users;
- configures tenant settings;
- configures integrations;
- accesses operations, audit and developer documentation.

### ANALYST

- processes requests;
- reviews document analysis;
- approves or rejects documents;
- reprocesses failed jobs when authorized.

### CLIENT

- accesses only their customer context;
- creates and tracks requests;
- uploads and downloads authorized documents;
- receives notifications.

## 4. Essential functional scope

### Authentication and tenancy

- login;
- refresh token rotation;
- logout;
- tenant context;
- role-based authorization;
- resource authorization;
- cross-tenant isolation;
- basic branding;
- tenant read-only mode.

### Customers

- create;
- update;
- activate or deactivate;
- search and filter;
- associate client users;
- address and geospatial coordinates;
- radius-based queries.

### Requests

- create;
- assign analyst;
- update priority;
- update status;
- simple comments;
- associate documents;
- basic SLA.

Statuses:

```text
OPEN
IN_PROGRESS
WAITING_CUSTOMER
COMPLETED
CANCELLED
```

### Documents

- metadata registration before upload;
- direct multipart upload to object storage;
- support for files larger than 500 MB;
- pause, resume, retry and cancel;
- checksum validation;
- asynchronous processing;
- preview generation;
- extracted text;
- AI analysis with deterministic fallback;
- approval or rejection;
- authorized download;
- archive;
- reprocessing;
- document legal hold.

### Approvals

Generic approval is limited to:

- document approval;
- mutable MCP or AI actions;
- destructive operational replay.

Statuses:

```text
PENDING
APPROVED
REJECTED
CANCELLED
```

### Activities and notifications

```text
Activity
→ a fact that happened in the system

Notification
→ a user-targeted message
```

Channels:

- in-app;
- email;
- SSE.

### Search and command palette

One shared search platform supports:

- customers;
- requests;
- document metadata;
- extracted document content;
- semantic search;
- command navigation.

Modes:

```text
keyword
semantic
hybrid
```

### Integrations

- one REST API integration;
- one external MCP server;
- connectivity test;
- enable or disable;
- read-only execution;
- payload archive;
- execution history;
- one outbound webhook example.

### Imports

Only one initial import type:

```text
Customer CSV import
```

It must include:

- schema inference;
- preview;
- validation;
- row-level errors;
- asynchronous import;
- idempotency.

### Operations

One operations area must support:

- jobs;
- attempts;
- failures;
- DLQ;
- retry;
- replay;
- cancellation;
- execution details.

### Dashboard

A single administrative dashboard must show:

- active customers;
- requests by status;
- documents processed by period;
- average processing duration;
- AI usage;
- job failure rate;
- upload volume.

### Developer documentation

One developer area must provide:

- Swagger/OpenAPI;
- AsyncAPI event catalog;
- SSE examples;
- webhook example;
- MCP example;
- authentication examples;
- error contract;
- idempotency examples.

## 5. Frontend scope

### Admin pages

```text
/admin/dashboard
/admin/customers
/admin/customers/:id
/admin/requests
/admin/requests/:id
/admin/documents
/admin/documents/:id
/admin/approvals
/admin/search
/admin/activities
/admin/operations
/admin/integrations
/admin/imports
/admin/audit
/admin/settings
/developers
```

### Client pages

```text
/portal/home
/portal/requests
/portal/requests/:id
/portal/documents
/portal/documents/upload
/portal/notifications
```

### Shared frontend infrastructure

- table infrastructure;
- upload manager;
- notification center;
- command palette;
- document preview;
- optimistic UI;
- ETag/version conflict handling;
- accessibility baseline;
- responsive layout.

## 6. Protocols

| Protocol | Purpose |
|---|---|
| REST/HTTP | Commands, queries and CRUD |
| OpenAPI | REST contract and generated clients |
| SSE | Activities, notifications and progress |
| Redis Streams | Internal event distribution |
| Webhook | Outbound integration example |
| MCP | External read-only tool integration |
| S3 Multipart Upload | Large-file upload |
| SMTP | Email notification |
| AsyncAPI | Event documentation |

Excluded from the MVP:

- WebSocket;
- GraphQL;
- gRPC;
- Kafka;
- Kubernetes.

## 7. Persistence

### Core

- PostgreSQL;
- Redis or Valkey;
- MinIO or S3-compatible storage.

### PostgreSQL extensions

- pgvector;
- PostGIS.

### Specialized databases

- MongoDB for raw REST and MCP execution payloads;
- Neo4j for relationship and impact projection;
- TimescaleDB for selected operational metrics;
- OpenSearch for textual global search;
- ClickHouse for a small analytics projection;
- DuckDB for customer CSV import;
- EventStoreDB only for the Approval aggregate;
- PostgreSQL append-only ledger;
- Redis Streams as the event backbone.

Specialized databases must be optional at runtime and enabled through feature flags or Docker Compose profiles.

## 8. Deliberately excluded functionality

- commercial pipeline;
- independent task management;
- workflow designer;
- advanced white-labeling;
- configurable permission editor;
- multi-level approvals;
- advanced notification rules;
- complete reporting product;
- multiple import types;
- collaboration and presence;
- chat;
- PWA;
- complete tenant migration;
- complete selective restore;
- integration marketplace;
- full event sourcing;
- multiple AI providers.

## 9. Success criteria

The MVP is successful when:

1. the complete vertical journey runs locally;
2. two tenants are seeded and cross-tenant access is denied;
3. multipart upload works for large files;
4. asynchronous processing works with retry and DLQ;
5. SSE delivers progress and notifications;
6. AI analysis works with deterministic fallback;
7. keyword, semantic and hybrid search work;
8. each selected database has one explicit validated use case;
9. critical actions are verified by the ledger;
10. quality gates pass;
11. the project can run in core-only and advanced profiles;
12. documentation is sufficient to resume the project on another machine.

# Required delivery scripts

The project is not considered complete without the scripts in this section.

## 10. Seed scripts

The repository must include deterministic and idempotent seed scripts.

Required commands:

```bash
make seed
make seed-reset
make seed-demo
make seed-tests
```

Equivalent package-manager commands may exist internally, but the public project commands must remain stable.

### `make seed`

Creates the minimum development dataset without deleting existing data.

### `make seed-reset`

Resets local project data and recreates the baseline dataset.

It must never target production or shared environments.

### `make seed-demo`

Creates a complete demonstrable scenario.

#### Tenant Alpha

- one ADMIN;
- one ANALYST;
- one CLIENT;
- one customer;
- one open request;
- one uploaded document;
- one processed document;
- one pending approval;
- sample activities;
- sample notifications;
- one REST integration;
- one MCP integration;
- sample search data;
- sample analytics data.

#### Tenant Beta

- one ADMIN;
- one CLIENT;
- one customer;
- one request;
- enough data to validate tenant isolation.

### `make seed-tests`

Creates isolated fixtures or factories for automated suites.

Requirements:

- deterministic data where useful;
- unique identifiers for parallel tests;
- no production credentials;
- local demo credentials documented;
- specialized projections rebuildable from source data;
- repeatable execution;
- automatic cleanup support.

## 11. Complete functional test script

The repository must include one command that validates the complete user journey.

Required command:

```bash
make test-functional
```

Preferred technology:

```text
Playwright
```

The test must run against an isolated environment and must not depend on existing developer data.

### Required journey

1. start or validate the test environment;
2. seed isolated tenants;
3. log in as ADMIN;
4. create a customer;
5. create or invite a CLIENT;
6. log in as CLIENT;
7. create a request;
8. start a multipart document upload;
9. complete the upload;
10. observe upload and processing progress;
11. wait for AI analysis or deterministic fallback;
12. log in as ANALYST;
13. approve or reject the document;
14. verify the client notification;
15. verify the activity feed;
16. verify global search;
17. verify command palette navigation;
18. verify the operations page;
19. verify the immutable audit entry;
20. verify cross-tenant access denial.

### Additional requirements

- generate screenshots, videos or traces on failure;
- support headless execution in CI;
- support retries only for diagnostics;
- use isolated data per worker;
- clean generated resources;
- expose a human-readable report.

Suggested commands:

```bash
make test-functional
make test-functional-headed
make test-functional-report
```

## 12. Load test scripts

The repository must include load tests.

Preferred technology:

```text
k6
```

Artillery is acceptable when the selected implementation stack has a stronger practical reason.

Required commands:

```bash
make test-load-smoke
make test-load
```

### Smoke profile

Suggested characteristics:

```text
1–5 virtual users
30–60 seconds
broad thresholds
```

### Load profile

Minimum scenarios:

- authentication;
- customer listing;
- customer creation;
- request creation;
- upload-session creation;
- upload completion;
- global search;
- activity feed read;
- SSE connection establishment;
- job status query;
- integration execution.

Required measurements:

- p50;
- p95;
- p99;
- throughput;
- error rate;
- failed checks;
- upload initiation latency;
- search latency;
- integration latency;
- worker processing duration;
- stream consumer lag where measurable.

### Threshold examples

```text
simple read p95 <= 300 ms
simple write p95 <= 500 ms
search p95 <= 800 ms
unexpected HTTP error rate < 1%
failed checks = 0 for smoke
```

Thresholds must be configurable by environment.

### Test data and isolation

- use dedicated test tenants;
- generate unique idempotency keys;
- avoid polluting demo data;
- clean temporary files;
- isolate Redis keys and streams;
- use separate object-storage prefixes;
- export results as JSON and HTML when supported.

Suggested command:

```bash
make test-load-report
```

## 13. Final delivery gate

Before declaring the project complete:

```bash
make seed-reset
make seed-demo
make verify
make test-functional
make test-load-smoke
```

For a release candidate:

```bash
make verify-full
make test-functional
make test-load
```

The CI pipeline must retain:

- functional test report;
- Playwright traces and screenshots on failure;
- k6 or Artillery result files;
- seed execution logs;
- application logs;
- relevant metrics;
- test environment metadata.
