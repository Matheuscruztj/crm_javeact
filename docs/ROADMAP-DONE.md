# AtlasOps AI — Roadmap histórico de entregas por frente

> Registro histórico reorganizado por frente de trabalho.
> Última atualização: 2026-07-31 | Versão: v1.0.0-rc.1
> Fonte de validação cruzada: [task-plans](./task-plans/), [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md), [ROADMAP.md](./ROADMAP.md)

---

## Leitura deste arquivo

- `Implementado` = existe código, script, teste ou workflow correspondente no repositório.
- `Implementado e validado` = além de existir, o entregável já tem enforcement ou evidência executável consistente.
- `Parcial` não entra neste arquivo. Itens parciais permanecem nos roadmaps ativos e nos task plans.

---

## Resumo por frente

| Frente | Status histórico | Observação |
| --- | --- | --- |
| Backend | Implementado em larga escala | Nem todo módulo está validado no mesmo nível de cobertura |
| Frontend | Implementado em larga escala | Coverage unitário já existe, mas gate ainda está em evolução |
| Qualidade | Implementado com enforcement parcial | Hooks, contratos, Trivy, Sonar e CI existem; breaking-change e governança fina seguem pendentes |
| Infraestrutura e operação | Implementado em larga escala | Parte dos adapters segue feature-flagged |

---

## Backend

### Implementado e validado

- Autenticação JWT, rotação de refresh token, revogação de sessões e filtros de autorização multi-tenant.
- Ledger append-only com hash chain em approvals e trilha de auditoria verificável.
- SSE com replay por `Last-Event-ID` e heartbeat operacional.
- Transactional outbox, Redis Streams, DLQ e locking distribuído.
- RAG local com Ollama + pgvector + extração de texto via Apache Tika.
- Prompt version registry e golden dataset para avaliação de IA.
- Módulos fundacionais e de aplicação já presentes para `auth`, `tenants`, `users`, `customers`, `documents`, `requests`, `approvals`, `activities`, `notifications`, `search`, `operations`, `audit`, `ai`, `app-boot` e `worker`.
- `resilienceTest` dedicado para `app-boot`.

### Implementado

- Integrações outbound com proteção SSRF e adapters REST.
- Imports com fluxo CSV.
- Analytics com base de infraestrutura e consultas agregadas.
- Adapters especializados feature-flagged para OpenSearch, Neo4j, TimescaleDB, ClickHouse e EventStoreDB.
- Testes de resiliência distribuídos por adapters e integrações principais.

### Marcos históricos transferidos do plano

- Contrato OpenAPI exportado pela aplicação e usado no fluxo de verificação local/CI.
- Configuração SonarQube ajustada para estrutura multi-módulo Java do monorepo.
- CI principal com job dedicado de resiliência.

---

## Frontend

### Implementado e validado

- Portal administrativo e portal do cliente em Next.js 15 + React 19.
- Auth state, hooks tipados de API, SSE em tempo real, upload manager e internacionalização.
- `prettier --check`, `eslint`, `typecheck` e `dependency-cruiser` integrados ao fluxo local e CI.
- Testes E2E Playwright para jornadas críticas.

### Implementado

- Suite de testes unitários com `Vitest`.
- Coverage unitário no frontend com job dedicado na CI.
- Páginas funcionais de operações, imports e integrações.

### Marcos históricos transferidos do plano

- A superfície de testes do frontend deixou de ser apenas E2E: `Vitest` e coverage já existem no repositório.
- `verify:fast` e `verify:full` do frontend ficaram explicitamente separados por custo.

---

## Qualidade

### Implementado e validado

- `verify-local-fast`, `verify-precommit`, `verify-prepush`, `verify-contracts` e `verify-security` no `Makefile`.
- Hook `pre-commit` leve, orientado a arquivos staged e checagens baratas.
- Hook `pre-push` blocking com backend, frontend, arquitetura, contratos, resiliência e segurança.
- CI principal segmentada por backend fast, static analysis, property tests, architecture, coverage, frontend fast, frontend unit coverage, contratos, resiliência, Trivy e Sonar.
- Nightly com load test médio, flaky detection, mutation testing opcional, SBOM e Trivy.
- `verify-contracts` deixou de ser placeholder operacional.
- Trivy integrado para filesystem e imagens Docker.
- Syft integrado para geração de SBOM.

### Implementado

- SonarQube self-hosted com análise acionada pela CI.
- OWASP Dependency-Check no fluxo noturno.
- AsyncAPI validado via CLI compatível.

### Marcos históricos transferidos do plano

- O contrato exportado em `backend/app-boot/build/reports/openapi/openapi.json` passou a ser a fonte real do check.
- A separação de responsabilidades entre `pre-commit`, `pre-push` e CI ficou explícita no código.
- O placeholder de SBOM via `cyclonedxBom` foi removido do nightly, assumindo `Syft` como fonte efetiva.

---

## Infraestrutura e operação

### Implementado e validado

- Docker Compose com perfis para core, advanced, analytics, event-sourcing, observability e quality.
- SonarQube provisionável localmente.
- k6 com cenários `smoke`, `average`, `stress` e geração de relatório HTML/JSON.
- Backup, restore e tenant export com evidência de checksum.
- Dashboards e alertas de observabilidade para métricas de AI, jobs e runtime.

### Implementado

- Runbooks de resiliência e evidência operacional.
- Pipeline noturna para mutation testing, flaky detection e carga média.

---

## O que não foi promovido para concluído

Os itens abaixo continuam fora deste histórico de “finalizado” porque ainda não têm evidência suficiente para status concluído em 2026-07-31:

- Cobertura backend “reerguida” de forma confiável. O gate atual foi reduzido para `10%`.
- Breaking-change check de OpenAPI.
- Geração de tipos/cliente frontend derivados do OpenAPI.
- Governança completa de severidade e ownership dos checks.
- Classificação final e documentada de todos os checks como `blocking` ou `advisory`.
- Inclusão formal do frontend na análise Sonar.

---

## Documentos relacionados

| Documento | Papel atual |
| --- | --- |
| [ROADMAP.md](./ROADMAP.md) | visão ativa do produto e gaps |
| [PENDING-ACTIVITIES.md](./PENDING-ACTIVITIES.md) | backlog consolidado de pendências |
| [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md) | status técnico das waves de qualidade |
| [STATUS.md](./STATUS.md) | visão detalhada por módulo |
| [00-current-status.md](./00-current-status.md) | snapshot consolidado do estado atual |
