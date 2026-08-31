# AtlasOps AI - Profiling and Capacity Validation Runbook

## 1. Purpose

This runbook defines a repeatable path to inspect JVM bottlenecks, correlate them with metrics and validate whether the core API can survive a constrained runtime budget.

Target questions:

- where does the JVM spend CPU and memory;
- which requests become slow under contention;
- whether the core request mix stays within acceptable p95/p99 limits on a low-resource host;
- whether Prometheus and Grafana can be used to correlate the observed degradation.

## 2. Observability prerequisites

The repository already provisions:

- Prometheus in `infra/monitoring/prometheus.yml`;
- Grafana datasources in `infra/monitoring/grafana/provisioning/datasources/datasources.yml`;
- dashboards in `infra/monitoring/grafana/dashboards/`;
- actuator metrics at `/actuator/prometheus`.

Start the observability stack with:

```bash
make compose-observability
```

## 3. JVM profiling with JFR

The backend and worker accept `JAVA_TOOL_OPTIONS` through Docker Compose.

### Example

```bash
export JAVA_TOOL_OPTIONS="-XX:+FlightRecorder -XX:StartFlightRecording=filename=/tmp/atlasops-api.jfr,settings=profile,dumponexit=true,maxsize=256m,maxage=30m"
docker compose up -d backend-api worker
```

This captures a lightweight Java Flight Recorder snapshot suitable for:

- CPU hotspots;
- allocation pressure;
- lock contention;
- safepoints;
- GC pauses.

After the run:

```bash
docker cp atlasops-backend-api:/tmp/atlasops-api.jfr ./atlasops-api.jfr
```

Open the recording in JDK Mission Control or a compatible JFR viewer.

## 4. Low-resource load validation

Use the dedicated k6 scenario:

```bash
make test-load-low-resource-report
```

The scenario is tuned for a constrained runtime and exercises:

- `/actuator/health`;
- `/api/v1/customers`;
- `/api/v1/search`;
- `/actuator/prometheus`.

Suggested budgets:

- p95 under 1200 ms;
- p99 under 2000 ms;
- error rate under 1%.

You can tighten or relax the budgets with environment variables:

```bash
K6_P95_MS=1000 K6_P99_MS=1800 K6_ERROR_RATE=0.01 make test-load-low-resource-report
```

The repository keeps a versioned reference artifact for this scenario at:

- [tests/load/reports/low-resource-baseline-2026-08-31.md](/home/matheus/zona_de_teste/crm_javeact/tests/load/reports/low-resource-baseline-2026-08-31.md)

Use it as the canonical baseline metadata when comparing fresh executions.

## 5. Correlation workflow

1. Start observability.
2. Start the backend with JFR enabled.
3. Run the low-resource k6 scenario.
4. Inspect the JFR recording for CPU, allocation and lock hot spots.
5. Compare request latency with Grafana panels backed by Prometheus.
6. Capture the generated k6 JSON and summary reports as release evidence.

## 6. Read-model alignment

The hot paths covered by the low-resource workflow are intentionally separated into explicit read models:

- dashboard metrics are aggregated in a dedicated projection before being exposed by the analytics API;
- the tenant activity feed is paginated and role-filtered before presentation;
- unified search keeps an application-level result view between the index port and the REST response.

## 7. Interpretation

- If p95/p99 is high and JFR shows CPU saturation, the bottleneck is compute-bound.
- If p95/p99 is high and JFR shows lock contention or GC, tune concurrency and allocations first.
- If Prometheus shows elevated latency but JFR is quiet, investigate I/O, downstream dependencies or network contention.
