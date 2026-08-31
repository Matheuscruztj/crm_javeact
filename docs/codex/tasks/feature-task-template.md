# Feature Task Contract

## Identificação

- **Feature:** `<nome-da-feature>`
- **Task:** `<id>`
- **Owner:** `<responsavel>`
- **Run ID:** `<run_id>`

## Objetivo

Descreva a mudança pretendida em uma frase.

## Contexto

Explique o problema, a motivação e as restrições relevantes.

## Fora de escopo

- itens que não serão alterados nesta execução;
- decisões arquiteturais que exigem aprovação humana;
- qualquer migração, segredo ou alteração protegida fora do contrato.

## Módulos afetados

- `backend/<modulo>`
- `frontend`
- `docs`
- `tests`

## Critérios de aceite

- [ ] critério verificável 1
- [ ] critério verificável 2
- [ ] critério verificável 3

## Riscos

- risco funcional;
- risco de arquitetura;
- risco de validação;
- risco operacional.

## Testes

- testes unitários;
- testes de integração;
- testes de contrato;
- testes de performance quando aplicável.

## Comandos de validação

```bash
make test-unit
make test-integration
make verify
```

## Evidências

- links absolutos para arquivos gerados;
- outputs de testes;
- relatórios ou capturas relevantes.

## Limite de mudança

Descreva o menor escopo aceitável para concluir a tarefa.
