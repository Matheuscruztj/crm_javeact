# Adequação — Infraestrutura de Testes (Playwright + k6)

> **Referência:** Seção "Quality & Testing" da TECHNICAL-SPECIFICATION e documento 12-QUALITY-TESTING-CICD  
> **Estado Atual:** JUnit 5 + jqwik (PBT) + Mockito para backend; sem testes E2E funcionais; sem testes de carga  
> **Estado Alvo:** Playwright para testes funcionais E2E, k6 para testes de carga, seed scripts para dados de teste

---

## Visão Geral

O sistema atual possui cobertura de testes exclusivamente no backend (JUnit 5, jqwik para property-based testing, Mockito para mocking). Não há infraestrutura de testes end-to-end (E2E) para validar jornadas de usuário no frontend, nem testes de carga para verificar performance e resiliência sob diferentes cenários de uso.

A visão alvo introduz:

- **Playwright** (`tests/functional/`) — Testes funcionais E2E cobrindo as jornadas principais do admin e portal cliente
- **k6** (`tests/load/`) — Testes de carga com cenários graduais (smoke, average, stress)
- **Seed scripts** — Scripts para geração de dados de teste reproduzíveis

---

## 1. Configuração Playwright

| Aspecto                       | Estado Atual                   | Estado Alvo                                                                                                       | Ação Necessária | Prioridade |
| ----------------------------- | ------------------------------ | ----------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Diretório de testes           | Não existe `tests/functional/` | Diretório `tests/functional/` com estrutura Playwright organizada por feature                                     | Criar           | P1         |
| Arquivo de configuração       | Não existe                     | `playwright.config.ts` com projeto chromium, base URL local, timeout global 30s                                   | Criar           | P1         |
| Captura de artefatos em falha | N/A                            | Screenshots, vídeos e traces capturados automaticamente em falha, armazenados em `tests/functional/test-results/` | Criar           | P2         |
| Relatório HTML                | N/A                            | Geração de relatório HTML em `tests/functional/playwright-report/` via `make test-functional-report`              | Criar           | P2         |
| Modo headed (debug)           | N/A                            | Execução em modo headed para debug local via `make test-functional-headed`                                        | Criar           | P2         |
| Dependências npm              | N/A                            | Playwright como devDependency no package.json do diretório `tests/functional/` ou workspace raiz                  | Criar           | P1         |
| Integração CI                 | N/A                            | Step de CI para execução de testes E2E com upload de artefatos de falha                                           | Criar           | P3         |

---

## 2. Cenários Funcionais E2E — Admin

| Aspecto                          | Estado Atual         | Estado Alvo                                                                                                                     | Ação Necessária | Prioridade |
| -------------------------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Login e autenticação             | Não existe teste E2E | Cenário: login com credenciais válidas → redirect ao dashboard; login inválido → mensagem de erro; logout → redirect ao login   | Criar           | P2         |
| Gestão de clientes (CRUD)        | Não existe teste E2E | Cenário: criar cliente → listar → editar → verificar atualização → excluir → verificar remoção                                  | Criar           | P2         |
| Upload de documento              | Não existe teste E2E | Cenário: navegar a documentos → upload de arquivo → aguardar processamento → verificar documento listado com status             | Criar           | P2         |
| Análise de documento (IA)        | Não existe teste E2E | Cenário: selecionar documento processado → solicitar análise → verificar resultado com summary, category e confidence score     | Criar           | P3         |
| Criação de solicitação (request) | Não existe teste E2E | Cenário: criar solicitação → preencher campos → submeter → verificar na lista com status pendente                               | Criar           | P2         |
| Fluxo de aprovação               | Não existe teste E2E | Cenário: solicitação pendente → aprovar/rejeitar → verificar mudança de status → verificar registro em activities               | Criar           | P3         |
| Busca unificada                  | Não existe teste E2E | Cenário: digitar query na busca → verificar resultados cross-entity (clientes, documentos, solicitações) → navegar ao resultado | Criar           | P3         |
| Importação de dados              | Não existe teste E2E | Cenário: navegar a imports → upload CSV → acompanhar progresso → verificar dados importados na listagem                         | Criar           | P3         |
| Navegação e layout responsivo    | Não existe teste E2E | Cenário: verificar sidebar visível em desktop, colapsável em tablet, overlay em mobile; breadcrumbs atualizados por rota        | Criar           | P2         |

---

## 3. Cenários Funcionais E2E — Portal Cliente

| Aspecto                      | Estado Atual         | Estado Alvo                                                                                         | Ação Necessária | Prioridade |
| ---------------------------- | -------------------- | --------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Login do portal              | Não existe teste E2E | Cenário: login com credenciais de cliente → redirect à home do portal; acesso negado a rotas admin  | Criar           | P2         |
| Visualizar solicitações      | Não existe teste E2E | Cenário: listar solicitações do cliente → filtrar por status → abrir detalhes → verificar histórico | Criar           | P2         |
| Upload de documento (portal) | Não existe teste E2E | Cenário: navegar a upload → selecionar arquivo → submeter → verificar na lista de documentos        | Criar           | P2         |
| Notificações                 | Não existe teste E2E | Cenário: verificar lista de notificações → marcar como lida → verificar badge atualizado            | Criar           | P3         |
| Navegação mobile (portal)    | Não existe teste E2E | Cenário: bottom navigation visível em mobile → transições entre seções → verificar usabilidade      | Criar           | P3         |

---

## 4. Configuração k6

| Aspecto             | Estado Atual             | Estado Alvo                                                                                                     | Ação Necessária | Prioridade |
| ------------------- | ------------------------ | --------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Diretório de testes | Não existe `tests/load/` | Diretório `tests/load/` com scripts k6 organizados por cenário                                                  | Criar           | P1         |
| Script base         | N/A                      | Script k6 executável com stages configuráveis (VUs e duração), base URL via variável de ambiente                | Criar           | P1         |
| Relatório HTML      | N/A                      | Geração de relatório em `tests/load/reports/` via `make test-load-report` (k6 HTML report ou summary JSON→HTML) | Criar           | P2         |
| Thresholds          | N/A                      | Thresholds configurados: p95 < 500ms, error rate < 1% para smoke; p95 < 2s, error rate < 5% para stress         | Criar           | P2         |
| Integração CI       | N/A                      | Step de CI para execução de smoke test em pipeline de deploy                                                    | Criar           | P3         |

---

## 5. Cenário de Carga — Smoke

| Aspecto            | Estado Atual                      | Estado Alvo                                                                                                          | Ação Necessária | Prioridade |
| ------------------ | --------------------------------- | -------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Configuração       | N/A                               | 5 VUs simultâneos, duração 60 segundos, ramp-up de 10s                                                               | Criar           | P1         |
| Endpoints testados | N/A                               | Health check (`/actuator/health`), listagem de clientes (`GET /api/v1/customers`), login (`POST /api/v1/auth/login`) | Criar           | P1         |
| Thresholds         | N/A                               | p95 latency < 500ms, error rate < 1%, checks pass rate > 99%                                                         | Criar           | P2         |
| Comando Make       | Não existe `make test-load-smoke` | `make test-load-smoke` executa cenário smoke contra API local (porta via env var)                                    | Criar           | P1         |
| Propósito          | N/A                               | Validação rápida de sanidade: confirmar que API responde sob carga mínima sem erros                                  | Criar           | P1         |

---

## 6. Cenário de Carga — Average

| Aspecto            | Estado Atual                | Estado Alvo                                                                                                                | Ação Necessária | Prioridade |
| ------------------ | --------------------------- | -------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Configuração       | N/A                         | 50 VUs simultâneos, duração 5 minutos, ramp-up de 30s, ramp-down de 15s                                                    | Criar           | P2         |
| Endpoints testados | N/A                         | Mix realista: 40% leitura (listagens paginadas), 30% criação (clientes, solicitações), 20% busca, 10% upload de documentos | Criar           | P2         |
| Thresholds         | N/A                         | p95 latency < 1s, p99 < 2s, error rate < 2%, throughput ≥ 100 req/s                                                        | Criar           | P2         |
| Comando Make       | Não existe `make test-load` | `make test-load` executa cenário average como teste de carga padrão                                                        | Criar           | P2         |
| Propósito          | N/A                         | Simular carga típica de uso diário; identificar degradação gradual e memory leaks                                          | Criar           | P2         |

---

## 7. Cenário de Carga — Stress

| Aspecto            | Estado Atual | Estado Alvo                                                                                                          | Ação Necessária | Prioridade |
| ------------------ | ------------ | -------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Configuração       | N/A          | 200 VUs simultâneos, duração 10 minutos, ramp-up escalonado (50 → 100 → 150 → 200 VUs)                               | Criar           | P3         |
| Endpoints testados | N/A          | Todos os endpoints do cenário average com adição de operações pesadas: análise IA, imports em lote, buscas complexas | Criar           | P3         |
| Thresholds         | N/A          | p95 latency < 3s, error rate < 10%, sistema não deve crashar (zero HTTP 500 por timeout de thread pool)              | Criar           | P3         |
| Comando Make       | N/A          | Integrado ao `make test-load` com flag `LOAD_SCENARIO=stress` ou target separado                                     | Criar           | P3         |
| Propósito          | N/A          | Identificar breaking point, validar circuit breakers, observar comportamento de fallback sob pressão extrema         | Criar           | P3         |

---

## 8. Seed Scripts

| Aspecto                     | Estado Atual                                   | Estado Alvo                                                                                                                                                                        | Ação Necessária | Prioridade |
| --------------------------- | ---------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Script de seed existente    | `make seed` popula dados de demo (idempotente) | Manter seed existente e estender com dados específicos para cenários de teste                                                                                                      | Alterar         | P2         |
| Seed para testes funcionais | N/A                                            | Script dedicado (`tests/functional/seed.ts` ou `make seed-functional`) criando: 2 tenants, 5 usuários por tenant, 20 clientes, 10 documentos, 5 solicitações com diferentes status | Criar           | P2         |
| Seed para testes de carga   | N/A                                            | Script dedicado (`tests/load/seed.sh` ou `make seed-load`) gerando volume maior: 100 clientes, 50 documentos, 30 solicitações por tenant para simular cenário realista             | Criar           | P2         |
| Dados determinísticos       | N/A                                            | Seeds devem gerar dados determinísticos (IDs e timestamps fixos ou baseados em seed numérico) para reprodutibilidade dos testes                                                    | Criar           | P2         |
| Cleanup entre execuções     | N/A                                            | Mecanismo de cleanup (`make clean-test-data`) que remove apenas dados de seed de teste sem afetar dados de demo                                                                    | Criar           | P3         |
| Isolamento por tenant       | N/A                                            | Seeds de teste usam tenants dedicados (`tenant-test-functional`, `tenant-test-load`) para não interferir com dados de demo                                                         | Criar           | P2         |

---

## 9. Comandos Make

| Aspecto                       | Estado Atual | Estado Alvo                                                                      | Ação Necessária | Prioridade |
| ----------------------------- | ------------ | -------------------------------------------------------------------------------- | --------------- | ---------- |
| `make test-functional`        | Não existe   | Executa Playwright em modo headless, retorna exit code ≠ 0 se algum teste falhar | Criar           | P1         |
| `make test-functional-headed` | Não existe   | Executa Playwright em modo headed para debug local                               | Criar           | P2         |
| `make test-functional-report` | Não existe   | Gera relatório HTML em `tests/functional/playwright-report/`                     | Criar           | P2         |
| `make test-load-smoke`        | Não existe   | Executa cenário k6 smoke (5 VUs, 60s) contra API local                           | Criar           | P1         |
| `make test-load`              | Não existe   | Executa cenário k6 average (50 VUs, 5min) como teste de carga padrão             | Criar           | P2         |
| `make test-load-report`       | Não existe   | Gera relatório HTML em `tests/load/reports/`                                     | Criar           | P2         |

---

## Resumo de Prioridades

| Prioridade | Descrição                  | Itens                                                                                                                             |
| ---------- | -------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **P0**     | Remover/alterar primeiro   | Nenhum item de teste requer remoção — infraestrutura existente (JUnit 5 + jqwik) é mantida                                        |
| **P1**     | Criar scaffolding          | Diretórios `tests/functional/` e `tests/load/`, configurações Playwright e k6, script smoke, comandos Make básicos                |
| **P2**     | Implementar funcionalidade | Cenários funcionais E2E (login, CRUD, upload, requests), cenário average de carga, seed scripts, captura de artefatos, relatórios |
| **P3**     | Hardening e polish         | Cenários avançados (aprovação, busca, imports, análise IA), cenário stress, integração CI, cleanup automático                     |

---

## Estrutura de Diretórios Alvo

```
tests/
├── functional/                  # Playwright E2E tests
│   ├── playwright.config.ts     # Configuração global
│   ├── package.json             # Dependências (Playwright)
│   ├── seed.ts                  # Script de seed para testes funcionais
│   ├── tests/
│   │   ├── admin/
│   │   │   ├── login.spec.ts
│   │   │   ├── customers.spec.ts
│   │   │   ├── documents.spec.ts
│   │   │   ├── requests.spec.ts
│   │   │   ├── approvals.spec.ts
│   │   │   ├── search.spec.ts
│   │   │   ├── imports.spec.ts
│   │   │   └── navigation.spec.ts
│   │   └── portal/
│   │       ├── login.spec.ts
│   │       ├── requests.spec.ts
│   │       ├── documents.spec.ts
│   │       ├── notifications.spec.ts
│   │       └── navigation.spec.ts
│   ├── test-results/            # Artefatos de falha (screenshots, vídeos, traces)
│   └── playwright-report/       # Relatório HTML gerado
└── load/                        # k6 load tests
    ├── scripts/
    │   ├── smoke.js             # Cenário smoke (5 VUs, 60s)
    │   ├── average.js           # Cenário average (50 VUs, 5min)
    │   └── stress.js            # Cenário stress (200 VUs, 10min)
    ├── seed.sh                  # Script de seed para testes de carga
    ├── helpers/
    │   └── config.js            # Configuração compartilhada (base URL, thresholds)
    └── reports/                 # Relatórios HTML gerados
```

---

## Referências

- [QUALITY-TESTING-CICD](../testing/QUALITY-TESTING-CICD.md)
- [TECHNICAL-SPECIFICATION — Quality & Testing](TECHNICAL-SPECIFICATION.md)
- [Playwright Documentation](https://playwright.dev/)
- [k6 Documentation](https://k6.io/docs/)
