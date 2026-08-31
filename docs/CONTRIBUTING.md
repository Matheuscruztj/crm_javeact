# Contributing to AtlasOps AI

> **Versão:** 1.0 | **Atualizado:** 2026-07-19

Welcome! This document covers the contribution workflow, standards, and tooling you need to work on AtlasOps AI.

---

## Quick Start

```bash
# 1. Clone and bootstrap the full environment
git clone <repo-url> && cd atlasops-ai
cp .env.example .env
make bootstrap          # Java 21+, Docker 24+ required

# 2. Run all quality gates before submitting a PR
make verify             # format-check → lint → compile → test → spotbugs → build

# 3. Check health
make doctor
```

---

## Branch and Commit Conventions

Follow the conventions in [docs/codex/steering/git-conventions.md](../docs/codex/steering/git-conventions.md).

**Branch naming:**

```
feature/ATLAS-42-customer-bulk-import
fix/ATLAS-128-null-text-ingestion
docs/ATLAS-50-update-agents-md
```

**Commit format (Conventional Commits):**

```
feat(customers): add pagination to list endpoint
fix(ai): handle null text in document ingestion
test(shared-kernel): add PBT for correlation ID generation
```

---

## Development Workflow

### Backend (Java 21 / Spring Boot)

All modules follow hexagonal architecture. See [docs/codex/steering/module-structure.md](../docs/codex/steering/module-structure.md).

```bash
# Compile a single module
./gradlew :backend:customers:compileJava

# Run unit tests for a module
./gradlew :backend:customers:test

# Run all unit tests
make test-unit

# Run integration tests (requires Docker)
make compose-up
make test-integration
```

**Code quality** (`make verify` runs all these):

- Spotless (formatting): `./gradlew spotlessCheck` / `./gradlew spotlessApply`
- Checkstyle: `./gradlew checkstyleMain`
- SpotBugs: `./gradlew spotbugsMain`
- ArchUnit: `./gradlew test --tests "*ArchitectureTest" --tests "*ArchRulesTest"`
- Fast local backend path: `make verify-local-fast`
- Fast local frontend path: `make verify-frontend-fast`
- Pre-commit baseline: `make verify-precommit`
- Pre-push baseline: `make verify-prepush`
- Jacoco coverage: `./gradlew jacocoTestReport`
- Local script wrappers: `./scripts/quality/local-fast.sh`, `./scripts/quality/pre-commit.sh`, `./scripts/quality/pre-push.sh`
- Git hook installer: `make install-git-hooks`
- Module-local backend diagnosis: `./scripts/quality/backend-module.sh customers`

### Frontend (Next.js 15 / React 19)

```bash
cd frontend
pnpm install
pnpm dev          # start dev server
pnpm format:check # Prettier formatting gate
pnpm lint         # ESLint
pnpm typecheck    # TypeScript strict check
pnpm verify:fast  # Formatting + lint + typecheck
pnpm build        # production build
pnpm test:e2e     # Playwright E2E (requires running app)
```

---

## Testing Standards

See [docs/codex/steering/testing-patterns.md](../docs/codex/steering/testing-patterns.md).

| Type        | Framework         | Naming                         | Min Coverage |
| ----------- | ----------------- | ------------------------------ | ------------ |
| Unit        | JUnit 5 + Mockito | `should_result_when_condition` | 75% lines    |
| Property    | jqwik             | `should_alwaysX_forAnyY`       | —            |
| Integration | Testcontainers    | `@Tag("integration")`          | —            |
| E2E         | Playwright        | `describe` + `test`            | —            |

**Key rules:**

- Never mock domain entities — test them directly
- Use `testfixtures/` Builders for complex objects
- Clock and IdGenerator must be injected (deterministic in tests)
- Integration tests extend `AbstractIntegrationTest` (in app-boot)

---

## Database Migrations

```bash
# Create a new migration
touch backend/app-boot/src/main/resources/db/migration/V$(date +%Y%m%d%H%M)__your_description.sql

# Apply migrations
make migrate

# Reset and re-apply from scratch
make compose-reset
```

Migration files are **immutable once deployed**. Never modify existing `V*.sql` files.

---

## Architecture Decisions

ADRs live in `docs/adr/`. To propose a new decision:

```bash
# Find next ADR number
ls docs/adr/ | sort | tail -1

# Create from template
cp docs/adr/ADR-001-template.md docs/adr/ADR-XXX-your-title.md
```

---

## Protected Files

Files that require explicit maintainer review before merging:

| File / Path                                               | Reason               |
| --------------------------------------------------------- | -------------------- |
| `AGENTS.md`                                               | Governance document  |
| `.env.production`                                         | Production secrets   |
| `backend/*/src/main/resources/db/migration/V*` (deployed) | Immutable migrations |
| `backend/app-boot/src/main/java/*/SecurityConfig.java`    | Auth/authz           |
| `settings.gradle.kts`                                     | Monorepo structure   |

---

## PR Checklist

```markdown
- [ ] Code follows steering file conventions
- [ ] Unit tests written and passing (`make test-unit`)
- [ ] Property tests where applicable (jqwik)
- [ ] `make verify` passes without errors
- [ ] No new compilation warnings
- [ ] Docs updated if public interfaces changed
- [ ] PR linked to spec/task (`Spec: feature-name`, `Task: N`)
```

---

## Need Help?

- Review the [AGENTS.md](../AGENTS.md) for agent roles and responsibilities
- Check [docs/runbooks/](runbooks/) for operational procedures
- Run `make doctor` to diagnose environment issues
