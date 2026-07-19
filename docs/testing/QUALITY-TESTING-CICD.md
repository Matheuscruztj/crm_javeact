# AtlasOps AI — Quality, Testing and CI/CD

## 1. Purpose

Quality gates prevent invalid, insecure, incompatible or untestable changes from progressing.

Quality includes:

- correctness;
- contracts;
- security;
- architecture;
- resilience;
- performance;
- AI behavior;
- documentation;
- supply chain.

---

## 2. Stable quality commands

```bash
make format-check
make lint
make typecheck
make build
make test-unit
make test-integration
make test-contract
make test-functional
make test-load-smoke
make test-load
make test-ai-evaluation
make security-scan
make docker-build
make verify
make verify-full
```

`make verify` is the fast local and PR baseline.

`make verify-full` executes broader integration, functional and specialized checks according to enabled profiles.

---

## 3. Quality gates

### G0 — Workspace

- runtime version;
- lockfile integrity;
- dependency installation;
- configuration validation;
- generated files current;
- no confirmed secrets;
- migrations ordered.

### G1 — Format

- formatter;
- JSON/YAML validity;
- line endings;
- final newline;
- Markdown lint where configured.

### G2 — Lint and static analysis

- unused code;
- unsafe patterns;
- forbidden imports;
- dependency cycles;
- architecture rules;
- complexity warnings;
- Dockerfile lint;
- OpenAPI and AsyncAPI lint.

### G3 — Typecheck or compile

Independent from tests.

### G4 — Unit

Covers entities, transitions, policies, authorization, idempotency, hashes, parsers, AI schema handling and Approval reducer.

### G5 — Build

Build backend, admin frontend, client frontend, worker, optional AI service, generated client and documentation artifacts.

### G6 — Migration

- empty database;
- all migrations;
- seed;
- upgrade from previous version;
- constraints;
- indexes;
- tenant keys;
- migration immutability.

### G7 — Integration

Real isolated PostgreSQL, Redis, MinIO, MailHog and affected specialized databases.

### G8 — Contract

- OpenAPI;
- generated client;
- event schema;
- AsyncAPI;
- webhook;
- AI request/response;
- MCP tool schema.

### G9 — Functional

Playwright primary journey and critical browser states.

### G10 — Security

- secret scan;
- dependency scan;
- SAST;
- IaC/container scan;
- license;
- DAST;
- authorization;
- tenant isolation;
- upload abuse;
- SSRF;
- MCP and AI abuse.

### G11 — Container

- non-root;
- health;
- no embedded secret;
- immutable tag;
- graceful shutdown;
- CVE policy.

### G12 — Performance

- k6 smoke;
- full load;
- thresholds;
- worker lag;
- search latency;
- upload-session latency.

### G13 — Observability

- metrics endpoint;
- structured logs;
- correlation;
- tracing;
- health/readiness;
- job and projection metrics.

### G14 — AI and Data

- schema;
- golden dataset;
- fallback;
- prompt regression;
- safety;
- LLM-as-a-judge;
- latency;
- grounding;
- approval flow.

### G15 — Release readiness

- version;
- changelog;
- migrations;
- restore validation;
- SBOM;
- image digest;
- smoke;
- functional;
- load;
- security;
- documentation;
- runbook;
- demo seed.

---

## 4. Pipeline

### Pull Request Fast

```text
workspace
format
lint
typecheck
unit
build
migration check
secret and dependency scan
contract
```

### Pull Request Integration

```text
ephemeral infrastructure
integration
Playwright smoke
Docker build
k6 smoke
```

### Main

```text
full integration
full Playwright
image scan
publish immutable artifact
SBOM
staging deploy
staging smoke
```

### Nightly

```text
full load
mutation
flaky detection
architecture scan
projection rebuild
ledger verification
AI evaluation
dependency report
restore smoke
```

### Release

```text
promote same artifact
production-like smoke
rollback and restore validation
retain evidence
```

---

## 5. Test portfolio

| Layer | Purpose |
|---|---|
| Static | Detect without execution |
| Unit | Domain and policies |
| Component | One module with controlled adapters |
| Integration | Real dependencies |
| Contract | Boundary compatibility |
| API E2E | Complete API flow |
| Browser E2E | Critical user flow |
| Security | Abuse and isolation |
| Performance | Capacity and regression |
| Resilience | Failure and recovery |
| AI evaluation | Probabilistic quality |
| Reconciliation | Projection correctness |

---

## 6. Isolation

Each run has:

```text
TEST_RUN_ID
TEST_WORKER_ID
TEST_CASE_ID
```

Isolate:

- database or schema;
- Redis prefix;
- storage prefix;
- stream consumer suffix;
- search-index suffix;
- tenant;
- user;
- files;
- temporary directories.

Do not use transaction rollback for tests requiring another process to observe committed state.

---

## 7. Unit-test rules

Unit tests do not use network, real database, global clock, global filesystem, arbitrary sleep or shared mutable singleton.

Inject:

- Clock;
- ID generator;
- random source;
- repository ports;
- storage;
- event publisher;
- AI adapter.

---

## 8. Integration matrix

### Auth

- session persistence;
- token rotation;
- tenant filtering.

### Documents

- multipart;
- checksum;
- outbox;
- job transaction;
- legal hold.

### Streams

- consumer group;
- duplicate event;
- retry;
- DLQ.

### Search

- OpenSearch mapping;
- pgvector query;
- fallback;
- deletion.

### Integrations

- HTTP stub;
- SSRF;
- MongoDB archive.

### Specialized stores

- Neo4j rebuild;
- TimescaleDB aggregate;
- ClickHouse reconciliation;
- EventStoreDB expected version;
- ledger verification.

---

## 9. Playwright

Commands:

```bash
make test-functional
make test-functional-headed
make test-functional-report
```

Artifacts:

- HTML report;
- trace;
- screenshot;
- video on failure;
- browser console;
- network summary.

Critical flows:

- authentication;
- customer creation;
- client request;
- multipart upload;
- SSE progress;
- analyst approval;
- notification;
- search;
- command palette;
- operations;
- tenant isolation.

Accessibility smoke is included.

---

## 10. k6

Commands:

```bash
make test-load-smoke
make test-load
make test-load-report
```

Profiles:

### Smoke

Low users, short duration and broad thresholds.

### Expected load

Representative concurrency, stable state and documented data.

### Stress exploration

Optional until the baseline is stable.

Metrics:

- p50/p95/p99;
- throughput;
- HTTP errors;
- failed checks;
- SSE connect;
- search;
- integration;
- worker duration;
- stream lag.

---

## 11. Security tests

Authentication:

- invalid credentials;
- inactive user;
- revoked session;
- refresh replay;
- expired token;
- brute force.

Authorization:

- role/resource matrix;
- customer ownership;
- portal boundary;
- destructive approval.

Multi-tenancy:

- direct object reference;
- search;
- graph;
- vector;
- activity;
- notification;
- job;
- cache;
- SSE;
- AI;
- archive.

Upload:

- fake MIME;
- double extension;
- oversized declaration;
- path traversal;
- predictable key;
- incomplete upload;
- duplicate completion.

Integration and AI:

- SSRF;
- redirect;
- secret redaction;
- prompt injection;
- tool allowlist;
- oversized response;
- timeout;
- cross-tenant content.

---

## 12. Coverage and mutation

Targets:

| Scope | Target |
|---|---:|
| Global lines | 75% |
| Global branches | 65% |
| Domain/Application | 85% |
| Security policy | 90% |

Mutation applies to authorization, transitions, idempotency, ledger and Approval reducer.

Coverage is a detector, not proof.

---

## 13. Flaky policy

- retry is diagnostic;
- flaky test opens an issue;
- critical test cannot remain quarantined for release;
- measure flaky rate;
- remove time, state, port, network and order causes;
- do not add arbitrary sleeps.

---

## 14. Specialized database gates

### MongoDB

- TTL;
- redaction;
- tenant filter;
- schema version;
- size limit;
- outage behavior.

### Neo4j

- tenant traversal;
- orphan nodes;
- path limit;
- rebuild;
- deletion.

### TimescaleDB

- hypertable;
- retention;
- timestamp;
- aggregates;
- tenant filter.

### OpenSearch

- mapping;
- analyzer;
- tenant/permission filter;
- deletion;
- alias rebuild;
- relevance regression.

### ClickHouse

- deduplication;
- partition;
- tenant filter;
- reconciliation.

### EventStoreDB

- expected version;
- upcasting;
- rebuild;
- temporal reconstruction.

### Ledger

- canonicalization;
- sequence;
- hash chain;
- tamper detection;
- concurrent append.

---

## 15. AI evaluation

Required:

- output schema;
- required fields;
- fallback;
- prompt-injection cases;
- grounding;
- citations;
- no tenant leakage;
- judge rubric;
- latency;
- human sample review.

Store dataset version, prompt version, candidate model, judge model, scores, failures and baseline comparison.

---

## 16. CI artifacts

Retain:

- JUnit;
- coverage;
- Playwright report;
- failure traces/screenshots/videos;
- service and Compose logs;
- k6 report;
- security report;
- SBOM;
- AI evaluation;
- projection reconciliation;
- ledger verification;
- environment metadata.

---

## 17. Exceptions

A gate exception records gate, reason, risk, owner, mitigation, issue, expiration and approver.

Permanent exceptions require an ADR.
