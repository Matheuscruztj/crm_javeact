# AGENTS.md — AtlasOps AI

> Última atualização: 2025-01-15
> Versão: 1.0.0

---

## Objetivo do Produto

O AtlasOps AI é um CRM inteligente multi-tenant com capacidades de análise de documentos via IA local (Ollama + pgvector). O sistema automatiza a classificação, extração e análise de documentos empresariais usando RAG (Retrieval-Augmented Generation) sem dependência de serviços cloud, garantindo privacidade e controle total dos dados.

O projeto é organizado como monorepo com backend Spring Boot 3.x, frontend React/Next.js e infraestrutura Docker Compose. Cada módulo de negócio segue arquitetura hexagonal (ports e adapters), mantendo o domínio isolado de frameworks externos e permitindo substituição de adapters sem impacto nas regras de negócio.

A engenharia é guiada por Spec Driven Development (SDD) com agentes de IA operando em sandboxes isolados, quality gates automatizados e human-in-the-loop obrigatório para ações críticas. Todo o fluxo de desenvolvimento prioriza segurança, reprodutibilidade e rastreabilidade.

---

## Arquitetura

**Tipo:** Monorepo com Gradle multi-project, arquitetura hexagonal por módulo.

**Padrão por módulo:**

```
presentation → application → domain ← infrastructure
                                ↑
                          shared-kernel
```

**Regras de dependência (validadas por ArchUnit):**

- `domain/` NÃO importa `infrastructure/`, `presentation/` nem frameworks externos
- `application/` NÃO importa `infrastructure/` nem `presentation/`
- `presentation/` depende apenas de `application/` e `shared-kernel`
- `infrastructure/` implementa ports definidos em `domain/`
- Sem ciclos entre módulos

**Estrutura do repositório:**

```
atlasops-ai/
├── backend/            # Gradle multi-project (18 módulos)
├── frontend/           # React/Next.js SPA
├── infra/              # Docker Compose, scripts, monitoring
├── docs/               # ADRs, runbooks, diagramas
├── shared/             # Artefatos compartilhados cross-stack
├── docs/codex/         # Specs SDD, steering, tasks, hooks
├── Makefile            # Comandos padronizados
└── AGENTS.md           # Este arquivo
```

---

## Módulos

| Módulo          | Responsabilidade                                                                                                                                     |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| `shared-kernel` | Tipos base (Entity, AggregateRoot, ValueObject, DomainEvent), ports compartilhados (Clock, IdGenerator, EventPublisher), value objects reutilizáveis |
| `auth`          | Autenticação JWT, sessão stateless, controle de acesso baseado em papéis                                                                             |
| `tenants`       | Gestão multi-tenant, isolamento de dados, configurações por tenant                                                                                   |
| `users`         | Cadastro de usuários, perfis, atribuição de papéis por tenant                                                                                        |
| `customers`     | Cadastro e gestão de clientes por tenant                                                                                                             |
| `documents`     | Upload, armazenamento (MinIO) e gestão de documentos                                                                                                 |
| `requests`      | Solicitações de serviço e workflow de aprovações                                                                                                     |
| `approvals`     | Fluxos de aprovação, tracking de decisões e event sourcing de estados                                                                                |
| `activities`    | Registro de atividades e timeline de eventos por entidade                                                                                            |
| `notifications` | Envio de notificações multi-canal e gerenciamento de preferências                                                                                    |
| `integrations`  | Conectores com sistemas externos, webhooks e MCP integration                                                                                         |
| `search`        | Busca unificada cross-entity com fallback OpenSearch/PostgreSQL                                                                                      |
| `imports`       | Importação de dados em lote com tracking de progresso                                                                                                |
| `operations`    | Operações administrativas, monitoramento e health checks do sistema                                                                                  |
| `ai`            | Integração Spring AI, RAG pipeline, análise com Ollama + pgvector                                                                                    |
| `analytics`     | Dashboards, relatórios e métricas de negócio                                                                                                         |
| `audit`         | Log de auditoria, rastreabilidade de ações                                                                                                           |
| `app-boot`      | Ponto de entrada Spring Boot, agrega módulos, configuração de infraestrutura                                                                         |
| `worker`        | Processo assíncrono separado para processamento pesado (documentos, IA, imports, notificações)                                                       |

### Worker Process

O Worker Process é uma aplicação Spring Boot independente responsável por executar processamento assíncrono pesado que não deve competir com a API principal por thread pool e memória. Ele consome tarefas de filas Redis e opera sobre os mesmos dados (PostgreSQL, MinIO) que o backend API, mas em processo isolado.

**Responsabilidades:**

- Processamento e extração de texto de documentos enviados via upload (PDF, DOCX, imagens)
- Análise de documentos via IA local (Ollama + RAG pipeline com pgvector)
- Geração de previews e thumbnails de documentos armazenados no MinIO
- Execução de importações de dados em lote com tracking de progresso e rollback parcial
- Envio de notificações multi-canal (email via MailHog em dev, push, in-app)
- Dispatch de webhooks outbound para integrações externas com retry e DLQ
- Projeções especializadas para bases de dados secundárias (OpenSearch, MongoDB, Neo4j, TimescaleDB, ClickHouse, EventStoreDB)

**Comando de execução:**

```bash
./gradlew :backend:worker:bootRun
```

**Relação com o backend API:**

O Worker opera como um processo completamente separado do backend API (`app-boot`). Enquanto o `app-boot` serve requisições HTTP síncronas (REST API), o Worker consome mensagens de filas Redis e executa operações de longa duração. Ambos compartilham a mesma base de dados PostgreSQL, o mesmo bucket MinIO e a mesma instância Redis, mas rodam em JVMs distintas. Essa separação garante que operações pesadas (análise de IA, imports massivos) não degradem a latência da API para os usuários finais.

---

## Comandos Disponíveis

| Comando                       | Descrição                                                                                                                                   |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `make bootstrap`              | Setup completo de ambiente novo (verifica Java 21+, Docker 24+, portas livres, instala deps, gera .env, inicia compose, executa migrations) |
| `make verify`                 | Execução sequencial de todos os quality gates: format-check → lint → compile → unit tests → spotbugs → build                                |
| `make test-unit`              | Executa apenas testes unitários (JUnit 5 + jqwik)                                                                                           |
| `make test-integration`       | Executa testes de integração (requer Docker Compose ativo)                                                                                  |
| `make build`                  | Compilação e empacotamento de todos os módulos                                                                                              |
| `make compose-up`             | Inicia infraestrutura local (PostgreSQL, Redis, MinIO, Ollama, Prometheus, Grafana, Loki, MailHog)                                          |
| `make compose-down`           | Para infraestrutura local                                                                                                                   |
| `make compose-reset`          | Remove volumes, recria containers do zero e executa migrations                                                                              |
| `make migrate`                | Executa migrations de banco de dados (Flyway)                                                                                               |
| `make seed`                   | Popula dados de demonstração (idempotente — seguro rodar várias vezes)                                                                      |
| `make format`                 | Formata código com Spotless                                                                                                                 |
| `make lint`                   | Executa verificações de lint (Checkstyle + SpotBugs)                                                                                        |
| `make doctor`                 | Diagnóstico completo do ambiente (Java, Docker, portas, env vars, conectividade)                                                            |
| `make compose-core`           | Inicia serviços core via Docker Compose profile (PostgreSQL, Redis, MinIO, Backend API, Worker)                                             |
| `make compose-advanced`       | Inicia core + serviços avançados (OpenSearch, MongoDB, Neo4j)                                                                               |
| `make compose-analytics`      | Inicia core + serviços de analytics (TimescaleDB, ClickHouse)                                                                               |
| `make compose-event-sourcing` | Inicia core + event sourcing (EventStoreDB)                                                                                                 |
| `make compose-observability`  | Inicia core + observabilidade (Prometheus, Grafana, Loki, Tempo, MailHog)                                                                   |
| `make compose-all`            | Inicia todos os serviços (todos os profiles)                                                                                                |
| `make test-functional`        | Executa testes funcionais Playwright (headless)                                                                                             |
| `make test-load-smoke`        | Executa teste de carga smoke com k6 (max 5 VUs, 60s)                                                                                        |
| `make test-load`              | Executa teste de carga médio com k6 (20 VUs, 2min)                                                                                          |

---

## Definição de Pronto

Uma tarefa é considerada **concluída** quando TODOS os itens abaixo forem satisfeitos:

- [ ] Código implementado conforme acceptance criteria da spec/task
- [ ] Testes unitários escritos e passando (`make test-unit`)
- [ ] Testes de propriedade (PBT) escritos e passando, quando aplicável
- [ ] Testes de integração passando, quando aplicável (`make test-integration`)
- [ ] `make verify` passa sem erros
- [ ] Cobertura de código atende metas (≥75% linhas, ≥65% branches; ≥85% linhas para módulos de domínio)
- [ ] Sem warnings de compilação
- [ ] Código formatado (`make format`)
- [ ] Lint sem erros (`make lint`)
- [ ] ArchUnit validações passando (sem ciclos, isolamento de domínio)
- [ ] Documentação atualizada (se interfaces públicas alteradas)
- [ ] PR vinculado à spec e task correspondente (`Spec: {feature-name}`, `Task: {N}`)
- [ ] Review humano aprovado

---

## Regras de Segurança

1. **Nenhum agente** pode acessar segredos de produção (`.env.production`, chaves de API reais, certificados)
2. **Nenhum agente** pode executar merge direto em branches protegidas (`main`, `release/*`, `hotfix/*`)
3. **Nenhum agente** pode executar comandos destrutivos fora de sandbox:
   - `DROP DATABASE`, `DROP TABLE`
   - `DELETE` sem cláusula `WHERE`
   - `TRUNCATE`
   - `rm -rf`
   - `git push --force` em branches protegidas
4. **Toda ação mutável** proposta por IA requer aprovação humana (human-in-the-loop)
5. **Tokens JWT** devem ter expiração configurada e rotação planejada
6. **Variáveis sensíveis** nunca são commitadas — usar `.env` (gitignored) e `.env.example` (sem valores reais)
7. **Dependências** com vulnerabilidades CRITICAL (CVSS ≥ 9.0) bloqueiam o build automaticamente (OWASP Dependency-Check)
8. **Logs** nunca devem conter dados sensíveis (senhas, tokens, dados pessoais não mascarados)
9. **Cross-sandbox access** é bloqueado — cada agente opera exclusivamente no namespace do seu run_id
10. **Tentativas de ações proibidas** são registradas em audit log com timestamp, agente, ação e motivo do bloqueio

---

## Padrões de Testes

### Testes Unitários (JUnit 5 + Mockito)

- **Naming:** `should_resultado_when_condicao` (ex: `should_rejectTask_when_objectiveIsEmpty`)
- **Builders:** Usar pattern Builder para fixtures (`TenantBuilder`, `UserBuilder`, `CustomerBuilder`, `TaskBuilder`)
- **Isolamento:** Mocking de ports via Mockito para isolar domínio
- **Clock/ID:** `Clock` e `IdGenerator` injetáveis (determinísticos em testes)
- **Localização:** `src/test/java/` dentro de cada módulo

### Testes de Propriedade (jqwik)

- **Framework:** [jqwik](https://jqwik.net/) — PBT nativo para JVM/JUnit 5
- **Iterações:** Mínimo 100 por propriedade (`@Property(tries = 100)`)
- **Tag:** `Feature: monorepo-sdd-harness, Property {N}: {título}`
- **Geradores:** Customizados por domínio (feature-names, agent tasks, sandbox configs)
- **Referência:** Cada teste referencia a propriedade do design document

### Testes de Integração (Testcontainers)

- **Requerem:** Docker Compose ativo
- **Isolamento:** Tenant isolado por teste
- **Cleanup:** Database limpo entre suítes
- **Timeout:** Máximo 30s por teste

### Cobertura (Jacoco)

| Escopo             | Linhas | Branches |
| ------------------ | ------ | -------- |
| Projeto total      | ≥ 75%  | ≥ 65%    |
| Módulos de domínio | ≥ 85%  | —        |

---

## Papéis de Agentes

### A1 — Planner

| Campo                 | Descrição                                                                                                                                                       |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Analisa issues, decompõe em tarefas, define acceptance criteria e plano de execução                                                                             |
| **Responsabilidades** | Analisar requisitos; criar specs SDD (requirements.md, design.md, tasks.md); definir acceptance criteria verificáveis; estimar complexidade; identificar riscos |
| **Saídas**            | Spec completa no formato SDD; plano de tarefas com dependências; lista de riscos                                                                                |
| **Permissões**        | Ler repositório inteiro; criar/editar arquivos em `docs/codex/specs/`; criar issues                                                                                  |
| **Restrições**        | Não pode alterar código de produção; não pode executar builds; não pode criar migrations                                                                        |

### A2 — Implementer

| Campo                 | Descrição                                                                                                                                      |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Implementa código conforme specs aprovadas, operando em sandbox isolado                                                                        |
| **Responsabilidades** | Escrever código de produção; criar testes unitários; respeitar arquitetura hexagonal; seguir steering files                                    |
| **Saídas**            | Código implementado; testes unitários; PR com referência à spec/task                                                                           |
| **Permissões**        | Ler repositório; alterar código em módulos especificados na task; executar builds e testes; operar dentro do sandbox                           |
| **Restrições**        | Não pode alterar migrations em produção; não pode modificar configuração de segurança; não pode acessar outros sandboxes; não pode fazer merge |

### A3 — Test_Engineer

| Campo                 | Descrição                                                                                                                       |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Cria e mantém testes automatizados, valida cobertura e qualidade dos testes                                                     |
| **Responsabilidades** | Escrever testes de propriedade (jqwik); testes de integração; validar cobertura; identificar cenários não cobertos              |
| **Saídas**            | Testes de propriedade; testes de integração; relatório de cobertura; gaps identificados                                         |
| **Permissões**        | Ler repositório; criar/editar arquivos de teste; executar testes; acessar Docker Compose para integration tests                 |
| **Restrições**        | Não pode alterar código de produção (exceto para corrigir bugs encontrados em testes, com aprovação); não pode criar migrations |

### A4 — Reviewer

| Campo                 | Descrição                                                                                                               |
| --------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Revisa código, valida conformidade com padrões e aprova ou rejeita PRs                                                  |
| **Responsabilidades** | Revisar PRs; validar aderência a steering files; verificar cobertura de testes; avaliar legibilidade; sugerir melhorias |
| **Saídas**            | Review comments; aprovação/rejeição de PR; sugestões de refatoração                                                     |
| **Permissões**        | Ler repositório; comentar em PRs; aprovar/solicitar mudanças                                                            |
| **Restrições**        | Não pode alterar código diretamente; não pode fazer merge sem aprovação humana; não pode ignorar quality gates          |

### A5 — Security_Agent

| Campo                 | Descrição                                                                                                                                                    |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Propósito**         | Valida aspectos de segurança do código e configurações                                                                                                       |
| **Responsabilidades** | Executar OWASP Dependency-Check; revisar configurações de autenticação/autorização; validar que segredos não estão expostos; verificar sanitização de inputs |
| **Saídas**            | Relatório de vulnerabilidades; alertas de segurança; recomendações de correção                                                                               |
| **Permissões**        | Ler repositório; executar scans de segurança; acessar relatórios de dependências                                                                             |
| **Restrições**        | Não pode alterar código de produção; não pode acessar segredos reais; não pode desabilitar quality gates de segurança                                        |

### A6 — Architecture_Agent

| Campo                 | Descrição                                                                                                            |
| --------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Valida conformidade arquitetural e propõe evoluções                                                                  |
| **Responsabilidades** | Executar ArchUnit tests; validar regras de dependência entre módulos; revisar novas interfaces públicas; propor ADRs |
| **Saídas**            | Relatório ArchUnit; ADR drafts; alertas de violação arquitetural                                                     |
| **Permissões**        | Ler repositório; executar testes de arquitetura; criar arquivos em `docs/adr/`                                       |
| **Restrições**        | Não pode alterar código de produção diretamente; não pode aprovar violações arquiteturais                            |

### A7 — Migration_Agent

| Campo                 | Descrição                                                                                                                                      |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Cria e valida migrations de banco de dados                                                                                                     |
| **Responsabilidades** | Criar scripts de migration (Flyway); validar idempotência; testar rollback; verificar compatibilidade backward                                 |
| **Saídas**            | Scripts de migration; evidência de teste em sandbox; documentação de mudanças no schema                                                        |
| **Permissões**        | Ler repositório; criar arquivos de migration; executar migrations em sandbox; acessar banco de dados do sandbox                                |
| **Restrições**        | Não pode executar migrations em produção; não pode alterar migrations já aplicadas em produção; não pode executar DDL destrutivo sem aprovação |

### A8 — Documentation_Agent

| Campo                 | Descrição                                                                                                            |
| --------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Mantém documentação técnica atualizada e consistente                                                                 |
| **Responsabilidades** | Atualizar AGENTS.md; manter `docs/00-current-status.md`; criar/atualizar runbooks; documentar APIs (OpenAPI)         |
| **Saídas**            | Documentação atualizada; runbooks; changelog; API docs                                                               |
| **Permissões**        | Ler repositório; criar/editar arquivos em `docs/`; editar AGENTS.md (com revisão humana); editar READMEs             |
| **Restrições**        | Não pode alterar código de produção; não pode alterar configurações; alterações em AGENTS.md requerem revisão humana |

### A9 — SRE_Agent

| Campo                 | Descrição                                                                                                                 |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Monitora, diagnostica e mantém a saúde da infraestrutura                                                                  |
| **Responsabilidades** | Configurar alertas; manter dashboards Grafana; analisar logs (Loki); otimizar Docker Compose; criar runbooks de incidente |
| **Saídas**            | Dashboards; alertas configurados; runbooks de operação; relatórios de incidente                                           |
| **Permissões**        | Ler repositório; editar arquivos em `infra/monitoring/`; acessar métricas e logs; editar Docker Compose local             |
| **Restrições**        | Não pode alterar código de aplicação; não pode modificar `docker-compose.prod.yml`; não pode acessar dados de produção    |

### A10 — Quality_Janitor

| Campo                 | Descrição                                                                                                             |
| --------------------- | --------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Mantém a saúde do código: formatação, lint, cobertura, dependências desatualizadas                                    |
| **Responsabilidades** | Executar `make format`; corrigir warnings de lint; atualizar dependências (patch/minor); remover código morto         |
| **Saídas**            | PRs de limpeza; relatório de health do código; lista de dependências desatualizadas                                   |
| **Permissões**        | Ler repositório; alterar código (formatação, lint fixes, dependency bumps); executar builds e testes                  |
| **Restrições**        | Não pode alterar lógica de negócio; não pode fazer upgrades major sem aprovação; não pode alterar interfaces públicas |

### A11 — AI_Evaluation_Agent

| Campo                 | Descrição                                                                                                                                |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| **Propósito**         | Avalia qualidade das respostas de IA, métricas de confiança e performance do RAG                                                         |
| **Responsabilidades** | Analisar scores de confiança; medir qualidade de chunking; avaliar relevância de resultados RAG; monitorar taxa de fallback              |
| **Saídas**            | Relatório de qualidade do RAG; métricas de performance; recomendações de tuning (prompts, chunk size, overlap)                           |
| **Permissões**        | Ler repositório; acessar registros de análise (AIAnalysisRecord); executar consultas de avaliação no sandbox                             |
| **Restrições**        | Não pode alterar prompts em produção; não pode modificar configurações de modelo sem aprovação; não pode acessar dados de clientes reais |

---

## Arquivos Protegidos

Os seguintes arquivos e diretórios **não podem ser alterados sem revisão humana explícita**:

| Arquivo/Diretório                                                    | Motivo                                         |
| -------------------------------------------------------------------- | ---------------------------------------------- |
| `AGENTS.md`                                                          | Documento central de governança do projeto     |
| `.env.production`                                                    | Segredos e configurações de produção           |
| `docker-compose.prod.yml`                                            | Infraestrutura de produção                     |
| `backend/*/src/main/resources/db/migration/V*` (já aplicadas)        | Migrations de produção — imutáveis após deploy |
| `infra/init-db/`                                                     | Scripts de inicialização do banco              |
| Configurações de segurança (`SecurityConfig.java`, `JwtConfig.java`) | Impacto direto na autenticação e autorização   |
| `settings.gradle.kts`                                                | Estrutura do monorepo                          |
| `gradle.properties`                                                  | Versões centralizadas de dependências          |
| `docs/codex/specs/*/requirements.md` (specs aprovadas)                    | Requisitos validados por humano                |

**Regra:** Qualquer PR que altere estes arquivos deve ter aprovação explícita de pelo menos um mantenedor humano antes do merge.

---

## Procedimentos

### Criar Migration

1. **Gerar arquivo de migration** com naming convention Flyway:

   ```bash
   # Formato: V{versão}__{descrição}.sql
   touch backend/app-boot/src/main/resources/db/migration/V$(date +%Y%m%d%H%M)__descricao_da_mudanca.sql
   ```

2. **Escrever o SQL** no arquivo criado (DDL e/ou DML):

   ```sql
   -- V20250115_1200__add_column_status_to_customers.sql
   ALTER TABLE customers ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
   CREATE INDEX idx_customers_status ON customers(status);
   ```

3. **Testar a migration localmente:**

   ```bash
   make compose-reset   # Recria ambiente do zero
   make migrate         # Aplica todas as migrations
   make test-integration  # Valida que testes de integração passam
   ```

4. **Validar idempotência** (rodar `make migrate` novamente não deve falhar).

5. **Documentar** a mudança de schema no PR (campos adicionados/removidos, índices, constraints).

---

### Rodar Ambiente Local

1. **Clonar o repositório:**

   ```bash
   git clone <url-do-repositorio>
   cd atlasops-ai
   ```

2. **Configurar variáveis de ambiente:**

   ```bash
   cp .env.example .env
   # Editar .env se necessário (portas, credenciais locais)
   ```

3. **Iniciar infraestrutura (Docker Compose):**

   ```bash
   make compose-up
   # Aguardar todos os serviços ficarem healthy (~30s)
   ```

4. **Executar verificação completa:**
   ```bash
   make verify
   ```

**Alternativa rápida (faz tudo automaticamente):**

```bash
make bootstrap
```

---

### Registrar ADR (Architecture Decision Record)

1. **Criar arquivo** seguindo o template:

   ```bash
   # Próximo número sequencial
   NEXT=$(ls docs/adr/ | grep -oP 'ADR-\K\d+' | sort -n | tail -1 | awk '{print $1+1}')
   cp docs/adr/ADR-001-template.md docs/adr/ADR-$(printf "%03d" $NEXT)-titulo-da-decisao.md
   ```

2. **Preencher o template** com:
   - **Título:** Nome descritivo da decisão
   - **Status:** Proposed | Accepted | Deprecated | Superseded
   - **Contexto:** Por que essa decisão é necessária
   - **Decisão:** O que foi decidido
   - **Consequências:** Impactos positivos e negativos
   - **Alternativas consideradas:** Opções avaliadas e motivos de rejeição

3. **Submeter PR** com a ADR para revisão do time.

---

### Provar Tarefa Concluída

Para evidenciar que uma tarefa foi concluída, apresente:

- [ ] **Spec referenciada:** Indicar `Spec: {feature-name}` e `Task: {número}`
- [ ] **Testes passando:** Output de `make test-unit` (e `make test-integration` se aplicável)
- [ ] **Quality gates:** Output de `make verify` sem erros
- [ ] **Cobertura:** Relatório Jacoco mostrando metas atingidas
- [ ] **PR criado:** Link para o PR com diff das mudanças
- [ ] **Acceptance criteria:** Cada critério da task verificado com evidência (screenshot, output de comando, ou teste automatizado)

---

## Fluxo de Orquestração

### Fluxo Padrão (multi-arquivo, interfaces, migrations ou security)

```
Issue → Planner (A1) → Human Approval → Implementer (A2) → Test_Engineer (A3)
    → Reviewer (A4) → Security/Architecture checks (A5/A6) → CI → Human Merge
```

**Detalhamento:**

1. **Issue criada** com descrição do problema/feature
2. **Planner (A1)** analisa, cria spec SDD, define tasks com acceptance criteria
3. **Human approval** do plano (obrigatório)
4. **Implementer (A2)** recebe task, provisiona sandbox, implementa código
5. **Test_Engineer (A3)** adiciona testes de propriedade e integração
6. **Reviewer (A4)** revisa código, valida padrões
7. **Security_Agent (A5)** executa scan de segurança
8. **Architecture_Agent (A6)** valida conformidade arquitetural
9. **CI pipeline** executa todos os quality gates
10. **Human merge** — aprovação final e merge por mantenedor humano

### Fluxo Simplificado (1 arquivo, sem interfaces/migrations/security)

```
Implementer (A2) → CI → Reviewer (A4) → Merge
```

**Condições para usar o fluxo simplificado:**

- Alteração em no máximo **1 arquivo**
- **Não modifica** interfaces públicas (ports, DTOs, contratos de API)
- **Não inclui** migrations de banco de dados
- **Não altera** configuração de segurança (auth, JWT, CORS, permissões)

Se qualquer condição acima não for atendida, usar o **fluxo padrão**.

---

## Modelo de Tarefa de Agente

Toda tarefa atribuída a um agente deve conter obrigatoriamente:

```yaml
task:
  objective: "Descrição do que deve ser feito (max 500 caracteres)"
  context:
    - "Módulos afetados"
    - "Arquivos relevantes"
  outOfScope:
    - "O que NÃO deve ser alterado"
  acceptanceCriteria:
    - "Critério verificável 1"
    - "Critério verificável 2"
  affectedInterfaces:
    - "Lista de contratos alterados" # ou "nenhuma"
  risks:
    - "Riscos identificados" # ou "nenhum identificado"
  requiredTests:
    - "make test-unit"
  validationCommands:
    - "make verify"
```

**Se qualquer campo obrigatório estiver ausente ou vazio, a tarefa será rejeitada antes da execução.**

---

## Sandbox — Convenção de Recursos

Cada tarefa de agente opera em sandbox isolado:

| Recurso                | Convenção                                        |
| ---------------------- | ------------------------------------------------ |
| Run ID                 | `{issue}-agent-{role}` (ex: `ATLAS-42-agent-A2`) |
| Branch                 | `sandbox/{run_id}`                               |
| Database               | `atlasops_{issue}` (ex: `atlasops_ATLAS_42`)     |
| Compose Project        | `atlasops_{issue}`                               |
| Bucket Prefix          | `{issue}/`                                       |
| TTL                    | 24 horas (cleanup automático)                    |
| Cleanup após conclusão | Máximo 5 minutos                                 |

---

## Stack Tecnológico

| Camada           | Tecnologia                                              |
| ---------------- | ------------------------------------------------------- |
| Linguagem        | Java 21                                                 |
| Framework        | Spring Boot 3.2+ / Spring Framework 6.x                 |
| Build            | Gradle (multi-project, Kotlin DSL)                      |
| Banco de dados   | PostgreSQL 16 + pgvector                                |
| Cache/Mensageria | Redis 7                                                 |
| Object Storage   | MinIO (S3-compatível)                                   |
| IA Local         | Ollama + Spring AI                                      |
| Vector Store     | pgvector (embeddings)                                   |
| Testes           | JUnit 5, jqwik, Mockito, ArchUnit, Testcontainers       |
| Qualidade        | Spotless, Checkstyle, SpotBugs, Jacoco, OWASP Dep-Check |
| Observabilidade  | Prometheus, Grafana, Loki, Micrometer                   |
| Containers       | Docker Compose                                          |
| Email (dev)      | MailHog                                                 |
