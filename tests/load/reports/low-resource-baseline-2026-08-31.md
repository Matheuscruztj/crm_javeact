# Low Resource Baseline - 2026-08-31

Scenario: `tests/load/low-resource.js`
Repository date: 2026-08-31

## Purpose

Reference artifact for the constrained-capacity scenario used by the profiling and capacity runbook.

## Stable baseline metadata

- command: `make test-load-low-resource-report`
- report directory: `tests/load/reports/`
- scenario mix: health, customers, search, Prometheus
- intended environment: 1 vCPU / 1 GB RAM or less
- budgets: p95 <= 1200 ms, p99 <= 2000 ms, error rate <= 1%

## Notes

- This artifact is intentionally versioned alongside the runbook so future executions can be compared against the same scenario contract.
- When a fresh execution is captured, add the JSON, summary JSON and HTML files in the same directory using the existing Makefile target.
