# AtlasOps AI — Task Plan P2: Specialized Data Capabilities

## 1. Objective

P2 validates specialized persistence through one focused feature per technology.

Every specialized store must demonstrate:

- a justified access pattern;
- tenant isolation;
- failure behavior;
- observability;
- rebuild or reconciliation;
- automated tests.

---

## 2. Capability matrix

| Technology | Focus |
|---|---|
| PostGIS | Customer radius query |
| Neo4j | Relationship and impact explorer |
| TimescaleDB | Four selected time-series |
| ClickHouse | Four selected analytical events |
| PostgreSQL ledger | Tamper-evident audit |
| EventStoreDB | Approval event sourcing |
| MongoDB | Integration archive hardening |
| OpenSearch/pgvector | Search reconciliation |

---

## 3. Wave P2.1 — PostGIS

### Tasks

- enable PostGIS;
- add `geography(Point, 4326)`;
- create GIST index;
- geospatial port;
- radius query;
- nearest-customer query;
- simple map;
- coordinate validation;
- migration backfill.

### Tests

- invalid coordinate;
- exact boundary;
- radius;
- nearest;
- tenant isolation;
- index verification.

### Exit criteria

ADMIN finds customers within a radius.

---

## 4. Wave P2.2 — Neo4j relationship projection

### Scope

Nodes:

```text
Customer
Request
Document
Integration
```

Relationships:

```text
(Customer)-[:OPENED]->(Request)
(Customer)-[:OWNS]->(Document)
(Request)-[:ATTACHED]->(Document)
(Document)-[:PROCESSED_BY]->(Integration)
```

### Tasks

- Neo4j profile;
- graph port;
- event consumer;
- tenant on every node;
- relationship API;
- customer impact query;
- graph visualization;
- rebuild;
- orphan detection;
- lag metric.

### Tests

- cross-tenant traversal denial;
- duplicate event;
- deletion;
- orphan cleanup;
- depth limit;
- unavailable graph;
- rebuild.

### Exit criteria

ADMIN sees a relationship graph and impact summary.

---

## 5. Wave P2.3 — TimescaleDB

Selected metrics:

```text
document_processing_duration
request_sla_consumption
integration_latency
ai_analysis_duration
```

### Tasks

- advanced profile;
- hypertable;
- ingestion port and consumer;
- retention;
- optional compression;
- aggregate buckets;
- operational chart;
- tenant filter;
- lag/health.

### Tests

- timestamp normalization;
- tenant isolation;
- aggregate accuracy;
- retention;
- duplicate ingestion;
- unavailable store.

### Exit criteria

Dashboard displays historical operational measurements.

---

## 6. Wave P2.4 — ClickHouse

Selected events:

```text
customer.created
request.created
document.processed
ai.analysis.completed
```

### Tasks

- analytics profile;
- event table;
- partition strategy;
- idempotent ingestion;
- analytical consumer;
- event volume query;
- average document duration query;
- AI fallback ratio query;
- dashboard integration;
- reconciliation job.

### Tests

- duplicate event;
- partition query;
- tenant filter;
- aggregate accuracy;
- PostgreSQL reconciliation;
- unavailable ClickHouse.

### Exit criteria

Historical analytics are served from ClickHouse when enabled.

---

## 7. Wave P2.5 — Verifiable ledger

Critical events:

```text
permission.changed
approval.decided
document.legal_hold.changed
document.deleted
job.replayed
integration.executed
```

### Tasks

- append-only table;
- sequence;
- canonical payload;
- previous/current hash;
- checkpoint;
- verification command;
- tamper alert;
- verification UI;
- restore verification.

### Tests

- changed payload;
- removed row;
- broken sequence;
- wrong previous hash;
- canonicalization;
- concurrent append;
- tenant separation.

### Exit criteria

`make verify-ledger` detects intentional tampering.

---

## 8. Wave P2.6 — EventStoreDB for Approval

Events:

```text
ApprovalRequested
ApprovalAssigned
ApprovalApproved
ApprovalRejected
ApprovalCancelled
```

### Tasks

- EventStoreDB profile;
- stream naming;
- expected-version write;
- aggregate reducer;
- PostgreSQL read model;
- event upcasting;
- replay;
- temporal query;
- feature-flagged migration from conventional model.

### Tests

- expected-version conflict;
- duplicate command;
- reducer;
- old event version;
- projection rebuild;
- temporal reconstruction;
- unavailable store;
- feature-disabled fallback.

### Exit criteria

Approval state is rebuilt and queried historically.

---

## 9. Wave P2.7 — Projection registry

Track:

```text
search
semantic
graph
timeseries
analytics
archive
approval-read-model
```

Fields:

- tenant;
- projection;
- status;
- last event;
- last event time;
- lag;
- error;
- rebuild timestamps.

Functions:

- view;
- rebuild tenant;
- replay from offset;
- disable;
- resume;
- compare.

### Exit criteria

Operations shows health and lag for all enabled stores.

---

## 10. Wave P2.8 — Retention and legal hold

### Tasks

- legal-hold check;
- storage retention;
- OpenSearch deletion;
- pgvector deletion;
- Neo4j deletion;
- MongoDB retention;
- analytical correction strategy;
- ledger evidence.

### Tests

- hold blocks deletion;
- partial outage;
- retry deletion;
- rebuild does not resurrect data;
- tenant isolation.

### Exit criteria

Document lifecycle remains consistent across stores.

---

## 11. Wave P2.9 — Validation

Demonstrate:

- radius query;
- relationship explorer;
- time-series chart;
- historical analytics;
- ledger verification;
- Approval replay.

Commands:

```bash
make compose-all
make rebuild-search-index
make rebuild-vector-index
make rebuild-graph
make rebuild-analytics
make replay-projections
make verify-ledger
make verify-full
```

Every store has one working feature, one failure test and one recovery path.
