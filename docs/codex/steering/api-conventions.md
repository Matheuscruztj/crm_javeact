# Convenções de API — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Versionamento

- Versionamento via path: `/api/v1/`, `/api/v2/`
- Versão atual: `v1`
- Nunca quebrar contratos existentes sem nova versão
- Versão antiga mantida por no mínimo 2 releases após deprecação
- Header `X-API-Deprecated: true` em endpoints deprecados

---

## Naming de Endpoints

### Regras gerais

- Substantivos no plural para coleções: `/customers`, `/documents`, `/tasks`
- Identificadores no path para recursos específicos: `/customers/{id}`
- Kebab-case para paths compostos: `/pending-approvals`, `/analysis-records`
- Verbos apenas em ações que não mapeiam para CRUD: `/documents/{id}/ingest`

### Padrão de URLs

```
GET    /api/v1/{recurso}              → Listar (com paginação)
GET    /api/v1/{recurso}/{id}         → Buscar por ID
POST   /api/v1/{recurso}              → Criar
PUT    /api/v1/{recurso}/{id}         → Atualizar (completo)
PATCH  /api/v1/{recurso}/{id}         → Atualizar (parcial)
DELETE /api/v1/{recurso}/{id}         → Remover
POST   /api/v1/{recurso}/{id}/{acao}  → Ação customizada
```

### Exemplos

```
GET    /api/v1/customers?page=0&size=20&sort=name,asc
GET    /api/v1/customers/123
POST   /api/v1/customers
PUT    /api/v1/customers/123
DELETE /api/v1/customers/123
POST   /api/v1/documents/456/ingest
POST   /api/v1/pending-approvals/789/approve
```

---

## Formato de Erros

Todas as respostas de erro seguem o formato padronizado (baseado em RFC 7807 Problem Details):

```json
{
  "type": "https://atlasops/errors/{error-type}",
  "title": "Descrição legível do erro",
  "status": 400,
  "code": "ERROR_CODE_ESTAVEL",
  "detail": "Detalhes específicos da ocorrência",
  "traceId": "correlation-id-propagado"
}
```

### Campos obrigatórios

| Campo     | Tipo         | Descrição                                         |
| --------- | ------------ | ------------------------------------------------- |
| `type`    | String (URI) | Identificador único do tipo de erro               |
| `title`   | String       | Descrição humana curta do erro                    |
| `status`  | Integer      | Código HTTP do erro                               |
| `code`    | String       | Código estável para integração (UPPER_SNAKE_CASE) |
| `detail`  | String       | Mensagem detalhada sobre a ocorrência             |
| `traceId` | String       | Correlation ID para rastreamento                  |

### Códigos de erro comuns

| Code                     | Status | Uso                                      |
| ------------------------ | ------ | ---------------------------------------- |
| `VALIDATION_FAILED`      | 400    | Campos inválidos no request body         |
| `RESOURCE_NOT_FOUND`     | 404    | Recurso não existe                       |
| `DUPLICATE_RESOURCE`     | 409    | Recurso já existe (constraint violation) |
| `FORBIDDEN_ACTION`       | 403    | Sem permissão para a ação                |
| `UNAUTHORIZED`           | 401    | Token ausente ou inválido                |
| `DEPENDENCY_UNAVAILABLE` | 503    | Serviço externo indisponível             |
| `INTERNAL_ERROR`         | 500    | Erro inesperado interno                  |

### Erros de validação (múltiplos campos)

```json
{
  "type": "https://atlasops/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "detail": "One or more fields have validation errors",
  "traceId": "abc-123",
  "violations": [
    { "field": "email", "message": "must be a valid email address" },
    { "field": "name", "message": "must not be blank" }
  ]
}
```

---

## Paginação

### Request

Parâmetros de query string:

| Parâmetro | Tipo    | Default | Descrição                                       |
| --------- | ------- | ------- | ----------------------------------------------- |
| `page`    | Integer | 0       | Número da página (zero-based)                   |
| `size`    | Integer | 20      | Itens por página (max: 100)                     |
| `sort`    | String  | —       | Campo e direção: `name,asc` ou `createdAt,desc` |

### Response

Envelope de paginação:

```json
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 142,
    "totalPages": 8
  }
}
```

### Regras

- Tamanho máximo de página: **100 itens**
- Tamanho padrão: **20 itens**
- Ordenação padrão: `createdAt,desc` (mais recentes primeiro)
- Página além do total retorna `content: []` com metadados corretos (não 404)

---

## Headers

### Request headers esperados

| Header             | Obrigatório           | Descrição                                  |
| ------------------ | --------------------- | ------------------------------------------ |
| `Authorization`    | Sim (exceto públicos) | `Bearer {jwt-token}`                       |
| `Content-Type`     | Sim (POST/PUT/PATCH)  | `application/json`                         |
| `X-Correlation-ID` | Não                   | UUID para rastreamento (gerado se ausente) |
| `X-Tenant-ID`      | Sim (multi-tenant)    | Identificador do tenant                    |

### Response headers

| Header             | Sempre presente  | Descrição                          |
| ------------------ | ---------------- | ---------------------------------- |
| `X-Correlation-ID` | Sim              | Mesmo valor do request (ou gerado) |
| `Content-Type`     | Sim              | `application/json`                 |
| `X-API-Deprecated` | Quando aplicável | `true` para endpoints deprecados   |

---

## Códigos HTTP

### Sucesso

| Código           | Uso                                           |
| ---------------- | --------------------------------------------- |
| `200 OK`         | GET com resultado, PUT/PATCH com update       |
| `201 Created`    | POST que cria recurso (com header `Location`) |
| `204 No Content` | DELETE com sucesso                            |

### Erro do cliente

| Código                     | Uso                                 |
| -------------------------- | ----------------------------------- |
| `400 Bad Request`          | Validação de campos falhou          |
| `401 Unauthorized`         | Token ausente ou expirado           |
| `403 Forbidden`            | Sem permissão para a ação           |
| `404 Not Found`            | Recurso não existe                  |
| `409 Conflict`             | Duplicata ou violação de constraint |
| `422 Unprocessable Entity` | Regra de negócio violada            |

### Erro do servidor

| Código                      | Uso                      |
| --------------------------- | ------------------------ |
| `500 Internal Server Error` | Erro inesperado          |
| `503 Service Unavailable`   | Dependência indisponível |

---

## OpenAPI / Swagger

- Documentação gerada via SpringDoc OpenAPI
- Acessível em `/swagger-ui.html` (apenas profile `local`)
- Schema JSON em `/v3/api-docs`
- Cada endpoint documentado com:
  - `@Operation(summary = "...", description = "...")`
  - `@ApiResponse` para cada código de retorno possível
  - `@Parameter` para query params
  - `@Schema` em DTOs com descrição e exemplos

### Exemplo de anotação

```java
@Operation(
    summary = "Listar clientes",
    description = "Retorna lista paginada de clientes do tenant"
)
@ApiResponse(responseCode = "200", description = "Lista de clientes")
@ApiResponse(responseCode = "401", description = "Token inválido")
@GetMapping("/api/v1/customers")
public Page<CustomerResponse> list(Pageable pageable) { ... }
```

---

## Multi-Tenancy

- Todo recurso pertence a um tenant
- Header `X-Tenant-ID` obrigatório em todas as requisições autenticadas
- Queries sempre filtradas por tenantId (nunca retornar dados de outro tenant)
- Validar que o usuário autenticado pertence ao tenant do header

---

## Idempotência

- POST com `Idempotency-Key` header para operações críticas
- Mesmo `Idempotency-Key` retorna mesma resposta sem duplicar recurso
- Chave válida por 24 horas após primeira requisição
