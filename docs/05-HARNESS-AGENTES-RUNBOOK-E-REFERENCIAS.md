# AtlasOps AI — Harness engineering, agentes e runbook

## Objetivo

Este documento permite operar, retomar e evoluir o projeto com segurança, inclusive em outra máquina ou usando agentes de engenharia.

Ele contém:

- princípios de harness engineering;
- papéis de agentes;
- sandboxes;
- contratos de tarefa;
- evidências exigidas;
- bootstrap;
- reset;
- current status;
- migração entre stacks;
- checklist de retomada;
- referências técnicas.

---

## Harness engineering e agentes

### 05 — Harness engineering e agentes

#### 1. Definição operacional

Harness engineering é o trabalho de criar o ambiente, os controles, as ferramentas e os ciclos de feedback que permitem que humanos e agentes modifiquem uma base de código com segurança e consistência.

O foco deixa de ser apenas “escrever código” e passa a incluir:

- instruções;
- comandos;
- verificações;
- limites arquiteturais;
- observabilidade;
- ambientes isolados;
- tarefas pequenas;
- evidências automatizadas;
- manutenção contínua.

O agente não substitui os quality gates. Ele trabalha dentro deles.

---

#### 2. Componentes do harness

##### 2.1 Contexto

Arquivos recomendados:

```text
AGENTS.md
README.md
docs/00-current-status.md
docs/architecture.md
docs/domain.md
docs/testing.md
docs/runbook.md
docs/adr/
```

##### 2.2 Comandos estáveis

```text
bootstrap
verify
verify-full
test-unit
test-integration
test-e2e
compose-up
compose-reset
```

##### 2.3 Feedback

- lint;
- typecheck;
- tests;
- architecture checks;
- security;
- logs;
- coverage;
- mutation;
- performance.

##### 2.4 Ambiente

- containers;
- fixtures;
- seed;
- ephemeral DB;
- fake external services;
- sandbox;
- restricted credentials.

##### 2.5 Controle

- branch protection;
- CODEOWNERS;
- PR template;
- approval rules;
- tool allowlist;
- destructive command policy;
- generated file policy.

---

#### 3. Arquivo AGENTS.md sugerido

Deve informar:

- objetivo do produto;
- arquitetura;
- módulos;
- comandos;
- definição de pronto;
- regras de segurança;
- padrões de testes;
- arquivos que não podem ser alterados;
- como criar migration;
- como rodar ambiente;
- como registrar ADR;
- como provar que a tarefa foi concluída.

##### Exemplo de regra

```text
Antes de concluir:
1. execute make verify;
2. execute testes do módulo alterado;
3. atualize OpenAPI se contrato mudou;
4. adicione migration se schema mudou;
5. não ignore falhas;
6. informe arquivos modificados e riscos.
```

---

#### 4. Unidade ideal de trabalho para agente

Uma tarefa deve:

- ter um resultado;
- possuir acceptance criteria;
- informar arquivos ou módulo;
- limitar escopo;
- listar comandos;
- indicar testes;
- não misturar refactor amplo e feature;
- caber em um PR revisável.

##### Ruim

```text
Implemente todo o sistema de documentos.
```

##### Melhor

```text
Implemente o caso de uso ConfirmDocumentUpload:
- validar tenant;
- validar status PENDING_UPLOAD;
- persistir checksum;
- mover para UPLOADED;
- adicionar DocumentUploaded à outbox;
- cobrir duplicidade;
- não implementar worker.
```

---

#### 5. Agentes recomendados

Agentes podem ser papéis lógicos, jobs automáticos ou prompts separados.

##### A1 — Planner

Responsabilidades:

- decompor;
- identificar dependências;
- apontar riscos;
- propor testes;
- indicar arquivos;
- não editar.

Saída:

- plano;
- acceptance criteria;
- ordem;
- comandos.

##### A2 — Implementer

- implementa escopo;
- mantém arquitetura;
- adiciona testes;
- executa gates locais;
- não altera escopo.

##### A3 — Test Engineer

- revisa riscos;
- cria cenários;
- procura falsos positivos;
- adiciona casos concorrentes;
- valida isolamento;
- executa mutation quando aplicável.

##### A4 — Reviewer

- verifica corretude;
- legibilidade;
- segurança;
- contratos;
- migrations;
- observabilidade;
- backward compatibility.

##### A5 — Security Agent

- threat model;
- autorização;
- secrets;
- injection;
- upload;
- SSRF;
- prompt injection;
- tenancy;
- dependencies.

##### A6 — Architecture Agent

- boundaries;
- ciclos;
- acoplamento;
- uso de ports;
- consistência de eventos;
- shared code;
- ADR necessário.

##### A7 — Migration Agent

- schema;
- index;
- constraint;
- rollback;
- volume;
- lock;
- compatibility;
- data migration.

##### A8 — Documentation Agent

- README;
- OpenAPI;
- ADR;
- examples;
- current status;
- changelog.

##### A9 — SRE/Observability Agent

- metrics;
- logs;
- traces;
- health;
- runbook;
- alerts;
- failure modes.

##### A10 — Quality Janitor

Executado periodicamente:

- código morto;
- TODO;
- warnings;
- dependências;
- duplicação;
- flakiness;
- quality grades;
- documentação obsoleta;
- refactors pequenos.

##### A11 — AI Evaluation Agent

- dataset;
- regressão de prompt;
- schema;
- safety;
- fallback;
- quality score;
- model drift.

---

#### 6. Orquestração de agentes

##### Fluxo recomendado

```text
Issue
→ Planner
→ Human approval
→ Implementer
→ Test Engineer
→ Reviewer
→ Security/Architecture checks
→ CI
→ Human merge
```

Para mudanças pequenas:

```text
Implementer
→ CI
→ Reviewer
→ merge
```

---

#### 7. Matriz de permissão de agentes

| Ação | Planner | Implementer | Reviewer | Security | Janitor |
|---|---:|---:|---:|---:|---:|
| Ler repo | sim | sim | sim | sim | sim |
| Alterar código | não | sim | não | opcional | sim |
| Criar migration | não | sim | não | não | não |
| Alterar CI | não | limitado | não | sugestão | limitado |
| Acessar segredo | não | não | não | não | não |
| Executar Docker | sim | sim | sim | sim | sim |
| Merge | não | não | não | não | não |
| Comando destrutivo | não | sandbox | não | não | sandbox |

---

#### 8. Sandboxes

Cada agente/tarefa deve usar:

- worktree;
- branch;
- container;
- banco;
- portas;
- volumes;
- namespace;
- seed exclusivos.

Padrão:

```text
run_id = pr-123-agent-implementer
database = atlasops_pr_123
compose_project = atlasops_pr_123
bucket_prefix = pr-123/
```

---

#### 9. Feedback progressivo

O agente deve executar na ordem:

```text
format
→ lint
→ typecheck
→ focused unit
→ focused integration
→ build
→ full verify
```

Não rodar E2E completo após cada alteração pequena.

---

#### 10. Ferramentas de inspeção

O harness deve oferecer scripts para:

- listar módulos;
- validar imports;
- detectar ciclos;
- verificar migrations;
- validar OpenAPI;
- comparar contratos;
- gerar fixtures;
- resetar ambiente;
- consultar logs;
- consultar métricas;
- reproduzir job;
- reprocessar DLQ em ambiente local.

---

#### 11. Qualidade como sensores

Sensores contínuos:

- coverage delta;
- mutation score;
- complexity;
- dependency age;
- vulnerability count;
- flaky rate;
- build time;
- test duration;
- architecture violations;
- unowned modules;
- stale docs;
- TODO count.

Os sensores não precisam bloquear todos imediatamente. Podem gerar grade e backlog.

---

#### 12. Contrato de tarefa

Template:

```markdown
## Objetivo
## Contexto
## Fora do escopo
## Acceptance criteria
## Interfaces afetadas
## Riscos
## Testes obrigatórios
## Comandos de validação
## Evidências esperadas
```

---

#### 13. Evidências exigidas

O agente deve devolver:

- resumo;
- arquivos;
- decisões;
- testes;
- comandos;
- resultados;
- limitações;
- riscos;
- migration;
- screenshots quando UI;
- logs quando async.

---

#### 14. Guardrails para IA no produto

Separar dois conceitos:

1. agentes de engenharia;
2. agente do AtlasOps AI.

O agente do produto:

- não recebe acesso direto ao banco;
- usa tools do backend;
- herda tenant e usuário;
- passa por authorization;
- ferramentas mutáveis exigem aprovação;
- possui limite de passos;
- registra tool calls;
- não revela prompts internos;
- não acessa documento sem permissão;
- não usa dados de outro tenant.

---

#### 15. Rotinas periódicas

##### Por PR

- architecture scan;
- test gap review;
- security review;
- docs check.

##### Diária

- dependency report;
- flaky report;
- quality grade;
- stale TODO;
- vulnerability update.

##### Semanal

- refactoring PRs pequenos;
- dead code;
- slow tests;
- log quality;
- alert review;
- prompt regression.

---

#### 16. Anti-patterns

- agente com acesso irrestrito;
- prompt enorme sem docs no repo;
- tarefa ampla;
- CI diferente do local;
- aceitar “funciona” sem evidência;
- agent review sem testes;
- muitos agentes editando os mesmos arquivos;
- generated code sem validação;
- merge automático de migrations críticas;
- retry infinito;
- esconder flaky test.

---

## Runbook de migração, retomada e bootstrap

### 09 — Runbook de migração, retomada e bootstrap

#### 1. Objetivo

Permitir que o projeto seja retomado em uma nova máquina sem a conversa original.

---

#### 2. Arquivos que devem existir no repositório

```text
README.md
AGENTS.md
.env.example
Makefile ou Taskfile
docker-compose.yml
docs/00-current-status.md
docs/architecture.md
docs/domain.md
docs/testing.md
docs/quality-gates.md
docs/runbooks/
docs/adr/
```

---

#### 3. Pré-requisitos

- Git;
- Docker;
- Docker Compose;
- runtime da stack;
- Make/Task opcional;
- memória suficiente;
- portas livres.

---

#### 4. Bootstrap esperado

```bash
git clone <repository>
cd atlasops-ai
cp .env.example .env
make bootstrap
make compose-up
make migrate
make seed
make verify
```

Após isso:

- API saudável;
- frontends acessíveis;
- MailHog;
- Grafana;
- MinIO;
- usuário de demo;
- tenants de demo.

---

#### 5. Dados de demonstração

Criar:

##### Tenant Alpha

- admin;
- analyst;
- customer;
- documents;
- requests;
- opportunity;
- alert.

##### Tenant Beta

Mesmo conjunto reduzido para validar isolamento.

Nunca reutilizar credenciais reais.

---

#### 6. Reset

```bash
make compose-down
make reset
make compose-up
make migrate
make seed
```

`reset` deve remover apenas recursos locais do projeto.

---

#### 7. Current status

Atualizar `docs/00-current-status.md` com:

- última data;
- wave;
- módulos;
- testes;
- quality gates;
- problemas;
- decisões;
- próxima atividade;
- comandos conhecidos.

##### Template

```markdown
# Current status

## Última atualização
## Wave atual
## Funcionalidades concluídas
## Quality gates
## Pendências
## Problemas conhecidos
## Próximas tarefas
## Como validar
```

---

#### 8. Como trocar stack

Preservar:

- APIs;
- eventos;
- banco;
- estados;
- comandos;
- quality gates;
- testes;
- docs.

##### Node → Go

Mapeamento:

```text
Nest module → Go package/module
Use case class → application function/service
Repository interface → Go interface
Jest → go test
ESLint/typecheck → golangci-lint/go vet
```

##### Go → Java

```text
Go package → Spring module
interface → port
handler → controller
go test → JUnit
```

---

#### 9. Como retomar com agente

Prompt mínimo:

```text
Leia README.md, AGENTS.md, docs/00-current-status.md,
docs/architecture.md e o ADR mais recente.

Não altere código ainda.
Resuma:
1. estado;
2. arquitetura;
3. gates;
4. riscos;
5. próxima tarefa.
Depois proponha um plano pequeno.
```

---

#### 10. Backup do contexto

Manter no repositório:

- este pacote;
- diagramas;
- OpenAPI;
- schema;
- sample events;
- prompts;
- evaluation dataset;
- demo script;
- ADRs.

Não depender de memória de chat.

---

#### 11. Checklist de retomada

- [ ] branch correta;
- [ ] status lido;
- [ ] env criado;
- [ ] containers sobem;
- [ ] migrations passam;
- [ ] seed passa;
- [ ] unit passa;
- [ ] integration passa;
- [ ] E2E smoke passa;
- [ ] logs acessíveis;
- [ ] documentação atualizada.

---

#### 12. Diagnóstico inicial

Comandos:

```text
make doctor
make verify
make compose-logs
make test-smoke
```

`doctor` deve verificar:

- versões;
- Docker;
- portas;
- env;
- rede;
- espaço;
- dependências;
- serviços.

---

#### 13. Artefatos de release

Guardar:

- source tag;
- image digest;
- SBOM;
- OpenAPI;
- migrations;
- changelog;
- test reports;
- security report;
- K6 report;
- AI evaluation report.

---

## Referências técnicas

### 99 — Referências técnicas

#### Harness engineering

- OpenAI — Harness engineering: leveraging Codex in an agent-first world  
  https://openai.com/index/harness-engineering/

- OpenAI — Unrolling the Codex agent loop  
  https://openai.com/index/unrolling-the-codex-agent-loop/

#### Segurança

- OWASP Application Security Verification Standard  
  https://owasp.org/www-project-application-security-verification-standard/

- OWASP Web Security Testing Guide  
  https://owasp.org/www-project-web-security-testing-guide/

- SLSA — Supply-chain Levels for Software Artifacts  
  https://slsa.dev/

#### Testes

- Martin Fowler — The Practical Test Pyramid  
  https://martinfowler.com/articles/practical-test-pyramid.html

- Martin Fowler — Testing Strategies in a Microservice Architecture  
  https://martinfowler.com/articles/microservice-testing/

#### ML e IA

- Martin Fowler — Continuous Delivery for Machine Learning  
  https://martinfowler.com/articles/cd4ml.html

#### Material interno de contexto

O pacote também foi derivado do escopo fornecido pelo usuário para:

- Banking / Fintech Platform — Microservices PoC;
- FinTech POC Brasil 2026;
- proposta consolidada AtlasOps AI.
