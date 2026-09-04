# Matriz de Checks

Esta matriz define a relação entre `check`, `owner`, `frequência`, `severidade` e `status`.

## Local Fast

| Check | Owner | Frequência | Status |
| --- | --- | --- | --- |
| `verify-local-fast` | Backend + Frontend | Pré-commit / validação curta | blocking |
| `verify-precommit` | Quality | Antes do commit | blocking |

## Pre-push

| Check | Owner | Frequência | Status |
| --- | --- | --- | --- |
| `verify-prepush` | Quality | Antes do push | blocking |
| `verify-contracts` | Backend + Quality | Quando contrato muda | blocking |

## CI Blocking

| Check | Owner | Frequência | Status |
| --- | --- | --- | --- |
| `backend-fast` | Backend | PR e main | blocking |
| `frontend-fast` | Frontend | PR e main | blocking |
| `secrets-scan` | Quality | PR e nightly | blocking |
| `workflow-security` | Quality | PR e nightly | blocking |
| `dependency-scan` | Quality | PR e nightly | advisory until delta policy lands |
| `dependency-check` | Backend + Quality | PR backend | blocking with NVD key |
| `sbom` | Quality | Nightly / release-adjacent | advisory |

## CI Advisory

| Check | Owner | Frequência | Status |
| --- | --- | --- | --- |
| `mutation-testing` | Backend | On demand | advisory |
| `flaky-test-detection` | Backend + Quality | Nightly | advisory |
| `full-load-test` | Quality | Nightly | advisory |
