# Implementation Plan: Project Adequation Restructure

## Overview

This plan implements the project adequation in 8 sequential phases following the directive **remove → alter → create → test**. Each phase ends with a `./gradlew build` (exit code 0) gate before progressing. Phases are treated as independent commits, enabling trivial rollback via `git revert`.

## Tasks

- [x] 1. Phase 1 — Docs Restructure
  - [x] 1.1 Create thematic subfolders and move documents
    - Create subdirectories: `docs/architecture/`, `docs/specifications/`, `docs/task-plans/`, `docs/runbooks/`, `docs/testing/`, `docs/diagrams/`
    - Preserve `docs/adr/` unchanged
    - Move documents to their target locations preserving content byte-for-byte:
      - `01-PROJECT-SCOPE.md` → `docs/specifications/PROJECT-SCOPE.md`
      - `02-TECHNICAL-SPECIFICATION.md` → `docs/specifications/TECHNICAL-SPECIFICATION.md`
      - `03-SPECIFICATION-PLANNING.md` → `docs/specifications/SPECIFICATION-PLANNING.md`
      - `04-DESIGN-PLANNING.md` → `docs/architecture/DESIGN-PLANNING.md`
      - `05-ARCHITECTURE-DIAGRAMS-C4-MERMAID.md` → `docs/diagrams/ARCHITECTURE-DIAGRAMS-C4-MERMAID.md`
      - `06-TASK-PLAN-P0-FOUNDATION-CORE.md` → `docs/task-plans/TASK-PLAN-P0-FOUNDATION-CORE.md`
      - `07-TASK-PLAN-P1-EXPERIENCE-INTEGRATIONS.md` → `docs/task-plans/TASK-PLAN-P1-EXPERIENCE-INTEGRATIONS.md`
      - `08-TASK-PLAN-P2-SPECIALIZED-DATA.md` → `docs/task-plans/TASK-PLAN-P2-SPECIALIZED-DATA.md`
      - `09-TASK-PLAN-P3-HARDENING-RELEASE.md` → `docs/task-plans/TASK-PLAN-P3-HARDENING-RELEASE.md`
      - `10-HARNESS-LOOP-ENGINEERING-AND-AGENTS.md` → `docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md`
      - `11-DATA-ENTITIES-BY-DATABASE.md` → `docs/architecture/DATA-ENTITIES-BY-DATABASE.md`
      - `12-QUALITY-TESTING-CICD.md` → `docs/testing/QUALITY-TESTING-CICD.md`
      - `13-OPERATIONS-RUNBOOK.md` → `docs/runbooks/OPERATIONS-RUNBOOK.md`
    - Move any remaining `.md` files from `docs/` root to the most thematically appropriate subfolder
    - Ensure `docs/` root contains only `README.md` and the 7 subfolders
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11, 1.13, 1.14, 1.15_

  - [x] 1.2 Create docs/README.md index file
    - Create `docs/README.md` with an index organized by subfolder
    - Each subfolder represented as an H2 heading (`##`)
    - Each document listed as a bullet with a working relative link (e.g., `- [PROJECT-SCOPE](specifications/PROJECT-SCOPE.md)`)
    - Include all documents moved in task 1.1
    - _Requirements: 1.12_

  - [x] 1.3 Write property test for file move content integrity
    - **Property 5: File move preserves content integrity**
    - **Validates: Requirements 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11**
    - Verify SHA-256 hash of each moved file matches expected content integrity
    - Can be implemented as a JUnit test asserting file hashes at destination match originals

- [x] 2. Phase 2 — Remove Deprecated Modules
  - [x] 2.1 Remove deprecated module directories and build references
    - Check that `backend/pipeline/`, `backend/tasks/`, `backend/workflows/` contain no `.java` files beyond `package-info.java`; if they do, abort and report files needing relocation
    - Remove directories: `backend/pipeline/`, `backend/tasks/`, `backend/workflows/`
    - Remove entries `"backend:pipeline"`, `"backend:tasks"`, `"backend:workflows"` from `settings.gradle.kts`
    - Remove `implementation(project(":backend:pipeline"))`, `implementation(project(":backend:tasks"))`, `implementation(project(":backend:workflows"))` from `backend/app-boot/build.gradle.kts`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.8_

  - [x] 2.2 Clean residual references to removed modules
    - Search all `.java` files for imports of `com.atlasops.pipeline`, `com.atlasops.tasks`, `com.atlasops.workflows` and remove them
    - Search all `build.gradle.kts` files for remaining `implementation(project(":backend:pipeline"))`, `implementation(project(":backend:tasks"))`, `implementation(project(":backend:workflows"))` and remove them
    - Update AGENTS.md: remove pipeline, tasks, workflows from the Módulos table and update module count
    - _Requirements: 2.5, 2.7, 2.9_

  - [x] 2.3 Validate build after module removal
    - Run `./gradlew build` and ensure exit code 0
    - Fix any compilation errors caused by residual references (up to 3 attempts)
    - _Requirements: 2.6, 12.2, 12.3_

- [x] 3. Phase 3 — Create New Backend Modules
  - [x] 3.1 Create module scaffolds with hexagonal structure
    - For each new module (`approvals`, `activities`, `notifications`, `integrations`, `search`, `imports`, `operations`):
      - Create directory `backend/{module}/`
      - Create `build.gradle.kts` with `java-library` plugin and `api(project(":backend:shared-kernel"))` dependency
      - Create hexagonal package structure under `src/main/java/com/atlasops/{module}/`:
        - `package-info.java` (root)
        - `domain/package-info.java`
        - `domain/ports/package-info.java`
        - `application/package-info.java`
        - `infrastructure/package-info.java`
        - `presentation/package-info.java`
      - Create mirrored test directories under `src/test/java/com/atlasops/{module}/`
      - If directory already exists, abort creation for that module and emit error
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.12_

  - [x] 3.2 Add port interfaces to each new module
    - Create port interfaces in `domain/ports/` for each module:
      - `approvals`: `ApprovalPort` with `submitApproval(ApprovalRequest): ApprovalResult`
      - `activities`: `ActivityRepository` with `record(ActivityEvent): void`, `findByEntity(String entityId): List<Activity>`
      - `notifications`: `NotificationPort` with `send(Notification): NotificationResult`
      - `integrations`: `IntegrationPort` with `dispatch(WebhookPayload): DispatchResult`
      - `search`: `SearchPort` with `search(SearchQuery): SearchResult`
      - `imports`: `ImportPort` with `startImport(ImportRequest): ImportJob`
      - `operations`: `OperationsPort` with `getSystemHealth(): HealthStatus`
    - _Requirements: 3.11_

  - [x] 3.3 Register new modules in settings.gradle.kts and validate build
    - Add `include()` entries for all 7 new modules in `settings.gradle.kts`
    - Run `./gradlew build` and ensure exit code 0 within 120 seconds
    - Update AGENTS.md: add each new module to the Módulos table with description (≤150 chars)
    - _Requirements: 3.8, 3.9, 3.10_

- [x] 4. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Phase 4 — Worker Process
  - [x] 5.1 Create worker module structure
    - Create `backend/worker/` directory
    - Create `build.gradle.kts` applying plugins `org.springframework.boot` and `io.spring.dependency-management`
    - Declare dependencies: `shared-kernel`, `documents`, `ai`, `notifications`, `imports`
    - Create main class `com.atlasops.worker.WorkerApplication` annotated with `@SpringBootApplication`
    - _Requirements: 4.1, 4.2, 4.3_

  - [x] 5.2 Add worker configuration and register in build
    - Create `src/main/resources/application.yml` with:
      - Redis connection (host, port, consumer group)
      - PostgreSQL connection (url, username, password via env vars)
      - MinIO connection (endpoint, access-key, secret-key via env vars)
      - Database feature flags (opensearch, mongodb, neo4j, timescaledb, clickhouse, eventstoredb)
    - Register `backend:worker` in `settings.gradle.kts`
    - Verify `./gradlew :backend:worker:build` compiles independently
    - Verify `./gradlew build` compiles both app-boot and worker
    - _Requirements: 4.4, 4.5, 4.7_

- [x] 6. Phase 5 — Docker Compose Profiles
  - [x] 6.1 Create Docker Compose with profile definitions
    - Define profile `core`: PostgreSQL (pgvector + PostGIS), Redis, MinIO, backend API, Worker
    - Define profile `advanced`: core + OpenSearch, MongoDB, Neo4j
    - Define profile `analytics`: core + TimescaleDB, ClickHouse
    - Define profile `event-sourcing`: core + EventStoreDB
    - Define profile `observability`: core + Prometheus, Grafana, Loki, Tempo, MailHog
    - Configure `depends_on` with `service_healthy` for non-core profiles depending on core services
    - Add health checks for all services (interval: 10s, timeout: 5s, retries: 12)
    - Include Worker as separate service from backend API on `atlasops-network`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.7, 5.8, 5.9, 5.10, 4.6_

  - [x] 6.2 Create Makefile targets for profiles
    - Add targets: `make compose-core`, `make compose-advanced`, `make compose-analytics`, `make compose-event-sourcing`, `make compose-observability`, `make compose-all`
    - Each target runs `docker compose --profile {name} up -d`
    - _Requirements: 5.6_

- [x] 7. Phase 6 — Frontend Scaffold
  - [x] 7.1 Initialize Next.js 15 project with pnpm workspace
    - Create `frontend/` directory with `package.json` (React 19, Next.js 15, TypeScript)
    - Create `tsconfig.json` with strict mode enabled
    - Create `next.config.ts` for App Router
    - Create `pnpm-workspace.yaml` in monorepo root including `frontend/`
    - Add `packageManager` field to root `package.json`
    - _Requirements: 8.1, 8.4_

  - [x] 7.2 Configure Tailwind CSS v4 and shadcn/ui
    - Create `tailwind.config.ts` with content paths for `app/` and `components/`, extended theme colors (primary, secondary, destructive, muted, accent, card, popover), and responsive breakpoints
    - Create `app/globals.css` with design tokens as CSS variables (--background, --foreground, --primary, etc.) with dark mode support via `.dark` class
    - Create `components.json` for shadcn/ui configuration pointing to `components/ui/`
    - Install base shadcn/ui components: button, input, card, dialog, dropdown-menu, table, badge, toast, tabs, separator, skeleton
    - _Requirements: 8.7, 8.8, 8.12_

  - [x] 7.3 Create admin routes and layout
    - Create `app/admin/layout.tsx` with sidebar, header, and breadcrumbs using shadcn/ui
    - Create `page.tsx` files for all admin routes:
      - `/admin/dashboard`, `/admin/customers`, `/admin/customers/[id]`
      - `/admin/requests`, `/admin/requests/[id]`
      - `/admin/documents`, `/admin/documents/[id]`
      - `/admin/approvals`, `/admin/search`, `/admin/activities`
      - `/admin/operations`, `/admin/integrations`, `/admin/imports`
      - `/admin/audit`, `/admin/settings`
    - Create `/developers/page.tsx`
    - Implement responsive navigation: persistent sidebar ≥1024px, collapsible 768–1023px, overlay <768px
    - _Requirements: 8.2, 8.10, 8.11_

  - [x] 7.4 Create portal routes and layout
    - Create `app/portal/layout.tsx` with simpler navigation using shadcn/ui
    - Create `page.tsx` files for all portal routes:
      - `/portal/home`, `/portal/requests`, `/portal/requests/[id]`
      - `/portal/documents`, `/portal/documents/upload`
      - `/portal/notifications`
    - Implement responsive navigation: bottom nav on mobile, sidebar on desktop
    - _Requirements: 8.3, 8.10, 8.11_

  - [x] 7.5 Create component directory structure
    - Create `components/ui/` (shadcn/ui generated)
    - Create `components/shared/` (layouts, navigation, data-table)
    - Create `components/admin/` (admin-specific)
    - Create `components/portal/` (portal-specific)
    - Create `lib/` (utils, API client)
    - Create `hooks/` (custom hooks)
    - Verify `pnpm --filter frontend build` compiles with zero TypeScript errors
    - _Requirements: 8.5, 8.6, 8.9_

- [x] 8. Phase 6 (continued) — AI Module Adequation
  - [x] 8.1 Create DocumentAnalysisRequest record with validations
    - Create `DocumentAnalysisRequest` record in `backend/ai/src/main/java/com/atlasops/ai/domain/`
    - Fields: `tenantId` (non-blank), `documentId` (non-blank), `extractedText` (non-blank, ≤100,000 chars), `promptVersion` (regex `^[a-zA-Z][a-zA-Z0-9_-]*:v\\d+$`), `outputSchema` (non-blank)
    - Implement compact constructor with all validations throwing `IllegalArgumentException`
    - _Requirements: 6.1_

  - [x] 8.2 Create DocumentAnalysisResult record with validations
    - Create `DocumentAnalysisResult` record in `backend/ai/src/main/java/com/atlasops/ai/domain/`
    - Fields: `summary` (non-blank), `category` (non-blank), `extractedFields` (non-null, immutable `List<KeyValuePair>`), `risks` (non-null, immutable `List<String>`), `missingInformation` (non-null, immutable `List<String>`), `confidenceScore` (0.0–1.0), `providerMetadata` (non-blank), `fallback` (boolean)
    - Create `KeyValuePair` record with `key` (non-blank) and `value` (non-null) validations
    - Wrap all lists with `List.copyOf()` for immutability in compact constructor
    - _Requirements: 6.2_

  - [x] 8.3 Rename AIAnalysisPort to DocumentAnalysisPort
    - Rename interface `AIAnalysisPort` → `DocumentAnalysisPort` in `domain/ports/`
    - Update method signature: `DocumentAnalysisResult analyze(DocumentAnalysisRequest request)`
    - Keep `List<RelevantChunk> searchRelevant(String query, int maxResults, double minScore)`
    - Update all internal references: adapters, use cases, tests
    - Ensure `DocumentIngestionPort` remains unchanged
    - Run `./gradlew :backend:ai:build` and ensure exit code 0 with no warnings
    - _Requirements: 6.3, 6.4, 6.5_

  - [x] 8.4 Write property test: DocumentAnalysisRequest rejects invalid inputs
    - **Property 1: DocumentAnalysisRequest rejects all invalid inputs**
    - **Validates: Requirements 6.1**
    - Use jqwik `@Property(tries = 100)` with `@Tag("Feature: project-adequation-restructure, Property 1: DocumentAnalysisRequest rejects all invalid inputs")`
    - Generate arbitrary invalid inputs: blank tenantId, blank documentId, blank extractedText, extractedText >100,000 chars, invalid promptVersion format, blank outputSchema
    - Assert `IllegalArgumentException` is thrown and record is NOT instantiated

  - [x] 8.5 Write property test: DocumentAnalysisRequest accepts valid inputs
    - **Property 2: DocumentAnalysisRequest accepts all valid inputs**
    - **Validates: Requirements 6.1**
    - Use jqwik `@Property(tries = 100)` with `@Tag("Feature: project-adequation-restructure, Property 2: DocumentAnalysisRequest accepts all valid inputs")`
    - Custom generators: `validPromptVersions()` producing `name:vN` format, `validExtractedTexts()` producing non-blank strings ≤100,000 chars
    - Assert construction succeeds and all field accessors return provided values

  - [x] 8.6 Write property test: DocumentAnalysisResult confidence score bounds
    - **Property 3: DocumentAnalysisResult confidence score bounds**
    - **Validates: Requirements 6.2**
    - Use jqwik `@Property(tries = 100)` with `@Tag("Feature: project-adequation-restructure, Property 3: DocumentAnalysisResult confidence score bounds")`
    - Generate arbitrary doubles outside [0.0, 1.0] and assert `IllegalArgumentException`
    - Generate arbitrary doubles within [0.0, 1.0] with valid other fields and assert successful construction

  - [x] 8.7 Write property test: DocumentAnalysisResult list immutability
    - **Property 4: DocumentAnalysisResult list immutability**
    - **Validates: Requirements 6.2**
    - Use jqwik `@Property(tries = 100)` with `@Tag("Feature: project-adequation-restructure, Property 4: DocumentAnalysisResult list immutability")`
    - Construct valid instances and attempt to mutate `extractedFields()`, `risks()`, `missingInformation()`
    - Assert `UnsupportedOperationException` is thrown on mutation attempts

  - [x] 8.8 Write unit tests for AI module edge cases
    - `should_throwException_when_extractedTextExactly100001Chars()`
    - `should_acceptText_when_extractedTextExactly100000Chars()`
    - `should_throwException_when_promptVersionMissingColon()`
    - `should_throwException_when_confidenceScoreIsNegativeEpsilon()`
    - `should_createImmutableLists_when_mutableListProvided()`
    - _Requirements: 6.1, 6.2, 6.6_

- [x] 9. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Phase 7 — Gap Analysis Documents
  - [x] 10.1 Create ADEQUATION-MODULES.md
    - Create `docs/specifications/ADEQUATION-MODULES.md`
    - Include table with 5 columns: Aspecto, Estado Atual, Estado Alvo, Ação Necessária, Prioridade
    - Cover: modules to remove (pipeline, tasks, workflows), modules to create (approvals, activities, notifications, integrations, search, imports, operations), modules to refactor (ai, documents, requests)
    - Reference current state from `backend/` and spec `monorepo-sdd-harness`, target from PROJECT-SCOPE and TECHNICAL-SPECIFICATION
    - Classify each row with priority: P0, P1, P2, or P3
    - _Requirements: 10.1, 10.7, 10.8_

  - [x] 10.2 Create ADEQUATION-INFRASTRUCTURE.md
    - Create `docs/specifications/ADEQUATION-INFRASTRUCTURE.md`
    - Include entries for: each new Docker Compose service, each profile (core, advanced, analytics, event-sourcing, observability), Worker_Process, each new Make command, each new environment variable
    - Use 5-column format with priority classification
    - _Requirements: 10.2, 10.7, 10.8_

  - [x] 10.3 Create ADEQUATION-PERSISTENCE.md
    - Create `docs/specifications/ADEQUATION-PERSISTENCE.md`
    - Include entries for: OpenSearch, MongoDB, Neo4j, TimescaleDB, ClickHouse, DuckDB, EventStoreDB
    - For each: adapter needed, consuming module, fallback/degradation behavior, projection lifecycle
    - Use 5-column format with priority classification
    - _Requirements: 10.3, 10.7, 10.8_

  - [x] 10.4 Create ADEQUATION-API-EVENTS.md
    - Create `docs/specifications/ADEQUATION-API-EVENTS.md`
    - Include entries for: each new REST contract, each new domain event, each SSE stream, each webhook outbound, MCP integration
    - Reference Target_Vision as "Estado Alvo"
    - Use 5-column format with priority classification
    - _Requirements: 10.4, 10.7, 10.8_

  - [x] 10.5 Create ADEQUATION-FRONTEND.md
    - Create `docs/specifications/ADEQUATION-FRONTEND.md`
    - Include entries for: each admin page, each portal page, shared infrastructure components (upload manager, notification center, command palette, document preview, design system)
    - Use 5-column format with priority classification
    - _Requirements: 10.5, 10.7, 10.8_

  - [x] 10.6 Create ADEQUATION-TESTING.md
    - Create `docs/specifications/ADEQUATION-TESTING.md`
    - Include entries for: Playwright configuration, each functional end-to-end scenario, k6 configuration, each load scenario (smoke, average, stress), seed scripts
    - Use 5-column format with priority classification
    - _Requirements: 10.6, 10.7, 10.8_

  - [x] 10.7 Verify gap analysis document completeness
    - Ensure every module, service, and component from Target_Vision appears in at least one document
    - No orphaned items between documents
    - Blocked by: 10.1, 10.2, 10.4
    - _Requirements: 10.9_

- [x] 11. Phase 8 — Test Infrastructure Scaffolding
  - [x] 11.1 Create Playwright configuration and functional test structure
    - Create `tests/functional/` directory
    - Create Playwright config with chromium project, local base URL, 30s global timeout
    - Configure screenshot, video, and trace capture on failure (output to `tests/functional/test-results/`)
    - _Requirements: 9.1, 9.6_

  - [x] 11.2 Create k6 load test scripts
    - Create `tests/load/` directory
    - Create at least one executable k6 script with configurable stages (VUs and duration)
    - Smoke scenario: max 5 VUs, max 60s against local API port via env variable
    - _Requirements: 9.2, 9.5_

  - [x] 11.3 Create Makefile targets for test infrastructure
    - Add targets: `make test-functional`, `make test-functional-headed`, `make test-functional-report`, `make test-load-smoke`, `make test-load`, `make test-load-report`
    - Ensure non-zero exit code on test failure
    - Report generation: HTML in `tests/functional/playwright-report/` and `tests/load/reports/`
    - _Requirements: 9.3, 9.4, 9.7_

- [x] 12. Phase 8 — Update AGENTS.md and Steering Files
  - [x] 12.1 Update AGENTS.md with new project structure
    - Update Módulos table: remove pipeline/tasks/workflows, add all new modules (approvals, activities, notifications, integrations, search, imports, operations)
    - Add Worker Process section (level 3 heading after Módulos): purpose, responsibilities (≥5 items), execution command, relation to backend API
    - Add new Make commands to the commands table: `make compose-core`, `make compose-advanced`, `make compose-analytics`, `make compose-event-sourcing`, `make compose-observability`, `make compose-all`, `make test-functional`, `make test-load-smoke`, `make test-load`
    - _Requirements: 11.1, 11.2, 11.3_

  - [x] 12.2 Update existing steering files
    - Update `module-structure.md`: replace references to pipeline/tasks/workflows with new modules
    - Update `git-conventions.md`: add scopes (approvals, activities, notifications, integrations, search, imports, operations, worker), remove scopes (pipeline, tasks, workflows)
    - _Requirements: 11.4, 11.5_

  - [x] 12.3 Create frontend-conventions.md steering file
    - Create `.kiro/steering/frontend-conventions.md` with sections:
      - Stack: React 19, Next.js 15 (App Router), TypeScript strict, Tailwind CSS v4, shadcn/ui
      - Directory structure: `app/`, `components/ui/`, `components/shared/`, `components/admin/`, `components/portal/`, `lib/`, `hooks/`
      - Naming: PascalCase components, kebab-case files, `use` prefix hooks, UPPER_SNAKE_CASE constants
      - Styling: Tailwind utility classes only, no CSS modules/styled-components, `cn()` for conditional classes, CSS variables for design tokens
      - Components: shadcn/ui base first, composition over inheritance, typed props, avoid prop drilling
      - Responsiveness: mobile-first, Tailwind breakpoints, adaptive layouts, portal fully usable on mobile
      - Accessibility: Radix UI native a11y, mandatory form labels, WCAG AA contrast, keyboard navigation, aria-labels
      - Linting: ESLint + Prettier + tailwindcss plugin
      - Prohibitions: no inline `style={}`, no `!important`, no untyped components, no `any`, no business logic in UI
    - _Requirements: 11.6_

- [x] 13. Final Checkpoint — Validate complete adequation
  - Run `./gradlew build` and ensure exit code 0
  - Run `make test-unit` and ensure all existing tests pass
  - Verify AGENTS.md lists all modules present in `settings.gradle.kts` and references no non-existent modules
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 12.2, 12.5, 12.6_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation between major phases
- Property tests validate universal correctness properties for DocumentAnalysisRequest and DocumentAnalysisResult records
- Unit tests validate specific examples and edge cases
- Each phase is an independent commit following Conventional Commits conventions
- If any phase fails build gate after 3 correction attempts, revert via `git revert` and report

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3"] },
    { "id": 2, "tasks": ["2.1"] },
    { "id": 3, "tasks": ["2.2"] },
    { "id": 4, "tasks": ["2.3"] },
    { "id": 5, "tasks": ["3.1"] },
    { "id": 6, "tasks": ["3.2"] },
    { "id": 7, "tasks": ["3.3"] },
    { "id": 8, "tasks": ["5.1"] },
    { "id": 9, "tasks": ["5.2"] },
    { "id": 10, "tasks": ["6.1", "7.1"] },
    { "id": 11, "tasks": ["6.2", "7.2"] },
    { "id": 12, "tasks": ["7.3", "7.4", "8.1", "8.2"] },
    { "id": 13, "tasks": ["7.5", "8.3"] },
    { "id": 14, "tasks": ["8.4", "8.5", "8.6", "8.7", "8.8"] },
    { "id": 15, "tasks": ["10.1", "10.2", "10.3", "10.4", "10.5", "10.6"] },
    { "id": 16, "tasks": ["10.7", "11.1", "11.2"] },
    { "id": 17, "tasks": ["11.3"] },
    { "id": 18, "tasks": ["12.1", "12.2", "12.3"] }
  ]
}
```
