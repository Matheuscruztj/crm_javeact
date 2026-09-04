# Sensitive Sinks Review

## Scope

Áreas que exigem revisão manual regular:

- SSRF e validação de URL de saída;
- upload e processamento de arquivos;
- SQL dinâmico e queries montadas manualmente;
- desserialização;
- execução de comandos externos;
- chamadas a serviços externos com credenciais;
- fluxos de webhook e integrações outbound;
- endpoints de administração e operação.

## Current policy

- Toda nova dependência sensível deve ter revisão de segurança.
- Sinks que recebem input de usuário devem ter validação de tipo, tamanho e formato.
- Sinks críticos devem ter testes dedicados e logging sem segredos.

## Review cadence

- revisar ao menos em cada wave de segurança;
- reavaliar ao alterar controllers, adapters ou integrações.
