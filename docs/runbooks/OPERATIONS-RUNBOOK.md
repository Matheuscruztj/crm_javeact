# AtlasOps AI — Operations, Bootstrap and Recovery Runbook

## 1. Purpose

This runbook enables a developer or engineering agent to bootstrap, diagnose, reset, seed, verify, recover projections, inspect jobs and validate backups without chat history.

---

## 2. Required files

```text
README.md
AGENTS.md
.env.example
Makefile or Taskfile
docker-compose.yml
docs/00-current-status.md
docs/specifications/
docs/architecture/
docs/testing/
docs/runbooks/
docs/adr/
```

---

## 3. Prerequisites

- Git;
- Docker;
- Docker Compose;
- selected runtime;
- Make or Task;
- sufficient disk and memory;
- free ports.

Run:

```bash
make doctor
```

Doctor checks runtime, Docker, Compose, environment, ports, disk, memory, network and required CLI tools.

---

## 4. Bootstrap

```bash
git clone <repository>
cd atlasops-ai
cp .env.example .env
make bootstrap
make compose-core
make migrate
make seed-demo
make verify
```

Expected:

- API healthy;
- admin and client web reachable;
- worker running;
- PostgreSQL, Redis, MinIO and MailHog reachable;
- Tenant Alpha and Beta available.

---

## 5. Compose profiles

### Core

```bash
make compose-core
```

Starts PostgreSQL, Redis/Valkey, MinIO, MailHog, API, worker and frontends.

### Advanced

```bash
make compose-advanced
```

Adds MongoDB, Neo4j, TimescaleDB and OpenSearch.

### Analytics

```bash
make compose-analytics
```

Adds ClickHouse.

### Event sourcing

```bash
make compose-event-sourcing
```

Adds EventStoreDB.

### All

```bash
make compose-all
```

May require substantial memory.

---

## 6. Seeds

```bash
make seed
make seed-reset
make seed-demo
make seed-tests
```

Local demo credentials must be documented and never reused in shared or production environments.

On seed failure check migrations, environment, constraints, service health and incompatible data.

---

## 7. Safe reset

```bash
make compose-down
make reset
make compose-core
make migrate
make seed-demo
```

Reset must affect only project-local resources, reject production-like environments, show targets and preserve files outside project ownership.

---

## 8. Health diagnosis

```bash
make health
make compose-logs
make worker-logs
make projection-status
```

### API not ready

Check PostgreSQL, migrations, Redis requirements, configuration and storage.

### Worker not processing

Check stream, consumer group, pending entries, job, DLQ, correlation ID, locks and unpublished outbox rows.

### SSE disconnected

Check token expiry, proxy buffering, heartbeat, connection metrics, browser network and persisted-notification fallback.

---

## 9. Upload recovery

### Failed before completion

- inspect UploadSession;
- inspect multipart state;
- identify parts;
- resume if valid;
- abort if expired or invalid.

### Storage complete but backend not confirmed

- call idempotent confirmation;
- validate checksum;
- verify outbox event.

### Confirmed but not processed

- inspect outbox;
- inspect stream;
- inspect job;
- retry safely.

### Abandoned cleanup

- check TTL;
- respect legal hold;
- abort multipart;
- mark expired;
- record evidence.

---

## 10. Failed-job recovery

```text
Open Operations
→ inspect error and correlation
→ inspect attempts
→ fix dependency or payload issue
→ retry or request replay
→ observe new execution
→ verify final state
→ retain ledger evidence
```

Do not edit raw production-like payloads without approved procedure.

---

## 11. Redis Streams

Inspect:

- stream length;
- groups;
- pending entries;
- idle consumers;
- lag;
- poison event;
- DLQ.

Recovery:

- restart consumer;
- claim stale entry;
- replay from event ID;
- rebuild projection.

All actions preserve idempotency.

---

## 12. Projection recovery

Commands:

```bash
make rebuild-search-index
make rebuild-vector-index
make rebuild-graph
make rebuild-analytics
make replay-projections
```

Flow:

```text
mark REBUILDING
→ create new version where possible
→ replay events or scan source
→ validate counts and samples
→ atomically switch
→ mark READY
→ retain report
```

OpenSearch uses versioned indexes and alias swap.

pgvector rebuilds by document and embedding version.

Neo4j uses idempotent merge or tenant rebuild.

ClickHouse reconciles event IDs and counts.

---

## 13. Ledger verification

```bash
make verify-ledger
```

On failure:

1. stop destructive maintenance;
2. identify tenant and sequence;
3. verify canonicalization version;
4. compare backup/checkpoint;
5. preserve evidence;
6. open an incident;
7. do not recalculate hashes without approved procedure.

---

## 14. Backup

Minimum set:

- PostgreSQL;
- MinIO/S3;
- EventStoreDB when enabled;
- configuration references;
- manifest with hashes;
- version and migration metadata.

OpenSearch and Neo4j are rebuildable, although optional backups may improve recovery time.
Local backup and portability scripts now also emit checksum manifests:

- `infra/scripts/backup.sh` writes `checksums.sha256` next to the PostgreSQL dump and mirrored objects;
- `infra/scripts/restore.sh` validates `checksums.sha256` before restoring;
- `infra/scripts/tenant-export.sh` writes `checksums.sha256` for tenant exports.

---

## 15. Restore validation

```bash
make restore-validate BACKUP_ID=<id>
```

Validate:

- migrations;
- tenant counts;
- customer/request/document counts;
- object existence;
- checksums;
- approval state;
- ledger;
- projection rebuild;
- smoke test.

Use an isolated environment.
If `checksums.sha256` exists, validation must fail before data restoration when any file hash differs.

---

## 16. Tenant export

Minimum:

- manifest;
- tenant;
- users without password secrets;
- customers;
- requests;
- document metadata;
- object references;
- approvals;
- audit references.

Export must be authorized, audited, legal-hold aware, versioned and hashed.
The portable export directory should retain `manifest.json` and `checksums.sha256` together.

Online tenant migration is excluded.

---

## 17. Degradation

### AI unavailable

Use deterministic fallback, mark result and allow later reprocess.

### OpenSearch unavailable

Use PostgreSQL fallback and mark projection stale.

### MongoDB unavailable

Persist execution metadata and retry archive.

### Neo4j unavailable

Business flow continues; graph is stale.

### TimescaleDB or ClickHouse unavailable

Charts degrade and ingestion retries.

### SMTP unavailable

In-app notification remains; email retries.

---

## 18. Current status

Maintain:

```text
docs/00-current-status.md
```

Template:

```markdown
# Current Status

## Last update
## Current priority and wave
## Completed capabilities
## Active feature flags
## Enabled profiles
## Quality-gate status
## Known issues
## Projection status
## Next task
## Validation commands
```

Update when a wave starts or ends, architecture changes, feature flags change behavior, a blocker appears or a release candidate is created.

---

## 19. Resume with an agent

```text
Read README.md, AGENTS.md, docs/00-current-status.md,
Project Scope, Technical Specification, relevant priority plan
and latest ADR.

Do not edit code yet.

Summarize:
1. current state;
2. architecture and ownership;
3. active quality gates;
4. known risks;
5. next smallest task;
6. validation commands.
```

The agent must not rely on chat memory.

---

## 20. Release artifacts

Retain:

- source tag;
- image digest;
- SBOM;
- OpenAPI;
- AsyncAPI;
- migrations;
- changelog;
- unit/integration reports;
- Playwright report;
- k6 report;
- security report;
- AI evaluation;
- projection reconciliation;
- ledger verification;
- restore report;
- known limitations.

---

## 21. Release candidate commands

```bash
make doctor
make seed-reset
make seed-demo
make verify-full
make test-functional
make test-load
make verify-ledger
```

A failure must produce actionable logs and artifacts.
