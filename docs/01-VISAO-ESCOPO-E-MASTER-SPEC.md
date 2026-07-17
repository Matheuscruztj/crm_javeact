# AtlasOps AI — Visão, escopo e Master Spec

## Como usar este documento

Este é o ponto inicial da documentação. Ele concentra:

- contexto e proposta do produto;
- portais e usuários;
- módulos consolidados;
- limites de escopo;
- arquitetura de referência;
- critérios de sucesso;
- resumo executivo para entrevistas e retomada.

## Mapa dos cinco documentos

1. **Visão, escopo e Master Spec** — este documento.
2. **Requisitos e arquitetura** — requisitos funcionais, não funcionais, contratos e convenções.
3. **Planejamento e backlog** — waves, dependências, paralelização, DoR, DoD e checklists.
4. **Qualidade, testes e CI/CD** — quality gates, isolamento, segurança, performance e pipeline.
5. **Harness, agentes e runbook** — engenharia assistida por agentes, bootstrap, migração e referências.

---

## Contexto, visão e limites

### 00 — Contexto, visão e limites do produto

#### 1. Nome

**AtlasOps AI**

#### 2. Definição

Plataforma white-label B2B para gestão operacional, relacionamento com clientes, acompanhamento de solicitações, processamento de documentos, pipeline comercial, automação de atividades, analytics e assistência por IA.

#### 3. Problema

Muitas operações utilizam planilhas, mensagens, e-mails e sistemas desconectados para:

- cadastrar clientes;
- receber documentos;
- controlar solicitações;
- acompanhar responsáveis;
- medir SLA;
- gerenciar oportunidades;
- produzir relatórios;
- analisar riscos;
- informar o cliente.

O AtlasOps AI oferece uma base única e customizável, separando claramente o ambiente interno do ambiente consumido pelo cliente.

#### 4. Proposta de valor

```text
Admin gerencia a operação
→ cliente acompanha e fornece informações
→ workflows automatizam atividades
→ IA analisa e recomenda
→ analytics mede resultados
→ auditoria registra ações
```

#### 5. Usuários

##### 5.1 Internos

- Owner;
- Administrator;
- Manager;
- Analyst;
- Operator;
- Viewer.

##### 5.2 Externos

- Client Administrator;
- Client User;
- Client Viewer.

##### 5.3 Plataforma

- Super Administrator, opcional e restrito à administração SaaS.

#### 6. Portais

##### 6.1 Backoffice

Páginas principais:

```text
/admin/dashboard
/admin/customers
/admin/customers/:id
/admin/documents
/admin/requests
/admin/requests/:id
/admin/pipeline
/admin/tasks
/admin/alerts
/admin/workflows
/admin/insights
/admin/reports
/admin/audit
/admin/users
/admin/roles
/admin/settings
/admin/branding
/admin/usage
```

##### 6.2 Portal do cliente

```text
/portal/home
/portal/profile
/portal/documents
/portal/documents/upload
/portal/requests
/portal/requests/:id
/portal/reports
/portal/insights
/portal/notifications
/portal/settings
```

#### 7. Módulos consolidados

##### 7.1 Obrigatórios

- Authentication;
- Sessions;
- Tenants;
- Branding;
- Users;
- Roles and Permissions;
- Customers;
- Documents;
- Requests;
- Pipeline;
- Tasks;
- Audit;
- Dashboard;
- Admin frontend;
- Client frontend;
- Docker;
- Automated tests.

##### 7.2 Diferenciais incorporados

- worker assíncrono;
- eventos de domínio;
- outbox pattern;
- idempotência;
- AI service desacoplado;
- fallback determinístico;
- versionamento de prompt;
- human-in-the-loop;
- operational alerts;
- importação em lote;
- analytics comparativo;
- quotas por tenant;
- observabilidade;
- testes de carga;
- CI/CD;
- ADRs;
- data governance;
- notificações locais;
- webhooks;
- feature flags.

#### 8. Arquitetura de referência

```text
┌─────────────────┐       ┌─────────────────┐
│ Admin Frontend  │       │ Client Frontend │
└────────┬────────┘       └────────┬────────┘
         └────────────┬────────────┘
                      ▼
              ┌───────────────┐
              │ Backend API   │
              │ Modular       │
              └──────┬────────┘
                     │
      ┌──────────────┼───────────────┐
      ▼              ▼               ▼
 PostgreSQL        Redis           MinIO
      │              │               │
      └───────┬──────┴───────┬──────┘
              ▼              ▼
           Worker       AI/Data Service
                            │
                       Ollama/modelo
                       ou fallback
```

#### 9. Estilo arquitetural

##### MVP

- monólito modular;
- arquitetura hexagonal por módulo;
- DDD pragmático;
- banco relacional único com isolamento por tenant;
- worker separado;
- fila baseada em Redis ou RabbitMQ;
- storage compatível com S3;
- IA como serviço ou adapter externo.

##### Evolução

Extrair serviço apenas quando houver motivo operacional:

- escala independente;
- ownership independente;
- restrições de segurança;
- cadência de deploy diferente;
- tecnologia especializada;
- isolamento de falhas.

#### 10. Decisões herdadas dos projetos anteriores

##### Do projeto fintech menor

- comunicação síncrona e assíncrona;
- OAuth2/JWT ou sessão segura;
- arquitetura hexagonal;
- adapter para LLM;
- processamento batch;
- reporting;
- testes unitários, integração e E2E;
- pipeline de segurança;
- teste de carga.

##### Da POC fintech extensa

- Docker Compose offline-first;
- MinIO;
- Redis;
- MailHog;
- Prometheus, Grafana e Loki;
- IA local com fallback;
- K6;
- analytics;
- notification module;
- data governance;
- estrutura de infraestrutura e scripts.

Não são herdados para o MVP:

- dezenas de microsserviços;
- banco por serviço;
- Kafka obrigatório;
- Event Sourcing global;
- múltiplos bancos sem necessidade;
- serviços financeiros fora do domínio atual.

#### 11. Guardrails de escopo

Não implementar no primeiro ciclo:

- billing financeiro real;
- editor visual de workflow;
- Kubernetes obrigatório;
- service discovery;
- arquitetura com muitos serviços;
- chat genérico;
- múltiplos provedores de IA simultaneamente;
- BI completo;
- marketplace de integrações.

#### 12. Critério de sucesso do MVP

O MVP está pronto quando:

1. é executável localmente;
2. possui dois portais utilizáveis;
3. isola tenants;
4. aplica autorização no backend;
5. processa documentos assincronamente;
6. gera análise com IA ou fallback;
7. atualiza dashboard com dados reais;
8. registra auditoria;
9. passa pelos quality gates;
10. possui fluxo E2E reproduzível;
11. contém documentação suficiente para ser retomado sem a conversa original.

---

## Master Spec executiva

### 10 — Master Spec

#### 1. Produto

AtlasOps AI é uma plataforma white-label B2B com backoffice e portal do cliente.

#### 2. Capacidades

```text
Identity
Multi-tenancy
White-label
RBAC
Customers
Documents
Requests
Tasks
Pipeline
Audit
Async processing
Workflows
AI/Data
Alerts
Notifications
Analytics
Imports
Quotas
```

#### 3. Arquitetura

```text
Modular backend
+ Admin frontend
+ Client frontend
+ Worker
+ AI/Data service
+ PostgreSQL
+ Redis
+ MinIO
+ local observability
```

#### 4. Princípios

- framework-agnostic;
- domain-first;
- tenant-safe;
- asynchronous where useful;
- AI behind a port;
- human approval for critical actions;
- reproducible;
- observable;
- testable;
- gated;
- agent-friendly.

#### 5. Ordem de implementação

```text
Harness
→ Auth/Tenant
→ Customer
→ Documents
→ Requests
→ Async
→ AI
→ Pipeline/Analytics
→ Operations
→ Hardening
→ Release
```

#### 6. Gate mínimo de PR

```text
format
lint
typecheck
unit
build
migration
security basic
contract
```

#### 7. Gate mínimo de main

```text
integration
E2E smoke/full
Docker
image scan
artifact
```

#### 8. Gate mínimo de release

```text
security
load
SBOM
rollback
docs
demo
```

#### 9. Estratégia de testes

- unit para regra;
- integration para adapters;
- contract para fronteiras;
- E2E para jornadas;
- security para abuso;
- load para capacidade;
- AI tests para modelo, prompt e dados.

#### 10. Isolamento

Todo teste paralelo usa identificador único para:

- tenant;
- DB/schema;
- Redis;
- bucket;
- queue;
- usuário;
- arquivos.

#### 11. Harness engineering

- AGENTS.md;
- comandos estáveis;
- sandboxes;
- tarefas pequenas;
- quality sensors;
- agentes especializados;
- evidence-driven completion;
- revisão humana;
- maintenance agents.

#### 12. MVP em duas semanas

A prioridade não é implementar todos os opcionais. A prioridade é concluir uma vertical slice:

```text
Admin cria cliente
→ cliente abre solicitação
→ envia documento
→ worker processa
→ IA analisa/fallback
→ analista decide
→ dashboard atualiza
→ auditoria registra
```

#### 13. Narrativa de entrevista

O projeto demonstra:

- escolha consciente de monólito modular;
- segurança multi-tenant;
- autorização granular;
- consistência entre DB e eventos;
- idempotência;
- processamento assíncrono;
- teste isolado;
- observabilidade;
- IA governada;
- supply-chain;
- harness engineering;
- execução reproduzível.
