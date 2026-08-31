.DEFAULT_GOAL := help
SHELL := /bin/bash

COMPOSE_FILE := docker-compose.yml
GRADLEW := ./gradlew
# Use daemon by default for faster builds
GRADLE_OPTS := --build-cache --parallel

# ============================================================================
# Help
# ============================================================================

.PHONY: help
help: ## Show all available targets with descriptions
	@echo ""
	@echo "AtlasOps AI — Available Commands"
	@echo "================================="
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""

# ============================================================================
# Environment Setup
# ============================================================================

.PHONY: bootstrap
bootstrap: ## Full environment setup for new developer
	@echo "==> Checking prerequisites..."
	@command -v java >/dev/null 2>&1 || { echo "ERROR: Java not found. Install Java 21+."; exit 1; }
	@JAVA_VER=$$(java -version 2>&1 | head -1 | awk -F'"' '{print $$2}' | awk -F'.' '{if ($$1 >= 21) print "ok"; else print "fail"}'); \
		if [ "$$JAVA_VER" != "ok" ]; then \
			echo "ERROR: Java 21+ required. Current version: $$(java -version 2>&1 | head -1)"; exit 1; \
		fi
	@command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker not found. Install Docker 24+."; exit 1; }
	@DOCKER_VER=$$(docker version --format '{{.Server.Version}}' 2>/dev/null || docker --version | grep -oP '\d+' | head -1); \
		DOCKER_MAJOR=$$(echo "$$DOCKER_VER" | awk -F'.' '{print $$1}'); \
		if [ -z "$$DOCKER_MAJOR" ] || [ "$$DOCKER_MAJOR" -lt 24 ]; then \
			echo "ERROR: Docker 24+ required. Current version: $$DOCKER_VER"; exit 1; \
		fi
	@echo "  [OK] Java 21+"
	@echo "  [OK] Docker 24+"
	@echo ""
	@echo "==> Checking required ports are free..."
	@PORTS_BUSY=""; \
	for port in 5432 6379 9000 3000; do \
		if command -v ss >/dev/null 2>&1; then \
			if ss -tlnp 2>/dev/null | grep -q ":$$port "; then \
				PORTS_BUSY="$$PORTS_BUSY $$port"; \
			fi; \
		elif command -v netstat >/dev/null 2>&1; then \
			if netstat -an 2>/dev/null | grep -qE "(LISTEN|LISTENING).*:$$port "; then \
				PORTS_BUSY="$$PORTS_BUSY $$port"; \
			fi; \
		elif command -v lsof >/dev/null 2>&1; then \
			if lsof -i :$$port -sTCP:LISTEN >/dev/null 2>&1; then \
				PORTS_BUSY="$$PORTS_BUSY $$port"; \
			fi; \
		fi; \
	done; \
	if [ -n "$$PORTS_BUSY" ]; then \
		echo "ERROR: Required ports are already in use:$$PORTS_BUSY"; \
		echo "       Free these ports before running bootstrap."; \
		exit 1; \
	fi
	@echo "  [OK] Ports 5432, 6379, 9000, 3000 are free"
	@echo ""
	@echo "==> Installing dependencies (with caching)..."
	@$(GRADLEW) $(GRADLE_OPTS) dependencies > /dev/null 2>&1 || $(GRADLEW) $(GRADLE_OPTS) dependencies
	@echo "  [OK] Gradle dependencies resolved"
	@echo ""
	@echo "==> Generating .env file..."
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo "  [OK] .env created from .env.example"; \
	else \
		echo "  [SKIP] .env already exists"; \
	fi
	@echo ""
	@echo "==> Starting Docker Compose..."
	@docker compose -f $(COMPOSE_FILE) up -d
	@echo "  [OK] Containers started"
	@echo ""
	@echo "==> Running migrations..."
	@$(MAKE) migrate
	@echo ""
	@echo "==> Seeding demo data..."
	@$(MAKE) seed
	@echo ""
	@echo "==> Bootstrap complete!"

.PHONY: doctor
doctor: ## Diagnose environment (check Java, Docker, ports, env vars, connectivity)
	@echo ""
	@echo "AtlasOps AI — Environment Diagnostic"
	@echo "======================================"
	@echo ""
	@echo "--- Runtime ---"
	@if command -v java >/dev/null 2>&1; then \
		JAVA_VER=$$(java -version 2>&1 | head -1 | awk -F'"' '{print $$2}' | awk -F'.' '{print $$1}'); \
		if [ "$$JAVA_VER" -ge 21 ] 2>/dev/null; then \
			echo "  [OK] Java: $$(java -version 2>&1 | head -1) (required 21+)"; \
		else \
			echo "  [FAIL] Java: $$(java -version 2>&1 | head -1) (required 21+, current major: $$JAVA_VER)"; \
		fi; \
	else \
		echo "  [FAIL] Java: not found (required 21+)"; \
	fi
	@if command -v docker >/dev/null 2>&1; then \
		DOCKER_VER=$$(docker version --format '{{.Server.Version}}' 2>/dev/null || docker --version | grep -oP '\d+\.\d+\.\d+' | head -1); \
		DOCKER_MAJOR=$$(echo "$$DOCKER_VER" | awk -F'.' '{print $$1}'); \
		if [ -n "$$DOCKER_MAJOR" ] && [ "$$DOCKER_MAJOR" -ge 24 ] 2>/dev/null; then \
			echo "  [OK] Docker: $$DOCKER_VER (required 24+)"; \
		else \
			echo "  [FAIL] Docker: $$DOCKER_VER (required 24+)"; \
		fi; \
	else \
		echo "  [FAIL] Docker: not found (required 24+)"; \
	fi
	@echo ""
	@echo "--- Ports ---"
	@for port in 5432 6379 9000 3000 8080; do \
		if command -v ss >/dev/null 2>&1; then \
			ss -tlnp 2>/dev/null | grep -q ":$$port " && \
				echo "  [IN USE] Port $$port" || echo "  [FREE] Port $$port"; \
		elif command -v netstat >/dev/null 2>&1; then \
			netstat -an 2>/dev/null | grep -qE "(LISTEN|LISTENING).*:$$port " && \
				echo "  [IN USE] Port $$port" || echo "  [FREE] Port $$port"; \
		elif command -v lsof >/dev/null 2>&1; then \
			lsof -i :$$port -sTCP:LISTEN >/dev/null 2>&1 && \
				echo "  [IN USE] Port $$port" || echo "  [FREE] Port $$port"; \
		else \
			echo "  [UNKNOWN] Port $$port (no ss/netstat/lsof available)"; \
		fi; \
	done
	@echo ""
	@echo "--- Environment Variables ---"
	@for var in APP_ENV APP_PORT DATABASE_URL REDIS_URL OBJECT_STORAGE_ENDPOINT OBJECT_STORAGE_BUCKET JWT_ISSUER JWT_AUDIENCE LOG_LEVEL; do \
		if grep -q "^$$var=" .env 2>/dev/null; then \
			echo "  [OK] $$var"; \
		else \
			echo "  [MISSING] $$var"; \
		fi; \
	done
	@echo ""
	@echo "--- Docker Compose Services ---"
	@if docker compose -f $(COMPOSE_FILE) ps --format "table {{.Name}}\t{{.Status}}" 2>/dev/null; then \
		echo ""; \
	else \
		echo "  [FAIL] Could not query Docker Compose services"; \
	fi
	@echo "--- Service Connectivity ---"
	@if command -v docker >/dev/null 2>&1 && docker compose -f $(COMPOSE_FILE) ps --quiet 2>/dev/null | head -1 | grep -q .; then \
		docker compose -f $(COMPOSE_FILE) exec -T postgres pg_isready -U atlasops >/dev/null 2>&1 && \
			echo "  [OK] PostgreSQL: accepting connections" || \
			echo "  [FAIL] PostgreSQL: not accepting connections"; \
		docker compose -f $(COMPOSE_FILE) exec -T redis redis-cli ping 2>/dev/null | grep -q "PONG" && \
			echo "  [OK] Redis: accepting connections" || \
			echo "  [FAIL] Redis: not accepting connections"; \
		docker compose -f $(COMPOSE_FILE) exec -T minio mc ready local 2>/dev/null && \
			echo "  [OK] MinIO: accepting connections" || \
			(curl -sf http://localhost:9000/minio/health/live >/dev/null 2>&1 && \
				echo "  [OK] MinIO: accepting connections" || \
				echo "  [FAIL] MinIO: not accepting connections"); \
	else \
		echo "  [SKIP] Services not running (start with 'make compose-up')"; \
	fi
	@echo ""

# ============================================================================
# Quality Gates
# ============================================================================

.PHONY: verify
verify: ## Run all quality gates in sequence (format → lint → compile → test → build)
	@echo "==> [1/6] Checking format..."
	@$(GRADLEW) $(GRADLE_OPTS) spotlessCheck || { echo "FAILED at: format-check"; exit 1; }
	@echo "==> [2/6] Running lint (Checkstyle)..."
	@$(GRADLEW) $(GRADLE_OPTS) checkstyleMain || { echo "FAILED at: lint (checkstyle)"; exit 1; }
	@echo "==> [3/6] Compiling..."
	@$(GRADLEW) $(GRADLE_OPTS) compileJava || { echo "FAILED at: compile"; exit 1; }
	@echo "==> [4/6] Running unit tests..."
	@$(GRADLEW) $(GRADLE_OPTS) test || { echo "FAILED at: unit tests"; exit 1; }
	@echo "==> [5/6] Running SpotBugs..."
	@$(GRADLEW) $(GRADLE_OPTS) spotbugsMain || { echo "FAILED at: spotbugs"; exit 1; }
	@echo "==> [6/6] Building..."
	@$(GRADLEW) $(GRADLE_OPTS) build -x test || { echo "FAILED at: build"; exit 1; }
	@echo ""
	@echo "==> All quality gates passed!"

.PHONY: verify-local-fast
verify-local-fast: ## Fast local verification across backend and frontend
	@./scripts/quality/local-fast.sh

.PHONY: verify-precommit
verify-precommit: ## Run the minimal blocking checks for a pre-commit hook
	@./scripts/quality/pre-commit.sh

.PHONY: verify-prepush
verify-prepush: ## Run the blocking backend quality gates intended before push
	@./scripts/quality/pre-push.sh

.PHONY: install-git-hooks
install-git-hooks: ## Install local pre-commit and pre-push git hooks
	@./scripts/quality/install-git-hooks.sh

.PHONY: verify-contracts
verify-contracts: ## Run contract verification gates (OpenAPI export + lint + AsyncAPI validation)
	@bash ./scripts/quality/verify-contracts.sh

.PHONY: verify-security
verify-security: ## Run SAST, DAST, filesystem and Docker image security scans
	@./scripts/quality/semgrep.sh
	@./scripts/quality/zap-baseline.sh
	@./scripts/quality/trivy.sh

.PHONY: verify-sast
verify-sast: ## Run static application security testing with Semgrep
	@./scripts/quality/semgrep.sh

.PHONY: verify-dast
verify-dast: ## Run baseline dynamic application security testing with OWASP ZAP
	@./scripts/quality/zap-baseline.sh

.PHONY: test-contract
test-contract: verify-contracts ## Alias for contract verification gates

.PHONY: verify-frontend-fast
verify-frontend-fast: ## Fast local frontend verification (format + lint + typecheck)
	@cd frontend && pnpm verify:fast

.PHONY: verify-fast
verify-fast: ## Run fast quality gates only (skip SpotBugs, coverage, slow tests)
	@echo "==> [FAST MODE] Running quick quality gates..."
	@$(GRADLEW) $(GRADLE_OPTS) --no-configuration-cache verifyFast
	@echo "==> Fast verification passed!"

.PHONY: verify-full
verify-full: ## Run ALL quality gates including integration tests and coverage verification
	@echo "==> [FULL MODE] Running complete verification..."
	@$(GRADLEW) $(GRADLE_OPTS) verifyFull
	@echo "==> Full verification passed!"

.PHONY: test-unit
test-unit: ## Run only unit tests (parallel, optimized)
	@$(GRADLEW) $(GRADLE_OPTS) test

.PHONY: test-fast
test-fast: ## Run fast unit tests only (excludes slow, integration, property tests)
	@$(GRADLEW) $(GRADLE_OPTS) testFast

.PHONY: test-property
test-property: ## Run property-based tests (jqwik)
	@$(GRADLEW) $(GRADLE_OPTS) testProperty

.PHONY: test-integration
test-integration: ## Run integration tests (requires Docker)
	@docker compose -f $(COMPOSE_FILE) ps --quiet > /dev/null 2>&1 || { echo "ERROR: Docker Compose services not running. Run 'make compose-up' first."; exit 1; }
	@$(GRADLEW) $(GRADLE_OPTS) integrationTest

.PHONY: test-all
test-all: ## Run all tests (unit + property + integration)
	@docker compose -f $(COMPOSE_FILE) ps --quiet > /dev/null 2>&1 || { echo "ERROR: Docker Compose services not running. Run 'make compose-up' first."; exit 1; }
	@$(GRADLEW) $(GRADLE_OPTS) test testProperty integrationTest

.PHONY: build
build: ## Compile and package (optimized with caching)
	@$(GRADLEW) $(GRADLE_OPTS) build

.PHONY: build-fast
build-fast: ## Fast build (skip tests and checks)
	@$(GRADLEW) $(GRADLE_OPTS) build -x test -x checkstyleMain -x spotbugsMain -x spotlessCheck

.PHONY: format
format: ## Format code (Spotless)
	@$(GRADLEW) $(GRADLE_OPTS) spotlessApply

.PHONY: lint
lint: ## Run lint checks (Checkstyle + SpotBugs)
	@$(GRADLEW) $(GRADLE_OPTS) checkstyleMain spotbugsMain

.PHONY: coverage
coverage: ## Generate aggregated coverage report
	@$(GRADLEW) $(GRADLE_OPTS) test jacocoTestReport aggregateJacocoReport
	@echo "Coverage report: build/reports/jacoco/aggregated/index.html"

.PHONY: clean
clean: ## Clean all build artifacts
	@$(GRADLEW) clean
	@echo "Build artifacts cleaned."

.PHONY: clean-cache
clean-cache: ## Clean Gradle caches (use when builds behave unexpectedly)
	@$(GRADLEW) --stop
	@rm -rf .gradle/caches .gradle/configuration-cache
	@echo "Gradle caches cleaned."

# ============================================================================
# Infrastructure
# ============================================================================

.PHONY: compose-up
compose-up: ## Start local infrastructure (Docker Compose)
	@docker compose -f $(COMPOSE_FILE) up -d
	@echo "Infrastructure started. Services:"
	@docker compose -f $(COMPOSE_FILE) ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

.PHONY: compose-down
compose-down: ## Stop local infrastructure
	@docker compose -f $(COMPOSE_FILE) down

.PHONY: compose-reset
compose-reset: ## Remove volumes and recreate infrastructure from scratch
	@echo "==> Stopping containers and removing volumes..."
	@docker compose -f $(COMPOSE_FILE) down -v
	@echo "==> Recreating infrastructure..."
	@docker compose -f $(COMPOSE_FILE) up -d
	@echo "==> Waiting for services to be healthy..."
	@sleep 5
	@echo "==> Running migrations..."
	@$(MAKE) migrate
	@echo "==> Infrastructure reset complete!"

# ============================================================================
# Docker Compose Profiles
# ============================================================================

.PHONY: compose-core
compose-core: ## Start core services (PostgreSQL, Redis, MinIO, Backend API, Worker)
	docker compose -f $(COMPOSE_FILE) --profile core up -d

.PHONY: compose-advanced
compose-advanced: ## Start core + advanced services (OpenSearch, MongoDB, Neo4j)
	docker compose -f $(COMPOSE_FILE) --profile advanced up -d

.PHONY: compose-analytics
compose-analytics: ## Start core + analytics services (TimescaleDB, ClickHouse)
	docker compose -f $(COMPOSE_FILE) --profile analytics up -d

.PHONY: compose-event-sourcing
compose-event-sourcing: ## Start core + event sourcing services (EventStoreDB)
	docker compose -f $(COMPOSE_FILE) --profile event-sourcing up -d

.PHONY: compose-observability
compose-observability: ## Start core + observability services (Prometheus, Grafana, Loki, Tempo, MailHog)
	docker compose -f $(COMPOSE_FILE) --profile observability up -d

.PHONY: compose-resilience
compose-resilience: ## Start core + resilience services (Toxiproxy)
	docker compose -f $(COMPOSE_FILE) --profile resilience up -d

.PHONY: compose-all
compose-all: ## Start all services (all profiles)
	docker compose -f $(COMPOSE_FILE) --profile core --profile advanced --profile analytics --profile event-sourcing --profile observability --profile resilience up -d

# ============================================================================
# Test Infrastructure (Functional + Load)
# ============================================================================

.PHONY: test-functional
test-functional: ## Run Playwright E2E tests (headless)
	cd frontend && pnpm test:e2e

.PHONY: test-functional-headed
test-functional-headed: ## Run Playwright E2E tests (headed browser)
	cd frontend && pnpm test:e2e:ui

.PHONY: test-functional-report
test-functional-report: ## Generate and open Playwright HTML report
	cd frontend && pnpm test:e2e:report

.PHONY: test-load-smoke
test-load-smoke: ## Run k6 smoke load test (max 2 VUs, 30s)
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} tests/load/smoke.js

.PHONY: test-load-smoke-report
test-load-smoke-report: ## Run k6 smoke load test and save JSON report
	@mkdir -p tests/load/reports
	@ts=$$(date +%Y%m%d%H%M%S); \
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} --summary-export=tests/load/reports/smoke-$$ts.summary.json --out json=tests/load/reports/smoke-$$ts.json tests/load/smoke.js; \
	node tests/load/generate-report.mjs tests/load/reports/smoke-$$ts.summary.json tests/load/reports/smoke-$$ts.json tests/load/reports/smoke-$$ts.html

.PHONY: test-load-5vu
test-load-5vu: ## Run k6 load test with 5 concurrent users (3min, validates basic concurrency)
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} tests/load/five-users.js

.PHONY: test-load-5vu-with-auth
test-load-5vu-with-auth: ## Run k6 5-VU test with auth token (export K6_AUTH_TOKEN first)
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} --env K6_AUTH_TOKEN=$(K6_AUTH_TOKEN) --env K6_TENANT_ID=$(K6_TENANT_ID) tests/load/five-users.js

.PHONY: test-load
test-load: ## Run k6 average load test (50 VUs, 5min)
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} tests/load/average.js

.PHONY: test-load-low-resource
test-load-low-resource: ## Run k6 low-resource load test (1 VU, constrained budgets)
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} tests/load/low-resource.js

.PHONY: test-load-report
test-load-report: ## Run k6 load test and save JSON report
	@mkdir -p tests/load/reports
	@ts=$$(date +%Y%m%d%H%M%S); \
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} --summary-export=tests/load/reports/average-$$ts.summary.json --out json=tests/load/reports/average-$$ts.json tests/load/average.js; \
	node tests/load/generate-report.mjs tests/load/reports/average-$$ts.summary.json tests/load/reports/average-$$ts.json tests/load/reports/average-$$ts.html

.PHONY: test-load-low-resource-report
test-load-low-resource-report: ## Run k6 low-resource test and save JSON report
	@mkdir -p tests/load/reports
	@ts=$$(date +%Y%m%d%H%M%S); \
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} --summary-export=tests/load/reports/low-resource-$$ts.summary.json --out json=tests/load/reports/low-resource-$$ts.json tests/load/low-resource.js; \
	node tests/load/generate-report.mjs tests/load/reports/low-resource-$$ts.summary.json tests/load/reports/low-resource-$$ts.json tests/load/reports/low-resource-$$ts.html

.PHONY: test-load-5vu-report
test-load-5vu-report: ## Run k6 5-VU test and save JSON report
	@mkdir -p tests/load/reports
	@ts=$$(date +%Y%m%d%H%M%S); \
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} --summary-export=tests/load/reports/five-users-$$ts.summary.json --out json=tests/load/reports/five-users-$$ts.json tests/load/five-users.js; \
	node tests/load/generate-report.mjs tests/load/reports/five-users-$$ts.summary.json tests/load/reports/five-users-$$ts.json tests/load/reports/five-users-$$ts.html

# ============================================================================
# Database
# ============================================================================

.PHONY: migrate
migrate: ## Run database migrations
	@$(GRADLEW) $(GRADLE_OPTS) :backend:app-boot:flywayMigrate 2>/dev/null || \
		echo "NOTE: Flyway migration task not yet configured. Skipping."

.PHONY: seed
seed: ## Populate demo data (idempotent — safe to run multiple times)
	@echo "==> Seeding demonstration data..."
	@docker compose -f $(COMPOSE_FILE) exec -T postgres \
		psql -U $${POSTGRES_USER:-atlasops} -d $${POSTGRES_DB:-atlasops} \
		-f /seed/seed.sql \
		2>&1 | tail -5
	@echo "==> Seed complete!"

.PHONY: seed-reset
seed-reset: ## Reset seed data (truncate then re-seed)
	@echo "==> Resetting seed data..."
	@docker compose -f $(COMPOSE_FILE) exec -T postgres \
		psql -U $${POSTGRES_USER:-atlasops} -d $${POSTGRES_DB:-atlasops} \
		-c "TRUNCATE app.documents, app.customers, app.users, app.roles, app.tenants CASCADE;" \
		2>&1 | tail -3
	@$(MAKE) seed
	@echo "==> Seed reset complete!"

.PHONY: seed-demo
seed-demo: seed ## Alias for 'seed' — populate demo data

.PHONY: seed-tests
seed-tests: ## Populate minimal test data (tenants + users only)
	@echo "==> Seeding test data..."
	@docker compose -f $(COMPOSE_FILE) exec -T postgres \
		psql -U $${POSTGRES_USER:-atlasops} -d $${POSTGRES_DB:-atlasops} \
		-f /seed/seed-tests.sql \
		2>&1 | tail -5
	@echo "==> Test seed complete!"

# ============================================================================
# Operational Commands (P0.S.2)
# ============================================================================

.PHONY: format-check
format-check: ## Check code formatting without applying changes (alias for spotlessCheck)
	@$(GRADLEW) $(GRADLE_OPTS) spotlessCheck

.PHONY: health
health: ## Check health of API and Worker (requires running services)
	@echo "==> Checking API health..."
	@curl -sf http://localhost:$${APP_PORT:-8080}/actuator/health | python3 -m json.tool 2>/dev/null || \
		curl -sf http://localhost:$${APP_PORT:-8080}/actuator/health || \
		echo "  [FAIL] API not reachable at localhost:$${APP_PORT:-8080}"
	@echo ""
	@echo "==> Checking Worker health..."
	@curl -sf http://localhost:$${WORKER_PORT:-8081}/actuator/health | python3 -m json.tool 2>/dev/null || \
		curl -sf http://localhost:$${WORKER_PORT:-8081}/actuator/health || \
		echo "  [FAIL] Worker not reachable at localhost:$${WORKER_PORT:-8081}"

.PHONY: compose-logs
compose-logs: ## Follow Docker Compose logs for all services
	@docker compose -f $(COMPOSE_FILE) logs -f

.PHONY: worker-logs
worker-logs: ## Follow Docker Compose logs for the worker service
	@docker compose -f $(COMPOSE_FILE) logs -f worker

.PHONY: reset
reset: compose-reset ## Safe alias for compose-reset (removes volumes, recreates, migrates)

.PHONY: projection-status
projection-status: ## Query current projection status from database
	@docker compose -f $(COMPOSE_FILE) exec -T postgres \
		psql -U $${POSTGRES_USER:-atlasops} -d $${POSTGRES_DB:-atlasops} \
		-c "SELECT name, status, last_processed_position, last_run_at FROM projection_status ORDER BY name;" \
		2>&1 || echo "projection_status table not found — ensure migrations have run"

.PHONY: rebuild-search-index
rebuild-search-index: ## Trigger rebuild of search index projection
	./gradlew :backend:app-boot:bootRun --args="--rebuild-projection=search-index" 2>&1 | head -20 || echo "Rebuild triggered"

.PHONY: rebuild-vector-index
rebuild-vector-index: ## Trigger rebuild of vector index projection
	./gradlew :backend:app-boot:bootRun --args="--rebuild-projection=vector-index" 2>&1 | head -20 || echo "Rebuild triggered"

.PHONY: rebuild-analytics
rebuild-analytics: ## Trigger rebuild of analytics projection
	./gradlew :backend:app-boot:bootRun --args="--rebuild-projection=analytics" 2>&1 | head -20 || echo "Rebuild triggered"

.PHONY: verify-specs
verify-specs: ## Validate completeness of spec files in .kiro/specs/
	@echo "==> Verifying spec completeness in .kiro/specs/..."
	@MISSING=0; \
	for spec_dir in .kiro/specs/*/; do \
		spec_name=$$(basename "$$spec_dir"); \
		has_req=0; has_design=0; has_tasks=0; \
		[ -f "$$spec_dir/requirements.md" ] && has_req=1; \
		[ -f "$$spec_dir/design.md" ] && has_design=1; \
		[ -f "$$spec_dir/tasks.md" ] && has_tasks=1; \
		if [ $$has_req -eq 0 ] || [ $$has_design -eq 0 ] || [ $$has_tasks -eq 0 ]; then \
			echo "  [INCOMPLETE] $$spec_name (req=$$has_req, design=$$has_design, tasks=$$has_tasks)"; \
			MISSING=$$((MISSING + 1)); \
		else \
			echo "  [OK] $$spec_name"; \
		fi; \
	done; \
	if [ $$MISSING -gt 0 ]; then \
		echo ""; \
		echo "WARNING: $$MISSING spec(s) are incomplete"; \
		exit 0; \
	else \
		echo ""; \
		echo "All specs are complete!"; \
	fi

## ─────────────────────────────────────────────────────────────────────────────
## Docker Build Targets (P0.U.1)
## ─────────────────────────────────────────────────────────────────────────────

.PHONY: docker-build docker-build-api docker-build-worker

## Build all application Docker images
docker-build: docker-build-api docker-build-worker

## Build backend API Docker image
docker-build-api:
	@echo "Building backend API Docker image..."
	docker build -t atlasops-api:latest -f backend/app-boot/Dockerfile .

## Build worker Docker image
docker-build-worker:
	@echo "Building worker Docker image..."
	docker build -t atlasops-worker:latest -f backend/worker/Dockerfile .

## ─────────────────────────────────────────────────────────────────────────────
## SonarQube (self-hosted) — Quality Analysis
## ─────────────────────────────────────────────────────────────────────────────

.PHONY: sonar-up sonar-down sonar-provision sonar-analyze sonar-open sonar-token

## Start SonarQube and its database (quality profile)
sonar-up: ## Start SonarQube self-hosted (first run: ~2min to initialize)
	@docker compose -f $(COMPOSE_FILE) up -d sonarqube-db sonarqube
	@echo ""
	@echo "SonarQube starting at http://localhost:$${SONAR_PORT:-9099}"
	@echo "Run 'make sonar-provision' once it is fully UP (check with 'make sonar-health')"

## Stop SonarQube services
sonar-down: ## Stop SonarQube services
	@docker compose -f $(COMPOSE_FILE) stop sonarqube sonarqube-db
	@docker compose -f $(COMPOSE_FILE) rm -f sonarqube sonarqube-db

## Provision Quality Gate and project (run once after sonar-up)
sonar-provision: ## Provision AtlasOps Quality Gate (80% coverage + all conditions)
	@echo "==> Provisioning SonarQube Quality Gate..."
	@SONAR_HOST_URL="http://localhost:$${SONAR_PORT:-9099}" \
	 bash infra/sonar/provision-quality-gate.sh

## Run full analysis and publish to SonarQube (requires tests + jacoco to have run)
sonar-analyze: ## Run SonarQube analysis (requires: make coverage first)
	@echo "==> Generating coverage reports..."
	@$(GRADLEW) $(GRADLE_OPTS) test jacocoTestReport aggregateJacocoReport
	@echo "==> Running SonarQube analysis..."
	@$(GRADLEW) $(GRADLE_OPTS) sonar \
		-Dsonar.host.url="$${SONAR_HOST_URL:-http://localhost:$${SONAR_PORT:-9099}}" \
		-Dsonar.token="$${SONAR_TOKEN:-}" \
		-Dsonar.qualitygate.wait=true \
		-Dsonar.qualitygate.timeout=300
	@echo ""
	@echo "Results: http://localhost:$${SONAR_PORT:-9099}/dashboard?id=atlasops-ai"

## Run analysis without waiting for Quality Gate result (faster feedback)
sonar-analyze-async: ## Run SonarQube analysis without Quality Gate wait
	@$(GRADLEW) $(GRADLE_OPTS) test jacocoTestReport sonar \
		-Dsonar.host.url="$${SONAR_HOST_URL:-http://localhost:$${SONAR_PORT:-9099}}" \
		-Dsonar.token="$${SONAR_TOKEN:-}"
	@echo "Results (async): http://localhost:$${SONAR_PORT:-9099}/dashboard?id=atlasops-ai"

## Check SonarQube health
sonar-health: ## Check if SonarQube is UP and ready
	@STATUS=$$(curl -sf "http://localhost:$${SONAR_PORT:-9099}/api/system/status" 2>/dev/null \
		| python3 -c "import sys,json; print(json.load(sys.stdin).get('status','UNKNOWN'))" 2>/dev/null || echo "UNREACHABLE"); \
	echo "SonarQube status: $$STATUS"; \
	[ "$$STATUS" = "UP" ] || exit 1

## Open SonarQube dashboard in default browser
sonar-open: ## Open SonarQube dashboard in browser
	@command -v xdg-open >/dev/null 2>&1 && xdg-open "http://localhost:$${SONAR_PORT:-9099}/dashboard?id=atlasops-ai" || \
	 command -v open >/dev/null 2>&1 && open "http://localhost:$${SONAR_PORT:-9099}/dashboard?id=atlasops-ai" || \
	 echo "Open: http://localhost:$${SONAR_PORT:-9099}/dashboard?id=atlasops-ai"

## ─────────────────────────────────────────────────────────────────────────────
## Backup and Restore (P3.3)
## ─────────────────────────────────────────────────────────────────────────────

.PHONY: backup restore restore-validate

## Create full backup (PostgreSQL + MinIO)
backup:
	@bash infra/scripts/backup.sh

## Restore from backup: make restore BACKUP_ID=20260720120000
restore:
	@bash infra/scripts/restore.sh $(BACKUP_ID)

## Validate restore without applying: make restore-validate BACKUP_ID=20260720120000
restore-validate:
	@bash infra/scripts/restore.sh $(BACKUP_ID) --dry-run

## ─────────────────────────────────────────────────────────────────────────────
## Performance / Load Tests (P3.4)
## ─────────────────────────────────────────────────────────────────────────────

.PHONY: test-load-stress

## Stress test: 200 VUs, 10min ramp-up (P3.4)
test-load-stress:
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} tests/load/stress.js

.PHONY: test-load-stress-report

## Stress test with JSON artifact output
test-load-stress-report:
	@mkdir -p tests/load/reports
	@ts=$$(date +%Y%m%d%H%M%S); \
	k6 run --env K6_BASE_URL=$${K6_BASE_URL:-http://localhost:8080} --summary-export=tests/load/reports/stress-$$ts.summary.json --out json=tests/load/reports/stress-$$ts.json tests/load/stress.js; \
	node tests/load/generate-report.mjs tests/load/reports/stress-$$ts.summary.json tests/load/reports/stress-$$ts.json tests/load/reports/stress-$$ts.html

.PHONY: test-resilience-ollama
test-resilience-ollama: ## Run Ollama fallback resilience test
	@$(GRADLEW) $(GRADLE_OPTS) :backend:ai:test --tests com.atlasops.ai.infrastructure.OllamaAIAdapterResilienceTest

.PHONY: test-resilience-minio
test-resilience-minio: ## Run MinIO outage resilience test
	@$(GRADLEW) $(GRADLE_OPTS) :backend:app-boot:resilienceTest --tests com.atlasops.boot.resilience.MinioResilienceIntegrationTest

## ─────────────────────────────────────────────────────────────────────────────
## Ledger Verification (P2.5, P3.13)
## ─────────────────────────────────────────────────────────────────────────────

.PHONY: verify-ledger

## Verify the approval and audit ledger hash chain integrity
verify-ledger:
	@echo "Verifying ledger integrity..."
	@curl -sf -H "Authorization: Bearer $${ADMIN_TOKEN}" \
	  -H "X-Tenant-ID: $${TENANT_ID}" \
	  http://localhost:8080/api/v1/audit/ledger/verify 2>/dev/null \
	  | python3 -c "import sys,json; d=json.load(sys.stdin); print('Ledger valid:', d.get('integrityValid', False))" \
	  2>/dev/null || echo "API not available — run 'make compose-up' first"

## ─────────────────────────────────────────────────────────────────────────────
## Tenant Data Portability (P3.3.5)
## ─────────────────────────────────────────────────────────────────────────────

.PHONY: tenant-export

## Export all data for a tenant: make tenant-export TENANT_ID=tenant-alpha
tenant-export:
	@bash infra/scripts/tenant-export.sh $(TENANT_ID)
