# AtlasOps AI — Atividades Pendentes

> Última atualização: 2026-07-31
> Objetivo: concentrar em um único lugar as atividades ainda pendentes ou apenas parcialmente concluídas.
> Fontes de referência: [00-current-status.md](./00-current-status.md), [ROADMAP.md](./ROADMAP.md), [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md) e [task-plans](./task-plans/).

---

## Como ler este backlog

- `Crítico` = bloqueia confiança técnica, qualidade mínima ou governança do release.
- `Alto` = não bloqueia tudo imediatamente, mas mantém risco estrutural ou operacional relevante.
- `Médio` = melhora robustez, rastreabilidade ou maturidade, sem ser o gargalo atual principal.
- Itens `parcialmente concluídos` aparecem aqui até que tenham evidência suficiente para sair do backlog.

---

## Resumo executivo

### Crítico

- Reerguer a cobertura real do backend e parar de depender do gate reduzido para `10%`.
- Fechar `make verify` e `make lint` com resultado compatível com a Definition of Done.
- Adicionar verificação de breaking change para OpenAPI.
- Consolidar a política documental de checks `blocking` vs `advisory`.

### Alto

- Expandir os checks arquiteturais do backend.
- Fechar a camada de contrato frontend/API com geração de tipos ou cliente derivado.
- Consolidar a matriz de governança: owner, frequência, severidade e exceções.

### Médio

- Expandir medição de performance frontend.
- Refinar relatórios de resiliência por dependência.
- Documentar métricas de tempo dos gates locais.
- Criar roteiro repetível de profiling JVM com JFR para identificar gargalos de CPU, heap e locks.
- Validar cenário de carga mínima com k6 em host equivalente a 1 vCPU e 1 GB de RAM.
- Correlacionar resultados de profiling e carga com Prometheus/Grafana.

---

## Backend

### Crítico

- Recuperar cobertura backend para um patamar confiável e remover a dependência do threshold de `10%` em `jacocoTestCoverageVerification`.
- Garantir que módulos críticos de domínio atinjam novamente cobertura compatível com a meta histórica do projeto.
- Fechar gaps reais de teste em módulos ainda frágeis, principalmente onde o roadmap antigo declarava conclusão sem validação homogênea.

### Alto

- Criar `Inheritance Rules` no ArchUnit.
- Criar `Annotation Rules` no ArchUnit.
- Expandir regras de desacoplamento entre módulos.
- Criar rule set para evitar imports de tecnologia externa dentro de `domain`.
- Adicionar suíte de testes arquiteturais dedicada por categoria.
- Garantir cobertura mínima dos endpoints críticos no contrato OpenAPI exportado.
- Adicionar testes de consistência de schema para endpoints críticos.
- Avaliar `Pact` ou `Spring Cloud Contract` apenas onde houver consumidor real.

### Médio

- Ampliar a matriz de resiliência para cenários ainda não cobertos uniformemente.
- Padronizar evidência de falha por dependência em suites de resiliência.

---

## Frontend

### Alto

- Mapear endpoints realmente consumidos pelo frontend e aderência ao contrato.
- Avaliar e adotar geração de tipos a partir do OpenAPI.
- Substituir tipos manuais frágeis por tipos derivados do contrato quando viável.
- Criar smoke de compatibilidade frontend-API em cenários críticos.
- Criar regra específica para impedir acoplamento indevido entre admin e portal.

### Médio

- Documentar melhor as regras mínimas obrigatórias de lint frontend.
- Revisar gaps entre convenção frontend e enforcement real.
- Medir impacto de bundle e requests em rotas críticas.
- Preparar smoke de integração UI + API quando aplicável.

---

## Qualidade e CI/CD

### Crítico

- Consolidar `make verify` para o estado real esperado pela Definition of Done.
- Fechar `make lint` com `Checkstyle` e `SpotBugs` sem pendências bloqueantes.
- Adicionar verificação de breaking changes de contrato na CI.
- Criar matriz documental única ligando cada check/job a owner, frequência, severidade e política de bloqueio.

### Alto

- Classificar formalmente os checks arquiteturais como `blocking` por estágio.
- Publicar relatório consolidado de violações arquiteturais.
- Formalizar a policy de bloqueio por severidade para segurança.
- Decidir e registrar o papel de `OWASP Dependency-Check` sem `NVD_API_KEY`.
- Avaliar `Snyk` ou `OSV-Scanner` como complemento.
- Adicionar `Dependabot` ou `Renovate`.
- Criar runbook de resposta rápida a vulnerabilidade crítica emergente.
- Avaliar subset dinâmico de integração por módulo alterado no `pre-push`.

### Médio

- Medir formalmente o tempo médio de execução dos gates locais.
- Refinar a segmentação entre CI principal e nightly para cenários pesados restantes.
- Consolidar artifacts e relatórios adicionais onde ainda houver only-baseline.

### Evidência registrada em 2026-08-31

- `make verify-local-fast`: `14.54s`
- `make verify-precommit`: `0.02s`
- `make verify-prepush`: `76.49s`
- Média amostral destes gates locais: `30.35s`
- Observação: o valor de `verify-precommit` reflete o alvo mínimo bloqueante atual e por isso não é representativo do custo de pipeline completo.

---

## Contratos e APIs

### Crítico

- Adicionar diff de breaking change para OpenAPI.

### Alto

- Consolidar o fluxo de contrato consumidor/provedor se `Pact` ou `Spring Cloud Contract` forem adotados.
- Fechar a derivação de tipos/cliente frontend a partir do contrato.

---

## Segurança e supply chain

### Alto

- Identificar sinks críticos: SSRF, desserialização, file upload, SQL dinâmico e chamadas externas.
- Preparar suppressions justificadas para falsos positivos.
- Garantir que configurações de segurança protegidas não sejam alteradas por automação indevida.

### Médio

- Consolidar a postura de resposta a zero-day com runbook dedicado.

---

## Resiliência e performance

### Alto

- Padronizar relatório de falha por dependência na suíte de resiliência.
- Fechar a política de thresholds por fluxo de performance.

### Médio

- Consolidar payloads e dados-base dos cenários k6 por módulo.
- Evoluir de smoke funcional frontend para evidência mais forte de performance correlacionada.
- Amarrar evidência de performance como critério operacional mais explícito de release.
- Publicar p95/p99 do cenário de recursos mínimos como referência de capacidade.

---

## Governança e documentação operacional

### Crítico

- Criar matriz `check -> owner -> frequência -> blocking/advisory`.
- Definir processo de exception handling com expiração.
- Definir SLA de correção por severidade.

### Alto

- Criar dashboard simples de qualidade.
- Revisar mensalmente suppressions e bypasses.
- Criar critério de promoção de checks `advisory` para `blocking`.
- Atualizar roadmap principal após fechamento real de cada wave.

---

## Fora do backlog de concluídos

Os itens abaixo não devem voltar para a lista de “finalizado” até nova validação:

- Cobertura backend tratada como resolvida.
- `make verify` tratado como verde por padrão.
- `make lint` tratado como encerrado.
- Sonar frontend tratado como decidido quando ainda não foi formalizado.
- Breaking-change de OpenAPI tratado como presente.

---

## Próxima ordem recomendada

1. Cobertura real backend + `make verify` + `make lint`
2. Breaking-change de OpenAPI + tipos derivados no frontend
3. ArchUnit expandido + arquitetura frontend complementar
4. Política final de segurança e matriz `blocking/advisory`
5. Governança contínua dos checks
