# Feature Graph

## Metadados

- **Feature:** `<nome-da-feature>`
- **Task:** `<id>`
- **Run ID:** `<run_id>`
- **Status:** `planned | in_progress | blocked | done`
- **Owner:** `<responsavel>`

## Grafo

```text
node-1: understand and reproduce
  inputs:
    - spec
    - current code
  outputs:
    - baseline
    - hypothesis
  human_gate: false

node-2: implement smallest coherent change
  depends_on:
    - node-1
  parallel_with:
    - node-3
  outputs:
    - code change
    - focused tests
  human_gate: false

node-3: validate focused scenario
  depends_on:
    - node-1
  outputs:
    - test evidence
    - benchmark evidence
  human_gate: false

node-4: review and decide
  depends_on:
    - node-2
    - node-3
  outputs:
    - accepted evidence
    - follow-up tasks
  human_gate: true
```

## Política de loop

- uma hipótese por vez;
- uma mudança pequena por hipótese;
- parar após falha repetida ou escopo expandido;
- exigir revisão humana quando houver decisão arquitetural, risco de segurança ou alteração protegida.

## Evidências obrigatórias

- contrato da tarefa;
- commit com o grafo;
- saída dos testes;
- artefatos versionados;
- comparação com baseline.
