# AtlasOps AI — Issue Breakdown for Quality Enforcement Waves

> **Created on:** 2026-07-27
> **Source roadmap:** [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md)
> **Execution model:** parallel tracks for `Backend Agent`, `Frontend Agent` and `Quality Agent`

---

## 1. Objective

This document converts the quality enforcement roadmap into numbered issue groups that can be tracked independently and executed in parallel.

Each issue group maps to one wave and one or more task plans under [`docs/task-plans/`](./task-plans/).

---

## 2. Issue Group Overview

| Issue Group | Theme | Primary Task Plan |
| --- | --- | --- |
| QE-001 to QE-099 | W1 Baseline Enforcement | Roadmap + STATUS |
| QE-100 to QE-199 | W2 Structural Decoupling | Roadmap + STATUS |
| QE-200 to QE-299 | W3 API and Contract Integrity | Roadmap + STATUS |
| QE-300 to QE-399 | W4 Security and Supply Chain | Roadmap + STATUS |
| QE-400 to QE-499 | W5 Runtime Resilience and Dynamic Coupling | Roadmap + STATUS |
| QE-500 to QE-599 | W6 Performance and Operational Evidence | Roadmap + STATUS |
| QE-600 to QE-699 | W7 Governance and Continuous Hardening | Roadmap + STATUS |

---

## 3. Issue Backlog

## 3.1 Wave W1 — Baseline Enforcement

- `QE-001` Inventory existing quality gates and classify them as blocking or advisory. [done]
- `QE-002` Remove CI paths that pass without executing critical backend validation. [done]
- `QE-003` Remove CI paths that pass without executing critical frontend validation. [done]
- `QE-004` Create fast local verification targets for backend, frontend and shared quality checks. [done]
- `QE-005` Standardize artifacts for tests, coverage, lint and security outputs. [in progress]
- `QE-006` Document expected execution time and purpose of each local and CI check. [in progress]
- `QE-007` Add explicit frontend formatting check to local and CI flows. [done]
- `QE-008` Define pre-commit vs pre-push vs CI scope boundaries. [done]
- `QE-009` Align Makefile commands with CI jobs to avoid drift. [done]
- `QE-010` Publish baseline enforcement matrix for developer onboarding. [done]

## 3.2 Wave W2 — Structural Decoupling

- `QE-100` Add backend inheritance rules to ArchUnit.
- `QE-101` Add backend annotation rules to ArchUnit.
- `QE-102` Add backend anti-framework rules for the domain layer.
- `QE-103` Add backend inter-module decoupling rules.
- `QE-104` Add backend anti-cycle and dependency visibility refinements.
- `QE-105` Introduce frontend architectural boundary tooling.
- `QE-106` Define frontend directory and dependency boundaries.
- `QE-107` Add frontend circular dependency detection.
- `QE-108` Add frontend admin vs portal separation rules.
- `QE-109` Add dedicated CI jobs for backend and frontend architecture.

## 3.3 Wave W3 — API and Contract Integrity

- `QE-200` Define a reproducible OpenAPI generation/export process.
- `QE-201` Make OpenAPI lint operate on a real generated or exported spec.
- `QE-202` Fix AsyncAPI lint to use the correct ruleset and validation model.
- `QE-203` Add schema presence checks for critical endpoints.
- `QE-204` Evaluate and choose contract testing strategy for integrations.
- `QE-205` Introduce API breaking change detection.
- `QE-206` Align frontend types and API client with the authoritative contract.
- `QE-207` Publish contract artifacts from CI.
- `QE-208` Define rollout policy for contract-breaking changes.
- `QE-209` Add compatibility smoke coverage for the most critical frontend flows.

## 3.4 Wave W4 — Security and Supply Chain

- `QE-300` Add CodeQL to GitHub Actions.
- `QE-301` Add Semgrep to GitHub Actions.
- `QE-302` Harden dependency scanning and suppression governance.
- `QE-303` Add Trivy filesystem and image scanning.
- `QE-304` Add Syft SBOM generation and publication.
- `QE-305` Add dependency update automation policy.
- `QE-306` Define zero-day response workflow and emergency override process.
- `QE-307` Review backend sensitive sinks and security hotspots.
- `QE-308` Standardize security artifact publication and retention.
- `QE-309` Define blocking thresholds for security findings by severity.

## 3.5 Wave W5 — Runtime Resilience and Dynamic Coupling

- `QE-400` Introduce fault injection with Toxiproxy and Testcontainers. [done]
- `QE-401` Automate dependency outage scenarios for core services. [done]
- `QE-402` Validate circuit breaker, timeout and fallback behavior. [done]
- `QE-403` Validate retry, DLQ and idempotency behavior under failure. [done]
- `QE-404` Separate resilience suites from standard integration suites where needed. [done]
- `QE-405` Add resilience reporting and CI routing. [done]
- `QE-406` Define nightly vs PR scope for resilience scenarios. [done]
- `QE-407` Document operator-facing degradation expectations. [done]
- `QE-408` Correlate resilience failures with observability metrics. [done]
- `QE-409` Add explicit dynamic coupling evidence to release readiness. [in progress]

### 3.5.1 Current implementation evidence

- `backend/app-boot/src/main/java/com/atlasops/boot/config/ResilienceConfig.java` registers circuit breaker metrics with Micrometer.
- `backend/app-boot/src/test/java/com/atlasops/boot/config/ResilienceConfigResilienceTest.java` covers the resilience baseline already in code.
- `backend/app-boot/build.gradle.kts` defines a dedicated `resilienceTest` task.
- `docs/runbooks/RESILIENCE-TESTING.md` documents the manual failure matrix and expected degradation behavior.

### 3.5.2 Remaining gaps

- Toxiproxy/Testcontainers fault injection is visible for MinIO and Ollama fallback validation.
- Broader integration outage suites for PostgreSQL, Redis and OpenSearch can still be expanded.
- Resilience artifact/report routing is documented, but CI routing is still limited to the existing quality workflows.

## 3.6 Wave W6 — Performance and Operational Evidence

- `QE-500` Normalize k6 scenario layout and ownership. [done]
- `QE-501` Fix CI to execute real smoke load tests from the repository. [done]
- `QE-502` Define baseline performance scenarios and thresholds. [done]
- `QE-503` Publish performance artifacts per execution. [done]
- `QE-504` Correlate backend metrics with load test scenarios. [done]
- `QE-505` Add frontend critical-path performance evidence where relevant. [done]
- `QE-506` Define nightly stress and soak strategy. [done]
- `QE-507` Establish regression review process for performance drift. [in progress]
- `QE-508` Integrate performance evidence into release documentation. [done]
- `QE-509` Define environment metadata requirements for test comparability. [done]

### 3.6.1 Current implementation evidence

- `tests/load/smoke.js`, `tests/load/average.js`, `tests/load/stress.js` and `tests/load/five-users.js` already provide the scenario layout.
- `Makefile` exposes `test-load-smoke`, `test-load-5vu`, `test-load`, `test-load-report`, `test-load-5vu-report` and `test-load-stress`.
- `.github/workflows/ci.yml` runs the smoke scenario on push to `main` and uploads a JSON artifact.
- `.github/workflows/nightly.yml` runs the average load scenario and uploads JSON + HTML artifacts.
- `tests/load/generate-report.mjs` renders a lightweight HTML performance report from k6 summary output.

### 3.6.2 Remaining gaps

- Release-readiness policy for performance artifacts is still not automated end-to-end.

## 3.7 Wave W7 — Governance and Continuous Hardening

- `QE-600` Create a quality check ownership matrix.
- `QE-601` Create an exception process with expiration and review.
- `QE-602` Define severity-based remediation SLA.
- `QE-603` Review suppressions and bypasses on a fixed cadence.
- `QE-604` Create a quality dashboard and reporting view.
- `QE-605` Define rules for promoting advisory checks to blocking.
- `QE-606` Add roadmap maintenance and wave completion bookkeeping.
- `QE-607` Align quality governance with agent execution boundaries.
- `QE-608` Define evidence requirements for completion of each wave.
- `QE-609` Close the program with a consolidated quality posture review.

---

## 4. Parallelization by Agent

### Backend Agent

Primary issue ranges:

- `QE-001` to `QE-005`
- `QE-100` to `QE-104`
- `QE-200` to `QE-204`
- `QE-307`
- `QE-400` to `QE-404`
- `QE-504`

### Frontend Agent

Primary issue ranges:

- `QE-003`
- `QE-007`
- `QE-105` to `QE-108`
- `QE-206`
- `QE-209`
- `QE-505`

### Quality Agent

Primary issue ranges:

- `QE-001` to `QE-010`
- `QE-109`
- `QE-201` to `QE-209`
- `QE-300` to `QE-309`
- `QE-405` to `QE-409`
- `QE-500` to `QE-509`
- `QE-600` to `QE-609`

---

## 5. Recommended Execution Order

1. `QE-001` to `QE-010`
2. `QE-100` to `QE-109`
3. `QE-200` to `QE-209`
4. `QE-300` to `QE-309`
5. `QE-400` to `QE-409`
6. `QE-500` to `QE-509`
7. `QE-600` to `QE-609`

---

## 6. Related Documents

| Document | Purpose |
| --- | --- |
| [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md) | strategic roadmap |
| [STATUS.md](./STATUS.md) | current execution status |
