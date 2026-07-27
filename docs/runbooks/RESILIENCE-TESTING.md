# Resilience Testing Runbook

> **Validates:** P3.2 — Dependency failure and circuit breaker tests  
> **Prerequisites:** `make compose-up` running for standard checks; `make compose-resilience` running for Toxiproxy-backed fault injection

---

## Test Matrix

| Test                          | Command                         | Expected Behavior                                              |
| ----------------------------- | ------------------------------- | -------------------------------------------------------------- |
| P3.2.1 PostgreSQL unavailable | `make test-resilience-postgres` | API returns 503 with `DEPENDENCY_UNAVAILABLE`                  |
| P3.2.2 Redis unavailable      | `make test-resilience-redis`    | Auth/SSE degrade gracefully, data writes still work            |
| P3.2.3 MinIO unavailable      | `make test-resilience-minio`    | Upload returns 503, existing documents readable                |
| P3.2.4 Ollama unavailable     | `make test-resilience-ollama`   | AI uses deterministic fallback (confidence=0.0, fallback=true) |
| P3.2.5 Search fallback        | `make test-resilience-search`   | Returns PostgreSQL FTS results when OpenSearch unavailable     |
| P3.2.6 Circuit breaker        | `make test-circuit-breaker`     | Ollama CB opens after 5 failures, half-opens after 30s         |

---

## Manual Test Procedures

### P3.2.1 — PostgreSQL Failure

```bash
# 1. Stop PostgreSQL
docker compose stop postgres

# 2. Verify health endpoint
curl http://localhost:8080/actuator/health
# Expected: {"status":"DOWN","components":{"db":{"status":"DOWN"}}}

# 3. Call API endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/customers
# Expected: 503 {"code":"DEPENDENCY_UNAVAILABLE","status":503}

# 4. Restart
docker compose start postgres
```

### P3.2.3 — MinIO Failure via Toxiproxy

```bash
# 1. Start the resilience stack
make compose-resilience

# 2. Run the integration test
make test-resilience-minio

# Expected: outage is detected, checksum succeeds before failure, delete fails while toxic is active,
# and recovery works after toxic removal.
```

### P3.2.4 — Ollama Fallback Verification

```bash
# 1. Stop Ollama (or use an invalid URL in config)
# 2. Upload a document and trigger analysis
# 3. Check the AIAnalysisRecord:
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/ai/analysis \
  | jq '.[] | select(.fallback == true)'
# Expected: fallback=true, confidenceScore=0.0
```

### P3.2.6 — Circuit Breaker State

```bash
# Prometheus metrics show circuit breaker state:
curl http://localhost:8080/actuator/prometheus \
  | grep resilience4j_circuitbreaker_state

# States: 0=CLOSED, 1=OPEN, 2=HALF_OPEN
# After 5 failures to Ollama: state should be 1 (OPEN)
# After 30s: state should be 2 (HALF_OPEN)
```

---

## Automated Resilience Tests (Integration)

The following integration tests in `app-boot` cover circuit breaker behavior:

- `AuthIntegrationTest` — verifies 401 on missing token
- `ApprovalsIntegrationTest` — verifies cross-tenant 403
- `AiIntegrationTest` — verifies AI endpoint requires auth
- `MinioResilienceIntegrationTest` — verifies Toxiproxy outage and recovery for S3-compatible storage
- `OllamaAIAdapterResilienceTest` — verifies deterministic fallback when the LLM provider is unavailable

Full dependency-failure tests now use Docker Compose plus Testcontainers for local orchestration and controlled fault injection.
