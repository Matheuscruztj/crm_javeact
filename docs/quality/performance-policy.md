# Performance Threshold Policy

## Scope

Aplica-se aos cenários k6 e às validações de capacidade mínima.

## Baseline thresholds

| Flow | p95 | p99 | Error rate |
| --- | --- | --- | --- |
| Smoke | 800 ms | 1500 ms | 1% |
| Average | 1200 ms | 2000 ms | 1% |
| Low resource | 1200 ms | 2000 ms | 1% |
| Stress | review only | review only | 3% |

## Rules

- thresholds are blocking when explicitly referenced by a release gate;
- stress and soak are advisory unless a release declares otherwise;
- budgets may be tightened only with owner approval and updated baseline evidence.
