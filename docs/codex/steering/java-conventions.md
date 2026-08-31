# Convenções Java — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Linguagem e Runtime

- **Java 21** é a versão mínima obrigatória
- Utilizar features do Java 21: records, sealed interfaces, pattern matching, text blocks
- Não utilizar `var` em assinaturas de método ou campos de classe (apenas variáveis locais onde o tipo é óbvio)

---

## Nomenclatura

### Pacotes

- Base: `com.atlasops.{modulo}.{camada}`
- Exemplos: `com.atlasops.customers.domain`, `com.atlasops.ai.infrastructure`
- Sempre lowercase, singular

### Classes

| Tipo             | Padrão                             | Exemplo                                    |
| ---------------- | ---------------------------------- | ------------------------------------------ |
| Entity           | `{Nome}`                           | `Customer`, `Document`                     |
| Value Object     | `{Nome}` (record)                  | `Email`, `TenantId`                        |
| Use Case         | `{Verbo}{Substantivo}UseCase`      | `CreateCustomerUseCase`                    |
| Port (interface) | `{Nome}Port` ou `{Nome}Repository` | `AIAnalysisPort`, `CustomerRepository`     |
| Adapter          | `{Tecnologia}{Nome}Adapter`        | `OllamaAIAdapter`, `PgVectorSearchAdapter` |
| Controller       | `{Nome}Controller`                 | `CustomerController`                       |
| Domain Event     | `{Entidade}{Ação}Event`            | `CustomerCreatedEvent`                     |
| DTO              | `{Nome}Request` / `{Nome}Response` | `CreateCustomerRequest`                    |
| Builder (test)   | `{Nome}Builder`                    | `CustomerBuilder`, `TenantBuilder`         |
| Exception        | `{Descrição}Exception`             | `VectorStoreUnavailableException`          |

### Métodos

- Verbos no infinitivo: `create`, `update`, `delete`, `find`, `validate`
- Queries: `findById`, `findAllByTenantId`, `existsByEmail`
- Commands: `create`, `update`, `archive`, `approve`
- Validações: `validate`, `ensureValid`, `checkPermission`
- Factories: `of`, `from`, `create`

### Constantes

- `UPPER_SNAKE_CASE`: `MAX_CHUNK_SIZE`, `DEFAULT_TIMEOUT_MS`
- Definidas como `private static final` ou em interface de constantes do módulo

---

## Formato de Código

### Imports

- Ordem obrigatória (forçada pelo Spotless):
  1. `java.*`
  2. `javax.*`
  3. `jakarta.*`
  4. Linha em branco
  5. `org.*`
  6. `com.*`
  7. Linha em branco
  8. `com.atlasops.*`
- **Proibido:** imports com wildcard (`*`). Sempre importar classes específicas
- **Proibido:** imports não utilizados (removidos automaticamente pelo Spotless)

### Formatação

- Indentação: 4 espaços (nunca tabs)
- Largura máxima de linha: 120 caracteres
- Abertura de chave na mesma linha
- Espaço após keywords (`if`, `for`, `while`, `try`)
- Linha em branco entre métodos
- Sem trailing whitespace

### Records e Sealed

```java
// Records para Value Objects e DTOs
public record Email(String value) {
    public Email {
        Objects.requireNonNull(value, "Email value must not be null");
        if (!value.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }
}

// Sealed para hierarquias controladas
public sealed interface DomainEvent permits CustomerCreated, CustomerUpdated {}
```

---

## Anotações

### Ordem de anotações (de cima para baixo)

1. Anotações de framework (`@Service`, `@Component`, `@RestController`)
2. Anotações de escopo (`@RequestScope`, `@Transactional`)
3. Anotações de documentação (`@Tag`, `@Operation`)
4. Anotações de validação (`@Valid`, `@NotNull`)

### Spring

- Preferir injeção por construtor (nunca `@Autowired` em campos)
- `@RequiredArgsConstructor` (Lombok) ou construtor explícito
- `@Transactional` apenas em Use Cases, nunca em Controllers ou Repositories

---

## Tratamento de Erros

- Exceptions de domínio estendem `RuntimeException`
- Nunca capturar `Exception` genérica (exceto em filtros globais)
- Usar `Optional` para retornos que podem ser vazios (nunca retornar `null`)
- Log de exceções no ponto de tratamento, não no ponto de lançamento

---

## Proibições

- ❌ `System.out.println` — usar SLF4J Logger
- ❌ `new Date()` — usar `Clock` injetável
- ❌ `UUID.randomUUID()` direto — usar `IdGenerator` injetável
- ❌ Dependência direta entre módulos via classes concretas
- ❌ Lógica de negócio em Controllers
- ❌ Acesso a banco de dados fora da camada `infrastructure`
- ❌ Imports de `infrastructure` dentro de `domain`
