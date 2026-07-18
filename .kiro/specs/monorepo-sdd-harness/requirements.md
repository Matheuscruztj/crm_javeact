# Requirements Document

## Introduction

Este documento define os requisitos para a fundação técnica do projeto AtlasOps AI: a reestruturação como monorepo, o setup do backend Spring Boot com Spring AI + RAG local, a implementação do fluxo de Spec Driven Development (SDD) e a configuração da infraestrutura de Harness Engineering com agentes de IA.

O foco é estabelecer a base que viabiliza todo o desenvolvimento posterior das waves 1-10 do projeto.

## Glossary

- **Monorepo**: Repositório único contendo múltiplos projetos (backend, frontend, infra, docs) com builds independentes mas configuração compartilhada
- **Build_System**: Gradle multi-project para módulos Java e pnpm workspaces para módulos frontend
- **Backend_API**: Aplicação Spring Boot 3.x que serve a API REST do AtlasOps AI
- **Modulo_Backend**: Subprojeto Gradle representando um domínio de negócio com arquitetura hexagonal interna
- **Spring_AI**: Framework Spring para integração com modelos de IA, incluindo suporte a RAG e vector stores
- **RAG_Pipeline**: Pipeline de Retrieval-Augmented Generation que combina busca vetorial com geração de texto via LLM local
- **Ollama**: Runtime local para execução de modelos de linguagem (LLMs) sem dependência de serviços cloud
- **Pgvector**: Extensão PostgreSQL para armazenamento e busca de embeddings vetoriais
- **SDD_Workflow**: Fluxo de Spec Driven Development com três fases: requisitos → design → tasks, com rastreamento de status
- **Harness_Engineering**: Conjunto de controles, ferramentas, instruções e ciclos de feedback que permitem que humanos e agentes modifiquem a base de código com segurança
- **Agente_AI**: Papel lógico ou job automatizado que executa uma função específica dentro do harness (Planner, Implementer, Reviewer, etc.)
- **Quality_Gate**: Conjunto de verificações automatizadas que devem passar antes de um PR ser integrado
- **Docker_Compose**: Ferramenta de orquestração local para levantar todos os serviços de infraestrutura do projeto
- **Steering_File**: Arquivo de convenções em .kiro/steering/ que guia o comportamento de agentes e ferramentas
- **Sandbox**: Ambiente isolado por agente/tarefa com banco, branch, container e portas exclusivos
- **Hexagonal_Architecture**: Padrão arquitetural onde o domínio é isolado de frameworks e infraestrutura por meio de ports e adapters
- **Upload_Service**: Componente responsável por receber, validar e persistir arquivos enviados pelo usuário no storage S3-compatível (MinIO)
- **Frontend_App**: Aplicação frontend SPA (Single Page Application) baseada em React/Next.js que consome a Backend_API
- **Design_System**: Conjunto de componentes reutilizáveis, tokens de design e convenções visuais que garantem consistência na interface do usuário
- **Responsive_Layout**: Layout que se adapta automaticamente a diferentes tamanhos de tela (mobile, tablet, desktop) usando breakpoints definidos

## Requirements

### Requisito 1: Estrutura de Diretórios do Monorepo

**User Story:** Como desenvolvedor, eu quero uma estrutura de monorepo organizada e padronizada, para que todos os artefatos do projeto (backend, frontend, infra, docs) coexistam em um único repositório com builds independentes.

#### Critérios de Aceitação

1. THE Build_System SHALL organizar o repositório raiz com os diretórios: `backend/`, `frontend/`, `infra/`, `docs/`, `shared/`, `.kiro/`, cada um presente como diretório não-vazio no nível raiz do repositório
2. WHEN o comando `./gradlew build` for executado no diretório raiz, THE Build_System SHALL compilar todos os módulos Java do backend e retornar exit code 0, sem warnings de compilação tratados como erro e sem falhas em testes unitários
3. THE Build_System SHALL configurar o Gradle multi-project com um arquivo `settings.gradle.kts` na raiz que declara cada subprojeto do backend por meio de `include()`, listando no mínimo os módulos correspondentes aos domínios definidos na arquitetura (auth, tenants, users, customers, documents, requests, pipeline, tasks, workflows, ai, analytics, audit)
4. THE Build_System SHALL manter um arquivo `gradle.properties` na raiz contendo, no mínimo, as versões de: linguagem Java/Kotlin, Spring Boot, e bibliotecas compartilhadas entre 2 ou mais módulos, cada uma declarada como propriedade nomeada no formato `nomeDaDependenciaVersion=X.Y.Z`
5. WHEN um novo módulo backend for adicionado, THE Build_System SHALL permitir sua inclusão alterando apenas o `settings.gradle.kts` (adicionando uma linha `include()`) e criando a pasta do módulo com um arquivo `build.gradle.kts` válido, de forma que o comando `./gradlew :<novoModulo>:build` retorne exit code 0 sem exigir alterações em outros módulos
6. THE Build_System SHALL conter um arquivo `docker-compose.yml` no diretório `infra/` que define, no mínimo, os serviços: postgres, redis, minio e mailhog, cada um com health check configurado e portas mapeadas para o host
7. THE Build_System SHALL manter o diretório `docs/` contendo pelo menos: um arquivo de arquitetura geral, um subdiretório `adr/` com no mínimo o ADR-001, um subdiretório `runbooks/` e um subdiretório `diagrams/`
8. THE Build_System SHALL configurar o diretório `.kiro/` com as subpastas `specs/`, `steering/` e `skills/` presentes como diretórios (podendo estar vazios) para suporte ao fluxo de Spec-Driven Development
9. IF o comando `./gradlew :<modulo>:build` for executado para um módulo individual, THEN THE Build_System SHALL compilar apenas o módulo especificado e suas dependências declaradas, sem recompilar módulos não relacionados, retornando exit code 0
10. IF o arquivo `settings.gradle.kts` referenciar um subprojeto cujo diretório não existe ou não contém `build.gradle.kts`, THEN THE Build_System SHALL falhar com exit code diferente de 0 e emitir mensagem indicando o módulo ausente

---

### Requisito 2: Módulos Backend com Arquitetura Hexagonal

**User Story:** Como arquiteto, eu quero que cada módulo do backend siga uma estrutura hexagonal padronizada, para que o domínio fique isolado de frameworks e adapters sejam facilmente substituíveis.

#### Critérios de Aceitação

1. THE Backend_API SHALL organizar cada Modulo_Backend com os pacotes internos: `domain/`, `application/`, `infrastructure/`, `presentation/`, `tests/`
2. THE Backend_API SHALL criar os módulos core iniciais: `auth`, `tenants`, `users`, `customers`, `documents`, `requests`, `pipeline`, `tasks`, `workflows`, `ai`, `analytics`, `audit`
3. THE Backend_API SHALL criar um módulo `shared-kernel` contendo: tipos base (Entity, AggregateRoot, ValueObject, DomainEvent), value objects reutilizados por 2 ou mais módulos, interfaces de ports compartilhados (Clock, IdGenerator, EventPublisher) e utilitários sem dependência de framework (validação de formato, conversão de tipos)
4. IF o código do pacote `domain/` de um Modulo_Backend importar classes de `infrastructure/`, `presentation/` ou de frameworks externos (exceto o módulo `shared-kernel`), THEN THE Build_System SHALL falhar na verificação de arquitetura via ArchUnit
5. IF o código do pacote `application/` de um Modulo_Backend importar classes de `infrastructure/` ou `presentation/`, THEN THE Build_System SHALL falhar na verificação de arquitetura via ArchUnit
6. THE Backend_API SHALL expor interfaces de port no pacote `domain/ports/` para cada dependência externa (banco, cache, storage, mensageria, IA)
7. IF um módulo consumir funcionalidade de outro módulo, THEN THE Backend_API SHALL utilizar exclusivamente interfaces públicas expostas pelo módulo provedor no pacote raiz ou em `domain/ports/`, sem acessar pacotes internos de implementação (`infrastructure/`, `persistence/`, `messaging/`)
8. IF um ciclo de dependência entre módulos for introduzido, THEN THE Build_System SHALL reportar erro na fase de verificação e impedir a conclusão do build
9. THE Backend_API SHALL restringir o pacote `presentation/` a depender apenas de `application/` (use cases e DTOs) e de `shared-kernel`, sem acessar diretamente o pacote `domain/entities/` ou `infrastructure/`

---

### Requisito 3: Configuração Spring Boot 3.x

**User Story:** Como desenvolvedor backend, eu quero um projeto Spring Boot 3.x configurado com todas as dependências base, para que eu possa iniciar o desenvolvimento dos módulos de negócio imediatamente.

#### Critérios de Aceitação

1. THE Backend_API SHALL utilizar Java 21 como versão mínima do runtime, rejeitando inicialização em versões anteriores
2. THE Backend_API SHALL utilizar Spring Boot 3.2 ou superior com Spring Framework 6.x como base, declarados explicitamente no arquivo de build
3. THE Backend_API SHALL configurar Spring Security com autenticação via token JWT assinado, sessão stateless (sem armazenamento de estado de sessão no servidor) e proteção CSRF desabilitada para endpoints de API
4. THE Backend_API SHALL configurar Spring Data JPA com Hibernate como provider JPA e PostgreSQL como banco de dados, com Hikari como connection pool e validação de conexão no startup
5. THE Backend_API SHALL configurar integração com Redis utilizando lettuce como client, validando conectividade no startup, para operações de cache e publicação/consumo de mensagens em filas
6. THE Backend_API SHALL configurar integração com MinIO como storage S3-compatível utilizando o SDK da AWS S3, validando acesso ao bucket configurado no startup
7. WHEN a aplicação iniciar, THE Backend_API SHALL validar que todas as variáveis de ambiente obrigatórias (APP_ENV, APP_PORT, DATABASE_URL, REDIS_URL, OBJECT_STORAGE_ENDPOINT, OBJECT_STORAGE_BUCKET, JWT_ISSUER, JWT_AUDIENCE, LOG_LEVEL) estão definidas e não vazias, e SHALL interromper a inicialização com mensagem de erro indicando o nome de cada variável ausente
8. WHEN uma requisição HTTP for recebida em `/actuator/health/liveness`, THE Backend_API SHALL retornar HTTP 200 se o processo estiver ativo, ou HTTP 503 caso contrário
9. WHEN uma requisição HTTP for recebida em `/actuator/health/readiness`, THE Backend_API SHALL verificar conectividade com PostgreSQL, Redis e MinIO, retornando HTTP 200 com status de cada dependência quando todas estiverem acessíveis, ou HTTP 503 indicando quais dependências falharam quando ao menos uma estiver inacessível
10. THE Backend_API SHALL emitir logs estruturados em formato JSON contendo obrigatoriamente os campos: timestamp (ISO 8601 UTC), level, service, environment, tenantId, actorId, correlationId, traceId, event, resource, duration (milissegundos), errorCode
11. WHEN uma requisição HTTP for recebida, THE Backend_API SHALL extrair o header `X-Correlation-ID` se presente, ou gerar um novo identificador único, e SHALL propagar esse valor no MDC do thread, em chamadas assíncronas, e no header `X-Correlation-ID` de respostas HTTP
12. IF a conexão com PostgreSQL não puder ser estabelecida durante o startup, THEN THE Backend_API SHALL interromper a inicialização em no máximo 30 segundos com mensagem de erro indicando falha de conexão com o banco de dados

---

### Requisito 4: Integração Spring AI com RAG Local

**User Story:** Como desenvolvedor, eu quero uma integração funcional do Spring AI com Ollama e pgvector, para que o sistema possa realizar análise de documentos com RAG usando modelos locais sem dependência de serviços cloud.

#### Critérios de Aceitação

1. THE Spring_AI SHALL integrar com Ollama como provedor de modelo de linguagem local via Spring AI Ollama starter, de modo que chamadas ao modelo retornem resposta textual válida com timeout configurável de no máximo 120 segundos por requisição
2. THE Spring_AI SHALL configurar pgvector como vector store para armazenamento e busca de embeddings, de modo que embeddings sejam persistidos e recuperáveis por consulta de similaridade com distância cosseno
3. THE RAG_Pipeline SHALL implementar um fluxo de ingestão que: recebe documento de até 50 MB, extrai texto, divide em chunks de no máximo 1000 tokens com sobreposição de 200 tokens, gera embeddings e armazena no pgvector associando cada chunk ao documento de origem
4. THE RAG_Pipeline SHALL implementar um fluxo de consulta que: recebe pergunta, busca no máximo 5 documentos relevantes no vector store com score de similaridade mínimo de 0.7, compõe prompt com contexto recuperado e submete ao modelo Ollama, retornando a resposta gerada e os identificadores dos chunks utilizados
5. THE Spring_AI SHALL expor a funcionalidade de IA por meio de uma interface de port (`AIAnalysisPort`) no pacote domain, sem dependência direta de Spring AI nas camadas de domínio
6. WHEN o Ollama estiver indisponível ou não responder dentro do timeout configurado, THE Spring_AI SHALL ativar um mecanismo de fallback que retorna resposta padrão contendo indicador `fallback=true`, o motivo da indisponibilidade, e emite métrica de contagem de fallback identificada por tipo de análise
7. IF o pgvector estiver indisponível durante uma consulta RAG, THEN THE Spring_AI SHALL retornar erro indicando indisponibilidade do vector store sem interromper outras funcionalidades do sistema
8. THE Spring_AI SHALL persistir para cada análise realizada: modelo utilizado, versão do prompt, hash SHA-256 do input, duração em milissegundos, score de confiança entre 0.0 e 1.0, indicador de fallback booleano, e resultado textual
9. THE Spring_AI SHALL suportar versionamento de prompts por meio de templates identificados por nome e versão numérica sequencial, permitindo selecionar qual versão ativa utilizar por tipo de análise
10. IF o resultado de uma análise de IA exigir ação mutável no sistema (criação, atualização ou exclusão de recurso), THEN THE Spring_AI SHALL criar um registro de aprovação pendente contendo: identificador da análise de origem, tipo de ação proposta, recurso alvo, payload da ação, e status PENDING_APPROVAL, que deve ser validado por usuário humano com permissão antes da execução
11. IF o documento submetido ao fluxo de ingestão não contiver texto extraível, THEN THE RAG_Pipeline SHALL registrar o documento com status FAILED e motivo indicando ausência de conteúdo textual, sem gerar embeddings

---

### Requisito 5: Docker Compose para Desenvolvimento Local

**User Story:** Como desenvolvedor, eu quero levantar toda a infraestrutura local com um único comando, para que eu possa desenvolver e testar sem configurar cada serviço manualmente.

#### Critérios de Aceitação

1. THE Docker_Compose SHALL definir serviços para: PostgreSQL (com pgvector), Redis, MinIO, Ollama, Prometheus, Grafana, Loki e MailHog, cada um com imagem versionada fixa (tag específica, não `latest`)
2. WHEN o comando `docker compose up` for executado no diretório `infra/`, THE Docker_Compose SHALL iniciar todos os serviços com portas configuráveis via `.env` e todos os serviços SHALL atingir estado healthy em no máximo 120 segundos
3. THE Docker_Compose SHALL configurar volumes nomeados para persistência de dados do PostgreSQL, Redis e MinIO entre reinicializações, de modo que dados inseridos antes de `docker compose stop` estejam disponíveis após `docker compose start`
4. THE Docker_Compose SHALL incluir health checks para cada serviço com intervalo de 10 segundos, timeout de 5 segundos e no máximo 12 retries, utilizando `depends_on` com condição `service_healthy` para que serviços dependentes aguardem predecessores
5. THE Docker_Compose SHALL fornecer um arquivo `.env.example` com todas as variáveis necessárias (portas, credenciais, URLs internas) e valores padrão seguros para ambiente local, contendo no mínimo as variáveis: `APP_PORT`, `DATABASE_URL`, `REDIS_URL`, `OBJECT_STORAGE_ENDPOINT`, `OBJECT_STORAGE_BUCKET`, `POSTGRES_PORT`, `REDIS_PORT`, `MINIO_PORT`, `GRAFANA_PORT`, `PROMETHEUS_PORT`, `MAILHOG_PORT` e `OLLAMA_PORT`
6. WHEN o PostgreSQL atingir estado healthy pela primeira vez, THE Docker_Compose SHALL criar automaticamente o banco de dados da aplicação com a extensão pgvector habilitada via script de inicialização montado em `/docker-entrypoint-initdb.d/`
7. WHEN o comando `make compose-reset` for executado, THE Docker_Compose SHALL remover todos os volumes nomeados do projeto, remover containers e recriar todos os serviços do zero, resultando em bancos sem dados, buckets vazios e caches limpos
8. THE Docker_Compose SHALL configurar uma rede bridge interna nomeada para comunicação entre serviços, expondo ao host apenas as portas definidas no `.env` e mantendo portas de comunicação interna (ex: porta interna do PostgreSQL entre serviços) inacessíveis diretamente do host
9. IF um serviço falhar no health check após esgotar todas as retries (12 tentativas), THEN THE Docker_Compose SHALL manter o serviço no estado unhealthy sem reinício automático, permitindo diagnóstico via `docker compose logs <serviço>`

---

### Requisito 6: Fluxo SDD (Spec Driven Development)

**User Story:** Como líder técnico, eu quero um fluxo estruturado de Spec Driven Development integrado ao repositório, para que cada feature passe por requisitos, design e tarefas antes da implementação.

#### Critérios de Aceitação

1. THE SDD_Workflow SHALL armazenar specs no diretório `.kiro/specs/{feature-name}/` com exatamente três arquivos: `requirements.md`, `design.md` e `tasks.md`, onde `{feature-name}` segue o padrão kebab-case com no máximo 50 caracteres
2. THE SDD_Workflow SHALL manter um arquivo `.config.kiro` em cada diretório de spec contendo os campos obrigatórios: `specId` (identificador único da spec), `workflowType` (valor: `sdd`) e `specType` (valor: `feature` ou `fix`)
3. THE SDD_Workflow SHALL definir cada task nos arquivos `tasks.md` com um status entre: `todo` (padrão para tasks novas), `in_progress`, `done` ou `blocked`, representado por checkbox markdown (`- [ ]` para todo/blocked, `- [x]` para done) seguido de indicador de status entre colchetes (ex: `- [ ] [blocked] Descrição`)
4. WHEN uma task mudar de status para `done`, THE SDD_Workflow SHALL atualizar o checkbox correspondente no `tasks.md` de `- [ ]` para `- [x]`, e WHEN mudar para `in_progress`, SHALL adicionar o marcador `[in_progress]` após o checkbox
5. THE SDD_Workflow SHALL manter Steering Files no diretório `.kiro/steering/` com arquivos separados para: convenções de codificação Java (`coding.md`), padrões de testes (`testing.md`), estrutura de módulos (`modules.md`) e convenções de commits (`commits.md`)
6. WHEN um novo PR for criado, THE SDD_Workflow SHALL permitir referência à spec e task correspondente na descrição do PR no formato `Spec: {feature-name}` e `Task: {número-da-task}`, possibilitando rastreabilidade entre código e especificação
7. THE SDD_Workflow SHALL suportar criação de hooks em `.kiro/hooks/` como arquivos JSON descrevendo regras de validação automatizada, onde cada hook contém: nome, evento gatilho, condição de aplicação (glob pattern de arquivos) e ação de validação
8. IF um diretório de spec não contiver os três arquivos obrigatórios (`requirements.md`, `design.md`, `tasks.md`) ou o `.config.kiro`, THEN THE SDD_Workflow SHALL considerar a spec como incompleta e indicar os arquivos faltantes quando consultada

---

### Requisito 7: AGENTS.md e Contexto do Projeto

**User Story:** Como agente de IA ou novo desenvolvedor, eu quero um arquivo AGENTS.md completo e atualizado, para que eu possa entender o projeto, suas regras e como contribuir sem depender de contexto externo.

#### Critérios de Aceitação

1. THE Harness_Engineering SHALL manter um arquivo `AGENTS.md` na raiz do repositório contendo as seguintes seções obrigatórias com headers markdown de nível 2: Objetivo do Produto (máximo 3 parágrafos), Arquitetura (tipo, módulos e diagrama textual), Módulos (lista com nome e responsabilidade de cada um), Comandos Disponíveis (tabela com comando e descrição), Definição de Pronto (checklist), Regras de Segurança (lista de restrições obrigatórias) e Padrões de Testes (convenções por tipo de teste)
2. THE Harness_Engineering SHALL listar no AGENTS.md, em formato de tabela markdown com colunas `Comando` e `Descrição`, todos os comandos padronizados: `bootstrap`, `verify`, `test-unit`, `test-integration`, `build`, `compose-up`, `compose-down`, `compose-reset`, `migrate`, `seed`, `format`, `lint`
3. THE Harness_Engineering SHALL documentar no AGENTS.md, sob a seção "Arquivos Protegidos", a lista de arquivos e diretórios que não podem ser alterados sem revisão humana explícita, incluindo no mínimo: migrations em produção, configuração de segurança, `AGENTS.md`, `.env.production` e `docker-compose.prod.yml`
4. THE Harness_Engineering SHALL incluir no AGENTS.md, sob a seção "Procedimentos", instruções passo-a-passo (com comandos executáveis) para: criar migration (no mínimo 3 passos), rodar ambiente local (no mínimo 4 passos incluindo clone, env, compose e verify), registrar ADR (template e local) e provar que uma tarefa foi concluída (checklist de evidências)
5. THE Harness_Engineering SHALL manter um arquivo `docs/00-current-status.md` com as seguintes seções obrigatórias: Última Atualização (data ISO 8601), Wave Atual (número e nome), Funcionalidades Concluídas (lista), Quality Gates Ativos (lista com status pass/fail), Pendências (lista), Problemas Conhecidos (lista com severidade) e Próximas Tarefas (lista ordenada por prioridade)
6. WHEN uma wave for concluída ou uma funcionalidade significativa for entregue, THE Harness_Engineering SHALL atualizar o campo "Última Atualização" do `docs/00-current-status.md` com a data corrente e atualizar as seções afetadas no mesmo commit ou PR da entrega

---

### Requisito 8: Papéis de Agentes e Orquestração

**User Story:** Como engenheiro de harness, eu quero papéis de agentes bem definidos com permissões e responsabilidades claras, para que cada agente opere dentro de limites seguros e contribua de forma previsível.

#### Critérios de Aceitação

1. THE Harness_Engineering SHALL definir os papéis de agentes: Planner (A1), Implementer (A2), Test_Engineer (A3), Reviewer (A4), Security_Agent (A5), Architecture_Agent (A6), Migration_Agent (A7), Documentation_Agent (A8), SRE_Agent (A9), Quality_Janitor (A10), AI_Evaluation_Agent (A11), cada um identificado por código único e com descrição de propósito em no máximo 200 caracteres
2. THE Harness_Engineering SHALL documentar para cada Agente_AI no arquivo AGENTS.md: responsabilidades (lista de ações permitidas), saídas esperadas (artefatos produzidos), permissões explícitas (ler repo, alterar código, criar migration, executar Docker) e restrições (ações proibidas), de forma que cada campo possua pelo menos um item concreto
3. THE Harness_Engineering SHALL definir o fluxo de orquestração padrão para mudanças que alteram mais de 1 arquivo ou afetam interfaces públicas: Issue → Planner → Human approval → Implementer → Test_Engineer → Reviewer → Security/Architecture checks → CI → Human merge
4. THE Harness_Engineering SHALL definir um fluxo simplificado para mudanças que alteram no máximo 1 arquivo, não modificam interfaces públicas, não incluem migrations e não alteram configuração de segurança: Implementer → CI → Reviewer → merge
5. WHEN um Agente_AI receber uma tarefa, THE Harness_Engineering SHALL exigir que a tarefa contenha os seguintes campos obrigatórios preenchidos: objetivo (descrição em até 500 caracteres), contexto (módulos e arquivos afetados), fora de escopo (limites explícitos), acceptance criteria (pelo menos 1 critério verificável), interfaces afetadas (lista de contratos ou "nenhuma"), riscos (lista ou "nenhum identificado"), testes obrigatórios (pelo menos 1 comando de teste) e comandos de validação (pelo menos 1 comando executável)
6. THE Harness_Engineering SHALL proibir que qualquer Agente_AI acesse segredos de produção, execute merge em branches protegidas ou execute comandos destrutivos (DROP, DELETE sem WHERE, rm -rf, truncate, force push) fora de sandbox
7. IF um Agente_AI receber uma tarefa com campos obrigatórios ausentes ou vazios, THEN THE Harness_Engineering SHALL rejeitar a tarefa e retornar indicação dos campos faltantes antes de qualquer execução
8. IF um Agente_AI tentar executar uma ação fora de suas permissões documentadas, THEN THE Harness_Engineering SHALL bloquear a ação, registrar a tentativa no log de auditoria com timestamp, agente, ação tentada e motivo do bloqueio, e notificar o responsável pela tarefa

---

### Requisito 9: Sandboxes e Isolamento por Tarefa

**User Story:** Como engenheiro de plataforma, eu quero que cada agente/tarefa opere em um sandbox isolado, para que trabalhos paralelos não interfiram entre si e erros sejam contidos.

#### Critérios de Aceitação

1. WHEN uma tarefa de agente for iniciada, THE Sandbox SHALL criar recursos exclusivos para a tarefa: branch Git exclusiva, banco de dados exclusivo, compose project exclusivo e bucket prefix exclusivo, todos provisionados antes do início da execução da tarefa
2. THE Sandbox SHALL seguir a convenção de naming: `run_id={issue}-agent-{role}`, `database=atlasops_{issue}`, `compose_project=atlasops_{issue}`, `bucket_prefix={issue}/`, onde `{issue}` corresponde ao identificador da issue e `{role}` ao código do agente (A1-A11)
3. WHEN uma tarefa de agente for concluída com sucesso ou cancelada explicitamente, THE Sandbox SHALL iniciar a limpeza automática dos recursos exclusivos criados em até 5 minutos após a mudança de estado da tarefa
4. THE Sandbox SHALL impedir que um Agente_AI acesse dados ou recursos de outro sandbox em execução simultânea, validando que cada operação de leitura e escrita utilize exclusivamente o namespace correspondente ao run_id da tarefa em execução
5. IF um Agente_AI tentar executar comando destrutivo (DROP, DELETE sem WHERE, rm -rf, truncate, force push) fora do seu sandbox, THEN THE Sandbox SHALL bloquear a execução, impedir qualquer efeito colateral e registrar a tentativa no log de auditoria com timestamp, run_id, comando bloqueado e agente responsável
6. IF a criação de qualquer recurso do sandbox falhar (branch, banco, compose project ou bucket), THEN THE Sandbox SHALL abortar a inicialização da tarefa, liberar os recursos parcialmente criados em até 2 minutos e registrar o erro com o recurso que falhou e o motivo
7. THE Sandbox SHALL definir um tempo máximo de vida (TTL) de 24 horas para cada sandbox ativo, após o qual os recursos são automaticamente marcados para limpeza independentemente do estado da tarefa

---

### Requisito 10: Quality Gates e Comandos de Build

**User Story:** Como desenvolvedor, eu quero quality gates automatizados e comandos padronizados, para que cada contribuição seja validada de forma consistente e problemas sejam detectados antes do merge.

#### Critérios de Aceitação

1. THE Quality_Gate SHALL implementar os seguintes comandos Gradle: `format` (Spotless), `lint` (Checkstyle/SpotBugs), `test` (JUnit 5 + Mockito), `build` (compilação e empacotamento), `verify` (execução sequencial de todos os gates)
2. THE Quality_Gate SHALL implementar verificação de arquitetura via ArchUnit que valida: ausência de ciclos entre módulos, isolamento do domínio (domain não importa infrastructure), regras de dependência entre camadas (presentation não acessa banco diretamente) e proibição de imports de adapters concretos pela camada de API
3. WHEN o comando `./gradlew verify` for executado, THE Quality_Gate SHALL executar na ordem estrita: format check → lint → compilation → unit tests → architecture check → build, completando a execução total em no máximo 10 minutos para o projeto completo em ambiente de CI
4. IF qualquer etapa do comando `verify` falhar, THEN THE Quality_Gate SHALL interromper a execução imediatamente, exibir na saída padrão o nome da etapa que falhou, o módulo afetado e a descrição do erro reportada pela ferramenta, e retornar código de saída diferente de zero
5. THE Quality_Gate SHALL suportar execução de testes de integração via `./gradlew integrationTest` que requer Docker Compose ativo com os serviços PostgreSQL, Redis e MinIO disponíveis
6. IF o comando `./gradlew integrationTest` for executado sem Docker Compose ativo, THEN THE Quality_Gate SHALL falhar com mensagem indicando quais serviços obrigatórios estão indisponíveis e listar o comando necessário para inicializá-los
7. THE Quality_Gate SHALL incluir scanning de segurança de dependências via plugin OWASP Dependency-Check ou equivalente, falhando o build quando vulnerabilidades de severidade CRITICAL (CVSS >= 9.0) forem detectadas
8. THE Quality_Gate SHALL configurar Jacoco para cobertura de código com relatório agregado por módulo, exigindo cobertura mínima de 75% de linhas e 65% de branches no total do projeto, e 85% de linhas para módulos de domínio
9. WHEN um PR for aberto ou atualizado, THE Quality_Gate SHALL executar no mínimo: format, lint, unit tests, build e architecture check, bloqueando o merge caso qualquer gate falhe
10. IF a cobertura de código no diff do PR for inferior à meta do módulo afetado, THEN THE Quality_Gate SHALL reportar a cobertura atual versus a meta exigida e bloquear o merge até que a meta seja atingida

---

### Requisito 11: Observabilidade Foundation

**User Story:** Como engenheiro SRE, eu quero uma base de observabilidade configurada desde o início, para que logs, métricas e health checks estejam disponíveis durante todo o desenvolvimento.

#### Critérios de Aceitação

1. THE Backend_API SHALL emitir logs estruturados em JSON contendo no mínimo os campos: timestamp, level, service, environment, tenantId, actorId, correlationId, event, resource, duration e errorCode, com integração Loki via Docker Compose
2. THE Backend_API SHALL expor métricas via Micrometer/Prometheus no endpoint `/actuator/prometheus` com tempo de resposta inferior a 500ms e formato de saída compatível com Prometheus text exposition format
3. THE Backend_API SHALL registrar métricas de: contagem de requests por endpoint e método HTTP, histograma de latência com buckets para p50/p95/p99, contagem de erros agrupados por código HTTP (4xx, 5xx) e por endpoint, e status de health checks (up/down) por dependência
4. WHEN uma requisição HTTP chegar sem o header `X-Correlation-ID`, THE Backend_API SHALL gerar um UUID v4 como correlation ID, propagá-lo em todas as chamadas internas e incluí-lo em cada linha de log associada à requisição
5. IF uma requisição HTTP chegar com o header `X-Correlation-ID` preenchido, THEN THE Backend_API SHALL reutilizar o valor recebido como correlation ID em toda a cadeia de processamento
6. WHEN um serviço do Docker_Compose não responder ao health check dentro de 30 segundos, THE Docker_Compose SHALL marcar o serviço como unhealthy e reiniciar automaticamente com limite máximo de 3 tentativas consecutivas e intervalo de 10 segundos entre tentativas
7. THE Docker_Compose SHALL disponibilizar dashboards pré-configurados no Grafana contendo no mínimo: painel de métricas da aplicação (request rate, latência p95, taxa de erros), painel de logs agregados filtráveis por correlationId e tenantId, e painel de status dos serviços com indicador up/down
8. WHEN o Backend_API receber uma requisição e a readiness probe retornar falha, THE Backend_API SHALL rejeitar a requisição com status HTTP 503
9. THE Backend_API SHALL implementar readiness probe no endpoint `/actuator/health/readiness` que verifica conectividade com PostgreSQL e Redis com timeout individual de 5 segundos por dependência, retornando status DOWN se qualquer verificação falhar ou exceder o timeout

---

### Requisito 12: Makefile e Scripts de Automação

**User Story:** Como desenvolvedor ou agente, eu quero comandos padronizados e estáveis via Makefile, para que operações comuns sejam executadas de forma consistente independentemente de quem as invoca.

#### Critérios de Aceitação

1. THE Build_System SHALL fornecer um Makefile na raiz do repositório com os targets: `bootstrap`, `verify`, `test-unit`, `test-integration`, `build`, `compose-up`, `compose-down`, `compose-reset`, `migrate`, `seed`, `format`, `lint`, `doctor`
2. WHEN o comando `make bootstrap` for executado em uma máquina nova, THE Build_System SHALL verificar pré-requisitos (Java 21+, Docker 24+, portas 5432/6379/9000/3000 livres), instalar dependências do projeto, gerar arquivo `.env` a partir do `.env.example` sem sobrescrever `.env` existente, iniciar containers via Docker Compose e executar migrations, completando com código de saída 0 em caso de sucesso
3. IF algum pré-requisito do `make bootstrap` não for atendido, THEN THE Build_System SHALL exibir mensagem indicando qual requisito falhou e a versão ou condição esperada, e encerrar com código de saída diferente de zero sem executar as etapas seguintes
4. WHEN o comando `make doctor` for executado, THE Build_System SHALL verificar e reportar individualmente: versão do Java (esperado 21+), Docker disponível e em execução, portas obrigatórias livres (5432, 6379, 9000, 3000, 8080), variáveis de ambiente obrigatórias definidas conforme `.env.example`, e conectividade com serviços em execução (PostgreSQL, Redis, MinIO), exibindo status OK ou FALHA para cada item
5. WHEN o comando `make verify` for executado, THE Build_System SHALL executar sequencialmente: format-check, lint, typecheck/compile, test-unit e build, interrompendo na primeira falha e retornando código de saída diferente de zero com indicação de qual etapa falhou
6. THE Build_System SHALL documentar cada target do Makefile com descrição acessível via `make help`, exibindo nome do target e descrição em uma linha por target
7. WHEN o comando `make seed` for executado, THE Build_System SHALL popular o banco com dados de demonstração incluindo: dois tenants (Alpha e Beta), ao menos um usuário de cada papel (OWNER, ADMIN, MANAGER, ANALYST, OPERATOR, VIEWER) por tenant, ao menos 3 clientes por tenant e ao menos 2 documentos de exemplo por cliente, de forma idempotente (executar múltiplas vezes produz o mesmo estado final sem duplicação de dados)
8. WHEN o comando `make compose-reset` for executado, THE Build_System SHALL parar todos os containers, remover volumes de dados persistentes (PostgreSQL, Redis, MinIO) e reiniciar os containers com estado limpo, executando migrations novamente

---

### Requisito 13: Steering Files e Convenções

**User Story:** Como líder técnico, eu quero convenções de código e arquitetura documentadas em steering files, para que agentes e desenvolvedores sigam os mesmos padrões sem necessidade de revisão manual repetitiva.

#### Critérios de Aceitação

1. THE Steering_File SHALL manter arquivo `.kiro/steering/java-conventions.md` documentando no mínimo: padrão de nomenclatura para classes (PascalCase), pacotes (lowercase), métodos e variáveis (camelCase), formato de nomenclatura de testes (should_resultado_when_condicao ou equivalente), e regras de organização de imports (ordem e agrupamento)
2. THE Steering_File SHALL manter arquivo `.kiro/steering/module-structure.md` com template de criação de novos módulos incluindo: lista de pacotes obrigatórios (domain, application, infrastructure, presentation, tests), classes base mínimas por pacote, e lista de testes mínimos exigidos para cada novo módulo (ao menos um teste unitário de domínio e um teste de integração de repositório)
3. THE Steering_File SHALL manter arquivo `.kiro/steering/api-conventions.md` documentando: padrão de versionamento de endpoints (`/api/v{n}/`), formato padronizado de erros (campos type, title, status, code, detail, traceId), formato de paginação (campos data, page com number/size/totalElements/totalPages), convenção de naming de endpoints (plural, kebab-case) e requisito de documentação OpenAPI por endpoint
4. THE Steering_File SHALL manter arquivo `.kiro/steering/testing-patterns.md` documentando: convenção de nomenclatura de testes, uso de builders (TenantBuilder, UserBuilder, CustomerBuilder), padrões de fixtures com defaults válidos, estratégia de isolamento de tenant por teste, e diretrizes de mocking de ports (quando usar fake vs mock)
5. THE Steering_File SHALL manter arquivo `.kiro/steering/git-conventions.md` documentando: formato de commits seguindo Conventional Commits (tipo(escopo): descrição com máximo de 72 caracteres no título), padrão de naming de branches (tipo/descricao-curta), tamanho máximo recomendado de PR (máximo 400 linhas alteradas excluindo arquivos gerados) e checklist de revisão com itens verificáveis
6. WHEN um Agente_AI iniciar uma tarefa de implementação no projeto, THE Steering_File SHALL estar disponível no diretório `.kiro/steering/` para carregamento como contexto, com cada arquivo contendo no cabeçalho a data de última atualização e versão do documento para rastreabilidade

---

### Requisito 14: Upload de Arquivos

**User Story:** Como usuário do sistema, eu quero fazer upload de arquivos (documentos, imagens) de forma segura e rastreável, para que eu possa associar evidências e documentos a registros do CRM.

#### Critérios de Aceitação

1. THE Upload_Service SHALL aceitar uploads de arquivos nos formatos: PDF, DOCX, XLSX, PNG, JPG, JPEG, GIF, WEBP e CSV, rejeitando qualquer arquivo cujo MIME type real (verificado por magic bytes) não corresponda a um dos formatos permitidos
2. THE Upload_Service SHALL impor limite máximo de 50 MB por arquivo individual e 200 MB por requisição de upload em lote (múltiplos arquivos), retornando HTTP 413 com mensagem indicando o limite excedido quando ultrapassado
3. WHEN um arquivo for submetido via endpoint `POST /api/v1/files/upload`, THE Upload_Service SHALL validar o tipo, tamanho e integridade do arquivo, gerar um identificador único (UUID v4) e persistir o conteúdo no MinIO com path no formato `{tenantId}/{ano}/{mes}/{fileId}.{extensao}`
4. WHEN um upload for iniciado, THE Upload_Service SHALL retornar imediatamente um identificador de operação e fornecer endpoint `GET /api/v1/files/{fileId}/status` que reporta o progresso com os campos: fileId, status (PENDING, UPLOADING, PROCESSING, COMPLETED, FAILED), percentComplete (0-100), createdAt e completedAt
5. THE Upload_Service SHALL registrar metadados para cada arquivo persistido: fileId, originalName, mimeType, sizeBytes, hash SHA-256, tenantId, uploadedBy (actorId), uploadedAt (timestamp ISO 8601), associatedResource (tipo e ID do recurso vinculado) e status
6. IF o arquivo submetido falhar na validação de tipo MIME (magic bytes não correspondem ao formato declarado), THEN THE Upload_Service SHALL rejeitar o upload com HTTP 422, registrar tentativa no log de auditoria com actorId e nome do arquivo, e retornar mensagem indicando o tipo detectado versus o tipo esperado
7. IF a conexão com MinIO estiver indisponível durante o upload, THEN THE Upload_Service SHALL retornar HTTP 503 com mensagem indicando indisponibilidade temporária do storage, registrar o erro no log e não corromper dados parcialmente escritos
8. WHEN um arquivo for armazenado com sucesso, THE Upload_Service SHALL gerar URL de download temporária (presigned URL) com validade configurável (padrão 1 hora, máximo 24 horas) acessível via `GET /api/v1/files/{fileId}/download`
9. THE Upload_Service SHALL suportar upload multipart (chunked) para arquivos acima de 10 MB, permitindo retomada de upload interrompido dentro de uma janela de 1 hora a partir do início do upload
10. IF o hash SHA-256 de um arquivo recém-enviado corresponder ao hash de outro arquivo já existente no mesmo tenant, THEN THE Upload_Service SHALL reutilizar o blob armazenado (deduplicação) e criar apenas novo registro de metadados apontando para o mesmo objeto no storage

---

### Requisito 15: Frontend com Design Responsivo

**User Story:** Como usuário, eu quero acessar o sistema com boa experiência em dispositivos móveis, tablets e desktops, para que eu possa operar o CRM independentemente do dispositivo utilizado.

#### Critérios de Aceitação

1. THE Frontend_App SHALL implementar Responsive_Layout utilizando breakpoints consistentes: mobile (< 640px), tablet (640px – 1023px), desktop (1024px – 1279px) e wide (≥ 1280px), garantindo que todos os componentes da interface se adaptem sem scroll horizontal em qualquer breakpoint
2. THE Frontend_App SHALL renderizar navegação principal como sidebar colapsável em desktop e wide, e como menu hamburger com drawer overlay em mobile e tablet, mantendo acesso a todas as rotas em qualquer breakpoint
3. THE Frontend_App SHALL garantir que tabelas de dados em viewports mobile e tablet utilizem padrão responsivo (card stack, horizontal scroll contido ou coluna colapsável) de modo que todas as colunas essenciais permaneçam acessíveis sem necessidade de zoom
4. THE Frontend_App SHALL implementar componentes de formulário que ocupem largura total em mobile, duas colunas em tablet e até três colunas em desktop/wide, mantendo labels, inputs e mensagens de validação legíveis em todos os breakpoints
5. THE Frontend_App SHALL garantir que áreas de toque (botões, links, toggles) tenham dimensão mínima de 44x44 pixels em viewports mobile e tablet conforme diretrizes WCAG 2.5.5, com espaçamento mínimo de 8px entre elementos interativos adjacentes
6. WHEN o viewport mudar de tamanho (resize ou rotação de dispositivo), THE Frontend_App SHALL reajustar o layout em tempo real sem necessidade de reload da página, mantendo estado do formulário e posição de scroll preservados
7. THE Frontend_App SHALL carregar assets otimizados por viewport: imagens responsivas via `srcset` com variantes para 1x e 2x de densidade de pixel, e fontes com subset reduzido para reduzir tempo de carregamento em conexões móveis
8. IF o viewport for inferior a 640px e o conteúdo incluir gráficos ou dashboards, THEN THE Frontend_App SHALL apresentar versão simplificada dos gráficos (valores numéricos resumidos ou gráfico compacto) em vez de ocultar completamente a informação
9. THE Frontend_App SHALL atingir score mínimo de 90 no Lighthouse para a métrica "Performance" em simulação mobile (throttled 4G, CPU 4x slowdown) na página principal e nas 3 páginas mais acessadas (listagem de clientes, detalhe de cliente, dashboard)
10. THE Frontend_App SHALL implementar meta tag viewport com `width=device-width, initial-scale=1` e desabilitar zoom máximo apenas em campos de input onde o zoom automático do iOS causaria reflow indesejado, mantendo zoom habilitado nas demais áreas para acessibilidade

---

### Requisito 16: Frontend com shadcn/ui e Tailwind CSS

**User Story:** Como desenvolvedor frontend, eu quero utilizar shadcn/ui como biblioteca de componentes e Tailwind CSS como framework de estilização, para que o desenvolvimento seja rápido, consistente e com tema customizável alinhado à identidade visual do produto.

#### Critérios de Aceitação

1. THE Design_System SHALL utilizar Tailwind CSS 3.4 ou superior como framework de estilização, configurado com arquivo `tailwind.config.ts` contendo: tokens de cor customizados (primary, secondary, accent, destructive, muted, background, foreground, card, popover, border, input, ring), tokens de espaçamento estendidos e tokens de border-radius do projeto
2. THE Design_System SHALL utilizar shadcn/ui como biblioteca de componentes base, com componentes instalados via CLI (`npx shadcn-ui@latest add`) no diretório `frontend/src/components/ui/`, seguindo a convenção de composição e customização por meio de variantes (class variance authority)
3. THE Design_System SHALL implementar suporte a tema claro e escuro via CSS variables e classe `dark` no elemento raiz, com alternância controlada pelo usuário persistida em localStorage e respeitando `prefers-color-scheme` como valor padrão inicial
4. THE Design_System SHALL definir tokens de design em arquivo `frontend/src/styles/globals.css` utilizando CSS custom properties no formato `--{categoria}-{variante}` (ex: `--primary`, `--primary-foreground`, `--destructive`, `--muted`), permitindo alteração de tema sem modificar código de componentes
5. THE Design_System SHALL manter consistência tipográfica com escala definida: xs (12px), sm (14px), base (16px), lg (18px), xl (20px), 2xl (24px), 3xl (30px) e 4xl (36px), com line-height e font-weight padronizados para cada nível, configurados como extensão do Tailwind
6. WHEN um novo componente de UI for necessário, THE Design_System SHALL priorizar composição a partir de primitivos do shadcn/ui (Button, Input, Select, Dialog, Sheet, Table, Card, Badge, Toast, Tooltip, DropdownMenu, Command, Popover) antes de criar componentes customizados do zero
7. THE Design_System SHALL configurar um Storybook ou catálogo de componentes acessível via `pnpm --filter frontend storybook` documentando visualmente todos os componentes do Design_System com variantes, estados (default, hover, focus, disabled, loading, error) e exemplos de uso
8. THE Design_System SHALL impor regra de lint (eslint-plugin-tailwindcss ou equivalente) que proíbe uso de CSS inline ou arquivos CSS avulsos fora de `globals.css`, garantindo que toda estilização utilize exclusivamente classes Tailwind ou CSS variables do tema
9. IF um componente shadcn/ui for customizado além de variantes de estilo (alteração de comportamento ou estrutura), THEN THE Design_System SHALL criar o componente em diretório separado `frontend/src/components/custom/` com documentação inline indicando a justificativa da customização e referência ao componente base original
10. THE Design_System SHALL garantir que todos os componentes interativos (Button, Input, Select, Dialog, DropdownMenu) possuam estados visuais distintos e perceptíveis para: focus-visible (outline de 2px com cor `--ring`), disabled (opacidade 50% e cursor not-allowed), loading (indicador de carregamento animado) e error (borda com cor `--destructive`), validados por testes visuais automatizados ou snapshot
