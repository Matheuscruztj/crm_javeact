# AtlasOps AI — Task Plan P3: Hardening, Resilience and Release

## 1. Objective

P3 converts the complete technical prototype into a reproducible release candidate.

Focus:

- security;
- resilience;
- performance;
- recovery;
- supply chain;
- documentation;
- release evidence.

No new major business capability enters P3.

---

## 2. Workstreams

### Security

- threat model;
- ASVS-oriented review;
- SAST and DAST;
- authorization abuse;
- SSRF;
- upload security;
- AI/MCP safety.

### Reliability

- dependency failure;
- retry;
- DLQ;
- projection rebuild;
- backup and restore;
- runbooks.

### Performance

- k6;
- large upload;
- search;
- streams;
- workers;
- database queries.

### Release Engineering

- immutable image;
- SBOM;
- version;
- changelog;
- release notes;
- demo.

### Documentation

- status;
- diagrams;
- ADRs;
- developer docs;
- known limitations.

---

## 3. Wave P3.1 — Threat model and security baseline

Threats:

- IDOR;
- cross-tenant leakage;
- role escalation;
- refresh replay;
- brute force;
- malicious upload;
- path traversal;
- SSRF;
- webhook replay;
- secret exposure;
- MCP tool abuse;
- prompt injection;
- AI exfiltration;
- projection leakage;
- destructive replay abuse.

Tasks:

- data-flow threat model;
- authorization matrix;
- upload abuse tests;
- SSRF suite;
- secret/dependency/container scans;
- license policy;
- headers;
- CORS;
- CSRF decision;
- DAST smoke;
- security runbook.

Exit: no known critical authorization, secret or tenant defect.

---

## 4. Wave P3.2 — Resilience

Test failures of:

- PostgreSQL;
- Redis;
- MinIO;
- AI;
- OpenSearch;
- MongoDB;
- Neo4j;
- TimescaleDB;
- ClickHouse;
- EventStoreDB;
- SMTP;
- external REST/MCP.

Tasks:

- bounded timeout;
- retry with backoff and jitter;
- circuit breaker only where justified;
- graceful degradation;
- job recovery;
- projection stale state;
- operator guidance;
- local/CI failure scripts.

Exit: transactional integrity remains correct and degradation is documented.

### 4.1 Current implementation evidence

- `backend/app-boot/src/test/java/com/atlasops/boot/resilience/MinioResilienceIntegrationTest.java` and `backend/ai/src/test/java/com/atlasops/ai/infrastructure/OllamaAIAdapterResilienceTest.java` cover MinIO and Ollama fallback behavior.
- `backend/search/src/test/java/com/atlasops/search/infrastructure/PostgresFullTextSearchAdapterResilienceTest.java`, `backend/search/src/test/java/com/atlasops/search/infrastructure/OpenSearchAdapterResilienceTest.java` and `backend/notifications/src/test/java/com/atlasops/notifications/infrastructure/RedisSSEEventStoreResilienceTest.java` cover PostgreSQL, OpenSearch and Redis failure paths.
- `backend/notifications/src/test/java/com/atlasops/notifications/infrastructure/SmtpEmailSenderAdapterResilienceTest.java` and `backend/integrations/src/test/java/com/atlasops/integrations/infrastructure/RestIntegrationAdapterResilienceTest.java` cover SMTP and external REST failure handling.
- `backend/search/src/test/java/com/atlasops/search/infrastructure/Neo4jRelationshipAdapterResilienceTest.java`, `backend/analytics/src/test/java/com/atlasops/analytics/infrastructure/TimescaleMetricsAdapterResilienceTest.java`, `backend/analytics/src/test/java/com/atlasops/analytics/infrastructure/ClickHouseAnalyticsAdapterResilienceTest.java` and `backend/approvals/src/test/java/com/atlasops/approvals/infrastructure/EventStoreApprovalAdapterResilienceTest.java` enforce fail-fast behavior for feature-flagged specialized stubs.

---

## 5. Wave P3.3 — Backup, restore and tenant operations

Supported:

- PostgreSQL backup;
- object-storage backup;
- tenant export;
- restore validation;
- single-document object restore;
- projection rebuild;
- ledger verification;
- read-only tenant mode.

Tasks:

- backup scripts;
- restore into isolated environment;
- export manifest with hashes;
- count and checksum validation;
- object verification;
- approval validation;
- runbook.

Excluded:

- arbitrary point-in-time restore UI;
- cross-region DR;
- online tenant migration;
- restore any arbitrary aggregate.

Exit: an isolated restore passes automated validation.

### 5.1 Current implementation evidence

- `infra/scripts/backup.sh` emits `checksums.sha256` alongside the backup manifest and PostgreSQL dump.
- `infra/scripts/restore.sh` validates `checksums.sha256` before any restore action and still supports `--dry-run`.
- `infra/scripts/tenant-export.sh` emits `checksums.sha256` for CSV exports and mirrored object-storage files.
- `docs/runbooks/OPERATIONS-RUNBOOK.md` documents the restore-validation and portability evidence flow.

---

## 6. Wave P3.4 — Full performance plan

k6 scenarios:

- auth;
- customer read/write;
- request create/update;
- upload-session create;
- upload completion;
- keyword/hybrid search;
- SSE connection;
- activity feed;
- job query;
- integration execution.

Additional:

- large multipart file;
- worker throughput;
- stream lag;
- OpenSearch lag;
- projection rebuild;
- ClickHouse ingestion;
- EventStoreDB concurrency.

Outputs:

- JSON;
- HTML;
- environment metadata;
- p50/p95/p99;
- throughput;
- error;
- bottleneck analysis.

Exit: thresholds pass or have expiring approved exceptions.

### 6.1 Current implementation evidence

- `tests/load/generate-report.mjs` converts k6 summaries into a lightweight HTML report.
- `Makefile` report targets emit raw JSON, `summary-export`, and HTML artifacts for the smoke and average scenarios.
- `.github/workflows/nightly.yml` stores the generated performance artifacts for later comparison.
- `frontend/tests/performance/critical-path.mjs` records frontend route-load evidence for the critical path.

---

## 7. Wave P3.5 — Test reliability and mutation

Tasks:

- flaky detection;
- quarantine policy;
- clock/ID review;
- test-order randomization;
- repeated parallel execution;
- mutation for authorization, transitions, idempotency, ledger and Approval reducer;
- slow-test report.

Exit: critical tests are deterministic and no critical test is quarantined.

### 7.1 Current implementation evidence

- The resilience-focused test additions around PostgreSQL, Redis, SMTP, OpenSearch, REST integrations, Neo4j, TimescaleDB, ClickHouse and EventStoreDB reduce the need for quarantines on fragile external dependencies.
- The existing `make verify` and targeted module tests provide a deterministic baseline for repeated execution and future flaky-test detection.

---

## 8. Wave P3.6 — Observability and SLOs

Tasks:

- log-quality review;
- trace coverage;
- dashboards;
- alert definitions;
- projection health;
- consumer lag;
- job failure alert;
- AI fallback alert;
- search latency alert;
- storage error alert;
- ledger alert.

Initial SLO candidates:

```text
Core API successful-request availability
Document job completion rate
Notification freshness
Search-projection freshness
```

Exit: a demo failure can be diagnosed from evidence.

### 8.1 Current implementation evidence

- `docs/STATUS.md` and `docs/00-current-status.md` now summarize W5 and W6 evidence together with the remaining gaps.
- The existing Prometheus/Grafana stack already covers job failures, AI fallback, ledger tampering and SLO-oriented health checks.
- The runbook work establishes the operational baseline needed for the remaining dashboards and alert definitions.

---

## 9. Wave P3.7 — Supply chain and artifacts

Tasks:

- reproducible build;
- pinned CI dependencies;
- lockfiles;
- non-root container;
- image scan;
- SBOM;
- image digest;
- source tag;
- changelog;
- release notes;
- OpenAPI and AsyncAPI;
- migrations;
- test reports;
- AI evaluation;
- k6 report;
- known limitations.

Exit: the same artifact is promoted without rebuilding.

---

## 10. Wave P3.8 — Documentation closure

Tasks:

- update specifications;
- update diagrams;
- verify commands;
- update ADR status;
- update current status;
- create demo script;
- troubleshooting guide;
- feature flags and profiles;
- local credentials;
- restore validation;
- specialized fallback.

Exit: a new engineer or agent resumes without chat context.

---

## 11. Wave P3.9 — Release candidate

Commands:

```bash
make doctor
make seed-reset
make seed-demo
make verify-full
make test-functional
make test-load
make verify-ledger
```

Checklist:

- main green;
- no critical vulnerability;
- no secret;
- no P0/P1 defect;
- migrations pass;
- functional journey passes;
- load report retained;
- backup restore validated;
- projection rebuild validated;
- AI evaluation passes;
- docs current;
- artifact identified by digest.

Release output:

```text
source tag
container image digest
SBOM
OpenAPI
AsyncAPI
migrations
test reports
security report
load report
AI evaluation report
demo seed
runbook
```
