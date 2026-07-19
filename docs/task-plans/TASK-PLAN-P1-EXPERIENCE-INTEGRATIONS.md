# AtlasOps AI — Task Plan P1: Experience, Search and Integrations

## 1. Objective

P1 improves user experience and validates external protocols without adding a new major business domain.

P1 delivers:

- shared table infrastructure;
- keyword, semantic and hybrid search;
- command palette;
- REST integration;
- MCP integration;
- MongoDB payload archive;
- outbound webhook;
- DuckDB customer import;
- accessibility and frontend hardening.

---

## 2. Parallel workstreams

### Search

- PostgreSQL fallback;
- OpenSearch;
- pgvector;
- chunking and embeddings;
- ranking.

### Integration Platform

- connection model;
- REST adapter;
- MCP adapter;
- MongoDB archive;
- webhook.

### Frontend Infrastructure

- table system;
- search page;
- command palette;
- import UI;
- conflict handling.

### Quality and Security

- SSRF;
- search authorization;
- relevance dataset;
- import isolation;
- Playwright extension.

---

## 3. Wave P1.1 — Table and interaction infrastructure

### Tasks

- generic server-driven table state;
- URL filters;
- sort;
- pagination;
- row selection;
- limited bulk actions;
- loading, empty and error states;
- keyboard behavior;
- reusable conflict dialog;
- stale-projection banner.

### Migration targets

- customers;
- requests;
- documents;
- approvals;
- operations.

### Exit criteria

All major lists share one table contract.

---

## 4. Wave P1.2 — Search foundation and fallback

### Tasks

- searchable-resource contract;
- search-index event;
- PostgreSQL text-search fallback;
- tenant and permission hydration;
- grouped search API;
- deletion propagation;
- rebuild-command base;
- search metrics.

### Tests

- tenant filter;
- permission filter;
- stale result;
- deleted resource;
- fallback.

### Exit criteria

Keyword search works without OpenSearch.

---

## 5. Wave P1.3 — OpenSearch

### Tasks

- advanced Compose profile;
- index templates;
- analyzers;
- customers, requests and documents indexes;
- event consumer;
- autocomplete;
- fuzzy matching;
- highlight;
- facets;
- versioned index alias;
- rebuild;
- lag metric.

### Tests

- mapping;
- tenant and permission filters;
- typo tolerance;
- deletion;
- rebuild;
- unavailable fallback.

### Exit criteria

OpenSearch is used when enabled and PostgreSQL when disabled.

---

## 6. Wave P1.4 — pgvector semantic search

### Tasks

- pgvector extension;
- KnowledgeChunk schema;
- chunking strategy;
- embedding port;
- deterministic fake;
- optional real adapter;
- semantic query;
- source citation;
- remove/reindex;
- golden-query dataset.

### Tests

- tenant isolation;
- permission filtering;
- source deletion;
- embedding version;
- relevance regression;
- deterministic fake.

### Exit criteria

Users search documents by meaning and open the source.

---

## 7. Wave P1.5 — Hybrid search and command palette

### Tasks

- hybrid ranker;
- configurable weights;
- PostgreSQL hydration;
- final authorization filter;
- command registry;
- permission-filtered commands;
- keyboard shortcut;
- navigation commands;
- create-request and upload commands;
- accessibility.

### Tests

- result order;
- command permission;
- no sensitive direct execution;
- keyboard flow;
- stale projection.

### Exit criteria

One API serves global search and command palette.

---

## 8. Wave P1.6 — Integration foundation

### Tasks

- Integration entity;
- secret reference;
- enable/disable;
- connectivity test;
- execution metadata;
- timeout;
- retry;
- normalized error;
- latency metric;
- audit and activity;
- integration UI.

### Exit criteria

ADMIN creates and tests a disabled-by-default connection.

---

## 9. Wave P1.7 — REST integration and SSRF

### Tasks

- REST provider port;
- HTTP adapter;
- URL validation;
- DNS validation;
- private-network blocking;
- redirect limit;
- request/response size limits;
- redacted logging;
- schema normalization;
- one free/public API example.

### Tests

- loopback denial;
- private address denial;
- redirect limit;
- timeout;
- oversized response;
- secret redaction;
- provider unavailable.

### Exit criteria

One public API operation is executed safely.

---

## 10. Wave P1.8 — MCP integration

### Tasks

- MCP adapter;
- tool catalog;
- read-only allowlist;
- schema validation;
- timeout;
- step limit;
- result-size limit;
- result redaction;
- execution trace;
- audit;
- developer example.

### Tests

- disallowed tool;
- invalid schema;
- timeout;
- cross-tenant attempt;
- prompt-injection content;
- oversized result;
- unavailable server.

### Exit criteria

One external read-only MCP tool is available.

---

## 11. Wave P1.9 — MongoDB payload archive

### Tasks

- advanced profile;
- archive port;
- REST request/response archive;
- MCP trace archive;
- header redaction;
- TTL index;
- PostgreSQL archive reference;
- retry when unavailable;
- ADMIN archive viewer.

### Tests

- TTL;
- tenant filter;
- redaction;
- unavailable archive;
- schema version;
- size limit.

### Exit criteria

Raw flexible payloads are inspectable without becoming transactional state.

---

## 12. Wave P1.10 — Outbound webhook

### Tasks

- minimal subscription/configuration;
- signed payload;
- timestamp;
- delivery ID;
- bounded retry;
- delivery job;
- DLQ;
- history;
- idempotent replay;
- AsyncAPI.

### Exit criteria

One event is delivered to a local receiver with signature verification.

---

## 13. Wave P1.11 — DuckDB customer import

### Tasks

- ImportJob;
- CSV upload;
- DuckDB adapter;
- schema inference;
- preview;
- validation;
- row-level errors;
- async import;
- progress;
- error report;
- idempotency;
- import UI.

### Tests

- malformed CSV;
- encoding;
- missing fields;
- duplicate rows;
- large input;
- tenant isolation;
- memory limit;
- cleanup.

### Exit criteria

ADMIN previews, validates and imports customers.

---

## 14. Wave P1.12 — P1 validation

Playwright adds:

- keyword search;
- semantic search;
- command palette;
- REST integration;
- MCP execution;
- CSV import.

k6 adds:

- search;
- integration execution;
- import creation;
- activity feed;
- SSE connection.

Exit:

```bash
make verify-full
make test-functional
make test-load-smoke
```
