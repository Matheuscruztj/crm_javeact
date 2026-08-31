# AtlasOps AI — Harness Engineering, Loop Engineering and Agents

## 1. Purpose

Harness engineering creates the environment, constraints, commands, context and feedback mechanisms that allow humans and agents to modify the repository safely.

Loop engineering defines repeatable cycles:

```text
understand
→ plan
→ implement
→ verify
→ evaluate
→ document
→ improve
```

Agents do not replace architecture, security, testing or human accountability. They operate inside the harness.

---

## 2. Harness components

### Context

Required files:

```text
README.md
AGENTS.md
.env.example
Makefile or Taskfile
docker-compose.yml
docs/00-current-status.md
backend/AGENTS.md
frontend/AGENTS.md
tests/AGENTS.md
docs/AGENTS.md
docs/specifications/
docs/architecture/
docs/testing/
docs/runbooks/
docs/adr/
```

Agent-local instructions are discovered progressively: read the root `AGENTS.md` first, then the nearest directory-level `AGENTS.md` that matches the work area. Local files must narrow scope, not redefine global rules.

### Stable commands

```bash
make doctor
make bootstrap
make verify
make verify-full
make test-unit
make test-integration
make test-contract
make test-functional
make test-load-smoke
make compose-core
make compose-all
make seed-tests
make reset
```

### Feedback

- format;
- lint;
- typecheck or compile;
- unit;
- integration;
- contract;
- functional;
- security;
- performance;
- architecture;
- coverage;
- mutation;
- AI evaluation;
- documentation drift.

### Environment

- Docker Compose profiles;
- worktrees;
- isolated schemas or databases;
- Redis namespaces;
- object-storage prefixes;
- stream consumer suffixes;
- fake external services;
- restricted credentials.

### Controls

- branch protection;
- CODEOWNERS;
- PR template;
- approval rules;
- generated-file policy;
- destructive-command policy;
- secret policy;
- tool allowlist;
- migration review.

---

## 3. Agent roles

### A1 — Orchestrator

Responsibilities:

- select agent sequence;
- prevent conflicting edits;
- enforce the task contract;
- aggregate evidence;
- stop when scope expands unexpectedly.

The Orchestrator cannot merge or access production secrets.

### A2 — Planner Agent

Responsibilities:

- read project context;
- decompose work;
- identify dependencies;
- identify parallel work;
- propose acceptance criteria;
- identify modules and files;
- propose tests and validation commands.

Output:

```text
objective
assumptions
out of scope
work breakdown
dependency graph
risks
test plan
evidence plan
```

The Planner does not edit code.

### A3 — Specification Agent

Responsibilities:

- create or update capability specifications;
- update API and event contracts;
- identify missing decisions;
- check consistency with scope;
- propose ADRs.

### A4 — Design Agent

Responsibilities:

- map user journeys;
- define states;
- identify components;
- define accessibility;
- align UI behavior with API and error contracts.

### A5 — Implementer Agent

Responsibilities:

- implement one bounded task;
- preserve module boundaries;
- add or update tests;
- update contracts through approved generation commands;
- run focused verification;
- report changes and risks.

The Implementer cannot silently broaden scope.

### A6 — Test Engineer Agent

Responsibilities:

- review acceptance criteria;
- create unit, integration, contract and E2E scenarios;
- test concurrency and idempotency;
- validate tenant isolation;
- investigate false positives;
- validate test isolation;
- run mutation testing when requested.

### A7 — Reviewer Agent

Responsibilities:

- correctness;
- readability;
- backward compatibility;
- migrations;
- contracts;
- observability;
- missing negative cases;
- evidence completeness.

The Reviewer should not be the primary author of the change.

### A8 — Security Agent

Responsibilities:

- threat model;
- authorization matrix;
- cross-tenant review;
- upload security;
- SSRF;
- webhook replay;
- secrets;
- dependency risk;
- prompt injection;
- MCP tool abuse;
- data exposure.

### A9 — Architecture Agent

Responsibilities:

- module boundaries;
- dependency cycles;
- ports and adapters;
- data ownership;
- projection lifecycle;
- event consistency;
- ADR need;
- stack-independent command consistency.

### A10 — Database and Migration Agent

Responsibilities:

- schema;
- constraints;
- indexes;
- migration order;
- upgrade;
- forward-fix or rollback;
- volume and lock risk;
- tenant key;
- data backfill;
- projection rebuild.

### A11 — Documentation Agent

Responsibilities:

- README;
- current status;
- OpenAPI;
- AsyncAPI;
- ADRs;
- diagrams;
- examples;
- runbooks;
- stale-document detection.

### A12 — SRE and Observability Agent

Responsibilities:

- logs;
- metrics;
- traces;
- health;
- readiness;
- alerts;
- SLO proposals;
- failure injection;
- recovery runbooks;
- release evidence.

### A13 — AI Evaluation Agent

Responsibilities:

- evaluation dataset;
- schema checks;
- prompt regression;
- fallback validation;
- safety cases;
- LLM-as-a-judge;
- baseline comparison;
- drift detection;
- release recommendation.

### A14 — Quality Janitor

Periodic responsibilities:

- dead code;
- warnings;
- stale TODOs;
- flaky tests;
- slow tests;
- outdated dependencies;
- duplication;
- stale docs;
- unowned modules;
- small maintenance pull requests.

---

## 4. Agent permission model

| Action | Planner | Spec | Implementer | Test | Reviewer | Security | Janitor |
|---|---:|---:|---:|---:|---:|---:|---:|
| Read repository | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Edit documentation | No | Yes | Limited | Limited | No | Suggest | Yes |
| Edit product code | No | No | Yes | Test code | No | Limited | Limited |
| Create migration | No | No | Yes | No | No | No | No |
| Execute Docker locally | Yes | Yes | Yes | Yes | Yes | Yes | Yes |
| Access secrets | No | No | No | No | No | No | No |
| Merge | No | No | No | No | No | No | No |
| Destructive command | No | No | Sandbox | Sandbox | No | No | Sandbox |

Human review is mandatory for destructive migrations, security policies, production deployment, secret rotation, releases and agent-permission changes.

---

## 5. Task contract

```markdown
## Objective
## Context
## Out of scope
## Acceptance criteria
## Modules and interfaces affected
## Data stores affected
## Security risks
## Required tests
## Validation commands
## Expected evidence
## Maximum change boundary
```

Bad:

```text
Implement the document platform.
```

Good:

```text
Implement ConfirmDocumentUpload:
- validate authenticated tenant;
- require PENDING_UPLOAD or UPLOADING;
- complete multipart storage upload;
- persist checksum and size;
- transition to UPLOADED;
- add document.uploaded.v1 to outbox;
- make duplicate confirmation idempotent;
- add unit and MinIO integration tests;
- do not implement the worker.
```

---

## 6. Main engineering loop

```text
Read current status and specifications
→ restate objective and constraints
→ create a small plan
→ implement the smallest coherent change
→ run cheap focused feedback
→ inspect failures
→ correct
→ run broader feedback
→ review risks and contracts
→ update documentation
→ produce evidence
```

Large tasks must be split instead of making one loop excessively broad.

---

## 7. Progressive verification loop

```text
format
→ lint
→ typecheck or compile
→ focused unit tests
→ focused integration tests
→ build
→ contract tests
→ architecture checks
→ functional smoke
→ full verify
```

Do not run full browser and load suites after every small edit. Run them at integration points and wave gates.

---

## 8. Documentation loop

```text
Scope or behavior changes
→ update specification
→ update OpenAPI or AsyncAPI
→ update diagram or ADR
→ update task plan
→ implement
→ attach evidence
→ mark VERIFIED
→ update current status
```

The Documentation Agent compares documentation with:

- runtime routes;
- OpenAPI;
- event definitions;
- migrations;
- commands;
- feature flags;
- current status.

---

## 9. Debugging loop

```text
Reproduce
→ collect correlation ID
→ inspect logs and trace
→ identify failing boundary
→ create smallest failing test
→ fix
→ rerun focused test
→ rerun affected integration
→ update runbook when useful
```

Agents must not hide symptoms through arbitrary sleeps, unlimited retries, disabled assertions, skipped tests or ignored warnings.

---

## 10. AI evaluation loop

```text
Curate dataset
→ execute candidate
→ validate schema
→ run deterministic checks
→ run LLM-as-a-judge
→ compare with baseline
→ sample human review
→ classify regression
→ approve, revise or reject
```

### Evaluation dataset

Each case includes:

```text
case_id
tenant-safe input fixture
expected structural fields
expected facts or constraints
prohibited claims
required citations
risk category
```

### Deterministic checks

- valid schema;
- required fields;
- no prohibited fields;
- citation targets exist;
- confidence range;
- fallback flag;
- no tenant leakage.

### LLM-as-a-judge output

```json
{
  "scores": {
    "correctness": 0,
    "completeness": 0,
    "grounding": 0,
    "safety": 0,
    "format": 0
  },
  "criticalFailure": false,
  "reasons": [],
  "uncertainty": 0
}
```

Rules:

- explicit fixed rubric;
- structured output;
- versioned judge prompt;
- model/version retained;
- baseline comparison;
- human review for low confidence or disagreement;
- judge score is never the only critical-release signal;
- no cross-tenant evaluation data.

---

## 11. Evaluation gates

Initial examples:

```text
invalid_schema_rate = 0
required_fields_completion >= 0.95
grounded_answer_rate >= 0.90
critical_safety_failures = 0
fallback_success_rate = 1.00
p95_latency <= configured budget
```

An exception records failed dimension, user impact, owner, mitigation, expiration and issue.

---

## 12. Sandboxes

Each task or parallel agent receives:

```text
RUN_ID
branch or worktree
Docker Compose project
database or schema
Redis prefix
bucket prefix
stream consumer suffix
search-index suffix
test tenant
```

Example:

```text
run_id = pr-123-document-confirm
database = atlasops_pr_123
redis_prefix = test:pr-123:
bucket_prefix = pr-123/
search_index = documents-pr-123
```

Agents cannot share mutable test data unless coordination is the subject under test.

---

## 13. Required evidence

Each implementation task returns:

- summary;
- files changed;
- decisions;
- migrations;
- contracts changed;
- tests added;
- commands run;
- actual results;
- screenshots for UI;
- logs or correlation IDs for async behavior;
- performance or AI evaluation results when relevant;
- limitations;
- remaining risks.

“It should work” is not evidence.

---

## 14. Orchestration patterns

### Normal feature

```text
Specification Agent
→ Planner
→ Human scope review
→ Implementer
→ Test Engineer
→ Reviewer
→ Security or Architecture Agent
→ CI
→ Documentation Agent
→ Human merge
```

### Small bug

```text
Implementer
→ focused test
→ Reviewer
→ CI
→ merge
```

### AI change

```text
Specification Agent
→ Implementer
→ AI Evaluation Agent
→ Security Agent
→ Human sample review
→ CI
```

### Database migration

```text
Planner
→ Database Agent
→ Implementer
→ Integration tests
→ Reviewer
→ Human migration approval
```

---

## 15. Periodic loops

### Per pull request

- architecture scan;
- test-gap review;
- risk-based security review;
- docs check;
- changed-code coverage.

### Daily or frequent

- dependency report;
- flaky report;
- vulnerability report;
- projection lag;
- stale current status.

### Weekly

- prompt regression;
- slow-test review;
- dead code;
- log quality;
- restore smoke;
- ledger verification;
- janitor pull requests.

---

## 16. Product-agent guardrails

The AtlasOps product agent is distinct from engineering agents.

It:

- has no raw database credentials;
- uses authorized backend tools;
- inherits tenant and actor;
- cannot override authorization;
- requires approval for mutable actions;
- has step, token and timeout limits;
- logs tool calls;
- does not expose system prompts;
- cannot access unauthorized documents;
- cannot retain cross-tenant memory.

---

## 17. Anti-patterns

- unrestricted permissions;
- huge prompt instead of repository context;
- broad task without acceptance criteria;
- local commands different from CI;
- self-approval of critical changes;
- many agents editing the same files;
- generated contracts without validation;
- automatic merge of risky migrations;
- arbitrary sleeps;
- infinite retry;
- hidden flaky tests;
- LLM judge as the only oracle.
