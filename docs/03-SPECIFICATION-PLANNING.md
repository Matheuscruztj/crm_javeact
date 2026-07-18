# AtlasOps AI — Specification Planning

## 1. Purpose

This document defines how product and technical specifications are created, decomposed, reviewed, approved, implemented and maintained.

A specification is an executable agreement between product intent, architecture, design, security, testing and delivery.

## 2. Specification hierarchy

```text
Project Scope
→ Technical Specification
→ Architecture Decision Records
→ Capability Specifications
→ API and Event Contracts
→ Design Specifications
→ Epics
→ Stories
→ Tasks
→ Acceptance Evidence
```

Rules:

- product intent belongs in Project Scope;
- cross-cutting engineering belongs in Technical Specification;
- irreversible or debated choices belong in ADRs;
- feature behavior belongs in capability specs;
- delivery order belongs in task plans;
- current implementation state belongs in `docs/00-current-status.md`.

## 3. Required inventory

Product specifications:

- authentication and tenancy;
- customers;
- requests;
- documents and upload;
- approvals;
- activities and notifications;
- search;
- integrations;
- imports;
- operations;
- dashboard;
- developer documentation.

Technical specifications:

- REST and OpenAPI;
- ETag and idempotency;
- SSE;
- outbox;
- Redis Streams;
- jobs and DLQ;
- object storage;
- AI adapter and fallback;
- specialized projections;
- immutable ledger;
- Approval event sourcing;
- observability;
- test isolation.

## 4. Capability specification template

```markdown
# <Capability Name>

## Objective
## Actors
## Business value
## Preconditions
## Primary flow
## Alternative flows
## Failure flows
## Out of scope
## Functional requirements
## Authorization
## Tenant isolation
## State model
## API contracts
## Events
## Data ownership
## Observability
## Security
## Accessibility
## Test strategy
## Acceptance criteria
## Required evidence
```

Requirements use `MUST`, `SHOULD` and `COULD`.

Every acceptance criterion must be observable through an API response, persisted state, emitted event, UI state, metric/log or automated test.

## 5. API specification template

```markdown
# <Operation>

## Purpose
## Method and path
## Authentication
## Authorization
## Tenant behavior
## Request headers
## Request body
## Response
## Error codes
## Idempotency
## ETag
## Rate limit
## Audit
## OpenAPI examples
## Contract tests
```

## 6. Event specification template

```markdown
# <event.name.v1>

## Producer
## Trigger
## Transaction boundary
## Envelope
## Payload
## Ordering key
## Idempotency key
## Consumers
## Retry
## DLQ
## Retention
## Data classification
## Versioning
## AsyncAPI reference
## Contract tests
```

Standard envelope:

```json
{
  "id": "evt_...",
  "type": "document.uploaded.v1",
  "version": 1,
  "tenantId": "ten_...",
  "aggregateType": "document",
  "aggregateId": "doc_...",
  "correlationId": "trc_...",
  "occurredAt": "2026-07-18T12:00:00Z",
  "data": {}
}
```

## 7. Data specification template

```markdown
# <Data Model or Projection>

## Classification
Source of truth | Event source | Projection | Index | Archive | Cache | Telemetry

## Owner module
## Access patterns
## Tenant partitioning
## Schema
## Indexes
## Constraints
## Consistency
## Retention
## Legal hold
## Backup
## Restore
## Rebuild
## Reconciliation
## Failure behavior
## Security
## Tests
```

A specialized database cannot be added without a documented access pattern and lifecycle.

## 8. AI specification template

```markdown
# <AI Capability>

## Objective
## Allowed input
## Prohibited input
## Output schema
## Prompt version
## Provider adapter
## Deterministic fallback
## Human approval
## Tool policy
## Safety risks
## Evaluation dataset
## Rule-based checks
## LLM-as-a-judge rubric
## Latency budget
## Cost budget
## Release threshold
```

Evaluation dimensions:

- correctness;
- completeness;
- grounding;
- citation quality;
- safety;
- format compliance;
- tenant isolation;
- recommendation appropriateness.

## 9. Lifecycle

```text
PROPOSED
→ DRAFT
→ REVIEWING
→ APPROVED
→ IMPLEMENTING
→ VERIFIED
→ DEPRECATED
```

## 10. Review responsibilities

Product review checks user value, scope, duplication and acceptance criteria.

Architecture review checks module boundaries, data ownership, consistency, failure behavior and ADR need.

Security review checks tenancy, authorization, secrets, upload, SSRF, prompt injection, audit and retention.

Design review checks user flow, error recovery, states, accessibility and conflict behavior.

Testability review checks determinism, clocks, IDs, isolation, fixtures and observable outcomes.

## 11. Definition of Ready

A capability is ready when:

- objective and actors are explicit;
- primary and failure flows exist;
- acceptance criteria are testable;
- out of scope is explicit;
- API or event contract exists;
- source of truth is identified;
- tenant and authorization rules exist;
- migration impact is known;
- observability and security are defined;
- design exists for UI work;
- test strategy exists;
- work can be split into reviewable pull requests.

Not ready:

```text
Implement documents.
Improve security.
Add AI.
Create integrations.
```

## 12. Definition of Done

A verified specification has merged implementation, passing acceptance criteria, updated contracts, migration evidence, relevant automated tests, logs/metrics, UI screenshots, current-status update and an ADR when architecture changed.

## 13. Change management

Breaking changes require migration plan, contract diff, owner approval, frontend/consumer coordination, compatibility window and release note.

The Documentation Agent should detect runtime routes missing from OpenAPI, emitted events missing from AsyncAPI, stale enums, stale commands, obsolete screenshots and outdated diagrams.

## 14. Evidence matrix

| Change | Minimum evidence |
|---|---|
| Domain rule | Unit tests |
| Repository or adapter | Integration tests |
| REST contract | OpenAPI and contract tests |
| Event | AsyncAPI and consumer contract |
| UI flow | Screenshot and Playwright |
| Async processing | Job state, logs and integration test |
| Security-sensitive action | Authorization and abuse tests |
| AI behavior | Evaluation report |
| Performance-sensitive path | k6 result |
| Migration | Fresh install and upgrade test |
| Specialized projection | Rebuild and reconciliation |

## 15. Initial ADR backlog

```text
ADR-001 Modular monolith
ADR-002 Shared-schema multi-tenancy
ADR-003 Hexagonal module structure
ADR-004 Transactional outbox
ADR-005 Redis Streams before Kafka
ADR-006 S3-compatible object storage
ADR-007 AI provider port and deterministic fallback
ADR-008 Human approval for sensitive actions
ADR-009 Test isolation
ADR-010 Quality-gate strategy
ADR-011 Observability baseline
ADR-012 Polyglot persistence as rebuildable projections
ADR-013 OpenSearch and pgvector hybrid search
ADR-014 Event sourcing limited to Approval
ADR-015 PostgreSQL append-only ledger
ADR-016 SSE instead of WebSocket
```

## 16. Planning cadence

Per task: read relevant specs and link work to acceptance criteria.

Per pull request: run documentation drift checks and update current status when capability state changes.

Per wave: close verified specs, reclassify deferred work and refresh diagrams.

Before release: verify P0 specs and export OpenAPI, AsyncAPI and evaluation reports.
