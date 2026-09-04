# Zero-Day Response Runbook

## Goals

- limitar exposição;
- impedir regressão silenciosa;
- acelerar triagem e remediação.

## Steps

1. Confirmar o impacto do advisory.
2. Identificar módulos, imagens e dependências afetadas.
3. Bloquear releases com o componente vulnerável.
4. Criar issue com owner e prazo.
5. Aplicar workaround temporário se existir.
6. Revalidar com scan local e CI.
7. Publicar nota de status interno.

## Escalation

- `CRITICAL`: resposta imediata.
- `HIGH`: resposta no mesmo dia útil.
- `MEDIUM`: resposta planejada em backlog prioritário.
