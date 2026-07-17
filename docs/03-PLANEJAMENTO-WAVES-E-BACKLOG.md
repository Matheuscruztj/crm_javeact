# AtlasOps AI — Planejamento, waves e backlog

## Objetivo

Este documento transforma o escopo em execução. Ele define:

- waves;
- ordem de implementação;
- atividades paralelizáveis;
- dependências obrigatórias;
- plano de 14 dias;
- critérios de saída;
- épicos;
- Definition of Ready;
- Definition of Done;
- checklists por tipo de mudança.

---

## Waves, dependências e paralelização

### 03 — Planejamento, waves, dependências e paralelização

#### 1. Objetivo

Organizar a implementação em ondas independentes de stack, com clareza sobre:

- ordem;
- dependências;
- atividades paralelas;
- critérios de saída;
- riscos;
- entregas demonstráveis.

---

#### 2. Estratégia

Cada wave deve:

1. entregar uma capacidade verificável;
2. deixar a main branch verde;
3. possuir testes;
4. atualizar documentação;
5. não depender de trabalho futuro para ser validada.

---

#### 3. Wave 0 — Foundation e harness

##### Objetivo

Criar o sistema que produz e valida o software antes de construir funcionalidades.

##### Atividades

- repositório;
- convenções;
- estrutura;
- scripts padronizados;
- lint;
- format;
- typecheck;
- build;
- test runner;
- Docker;
- Compose;
- health check;
- migrations;
- seed;
- CI;
- OpenAPI base;
- logging;
- correlation ID;
- ADR template;
- PR template;
- CODEOWNERS;
- issue template;
- agent instructions;
- arquitetura de pastas;
- dependency rules.

##### Saída

```text
make verify
docker compose up
```

devem funcionar.

##### Dependências

Nenhuma.

##### Paralelização

- CI;
- Docker;
- convenções;
- arquitetura;
- frontend shell;
- backend shell;
- documentação.

---

#### 4. Wave 1 — Identity, tenancy e autorização

##### Objetivo

Criar a fronteira de segurança.

##### Atividades

- users;
- tenants;
- memberships;
- roles;
- permissions;
- sessions;
- login;
- refresh;
- logout;
- tenant context;
- policy service;
- seed;
- testes cross-tenant;
- frontend login;
- route guards.

##### Saída

- dois tenants;
- usuários internos e externos;
- isolamento comprovado;
- menus por permissão;
- auditoria de login.

##### Dependências

Wave 0.

##### Sequência interna

```text
Tenant model
→ User/Membership
→ Session/Auth
→ Permissions
→ Route protection
→ Cross-tenant tests
```

---

#### 5. Wave 2 — Customers e portal base

##### Objetivo

Entregar o primeiro domínio consumível.

##### Atividades

- customer aggregate;
- CRUD controlado;
- busca;
- filtros;
- detalhe;
- timeline;
- portal customer context;
- usuários do cliente;
- frontend admin;
- frontend portal;
- testes.

##### Saída

Admin cria cliente e cliente autenticado visualiza apenas seus dados.

##### Dependências

Wave 1.

---

#### 6. Wave 3 — Documents e storage

##### Objetivo

Implementar upload/download reais.

##### Atividades

- document model;
- MinIO;
- presigned upload;
- confirm upload;
- checksum;
- status;
- download;
- access log;
- cleanup;
- tela admin;
- tela cliente;
- testes de storage;
- testes cross-tenant.

##### Saída

Cliente envia arquivo e analista baixa com autorização.

##### Dependências

Wave 1 e parte de Wave 2.

##### Paralelização

- storage adapter;
- APIs;
- UI;
- testes;
- policies.

---

#### 7. Wave 4 — Requests, tasks e SLA

##### Objetivo

Criar fluxo operacional.

##### Atividades

- requests;
- state machine;
- comments;
- assignment;
- tasks;
- SLA;
- timeline;
- notificações básicas;
- páginas;
- testes de transição.

##### Saída

Cliente abre solicitação; operador processa; cliente acompanha.

##### Dependências

Wave 2. Documentos podem ser integrados ao final.

---

#### 8. Wave 5 — Async, outbox e workflows

##### Objetivo

Desacoplar processamento demorado.

##### Atividades

- domain events;
- outbox;
- dispatcher;
- worker;
- retries;
- DLQ;
- idempotency;
- job table;
- workflow execution;
- observabilidade;
- testes de duplicidade;
- testes de falha.

##### Saída

Evento persistido na mesma transação e processado de forma idempotente.

##### Dependências

Waves 3 e 4 fornecem casos reais.

---

#### 9. Wave 6 — IA e data service

##### Objetivo

Adicionar análise útil sem acoplar o core.

##### Atividades

- AIAnalysisPort;
- mock adapter;
- AI service;
- Ollama adapter;
- prompt templates;
- prompt version;
- schema validation;
- fallback;
- document analysis;
- request classification;
- human approval;
- evaluation dataset;
- testes de regressão.

##### Saída

Documento analisado assincronamente; fallback funcional.

##### Dependências

Wave 5 e Wave 3.

---

#### 10. Wave 7 — Pipeline e analytics

##### Objetivo

Adicionar capacidade gerencial e comercial.

##### Atividades

- opportunity;
- stages;
- stage history;
- weighted forecast;
- stalled detection;
- analytics queries;
- period comparison;
- dashboards;
- materialized summary opcional;
- testes de agregação.

##### Saída

Dashboard com dados reais e pipeline operacional.

##### Dependências

Wave 2. Pode ocorrer parcialmente em paralelo às Waves 5 e 6.

---

#### 11. Wave 8 — Alerts, notifications, import e quotas

##### Objetivo

Completar capacidades de produto.

##### Atividades

- operational alerts;
- MailHog;
- in-app notifications;
- import jobs;
- error report;
- plans;
- usage meter;
- quota warning;
- feature flags;
- webhooks opcionais.

##### Dependências

Módulos anteriores.

---

#### 12. Wave 9 — Hardening

##### Objetivo

Elevar qualidade e preparação de entrevista.

##### Atividades

- threat model;
- ASVS checklist;
- SAST;
- dependency scan;
- secret scan;
- image scan;
- DAST;
- load tests;
- chaos scenarios;
- backup/restore;
- runbooks;
- tracing;
- SLOs;
- quality grades;
- performance profiling;
- ADR review.

---

#### 13. Wave 10 — Demo e release candidate

##### Objetivo

Preparar entrega reproduzível.

##### Atividades

- seed de demo;
- script de demo;
- screenshots;
- README;
- diagramas;
- release notes;
- version tag;
- SBOM;
- image tag;
- known limitations;
- roadmap;
- reset script;
- smoke test.

---

#### 14. Plano de 14 dias

| Dia | Entrega principal |
|---|---|
| 1 | Wave 0: repositório, harness, Docker e CI |
| 2 | Tenant, user, session e autorização |
| 3 | Customers e portal base |
| 4 | Documents e MinIO |
| 5 | Requests, state machine e tasks |
| 6 | Frontend admin dos domínios |
| 7 | Frontend cliente e fluxo completo básico |
| 8 | Outbox, worker e idempotência |
| 9 | AI adapter, mock e Ollama/Python |
| 10 | Pipeline e analytics |
| 11 | Alerts, notifications e import |
| 12 | Observabilidade, segurança e carga |
| 13 | E2E, correções, documentação |
| 14 | Release candidate e demo |

---

#### 15. Matriz de dependências

Legenda:

- `S`: deve ocorrer em sequência;
- `P`: pode ocorrer em paralelo;
- `I`: integração posterior;
- `-`: não aplicável.

| Atividade | Foundation | Auth | Customer | Documents | Requests | Async | AI | Analytics | Frontend |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Foundation | - | S | S | S | S | S | S | S | P |
| Auth/Tenant |  | - | S | S | S | S | S | S | I |
| Customer |  |  | - | P | P | I | I | S | P |
| Documents |  |  |  | - | I | S | S | I | P |
| Requests |  |  |  |  | - | S | S | S | P |
| Async |  |  |  |  |  | - | S | P | I |
| AI |  |  |  |  |  |  | - | I | P |
| Analytics |  |  |  |  |  |  |  | - | P |

---

#### 16. Trilhas paralelas

##### Trilha A — Backend core

- foundation;
- auth;
- tenancy;
- domain modules;
- API.

##### Trilha B — Frontend

- design tokens;
- layouts;
- components;
- mocks OpenAPI;
- pages;
- integration.

##### Trilha C — Infra/quality

- Docker;
- CI;
- scans;
- observability;
- load tests.

##### Trilha D — Data/AI

- service skeleton;
- schemas;
- mock;
- dataset;
- evaluation;
- Ollama.

##### Trilha E — Docs

- ADRs;
- API;
- diagrams;
- current status;
- runbook.

---

#### 17. Sequências que não devem ser invertidas

##### Segurança

```text
tenant context
→ repository filtering
→ permissions
→ endpoints
→ UI
```

##### Upload

```text
document record
→ presigned URL
→ upload
→ confirmation
→ processing
```

##### Async

```text
domain transaction
→ outbox
→ dispatch
→ consume
→ idempotent effect
```

##### IA

```text
contract/schema
→ mock adapter
→ persistence
→ worker integration
→ real provider
→ evaluation
```

##### E2E

```text
stable API
→ deterministic seed
→ environment isolation
→ E2E
→ parallel execution
```

---

#### 18. Critérios de saída por wave

Uma wave somente encerra quando:

- acceptance criteria passam;
- lint/typecheck/build passam;
- testes adequados passam;
- migrations funcionam do zero;
- documentação é atualizada;
- logs e métricas existem;
- nenhum P0/P1 aberto;
- integração com main é verde;
- demo mínima é executável.

---

## Backlog base, DoR, DoD e checklists

### 07 — Backlog base, Definition of Ready e Definition of Done

#### 1. Épicos

##### EPIC-00 — Engineering foundation

- repository;
- scripts;
- CI;
- Docker;
- observability skeleton;
- quality gates;
- docs;
- agent harness.

##### EPIC-01 — Identity and tenancy

- tenants;
- branding;
- users;
- sessions;
- RBAC;
- portal boundaries.

##### EPIC-02 — Customer management

- customer;
- onboarding;
- detail;
- timeline.

##### EPIC-03 — Documents

- upload;
- download;
- processing;
- analysis;
- approval.

##### EPIC-04 — Requests

- state machine;
- assignment;
- SLA;
- comments;
- tasks.

##### EPIC-05 — Async platform

- domain event;
- outbox;
- worker;
- DLQ;
- retry;
- idempotency.

##### EPIC-06 — AI/data

- adapter;
- mock;
- service;
- Ollama;
- evaluation;
- human approval.

##### EPIC-07 — Pipeline and analytics

- opportunities;
- stages;
- metrics;
- dashboards.

##### EPIC-08 — Operations

- alerts;
- notifications;
- import;
- quotas;
- webhooks.

##### EPIC-09 — Hardening

- security;
- performance;
- resilience;
- release.

---

#### 2. Definition of Ready

Uma atividade está pronta para desenvolvimento quando:

- objetivo está claro;
- usuário/ator está definido;
- acceptance criteria existem;
- fora de escopo está explícito;
- dependências foram identificadas;
- contrato foi definido;
- risco de segurança foi avaliado;
- migration foi prevista;
- observabilidade foi prevista;
- estratégia de teste foi definida;
- design ou API mock existe quando necessário;
- tamanho cabe em um PR.

---

#### 3. Definition of Done

- código implementado;
- acceptance criteria validados;
- format/lint/typecheck;
- unit tests;
- integration conforme impacto;
- E2E conforme fluxo;
- security tests conforme risco;
- build;
- migration fresh;
- OpenAPI;
- logs/metrics;
- docs;
- ADR se decisão;
- nenhum segredo;
- revisão;
- CI verde;
- demo ou evidência.

---

#### 4. Checklist de backend

- [ ] use case explícito;
- [ ] autorização;
- [ ] tenant filter;
- [ ] validação;
- [ ] transação;
- [ ] idempotência;
- [ ] erro padronizado;
- [ ] log;
- [ ] métrica;
- [ ] audit event;
- [ ] unit test;
- [ ] integration test;
- [ ] OpenAPI;
- [ ] migration.

---

#### 5. Checklist de frontend

- [ ] loading;
- [ ] empty;
- [ ] error;
- [ ] permission;
- [ ] responsive;
- [ ] accessibility;
- [ ] form validation;
- [ ] cache invalidation;
- [ ] URL filters;
- [ ] component test;
- [ ] E2E crítico;
- [ ] analytics event opcional.

---

#### 6. Checklist de worker

- [ ] job ID;
- [ ] tenant;
- [ ] correlation;
- [ ] idempotency;
- [ ] retry;
- [ ] timeout;
- [ ] DLQ;
- [ ] structured log;
- [ ] duration metric;
- [ ] poison message;
- [ ] duplicate test;
- [ ] replay procedure.

---

#### 7. Checklist de IA

- [ ] port;
- [ ] schema;
- [ ] prompt version;
- [ ] input hash;
- [ ] timeout;
- [ ] fallback;
- [ ] tool allowlist;
- [ ] tenant;
- [ ] human approval;
- [ ] golden dataset;
- [ ] injection tests;
- [ ] metric;
- [ ] audit.

---

#### 8. Checklist de migration

- [ ] backward compatible;
- [ ] index;
- [ ] constraint;
- [ ] default;
- [ ] nullability;
- [ ] volume;
- [ ] lock risk;
- [ ] fresh install;
- [ ] upgrade;
- [ ] rollback/forward fix;
- [ ] data migration separate.

---

#### 9. Checklist de endpoint

- [ ] method;
- [ ] status;
- [ ] request;
- [ ] response;
- [ ] validation;
- [ ] auth;
- [ ] tenant;
- [ ] permission;
- [ ] idempotency;
- [ ] pagination;
- [ ] errors;
- [ ] OpenAPI;
- [ ] contract test;
- [ ] rate limit.

---

#### 10. Checklist de PR

- [ ] escopo pequeno;
- [ ] descrição;
- [ ] screenshots;
- [ ] testes;
- [ ] risk;
- [ ] migration;
- [ ] API change;
- [ ] rollout;
- [ ] observability;
- [ ] docs;
- [ ] no unrelated refactor.

---

#### 11. Backlog técnico contínuo

- arquitetura;
- flaky tests;
- dependências;
- vulnerabilities;
- slow queries;
- indexes;
- slow tests;
- logs;
- metrics;
- TODOs;
- dead code;
- quality grades;
- prompt regression;
- data retention;
- backup restore.

---

#### 12. Sugestões para entrevista

Prepare respostas para:

1. por que monólito modular;
2. como extrair um serviço;
3. como proteger multi-tenancy;
4. como garantir evento após commit;
5. como lidar com duplicidade;
6. como testar worker;
7. como trocar IA;
8. como impedir agente de agir fora da permissão;
9. como isolar E2E;
10. como medir qualidade;
11. como executar localmente;
12. como evoluir para escala;
13. como tratar migration;
14. como diagnosticar falha;
15. como proteger supply chain.
