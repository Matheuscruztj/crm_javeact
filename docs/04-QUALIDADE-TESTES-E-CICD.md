# AtlasOps AI — Qualidade, testes e CI/CD

## Objetivo

Este documento concentra o sistema de validação do projeto:

- quality gates;
- comandos universais;
- pipeline de PR, main, nightly e release;
- testes unitários, integração, contrato e E2E;
- isolamento de ambientes;
- segurança;
- containers;
- performance;
- resiliência;
- testes de IA e dados;
- política de cobertura e flaky tests.

---

## Quality gates e CI/CD

### 04 — Quality gates e CI/CD

#### 1. Objetivo

Impedir que código inválido, inseguro, incompatível ou não testável avance entre etapas.

Quality gate não é apenas cobertura. É uma decisão automatizada baseada em evidências.

---

#### 2. Princípios

1. Feedback barato deve ocorrer primeiro.
2. Falhas determinísticas bloqueiam.
3. Testes lentos ficam depois dos rápidos.
4. O mesmo comando deve funcionar localmente e no CI.
5. Não reconstruir artefato entre promoção de ambientes.
6. Main deve estar sempre verde.
7. Flaky test é defeito.
8. Warning novo deve ser tratado.
9. Exceções de gate devem expirar.
10. Todo gate possui owner.

---

#### 3. Comando universal

O projeto deve oferecer:

```bash
make verify
```

ou equivalente, executando no mínimo:

```text
format-check
lint
typecheck/static-analysis
unit
build
migration-check
```

Comandos separados:

```bash
make bootstrap
make dev
make lint
make format-check
make typecheck
make build
make test-unit
make test-integration
make test-contract
make test-e2e
make test-load-smoke
make security-scan
make docker-build
make compose-up
make compose-down
make verify
make verify-full
```

---

#### 4. Gate G0 — Workspace

Executado antes de qualquer análise.

- versão de runtime correta;
- lockfile íntegro;
- dependências instaláveis;
- variáveis obrigatórias validadas;
- arquivos gerados atualizados;
- repositório sem arquivos secretos;
- migrations ordenadas;
- working tree limpo em release.

Bloqueia: sim.

---

#### 5. Gate G1 — Formatação

- formatter;
- line endings;
- arquivos finais;
- YAML/JSON válido;
- markdown lint opcional.

Bloqueia: sim.

Não alterar arquivos automaticamente no CI. O CI apenas verifica.

---

#### 6. Gate G2 — Lint e análise estática

##### Agnóstico

- imports inválidos;
- código morto;
- variáveis não usadas;
- padrões inseguros;
- dependências proibidas;
- ciclos;
- complexidade;
- erros.

##### Node

- ESLint;
- dependency-cruiser ou equivalente.

##### Go

- golangci-lint;
- go vet.

##### Java

- Checkstyle;
- SpotBugs;
- ArchUnit;
- PMD opcional.

Bloqueia:

- erros;
- novos warnings críticos;
- violação arquitetural.

---

#### 7. Gate G3 — Typecheck ou compile check

##### Node/TypeScript

```text
tsc --noEmit
```

##### Go

```text
go test -run=^$ ./...
go vet ./...
```

##### Java

```text
compile/testClasses
```

##### Regra

Typecheck não deve depender da execução de testes.

Bloqueia: sim.

---

#### 8. Gate G4 — Unit tests

Cobrir:

- domain rules;
- use cases;
- policies;
- state machines;
- value objects;
- calculations;
- idempotency logic;
- prompt parsing;
- workflow decisions.

Metas iniciais:

- zero falhas;
- zero skipped sem justificativa;
- duração curta;
- cobertura de linhas >= 75%;
- cobertura de branches >= 65%;
- módulos críticos com meta maior.

A meta não substitui mutation testing ou revisão de cenários.

---

#### 9. Gate G5 — Build

Validar:

- backend;
- frontend;
- worker;
- AI service;
- migrations;
- assets;
- OpenAPI;
- geração de client.

Build deve:

- ser reproduzível;
- não depender de internet além da resolução de dependências;
- gerar artefato versionado;
- produzir hash ou metadata.

Bloqueia: sim.

---

#### 10. Gate G6 — Migration verification

Executar:

1. banco vazio;
2. aplicar todas migrations;
3. validar schema;
4. seed mínimo;
5. opcionalmente downgrade;
6. detectar migration modificada após merge.

Testar:

- fresh install;
- upgrade da versão anterior;
- constraints;
- índices;
- tenant keys.

Bloqueia: sim.

---

#### 11. Gate G7 — Integration tests

Executar com infraestrutura real isolada:

- PostgreSQL;
- Redis;
- MinIO;
- fila;
- MailHog quando necessário.

Cobrir:

- repositories;
- transactions;
- migrations;
- outbox;
- storage;
- auth;
- tenant filtering;
- workers.

Bloqueia: sim em PRs do módulo; obrigatório na main.

---

#### 12. Gate G8 — Contract tests

Contratos:

- OpenAPI compatibility;
- frontend/API;
- worker event schemas;
- AI service;
- webhooks;
- notification payloads.

Validar:

- schema;
- campos obrigatórios;
- enum;
- backward compatibility;
- versionamento de evento.

Bloqueia breaking change não aprovado.

---

#### 13. Gate G9 — E2E

Smoke em PR:

```text
login
create customer
open request
upload document
process job
view result
```

Full suite:

- main;
- nightly;
- release candidate.

Bloqueia release.

---

#### 14. Gate G10 — Security

##### Em PR

- secret scan;
- dependency scan;
- SAST;
- license policy;
- IaC scan;
- Dockerfile lint.

##### Em main/release

- image scan;
- DAST;
- SBOM;
- signature/provenance futura;
- ASVS checklist;
- authorization tests.

Bloqueia:

- vulnerabilidade crítica;
- segredo confirmado;
- licença proibida;
- falha de autorização;
- imagem crítica sem exceção.

---

#### 15. Gate G11 — Container

Validar:

- Docker build;
- usuário não-root;
- health check;
- image size budget;
- sem segredo;
- tag imutável;
- CVE scan;
- startup;
- graceful shutdown.

Bloqueia release.

---

#### 16. Gate G12 — Performance

##### Smoke por PR

- poucos usuários;
- thresholds amplos;
- regressão óbvia.

##### Load em main/nightly

- p95;
- error rate;
- throughput;
- worker lag;
- AI timeout;
- dashboard latency.

##### Budget

Uma regressão superior ao limite deve falhar ou abrir aprovação explícita.

---

#### 17. Gate G13 — Observabilidade

Smoke verifica:

- endpoint de metrics;
- correlation ID;
- structured log;
- health;
- readiness;
- trace quando habilitado;
- job metric.

---

#### 18. Gate G14 — AI/Data

- schema validation;
- golden dataset;
- prompt regression;
- injection cases;
- fallback;
- max latency;
- output completeness;
- model availability;
- human approval flow.

Não usar apenas snapshot literal para texto probabilístico. Avaliar estrutura e critérios.

---

#### 19. Gate G15 — Release readiness

Checklist:

- versão;
- changelog;
- migrations;
- backup;
- rollback;
- known issues;
- SBOM;
- image digest;
- smoke;
- E2E;
- load;
- security;
- docs;
- runbook;
- demo seed.

---

#### 20. Pipeline recomendado

```text
PR Fast
├── workspace
├── format
├── lint
├── typecheck
├── unit
├── build
├── migration
├── SAST/dependency/secret
└── contract

PR Integration
├── ephemeral infra
├── integration
├── E2E smoke
├── Docker
└── load smoke

Main
├── full integration
├── full E2E
├── image scan
├── publish artifact
├── SBOM
└── deploy staging

Nightly
├── full security
├── full load
├── mutation tests
├── flaky detection
├── architecture scan
└── dependency update report

Release
├── promote same artifact
├── production smoke
└── rollback verification
```

---

#### 21. Política de cobertura

Cobertura deve ser usada como detector.

##### Sugestão

| Tipo | Meta |
|---|---:|
| Global lines | 75% |
| Global branches | 65% |
| Domain/application | 85% |
| Security policies | 90% |
| Generated/config | excluído justificadamente |

##### Regras

- cobertura não pode cair no diff;
- código novo crítico exige teste;
- exclusões documentadas;
- mutation testing em módulos de domínio.

---

#### 22. Flaky test policy

- retry não esconde falha;
- retry serve para diagnóstico;
- teste flaky é quarantined com issue e prazo;
- nenhuma release com teste crítico em quarantine;
- medir taxa de flakiness;
- corrigir causa: tempo, estado, porta, concorrência, dependência externa.

---

#### 23. Quality gate de arquitetura

Verificar automaticamente:

- domain não importa infrastructure;
- presentation não acessa banco;
- módulos não criam ciclos;
- shared não vira depósito;
- API não chama adapter concreto;
- frontend não importa camada interna indevida;
- AI provider está atrás de port.

---

#### 24. Supply chain

Aplicar progressivamente:

- lockfiles;
- pin de actions/plugins;
- SBOM;
- imagem por digest;
- build em runner confiável;
- provenance;
- assinatura;
- controle de dependências;
- atualização automática revisada.

---

#### 25. Exceções

Uma exceção deve conter:

- gate;
- motivo;
- risco;
- owner;
- compensação;
- prazo;
- issue;
- aprovação.

Exceção permanente não é aceita sem ADR.

---

## Estratégia de testes e isolamento

### 06 — Estratégia de testes e isolamento

#### 1. Objetivo

Criar uma suíte confiável, rápida e paralelizável que valide:

- domínio;
- integração;
- contratos;
- segurança;
- concorrência;
- fluxos;
- dados;
- IA;
- infraestrutura.

---

#### 2. Portfólio de testes

Não adotar somente uma pirâmide rígida. Usar diferentes granularidades.

| Camada | Finalidade |
|---|---|
| Static | detectar problemas antes de executar |
| Unit | regras isoladas |
| Component | módulo com adapters controlados |
| Integration | integração real com dependências |
| Contract | compatibilidade entre componentes |
| E2E API | fluxo completo sem UI |
| E2E UI | jornadas críticas |
| Security | controles e abuso |
| Performance | latência e capacidade |
| Resilience | falhas e recuperação |
| AI/Data | qualidade de modelo, prompt e dados |

---

#### 3. Testes estáticos

- formatter;
- lint;
- typecheck;
- dependency rules;
- dead code;
- migration lint;
- OpenAPI lint;
- Dockerfile lint;
- IaC;
- secret scan.

---

#### 4. Unitários

Testar:

- entities;
- value objects;
- use cases;
- policies;
- state machines;
- calculations;
- parsers;
- serializers;
- retry policy;
- quota;
- idempotency hash.

##### Características

- sem DB;
- sem rede;
- sem relógio real;
- sem filesystem global;
- determinísticos;
- paralelos.

##### Test doubles

- fake repository;
- fake clock;
- deterministic ID generator;
- fake event publisher;
- fake AI;
- fake storage.

---

#### 5. Component tests

Testam um módulo com infraestrutura parcial.

Exemplo:

```text
Documents module
+ HTTP handler
+ application
+ repository real em banco efêmero
+ storage fake
```

São mais rápidos que E2E e capturam wiring.

---

#### 6. Integration tests

Usar serviços reais:

- PostgreSQL;
- Redis;
- MinIO;
- fila;
- MailHog.

Cobrir:

- migrations;
- transactions;
- constraints;
- repository queries;
- tenant filters;
- outbox;
- job consumption;
- upload;
- auth;
- cache.

---

#### 7. Contract tests

##### HTTP

- OpenAPI;
- frontend generated client;
- error schema;
- pagination;
- enums.

##### Eventos

Envelope:

```json
{
  "id": "...",
  "type": "document.uploaded.v1",
  "tenantId": "...",
  "aggregateId": "...",
  "occurredAt": "...",
  "correlationId": "...",
  "data": {}
}
```

##### AI service

- request schema;
- response schema;
- timeout;
- errors;
- fallback.

---

#### 8. E2E API

Fluxos:

##### E2E-001

```text
owner cria tenant
→ cria admin
→ admin cria cliente
→ cliente recebe usuário
```

##### E2E-002

```text
cliente abre solicitação
→ operador atribui
→ status muda
→ cliente visualiza
```

##### E2E-003

```text
cliente solicita upload
→ envia arquivo
→ confirma
→ outbox
→ worker
→ análise
→ tarefa
→ dashboard
```

##### E2E-004

```text
oportunidade muda de etapa
→ histórico
→ forecast
→ dashboard
```

##### E2E-005

```text
usuário do tenant A tenta acessar recurso B
→ resposta indistinguível e segura
→ auditoria opcional
```

---

#### 9. E2E UI

Limitar às jornadas que precisam do navegador:

- login;
- troca de portal;
- upload;
- acompanhamento;
- pipeline drag/drop;
- branding;
- aprovação humana;
- sessão expirada.

Não duplicar todas as regras do backend.

---

#### 10. Isolamento de testes

##### Estratégia preferencial

Cada worker de teste recebe:

- banco próprio ou schema próprio;
- Redis prefix;
- bucket prefix;
- fila prefix;
- usuário;
- tenant;
- portas ou namespace.

##### Identificador

```text
TEST_RUN_ID
TEST_WORKER_ID
TEST_CASE_ID
```

##### Banco

Opções:

1. container por suíte;
2. database por worker;
3. schema por worker;
4. transaction rollback, apenas quando seguro.

Não usar transaction rollback para validar jobs em outro processo.

##### Redis

```text
key = test:{run_id}:{worker_id}:...
```

##### MinIO

```text
bucket = atlasops-tests-{run_id}
prefix = {worker_id}/{case_id}/
```

##### Filas

```text
queue = document-processing-{run_id}-{worker_id}
```

---

#### 11. Lifecycle

##### Suite

```text
provision
→ migrate
→ seed base
→ run
→ collect artifacts
→ destroy
```

##### Test

```text
create unique tenant
→ execute
→ assert
→ cleanup optional
```

Preferir dados únicos a cleanup frágil.

---

#### 12. Proibição de estado compartilhado

Evitar:

- cliente com e-mail fixo;
- tenant global;
- ordem alfabética;
- singleton mutável;
- porta fixa conflitante;
- bucket compartilhado;
- relógio real;
- fila sem namespace;
- banco local do desenvolvedor.

---

#### 13. Testes de concorrência

Cenários:

- duas atualizações optimistic locking;
- duas confirmações de upload;
- mesmo webhook duas vezes;
- consumer duplicado;
- mesma idempotency key;
- quota simultânea;
- duas movimentações de etapa;
- refresh token reutilizado.

---

#### 14. Testes de idempotência

Para cada operação:

1. primeira execução;
2. repetição igual;
3. repetição com payload diferente;
4. retry após timeout;
5. execução concorrente;
6. registro expirado.

---

#### 15. Testes de outbox

- evento salvo com aggregate;
- rollback não publica;
- dispatcher publica;
- falha incrementa attempts;
- duplicidade não duplica efeito;
- mensagem inválida vai para DLQ;
- correlation ID preservado.

---

#### 16. Testes de segurança

##### Autenticação

- senha incorreta;
- usuário inativo;
- sessão revogada;
- refresh replay;
- token expirado;
- brute force.

##### Autorização

Matriz papel x permissão x recurso.

##### Multi-tenancy

- IDOR;
- filtros;
- busca;
- dashboard;
- download;
- eventos;
- jobs;
- cache;
- IA.

##### Upload

- MIME falso;
- extensão dupla;
- arquivo grande;
- path traversal;
- chave previsível;
- arquivo não confirmado.

##### IA

- prompt injection;
- tool abuse;
- exfiltração;
- cross-tenant;
- output inválido;
- loop;
- timeout.

---

#### 17. Testes de performance

##### Smoke

- 1–5 VUs;
- 30–60 segundos;
- PR.

##### Load

- carga esperada;
- main/nightly.

##### Stress

- achar limite;
- manual ou agendado.

##### Soak

- vazamento;
- fila;
- conexões;
- memória.

##### Cenário de pico

```text
clientes abrem solicitações
+ upload de documentos
+ admins consultam dashboard
+ worker processa IA
```

---

#### 18. Resiliência

Simular:

- AI timeout;
- Redis indisponível;
- MinIO indisponível;
- worker reiniciado;
- webhook 500;
- evento duplicado;
- DB connection exhaustion;
- fila atrasada;
- poison message.

Assertar:

- resposta;
- retry;
- DLQ;
- estado;
- métrica;
- log;
- recuperação.

---

#### 19. AI/Data testing

##### Código

- unit;
- integration;
- API contract.

##### Dados

- schema;
- missing values;
- distribuição;
- leakage;
- duplicidade;
- tenant separation.

##### Modelo/prompt

- golden dataset;
- thresholds;
- output schema;
- factual constraints;
- refusal/safety;
- latency;
- fallback.

##### Exemplo de avaliação

```text
document_category_accuracy >= 0.85
required_fields_completion >= 0.95
invalid_json_rate = 0
p95 <= budget
```

---

#### 20. Mutation testing

Aplicar em:

- permission policy;
- state transitions;
- quota;
- calculations;
- idempotency;
- risk rules.

Não precisa executar em todo PR. Usar nightly ou por módulo.

---

#### 21. Test data builders

Criar builders:

```text
TenantBuilder
UserBuilder
CustomerBuilder
DocumentBuilder
RequestBuilder
OpportunityBuilder
```

Defaults válidos e overrides explícitos.

Evitar fixtures gigantes e opacas.

---

#### 22. Clock e IDs

Injetar:

- Clock;
- ID Generator;
- Random Generator.

Permite:

- SLA;
- expiração;
- retry;
- token;
- ordenação;
- reprodutibilidade.

---

#### 23. Test reports

Armazenar no CI:

- JUnit report;
- coverage;
- E2E traces;
- screenshots;
- videos;
- service logs;
- compose logs;
- K6 report;
- security report;
- AI evaluation report.

---

#### 24. Política de falha

- falha determinística bloqueia;
- flaky abre defeito;
- infra failure é distinguida;
- timeout gera diagnóstico;
- retries são limitados;
- testes críticos não são skipped.

---

#### 25. Matriz mínima por módulo

| Módulo | Unit | Integration | Contract | E2E | Security |
|---|---:|---:|---:|---:|---:|
| Auth | alto | alto | médio | alto | alto |
| Tenants | alto | alto | médio | alto | alto |
| Customers | alto | médio | médio | médio | médio |
| Documents | alto | alto | alto | alto | alto |
| Requests | alto | médio | médio | alto | médio |
| Pipeline | alto | médio | médio | médio | baixo |
| Workflows | alto | alto | alto | alto | médio |
| AI | médio | alto | alto | alto | alto |
| Analytics | alto | alto | médio | médio | médio |

---

## Manifesto de qualidade legível por automações

```yaml
project:
  name: AtlasOps AI
  architecture: modular-monolith
  portals:
    - admin
    - client
  processes:
    - api
    - worker
    - ai-service
quality_gates:
  pr_fast:
    - format-check
    - lint
    - typecheck
    - unit
    - build
    - migration-check
    - secret-scan
    - dependency-scan
    - contract
  pr_integration:
    - integration
    - e2e-smoke
    - docker-build
    - load-smoke
  main:
    - integration-full
    - e2e-full
    - image-scan
    - publish-artifact
    - sbom
  nightly:
    - dast
    - load
    - mutation
    - flaky-detection
    - architecture-scan
test_isolation:
  unique_per_run:
    - tenant
    - database_or_schema
    - redis_prefix
    - bucket_prefix
    - queue
    - user
    - file_prefix
required_commands:
  - bootstrap
  - dev
  - format-check
  - lint
  - typecheck
  - build
  - test-unit
  - test-integration
  - test-contract
  - test-e2e
  - test-load-smoke
  - security-scan
  - docker-build
  - compose-up
  - compose-down
  - verify
  - verify-full
```
