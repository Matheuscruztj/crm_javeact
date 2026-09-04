# Processo de Exceção

## Quando usar

- falso positivo confirmado;
- risco residual aceitável com mitigação documentada;
- dependência externa sem correção imediata.

## Campos obrigatórios

- check afetado;
- severidade;
- justificativa;
- owner;
- prazo de expiração;
- link para evidência;
- plano de correção.

## Ciclo

1. Registrar exceção.
2. Validar impacto com o owner técnico.
3. Aprovar com revisão humana.
4. Revisar antes da expiração.
5. Remover a exceção quando corrigido.

## Regras

- exceções nunca são permanentes;
- exceções de segurança crítica exigem revisão humana explícita;
- exceções não podem esconder falha de regressão nova.
