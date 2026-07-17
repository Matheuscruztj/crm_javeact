# AtlasOps AI — Requisitos e arquitetura

## Objetivo

Este documento reúne tudo que define **o que o sistema faz** e **como deve ser construído**, independentemente da stack adotada.

Ele contém:

- requisitos funcionais;
- requisitos não funcionais;
- arquitetura modular;
- contratos de API e eventos;
- modelo de dados;
- convenções de IDs, datas e erros;
- regras de segurança, multi-tenancy, IA e observabilidade.

---

## Requisitos funcionais

### 01 — Escopo e requisitos funcionais

#### 1. Convenções

- **RF:** requisito funcional.
- **MUST:** obrigatório no MVP.
- **SHOULD:** recomendado.
- **COULD:** opcional.
- Todos os recursos devem considerar `tenantId`, autorização e auditoria.

---

#### 2. Identidade e acesso

##### RF-001 — Login

**MUST**

O usuário deve poder autenticar-se com credenciais válidas.

Critérios:

- retorna sessão e token conforme estratégia;
- registra sucesso e falha;
- aplica rate limit;
- não revela se usuário existe;
- respeita usuário, tenant e sessão inativos.

##### RF-002 — Refresh de sessão

**MUST**

- renovar access token;
- rotacionar refresh token;
- impedir reutilização de token revogado;
- registrar dispositivo e sessão.

##### RF-003 — Logout

**MUST**

- revogar sessão atual;
- permitir revogar todas as sessões;
- registrar auditoria.

##### RF-004 — Recuperação de senha

**SHOULD**

- gerar token de uso único;
- expiração curta;
- invalidar sessões após troca;
- envio por MailHog no ambiente local.

##### RF-005 — Gestão de sessões

**SHOULD**

- listar dispositivos;
- informar última atividade;
- revogar sessão específica;
- registrar IP e user-agent.

---

#### 3. Multi-tenancy e white-label

##### RF-010 — Gestão de tenant

**MUST**

- criar tenant;
- ativar;
- suspender;
- consultar;
- atualizar informações;
- bloquear operações de tenant suspenso.

##### RF-011 — Isolamento

**MUST**

- todo recurso de negócio pertence a um tenant;
- nenhuma consulta pode retornar recurso de outro tenant;
- tokens não podem selecionar tenant arbitrariamente;
- super-admin deve usar fluxo explicitamente auditado.

##### RF-012 — Branding

**MUST**

Configuração por tenant:

- nome;
- logo;
- cor primária;
- cor secundária;
- favicon;
- título de login;
- e-mail de suporte;
- timezone;
- idioma;
- domínio ou subdomínio;
- textos institucionais.

##### RF-013 — Configuração de módulos

**SHOULD**

Ativar/desativar:

- pipeline;
- IA;
- workflows;
- relatórios;
- importação;
- portal cliente.

---

#### 4. Usuários e autorização

##### RF-020 — Usuários internos

**MUST**

- convidar;
- criar;
- editar;
- ativar;
- desativar;
- reenviar convite;
- associar ao tenant;
- atribuir papel.

##### RF-021 — Usuários de cliente

**MUST**

- associar a um cliente;
- limitar acesso ao próprio cliente;
- diferenciar administrador, usuário e viewer;
- impedir acesso ao backoffice.

##### RF-022 — Papéis

**MUST**

Papéis iniciais:

```text
OWNER
ADMIN
MANAGER
ANALYST
OPERATOR
VIEWER
CLIENT_ADMIN
CLIENT_USER
CLIENT_VIEWER
```

##### RF-023 — Permissões

**MUST**

Exemplos:

```text
customer:read
customer:create
customer:update
customer:disable
document:read
document:upload
document:download
document:approve
document:reject
request:read
request:create
request:update
request:assign
pipeline:read
pipeline:move
task:manage
workflow:execute
ai:analyze
alert:resolve
audit:read
settings:update
usage:read
```

##### RF-024 — Política contextual

**MUST**

A autorização deve considerar:

- tenant;
- papel;
- permissão;
- ownership;
- estado do recurso;
- tipo de portal.

---

#### 5. Clientes

##### RF-030 — Criar cliente

**MUST**

Dados mínimos:

- nome;
- tipo;
- documento;
- e-mail;
- telefone;
- status;
- responsável;
- tags.

##### RF-031 — Consultar clientes

**MUST**

- paginação;
- busca;
- filtro por status;
- filtro por tags;
- filtro por responsável;
- ordenação;
- exportação futura.

##### RF-032 — Detalhe do cliente

**MUST**

Exibir:

- dados;
- usuários externos;
- documentos;
- solicitações;
- oportunidades;
- tarefas;
- atividades;
- alertas;
- insights;
- métricas;
- auditoria relacionada.

##### RF-033 — Classificação de risco

**SHOULD**

- LOW;
- MEDIUM;
- HIGH;
- CRITICAL;
- justificativa;
- origem manual ou automática.

##### RF-034 — Onboarding

**SHOULD**

Estados:

```text
PENDING_DATA
PENDING_DOCUMENTS
UNDER_REVIEW
APPROVED
REJECTED
NEEDS_MORE_INFO
```

---

#### 6. Documentos e arquivos

##### RF-040 — Registro de documento

**MUST**

Criar registro antes do upload.

Estados:

```text
PENDING_UPLOAD
UPLOADED
PROCESSING
READY
APPROVED
REJECTED
FAILED
ARCHIVED
```

##### RF-041 — Upload seguro

**MUST**

- validar tamanho;
- validar extensão;
- validar MIME;
- gerar URL temporária;
- enviar diretamente ao storage;
- confirmar upload;
- calcular checksum;
- impedir arquivo duplicado quando aplicável.

##### RF-042 — Processamento

**MUST**

Após confirmação:

1. publicar evento;
2. criar job;
3. extrair metadados;
4. executar análise opcional;
5. persistir resultado;
6. atualizar status;
7. notificar;
8. registrar auditoria.

##### RF-043 — Download

**MUST**

- verificar permissão;
- verificar tenant;
- gerar URL temporária;
- registrar acesso;
- impedir download de arquivo inválido.

##### RF-044 — Aprovação e rejeição

**SHOULD**

- permitir decisão humana;
- exigir motivo de rejeição;
- notificar cliente;
- guardar análise e decisão separadamente.

##### RF-045 — Versionamento

**COULD**

- nova versão sem substituir original;
- indicar versão ativa;
- preservar histórico.

---

#### 7. Solicitações

##### RF-050 — Criar solicitação

**MUST**

Pode ser criada por cliente ou operador.

Campos:

- título;
- descrição;
- categoria;
- prioridade;
- cliente;
- responsável;
- SLA;
- anexos.

##### RF-051 — Estados

```text
OPEN
IN_PROGRESS
WAITING_CUSTOMER
WAITING_INTERNAL_REVIEW
APPROVED
REJECTED
CLOSED
CANCELLED
```

##### RF-052 — Transições

**MUST**

- validar transições;
- registrar autor;
- registrar data;
- registrar justificativa;
- disparar eventos.

##### RF-053 — Comentários

**SHOULD**

- comentário público;
- comentário interno;
- anexos;
- menção futura.

##### RF-054 — SLA

**SHOULD**

- prazo calculado;
- risco de atraso;
- violação;
- alertas;
- dashboard.

---

#### 8. Pipeline comercial

##### RF-060 — Leads e oportunidades

**SHOULD**

- criar lead;
- converter;
- criar oportunidade;
- associar cliente;
- atribuir responsável;
- informar valor;
- probabilidade;
- previsão de fechamento.

##### RF-061 — Etapas

```text
NEW
QUALIFIED
PROPOSAL
NEGOTIATION
WON
LOST
```

##### RF-062 — Movimentação

**SHOULD**

- validar etapa;
- registrar histórico;
- exigir motivo em perda;
- recalcular previsão;
- disparar workflow.

##### RF-063 — Indicadores

**SHOULD**

- valor total;
- valor ponderado;
- conversão;
- tempo por etapa;
- oportunidades paradas;
- ganhos e perdas.

---

#### 9. Tarefas e atividades

##### RF-070 — Tarefas

**SHOULD**

- criar;
- atribuir;
- priorizar;
- definir prazo;
- concluir;
- cancelar;
- associar a cliente, solicitação ou oportunidade.

##### RF-071 — Timeline

**MUST**

Consolidar:

- eventos;
- comentários;
- mudanças de status;
- documentos;
- ações de IA;
- tarefas;
- notificações.

---

#### 10. Auditoria e governança

##### RF-080 — Audit log

**MUST**

Eventos append-only contendo:

- tenant;
- actor;
- ação;
- recurso;
- IP;
- user-agent;
- correlation ID;
- metadata;
- timestamp.

##### RF-081 — Data access log

**SHOULD**

Registrar:

- download;
- visualização sensível;
- exportação;
- uso por agente.

##### RF-082 — Retenção

**COULD**

- política por tipo de dado;
- expiração;
- anonimização;
- exclusão definitiva controlada.

---

#### 11. Workflows

##### RF-090 — Execução

**SHOULD**

Workflows iniciais fixos e configuráveis por parâmetros.

Exemplos:

```text
DocumentUploaded
→ ProcessDocument
→ AnalyzeDocument
→ CreateReviewTask
→ NotifyAnalyst
```

```text
RequestCreated(priority=HIGH)
→ AssignAnalyst
→ CreateAlert
→ StartSLA
```

##### RF-091 — Rastreabilidade

**MUST quando workflows forem habilitados**

- execução;
- etapas;
- tentativas;
- estado;
- erro;
- entrada;
- saída;
- duração;
- retry.

##### RF-092 — Estados

```text
PENDING
RUNNING
WAITING
COMPLETED
FAILED
CANCELLED
```

---

#### 12. IA, predição e agentes

##### RF-100 — Análise de documento

**SHOULD**

Retornar:

- resumo;
- categoria;
- campos;
- riscos;
- pendências;
- confiança;
- recomendação.

##### RF-101 — Classificação de solicitação

**SHOULD**

- categoria;
- prioridade;
- sentimento opcional;
- motivo;
- confiança.

##### RF-102 — Predição comercial

**COULD**

- probabilidade de fechamento;
- risco de estagnação;
- fatores;
- recomendação.

##### RF-103 — Human-in-the-loop

**MUST para ação sensível**

A IA pode propor, mas a execução depende de:

- aprovação humana;
- autorização do backend;
- validação de estado;
- auditoria.

##### RF-104 — Fallback

**MUST**

Se o provedor estiver indisponível:

- não quebrar o fluxo principal;
- executar fallback;
- marcar resposta como fallback;
- emitir métrica;
- permitir reprocessamento.

##### RF-105 — Versionamento

**MUST**

Guardar:

- modelo;
- promptVersion;
- inputHash;
- duração;
- confiança;
- resultado;
- fallback;
- status.

##### RF-106 — Agente operacional

**COULD**

Ferramentas read-only iniciais:

- getCustomerSummary;
- getRequest;
- getDocumentAnalysis;
- getOperationalMetrics;
- getPipelineSummary.

Ferramentas mutáveis devem exigir aprovação.

---

#### 13. Alertas e notificações

##### RF-110 — Alertas operacionais

**SHOULD**

Tipos:

```text
REQUEST_OVERDUE
DOCUMENT_REJECTED
WORKFLOW_FAILED
AI_HIGH_RISK
OPPORTUNITY_STALLED
UPLOAD_FAILED
QUOTA_WARNING
```

##### RF-111 — Notificações

**SHOULD**

Canais:

- IN_APP;
- EMAIL;
- WEBHOOK.

##### RF-112 — Templates

**COULD**

- template por tenant;
- variáveis;
- idioma;
- preview.

---

#### 14. Analytics e relatórios

##### RF-120 — Dashboard administrativo

**MUST**

- clientes ativos;
- solicitações por status;
- solicitações atrasadas;
- documentos por status;
- tempo médio de processamento;
- pipeline;
- workflows com erro;
- uso de IA;
- alertas;
- comparação de período.

##### RF-121 — Dashboard do cliente

**MUST**

- solicitações;
- documentos pendentes;
- status;
- relatórios;
- notificações;
- insights autorizados.

##### RF-122 — Exportação

**COULD**

- CSV;
- PDF;
- relatório assíncrono.

---

#### 15. Importação em lote

##### RF-130 — ImportJob

**SHOULD**

- upload CSV;
- validação de schema;
- processamento assíncrono;
- progresso;
- erros por linha;
- relatório de falha;
- idempotência.

Tipos iniciais:

- customers;
- requests;
- opportunities.

---

#### 16. Planos, quotas e feature flags

##### RF-140 — Quotas

**SHOULD**

- usuários;
- clientes;
- storage;
- documentos;
- análises IA;
- jobs.

##### RF-141 — Uso

**SHOULD**

- consumo atual;
- percentual;
- alerta em 80%;
- bloqueio configurável em 100%.

##### RF-142 — Feature flags

**COULD**

- por ambiente;
- por tenant;
- desligamento emergencial;
- rollout futuro.

---

#### 17. Integrações

##### RF-150 — Webhooks de entrada

**COULD**

- segredo;
- assinatura;
- idempotência;
- replay protection;
- log;
- retry interno.

##### RF-151 — Webhooks de saída

**COULD**

- assinatura;
- tentativas;
- DLQ;
- visualização da entrega;
- reenvio.

---

#### 18. Fora do escopo inicial

- billing real;
- pagamentos;
- workflow designer visual;
- múltiplos modelos em produção;
- aplicativo mobile;
- microserviços completos;
- data warehouse;
- Kubernetes obrigatório;
- suporte real multirregião.

---

## Requisitos não funcionais e arquitetura

### 02 — Requisitos não funcionais e arquitetura

#### 1. Objetivo

Definir os atributos de qualidade, restrições e padrões técnicos que devem permanecer válidos independentemente da linguagem escolhida.

---

#### 2. RNF-001 — Portabilidade tecnológica

A arquitetura deve permitir implementação em diferentes stacks sem alterar:

- limites de módulos;
- contratos de API;
- modelo de domínio;
- eventos;
- quality gates;
- estratégia de testes;
- requisitos de segurança;
- observabilidade.

Frameworks são detalhes de infraestrutura.

---

#### 3. RNF-002 — Modularidade

O backend deve ser organizado por domínio, não por tipo técnico global.

```text
modules/
  auth/
  tenants/
  users/
  customers/
  documents/
  requests/
  pipeline/
  tasks/
  workflows/
  ai/
  analytics/
  audit/
```

Cada módulo deve possuir, conforme necessidade:

```text
domain/
application/
infrastructure/
presentation/
tests/
```

##### Regras

- domínio não depende do framework;
- domínio não depende de banco;
- adapters implementam ports;
- módulos expõem interfaces explícitas;
- importações entre módulos são controladas;
- ciclos de dependência são proibidos.

---

#### 4. RNF-003 — Multi-tenancy seguro

##### Estratégia inicial

Banco compartilhado e schema compartilhado, com `tenant_id`.

##### Controles obrigatórios

- tenant extraído de sessão confiável;
- filtro de tenant em repositórios;
- índices compostos;
- unique constraints por tenant;
- testes cross-tenant;
- auditoria de super-admin;
- nunca aceitar `tenantId` do body como autoridade.

##### Defesa em profundidade

Opcionalmente:

- Row-Level Security no PostgreSQL;
- contexto transacional de tenant;
- conexão/repositório tenant-aware.

---

#### 5. RNF-004 — Segurança

Baseline sugerido: OWASP ASVS compatível com aplicação web de risco moderado.

Controles:

- hash de senha forte;
- rotação de refresh token;
- proteção contra replay;
- RBAC;
- autorização de recurso;
- rate limit;
- validação de entrada;
- queries parametrizadas;
- upload seguro;
- segredos fora do código;
- headers de segurança;
- CORS restrito;
- CSRF conforme estratégia;
- logs sem segredos;
- erro sem stack em produção;
- dependências verificadas;
- imagens verificadas.

##### Ameaças obrigatórias no threat model

- IDOR;
- vazamento cross-tenant;
- escalada de privilégio;
- token roubado;
- upload malicioso;
- SSRF por integrações;
- prompt injection;
- exfiltração por agente;
- replay de webhook;
- duplicação de job;
- brute force;
- abuso de quota.

---

#### 6. RNF-005 — Performance

Metas iniciais locais, sem LLM:

- p95 leitura simples: <= 300 ms;
- p95 escrita simples: <= 500 ms;
- p95 dashboard agregado: <= 1 s;
- geração de URL: <= 500 ms;
- erro HTTP inesperado: < 1%.

Processos lentos devem ser assíncronos.

IA não compartilha o mesmo SLA das APIs transacionais.

---

#### 7. RNF-006 — Disponibilidade e degradação

O sistema deve continuar funcional quando:

- IA estiver indisponível;
- serviço de e-mail estiver indisponível;
- analytics estiver atrasado;
- webhook externo falhar.

Não deve aceitar operação quando:

- banco estiver indisponível;
- autorização não puder ser validada;
- storage não puder confirmar upload crítico.

##### Técnicas

- timeout;
- retry com backoff e jitter;
- circuit breaker quando necessário;
- DLQ;
- fallback;
- health checks;
- readiness;
- bulkhead opcional.

---

#### 8. RNF-007 — Consistência e idempotência

Aplicar idempotência em:

- criação de solicitação pública;
- confirmação de upload;
- análise IA;
- workflows;
- import jobs;
- webhooks;
- publicação de eventos.

##### Regra

```text
mesma key + mesmo request hash
→ mesma resposta

mesma key + request hash diferente
→ conflito
```

##### Outbox

Alteração de negócio e registro do evento devem ocorrer na mesma transação local.

---

#### 9. RNF-008 — Processamento assíncrono

Todo job deve possuir:

- ID;
- tipo;
- tenant;
- correlation ID;
- status;
- tentativas;
- timeout;
- data inicial;
- data final;
- erro normalizado;
- dead-letter após limite;
- idempotency key.

Consumers devem suportar entrega pelo menos uma vez.

---

#### 10. RNF-009 — Observabilidade

##### Logs

JSON estruturado contendo:

- timestamp;
- level;
- service;
- environment;
- tenantId;
- actorId;
- correlationId;
- traceId;
- event;
- resource;
- duration;
- errorCode.

##### Métricas

- request count;
- latency histogram;
- error count;
- job count;
- job duration;
- retries;
- DLQ;
- AI latency;
- fallback count;
- upload count;
- storage use;
- workflow failures;
- quota use.

##### Tracing

Obrigatório após múltiplos processos:

```text
frontend request
→ API
→ outbox
→ worker
→ AI service
```

##### Alertas

- taxa de erro;
- fila crescendo;
- job falhando;
- p95 acima da meta;
- storage indisponível;
- AI fallback elevado;
- quota próxima do limite.

---

#### 11. RNF-010 — Dados e privacidade

- dados sensíveis classificados;
- logs mascarados;
- exportação auditada;
- download auditado;
- soft delete;
- retenção configurável;
- backup;
- restore testado;
- timestamps em UTC;
- timezone aplicado apenas na apresentação;
- documentos privados por padrão.

---

#### 12. RNF-011 — Arquivos

- storage externo ao banco;
- nome original separado da chave;
- chave não previsível;
- checksum;
- MIME verificado;
- tamanho máximo;
- URL temporária;
- metadata no banco;
- limpeza de uploads abandonados;
- antivírus como evolução.

---

#### 13. RNF-012 — IA e dados

##### Adapter

O domínio depende de uma interface, não de Ollama ou fornecedor.

##### Reprodutibilidade

Persistir:

- versão do modelo;
- versão do prompt;
- parâmetros;
- input hash;
- output;
- latência;
- confidence;
- fallback;
- erro.

##### Testes de IA

- schema;
- segurança;
- regressão de prompt;
- dataset de avaliação;
- limite de custo;
- tempo;
- qualidade mínima.

##### Governança

- human-in-the-loop;
- ferramentas allowlisted;
- autorização no backend;
- limite de passos;
- limite de tokens;
- timeout;
- dados mínimos;
- proteção contra prompt injection.

---

#### 14. RNF-013 — Testabilidade

Todo módulo deve permitir:

- domínio sem infraestrutura;
- repositories fake;
- clocks injetáveis;
- IDs injetáveis;
- adapters substituíveis;
- fixtures;
- builders;
- execução determinística.

Proibido:

- depender do relógio global em testes;
- depender de ordem de testes;
- reutilizar banco sujo;
- depender de internet;
- compartilhar usuário fixo entre suítes paralelas;
- sleep arbitrário.

---

#### 15. RNF-014 — Developer Experience

Comandos padronizados:

```text
bootstrap
dev
lint
format-check
typecheck
build
test-unit
test-integration
test-e2e
test-contract
test-load-smoke
security-scan
docker-build
compose-up
compose-down
verify
```

Podem ser implementados com:

- Makefile;
- Taskfile;
- scripts;
- package manager;
- Gradle/Maven;
- Mage;
- just.

O nome dos comandos deve permanecer estável entre stacks.

---

#### 16. RNF-015 — CI/CD

- build reproduzível;
- lockfiles versionados;
- cache controlado;
- artifacts imutáveis;
- imagem identificada pelo commit;
- SBOM;
- scan de imagem;
- provenance futura;
- deploy separado de build;
- rollback documentado.

---

#### 17. RNF-016 — Compatibilidade de API

- versionamento;
- OpenAPI;
- erros padronizados;
- paginação consistente;
- datas ISO 8601;
- IDs opacos;
- breaking changes controladas;
- contract tests.

##### Erro

```json
{
  "type": "https://atlasops/errors/document-not-ready",
  "title": "Document is not ready",
  "status": 409,
  "code": "DOCUMENT_NOT_READY",
  "detail": "The document is still processing.",
  "traceId": "..."
}
```

---

#### 18. RNF-017 — Acessibilidade e frontend

- navegação por teclado;
- labels;
- foco;
- contraste;
- estados loading/error/empty;
- responsividade;
- rotas protegidas;
- cache previsível;
- formulários validados;
- autorização visual não substitui autorização do backend.

---

#### 19. RNF-018 — Manutenibilidade

##### Limites sugeridos

- complexidade ciclomática monitorada;
- arquivos e funções pequenos;
- cobertura de código como sinal, não objetivo único;
- duplicação controlada;
- dependências internas válidas;
- TODOs rastreáveis;
- warnings tratados;
- migrations revisadas.

##### Quality grade

Cada módulo pode receber nota baseada em:

- cobertura;
- dependências;
- complexidade;
- documentação;
- vulnerabilidades;
- falhas recentes;
- dívida.

---

#### 20. Modelo de implantação local

```text
api
web-admin
web-client
worker
ai-service
postgres
redis
minio
mailhog
prometheus
grafana
loki
```

Opcionais:

```text
jaeger
rabbitmq
sonarqube
zap
```

---

## Contratos, convenções e estrutura agnóstica

### 08 — Contratos, convenções e estrutura agnóstica

#### 1. Estrutura de repositório

```text
atlasops-ai/
├── apps/
│   ├── api/
│   ├── worker/
│   ├── web-admin/
│   ├── web-client/
│   └── ai-service/
├── packages/
│   ├── contracts/
│   ├── testing/
│   ├── observability/
│   └── config/
├── infra/
│   ├── docker/
│   ├── monitoring/
│   └── scripts/
├── docs/
│   ├── adr/
│   ├── diagrams/
│   └── runbooks/
├── tests/
│   ├── e2e/
│   ├── load/
│   └── security/
├── Makefile
├── docker-compose.yml
├── .env.example
├── AGENTS.md
└── README.md
```

Em stacks sem monorepo, preservar equivalência conceitual.

---

#### 2. Estrutura de módulo

```text
documents/
├── domain/
│   ├── entities/
│   ├── value-objects/
│   ├── events/
│   ├── services/
│   └── ports/
├── application/
│   ├── commands/
│   ├── queries/
│   ├── use-cases/
│   └── dto/
├── infrastructure/
│   ├── persistence/
│   ├── storage/
│   ├── messaging/
│   └── ai/
├── presentation/
│   ├── http/
│   └── consumers/
└── tests/
```

---

#### 3. Convenção de APIs

```text
/api/v1/auth
/api/v1/customers
/api/v1/documents
/api/v1/requests
/api/v1/opportunities
/api/v1/analytics
```

##### Paginação

```json
{
  "data": [],
  "page": {
    "number": 1,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

##### Cursor para timeline

```json
{
  "data": [],
  "nextCursor": "..."
}
```

---

#### 4. Eventos

##### Naming

```text
document.uploaded.v1
document.analysis.completed.v1
request.status.changed.v1
opportunity.stage.changed.v1
```

##### Envelope

```json
{
  "id": "evt_...",
  "type": "document.uploaded.v1",
  "version": 1,
  "tenantId": "ten_...",
  "aggregateType": "Document",
  "aggregateId": "doc_...",
  "occurredAt": "2026-07-17T12:00:00Z",
  "correlationId": "cor_...",
  "causationId": "cmd_...",
  "data": {}
}
```

---

#### 5. IDs

IDs opacos com prefixo opcional:

```text
ten_
usr_
cus_
doc_
req_
opp_
job_
evt_
ana_
```

Não expor sequência incremental quando isso ampliar enumeração.

---

#### 6. Datas

- UTC no backend e banco;
- ISO 8601 na API;
- timezone do tenant na UI;
- Clock injetável;
- sem datas locais ambíguas.

---

#### 7. Money e valores

Para pipeline:

```text
amountMinor
currency
```

Não usar float.

---

#### 8. Erros

Campos:

- type;
- title;
- status;
- code;
- detail;
- traceId;
- violations.

Códigos estáveis:

```text
TENANT_SUSPENDED
PERMISSION_DENIED
DOCUMENT_NOT_READY
INVALID_STATE_TRANSITION
IDEMPOTENCY_CONFLICT
QUOTA_EXCEEDED
AI_PROVIDER_UNAVAILABLE
```

---

#### 9. OpenAPI

- fonte versionada;
- lint;
- examples;
- auth;
- pagination;
- errors;
- idempotency;
- deprecation;
- generated client.

---

#### 10. Banco

Tabelas mínimas:

```text
tenants
tenant_branding
users
tenant_memberships
sessions
roles
permissions
role_permissions
membership_roles

customers
customer_users
documents
requests
request_status_history
comments
tasks
opportunities
opportunity_stage_history

outbox_events
jobs
idempotency_records
workflow_executions
workflow_steps
ai_analyses
operational_alerts
notifications
audit_events
data_access_logs
tenant_usage
plans
feature_flags
```

---

#### 11. Índices críticos

- `(tenant_id, id)`;
- `(tenant_id, status)`;
- `(tenant_id, created_at)`;
- unique `(tenant_id, document_number)`;
- outbox `(status, created_at)`;
- job `(status, available_at)`;
- idempotency `(tenant_id, operation, key)`;
- audit `(tenant_id, created_at)`.

---

#### 12. Status machines

Implementar transições em uma única política.

##### Document

```text
PENDING_UPLOAD → UPLOADED
UPLOADED → PROCESSING
PROCESSING → READY | FAILED
READY → APPROVED | REJECTED | ARCHIVED
FAILED → PROCESSING | ARCHIVED
```

##### Request

```text
OPEN → IN_PROGRESS
IN_PROGRESS → WAITING_CUSTOMER | WAITING_INTERNAL_REVIEW | APPROVED | REJECTED
WAITING_CUSTOMER → IN_PROGRESS | CANCELLED
APPROVED → CLOSED
REJECTED → CLOSED
```

---

#### 13. Configuração

Variáveis padronizadas:

```text
APP_ENV
APP_PORT
DATABASE_URL
REDIS_URL
OBJECT_STORAGE_ENDPOINT
OBJECT_STORAGE_BUCKET
JWT_ISSUER
JWT_AUDIENCE
AI_PROVIDER
AI_BASE_URL
OTEL_ENDPOINT
LOG_LEVEL
```

Validar no startup.

---

#### 14. Health

##### Liveness

Processo vivo.

##### Readiness

- DB;
- Redis, se obrigatório;
- migrations;
- storage, conforme operação;
- não incluir IA opcional como bloqueio total.

---

#### 15. Branch e commits

Sugestão:

- trunk-based;
- branches curtas;
- conventional commits opcional;
- PR pequeno;
- feature flags;
- main protegida.

---

#### 16. ADRs iniciais

```text
ADR-001 Modular monolith
ADR-002 Shared-schema multi-tenancy
ADR-003 Hexagonal module structure
ADR-004 Outbox
ADR-005 Redis/RabbitMQ before Kafka
ADR-006 Object storage
ADR-007 AI provider port and fallback
ADR-008 Human-in-the-loop
ADR-009 Test isolation
ADR-010 Quality gates
ADR-011 Observability
ADR-012 Supply-chain baseline
```
