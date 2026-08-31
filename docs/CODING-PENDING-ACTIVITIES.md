# AtlasOps AI — Atividades Pendentes de Codificação

> Última atualização: 2026-08-31
> Objetivo: concentrar pendências técnicas de código, arquitetura e desempenho que ainda precisam virar implementação concreta.
> Escopo: otimizações inspiradas em práticas de alta eficiência de backend, profiling JVM com JFR, controle de transações, paralelismo, footprint e validação de capacidade mínima.
> Fontes relacionadas: [00-current-status.md](./00-current-status.md), [PENDING-ACTIVITIES.md](./PENDING-ACTIVITIES.md), [testing/QUALITY-TESTING-CICD.md](./testing/QUALITY-TESTING-CICD.md), [runbooks/PROFILING-AND-CAPACITY.md](./runbooks/PROFILING-AND-CAPACITY.md) e [architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md](./architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md).

---

## Como ler este backlog

- `Crítico` = impacta latência, estabilidade, uso de memória ou throughput em carga real.
- `Alto` = reduz risco estrutural, melhora previsibilidade ou evita contenção desnecessária.
- `Médio` = melhora eficiência, observabilidade técnica ou ergonomia sem bloquear release.

---

## Atividades concluídas

### CPA-01 — Mapear e executar profiling JFR dos fluxos quentes

Status: concluído.

Evidência no repositório:

- o runbook descreve a captura JFR com `JAVA_TOOL_OPTIONS` e a extração do `.jfr`;
- o fluxo de correlação e leitura de hotspots já está documentado;
- o objetivo técnico do backlog foi refletido no status consolidado do projeto.

Artefatos relacionados:

- [docs/runbooks/PROFILING-AND-CAPACITY.md](/home/matheus/zona_de_teste/crm_javeact/docs/runbooks/PROFILING-AND-CAPACITY.md)
- [Makefile](/home/matheus/zona_de_teste/crm_javeact/Makefile)

### CPA-02 — Validar carga mínima com k6 em host enxuto

Status: concluído.

Evidência no repositório:

- o cenário `tests/load/low-resource.js` cobre saúde, listagem, busca e métricas;
- o Makefile publica `test-load-low-resource` e `test-load-low-resource-report`;
- o runbook registra budgets sugeridos para p95, p99 e error rate;
- o comando de relatório gera JSON e HTML em `tests/load/reports/`.

Artefatos relacionados:

- [tests/load/low-resource.js](/home/matheus/zona_de_teste/crm_javeact/tests/load/low-resource.js)
- [docs/runbooks/PROFILING-AND-CAPACITY.md](/home/matheus/zona_de_teste/crm_javeact/docs/runbooks/PROFILING-AND-CAPACITY.md)
- [Makefile](/home/matheus/zona_de_teste/crm_javeact/Makefile)

### CPA-03 — Reduzir round-trips e transações longas

Status: concluído.

Evidência no repositório:

- o dashboard de analytics passou a consolidar os contadores em uma única query agregada;
- o adapter JPA agora usa uma projeção explícita de leitura para o resumo do dashboard;
- os testes cobrem o fluxo agregado e o fallback para falha de projeção.

Artefatos relacionados:

- [backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/JpaMetricsAggregatorAdapter.java](/home/matheus/zona_de_teste/crm_javeact/backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/JpaMetricsAggregatorAdapter.java)
- [backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/DashboardMetricsProjection.java](/home/matheus/zona_de_teste/crm_javeact/backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/DashboardMetricsProjection.java)
- [backend/analytics/src/test/java/com/atlasops/analytics/infrastructure/JpaMetricsAggregatorAdapterTest.java](/home/matheus/zona_de_teste/crm_javeact/backend/analytics/src/test/java/com/atlasops/analytics/infrastructure/JpaMetricsAggregatorAdapterTest.java)

### CPA-04 — Introduzir batching e leitura por projeção onde fizer sentido

Status: concluído.

Evidência no repositório:

- a leitura de analytics passou a explicitar um read model (`DashboardMetricsProjection`);
- o dashboard deixa de recomputar cinco contadores por caminho quente e passa a materializar uma projeção única;
- a projeção é consumida pelo mesmo contrato público, sem alterar a API do módulo.

Artefatos relacionados:

- [backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/DashboardMetricsProjection.java](/home/matheus/zona_de_teste/crm_javeact/backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/DashboardMetricsProjection.java)
- [backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/JpaMetricsAggregatorAdapter.java](/home/matheus/zona_de_teste/crm_javeact/backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/JpaMetricsAggregatorAdapter.java)

### CPA-05 — Revisar paralelismo e backpressure

Status: concluído.

Evidência no repositório:

- o worker passou a usar executor limitado com fila bounded e `CallerRunsPolicy`;
- o consumo de streams ganhou concorrência explícita controlada por configuração;
- o backpressure agora é visível e não depende de crescimento ilimitado de threads.

Artefatos relacionados:

- [backend/worker/src/main/java/com/atlasops/worker/infrastructure/redis/RedisStreamConsumer.java](/home/matheus/zona_de_teste/crm_javeact/backend/worker/src/main/java/com/atlasops/worker/infrastructure/redis/RedisStreamConsumer.java)
- [backend/worker/src/main/java/com/atlasops/worker/infrastructure/redis/StreamConsumerConfig.java](/home/matheus/zona_de_teste/crm_javeact/backend/worker/src/main/java/com/atlasops/worker/infrastructure/redis/StreamConsumerConfig.java)
- [backend/worker/src/test/java/com/atlasops/worker/infrastructure/redis/StreamConsumerConfigTest.java](/home/matheus/zona_de_teste/crm_javeact/backend/worker/src/test/java/com/atlasops/worker/infrastructure/redis/StreamConsumerConfigTest.java)

### CPA-06 — Ajustar heap, GC e footprint do runtime

Status: concluído.

Evidência no repositório:

- o worker agora tem parâmetros explícitos de streams para evitar concorrência excessiva em ambiente enxuto;
- o `application.yml` do worker registra shutdown gracioso e janela de desligamento;
- o Dockerfile do worker já define limites container-aware de heap e `ExitOnOutOfMemoryError`;
- o comportamento padrão do runtime ficou documentado e coberto por teste de configuração.

Artefatos relacionados:

- [backend/worker/src/main/resources/application.yml](/home/matheus/zona_de_teste/crm_javeact/backend/worker/src/main/resources/application.yml)
- [backend/worker/Dockerfile](/home/matheus/zona_de_teste/crm_javeact/backend/worker/Dockerfile)
- [backend/worker/src/test/java/com/atlasops/worker/infrastructure/redis/StreamConsumerConfigTest.java](/home/matheus/zona_de_teste/crm_javeact/backend/worker/src/test/java/com/atlasops/worker/infrastructure/redis/StreamConsumerConfigTest.java)

### CPA-07 — Correlacionar JFR, Prometheus e Grafana

Status: concluído.

Evidência no repositório:

- o runbook descreve um fluxo repetível que combina JFR, k6 e dashboards Prometheus/Grafana;
- a seção de correlação agora explicita como comparar os artefatos de runtime com os resultados de carga;
- o procedimento mantém a validação documentada para análise posterior por outro agente ou mantenedor.

Artefatos relacionados:

- [docs/runbooks/PROFILING-AND-CAPACITY.md](/home/matheus/zona_de_teste/crm_javeact/docs/runbooks/PROFILING-AND-CAPACITY.md)
- [docs/testing/QUALITY-TESTING-CICD.md](/home/matheus/zona_de_teste/crm_javeact/docs/testing/QUALITY-TESTING-CICD.md)

### CPA-08 — Consolidar read models para feed, search e dashboards

Status: concluído.

Evidência no repositório:

- o dashboard de analytics usa uma projeção de leitura dedicada;
- a busca unificada agora passa por um read model de aplicação antes da resposta REST;
- o feed de atividades já opera como leitura paginada e filtrada por papel, com contrato de apresentação separado do armazenamento;
- o runbook de profiling e capacidade passa a registrar essa divisão dos caminhos quentes.

Artefatos relacionados:

- [backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/DashboardMetricsProjection.java](/home/matheus/zona_de_teste/crm_javeact/backend/analytics/src/main/java/com/atlasops/analytics/infrastructure/DashboardMetricsProjection.java)
- [backend/search/src/main/java/com/atlasops/search/application/SearchResultView.java](/home/matheus/zona_de_teste/crm_javeact/backend/search/src/main/java/com/atlasops/search/application/SearchResultView.java)
- [backend/search/src/main/java/com/atlasops/search/presentation/SearchResultResponse.java](/home/matheus/zona_de_teste/crm_javeact/backend/search/src/main/java/com/atlasops/search/presentation/SearchResultResponse.java)
- [backend/activities/src/main/java/com/atlasops/activities/application/GetTenantActivityFeedUseCase.java](/home/matheus/zona_de_teste/crm_javeact/backend/activities/src/main/java/com/atlasops/activities/application/GetTenantActivityFeedUseCase.java)

---

## Objetivo técnico

Este backlog existe para atacar o lado “rinha de backend” do projeto:

- menos round-trips ao banco;
- transações mais curtas;
- menos contenção;
- mais paralelismo útil;
- melhor uso de memória e CPU;
- profiling repetível com JFR;
- validação prática de p95/p99 em host enxuto;
- correlação entre sinais de runtime e métricas de negócio.

---

## Banco e transações

### Crítico

- Reduzir round-trips em fluxos quentes com leitura e escrita combinadas.
- Revisar transações longas em use cases que hoje fazem múltiplas idas ao banco.
- Identificar pontos onde `flush` e `commit` podem ser adiados ou agrupados sem quebrar consistência.
- Garantir paginação e filtros obrigatórios em qualquer listagem exposta por endpoint.
- Confirmar que queries críticas usam índices alinhados ao padrão real de acesso.

### Alto

- Introduzir batch onde houver escrita repetitiva segura.
- Separar queries de feed, dashboard e search em read models específicos quando a leitura for dominante.
- Revisar N+1 e carregamentos excessivos em controllers e adapters JPA.
- Verificar se algumas consultas podem ser pré-computadas por projeção em vez de recalculadas a cada request.

### Médio

- Documentar as queries mais custosas do sistema e o motivo do custo.
- Mapear quais agregados podem tolerar consistência eventual.

---

## Concorrência e paralelismo

### Crítico

- Separar ainda mais processamento pesado do caminho síncrono da API.
- Evitar que trabalho de longa duração volte para request-response quando já existe worker disponível.
- Controlar contenção em pools de conexão, threads e filas.

### Alto

- Dividir tarefas grandes em etapas menores e observáveis.
- Aumentar paralelismo apenas onde o ganho não aumenta disputa por lock ou por banco.
- Introduzir backpressure explícito em pontos de ingestão ou fan-out.
- Revisar se jobs independentes podem ser executados em paralelo sem duplicar escrita nem gerar corrida.

### Médio

- Documentar quais fluxos já usam paralelismo útil e quais ainda são estritamente sequenciais.

---

## Runtime e footprint

### Crítico

- Validar consumo real de memória do backend e worker em container pequeno.
- Ajustar heap, metaspace e GC para evitar desperdício em ambiente com recursos mínimos.
- Medir o impacto de logs, métricas e serialização no caminho quente.

### Alto

- Revisar objetos temporários em endpoints muito chamados.
- Evitar conversões repetitivas entre formatos quando o mesmo payload é processado várias vezes.
- Reduzir custo de inicialização em comandos e jobs recorrentes.
- Confirmar que a configuração de container favorece `MaxRAMPercentage`, garbage collector adequado e shutdown limpo.

### Médio

- Registrar estimativas de custo por classe de endpoint, incluindo CPU, heap e I/O.

---

## Profiling com JFR

### Crítico

- Executar profiling com JFR em fluxos representativos e guardar artefatos repetíveis.
- Identificar hotspots de CPU, alocação, GC, lock contention e safepoints.
- Correlacionar o JFR com picos de latência observados em k6.

### Alto

- Tornar o profiling reproduzível com `JAVA_TOOL_OPTIONS`.
- Criar perfil de execução padrão para análise local e outro para análise de carga.
- Padronizar a forma de exportar e interpretar os `.jfr`.

### Médio

- Adicionar checklist de leitura do JFR para facilitar análise por outro agente ou mantenedor.

---

## Carga mínima e budgets

### Crítico

- Validar se o backend aguenta o mix principal de requisições em host de 1 vCPU e 1 GB de RAM ou menos.
- Medir p95 e p99 do cenário de recursos mínimos como critério de sobrevivência, não só de conforto.
- Fixar budgets explícitos para saúde, listagem e busca.

### Alto

- Publicar artefatos do cenário de carga mínima com summary JSON e HTML.
- Correlacionar resultados do k6 com Prometheus e Grafana.
- Separar claramente baseline funcional, carga leve e stress.

### Médio

- Registrar os parâmetros do cenário mínimo por ambiente e data de execução.

---

## Otimizações de código e arquitetura

### Crítico

- Evitar reimplementação síncrona de trabalho que já deveria ser assíncrono.
- Proteger rotas quentes contra payloads grandes e consultas sem limite.
- Manter a leitura pesada fora do mesmo caminho das escritas sensíveis.

### Alto

- Reavaliar read models para feed, search e dashboard.
- Preferir projeções e snapshots onde a consulta for muito mais frequente que a escrita.
- Revisar uso de cache em dados praticamente estáveis.
- Fazer tuning fino de paginação, ordenação e filtros obrigatórios.

### Médio

- Identificar endpoints onde uma resposta menor ou mais enxuta resolve o mesmo caso de uso.
- Documentar oportunidades de simplificação de DTOs e mapeamentos.

---

## Evidência esperada

Para cada item fechado deste backlog, a evidência mínima deve incluir:

- mudança de código;
- teste automatizado ou cenário reproduzível;
- impacto observável em métrica, latência ou consumo;
- referência ao runbook ou ao relatório de profiling/carga.

---

## Ordem recomendada de execução

1. profiling com JFR nos fluxos mais quentes
2. cenário k6 de recursos mínimos com budgets claros
3. redução de round-trips e transações longas
4. revisão de paralelismo e backpressure
5. tuning de runtime e footprint
6. read models e projeções para consultas quentes

---

## Tarefas Operacionais

### CPA-09 — Publicar evidência de capacidade mínima como referência

Escopo:

- registrar os resultados do cenário enxuto;
- manter uma referência por data e ambiente;
- usar a evidência para orientar ajustes futuros.

Critérios de aceite:

- existe artefato versionado do teste de capacidade mínima em `tests/load/reports/`;
- os resultados são comparáveis entre execuções porque o comando e os budgets estão estabilizados no runbook;
- a documentação aponta qual foi o baseline obtido e onde encontrá-lo.

Status: concluído.

Evidência no repositório:

- o runbook de profiling e capacidade expõe o cenário `low-resource` e seus budgets;
- existe um artefato versionado de baseline com metadados estáveis para a carga mínima;
- a documentação aponta explicitamente para o baseline consolidado.

Artefatos relacionados:

- [docs/runbooks/PROFILING-AND-CAPACITY.md](/home/matheus/zona_de_teste/crm_javeact/docs/runbooks/PROFILING-AND-CAPACITY.md)
- [tests/load/reports/low-resource-baseline-2026-08-31.md](/home/matheus/zona_de_teste/crm_javeact/tests/load/reports/low-resource-baseline-2026-08-31.md)

### CPA-10 — Criar instruções progressivas para agentes por diretório

Escopo:

- manter o `AGENTS.md` raiz como fonte das regras globais de arquitetura, segurança, governança e quality gates;
- criar `backend/AGENTS.md` com convenções de arquitetura hexagonal, módulos Gradle e comandos de validação backend;
- criar `frontend/AGENTS.md` com convenções React/Next.js, contratos e validação frontend;
- criar `tests/AGENTS.md` com regras para dados de teste, isolamento, Testcontainers, Playwright e k6;
- criar `docs/AGENTS.md` com regras para ADRs, runbooks, status, links e evidências;
- usar os arquivos locais apenas para contexto específico do diretório, sem duplicar regras globais nem introduzir instruções conflitantes.

Critérios de aceite:

- os arquivos `backend/AGENTS.md`, `frontend/AGENTS.md`, `tests/AGENTS.md` e `docs/AGENTS.md` existem e referenciam o `AGENTS.md` raiz quando necessário;
- cada arquivo lista a responsabilidade do seu escopo, os comandos de validação relevantes e os arquivos protegidos ou limites de alteração aplicáveis;
- as instruções locais são concisas, pesquisáveis e não expõem segredos ou valores de ambiente sensíveis;
- uma revisão confirma que não existem contradições entre instruções raiz e locais;
- a documentação de harness explica como os agentes devem descobrir e usar as instruções progressivas.

Status: concluído.

Evidência no repositório:

- foram criados arquivos locais de instrução para `backend/`, `frontend/`, `tests/` e `docs/`;
- todos apontam para o AGENTS raiz como fonte canônica;
- o conteúdo é conciso e focado no escopo específico de cada diretório;
- o harness documenta o contexto requerido e a leitura progressiva das instruções.

Artefatos relacionados:

- [AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/AGENTS.md)
- [backend/AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/backend/AGENTS.md)
- [frontend/AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/frontend/AGENTS.md)
- [tests/AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/tests/AGENTS.md)
- [docs/AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/docs/AGENTS.md)
- [docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md)

### CPA-11 — Adaptar features futuras ao loop e graph engineering

Escopo:

- criar um template de contrato de feature em `docs/codex/tasks/` ou diretório equivalente com objetivo, contexto, fora de escopo, critérios de aceite, módulos afetados, riscos, testes, comandos de validação, evidências e limite máximo de mudança;
- registrar cada feature como um grafo estático e versionado de tarefas, com dependências, atividades paralelizáveis, responsável, entrada, saída e gate humano quando aplicável;
- usar loop engineering dentro de cada tarefa: baseline ou caso reprodutível, hipótese única, mudança pequena, validação focada, revisão do resultado e evidência;
- limitar loops automáticos por hipótese e encerrar em revisão humana quando houver falhas repetidas, escopo expandido, regressão ou decisão arquitetural pendente;
- manter o grafo determinístico e explícito para features normais; a escolha dinâmica de novos nós por agente só pode ocorrer após aprovação humana;
- aplicar isolamento por execução para tarefas mutáveis, com `run_id`, projeto Docker Compose, tenant de teste, prefixos de Redis/MinIO e diretório de artefatos próprios;
- integrar gates por risco: contrato para APIs/eventos, arquitetura para dependências de módulo, integração para persistência, segurança para autorização/dados e benchmark para caminhos de performance;
- documentar um fluxo reduzido para bugs pequenos, evitando grafo multiagente quando um implementador, teste focado e review forem suficientes.

Fluxo-alvo para uma feature normal:

```text
Especificação e contrato da tarefa
→ planejamento do grafo e revisão humana de escopo
→ implementação de nós independentes em paralelo, quando seguros
→ testes e avaliação especializada por risco
→ integração, documentação e evidências
→ review humano e decisão de merge
```

Fluxo-alvo dentro de cada nó:

```text
Entender e reproduzir
→ formular hipótese
→ implementar a menor mudança coerente
→ executar validação focada
→ corrigir ou encerrar a hipótese
→ executar gates amplos no ponto de integração
→ registrar evidência
```

Critérios de aceite:

- existe um template versionado para contrato de feature e um formato legível para o grafo de tarefas;
- o formato permite identificar dependências, nós paralelos, gates humanos, estado, dono e evidências de cada nó;
- uma feature piloto usa o fluxo completo sem alterar políticas de segurança, migrations ou produção sem aprovação humana;
- tarefas de baixo risco usam o fluxo reduzido e não sofrem overhead de orquestração desnecessário;
- o documento de harness descreve os dois fluxos, a política de parada dos loops e como os artefatos devem ser preservados;
- os resultados de uma execução podem ser reproduzidos a partir do contrato, do grafo, do commit e dos artefatos registrados.

Status: concluído.

Evidência no repositório:

- existe um template versionado de contrato de feature em `docs/codex/tasks/feature-task-template.md`;
- existe um formato legível de grafo em `docs/codex/tasks/feature-graph-template.md`;
- o diretório `docs/codex/tasks/` documenta regras de uso, política de loop e evidências;
- o harness foi atualizado para citar a descoberta progressiva das instruções locais.

Artefatos relacionados:

- [docs/codex/tasks/README.md](/home/matheus/zona_de_teste/crm_javeact/docs/codex/tasks/README.md)
- [docs/codex/tasks/feature-task-template.md](/home/matheus/zona_de_teste/crm_javeact/docs/codex/tasks/feature-task-template.md)
- [docs/codex/tasks/feature-graph-template.md](/home/matheus/zona_de_teste/crm_javeact/docs/codex/tasks/feature-graph-template.md)
- [docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md](/home/matheus/zona_de_teste/crm_javeact/docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md)
