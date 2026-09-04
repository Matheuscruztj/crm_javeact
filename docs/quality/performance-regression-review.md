# Performance Regression Review

## Inputs

- k6 JSON summary;
- HTML report;
- Prometheus/Grafana screenshots or exported panels;
- JFR recording when available.

## Review steps

1. Compare current run against baseline.
2. Identify p95/p99 regressions.
3. Correlate with CPU, GC, locks, I/O or downstream latency.
4. Decide whether the change is acceptable, needs mitigation or must block release.
5. Record owner, action and expiry if a waiver is used.

## Outcome categories

- pass;
- pass with waiver;
- block;
- needs more data.
