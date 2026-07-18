# Padrões de Testes — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Visão Geral

O projeto utiliza duas estratégias complementares de testes:

1. **Unit Tests (JUnit 5 + Mockito)** — Verificam exemplos específicos e edge cases
2. **Property-Based Tests (jqwik)** — Verificam propriedades universais para todas as entradas válidas

Ambas são obrigatórias. Uma não substitui a outra.

---

## Naming de Testes

### Convenção obrigatória

```
should_{resultado}_when_{condição}
```

### Exemplos

```java
// Unit tests
should_rejectTask_when_objectiveIsEmpty()
should_createCustomer_when_allFieldsValid()
should_returnFallback_when_ollamaUnavailable()
should_blockAction_when_outsideSandboxNamespace()
should_generateUuidV4_when_noCorrelationIdHeader()

// Property tests
should_alwaysProduceValidChunks_forAnyNonEmptyDocument()
should_neverExceedMaxResults_forAnyRagQuery()
should_rejectAllInvalidFeatureNames_forAnyNonKebabCase()
```

### Proibições de naming

- ❌ `test1`, `testCreate`, `testValidation`
- ❌ Nomes genéricos sem indicar cenário
- ❌ Nomes em português (manter consistência com código em inglês)

---

## Builders

### Propósito

Builders criam objetos de teste com valores default válidos, permitindo override apenas dos campos relevantes para cada teste.

### Padrão

```java
public class CustomerBuilder {
    private String id = "customer-001";
    private String name = "Empresa Alpha";
    private String email = "alpha@empresa.com";
    private String tenantId = "tenant-alpha";
    private Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");

    public static CustomerBuilder aCustomer() {
        return new CustomerBuilder();
    }

    public CustomerBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public CustomerBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CustomerBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CustomerBuilder withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public CustomerBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Customer build() {
        return new Customer(id, name, email, tenantId, createdAt);
    }
}
```

### Builders obrigatórios

| Builder           | Módulo                  | Responsabilidade               |
| ----------------- | ----------------------- | ------------------------------ |
| `TenantBuilder`   | shared-kernel / tenants | Criar tenants de teste         |
| `UserBuilder`     | users                   | Criar usuários com papéis      |
| `CustomerBuilder` | customers               | Criar clientes por tenant      |
| `TaskBuilder`     | tasks / harness         | Criar tarefas de agente        |
| `AnalysisBuilder` | ai                      | Criar registros de análise     |
| `SandboxBuilder`  | harness                 | Criar configurações de sandbox |

### Regras

- Todo builder tem método factory estático: `aCustomer()`, `aTenant()`, `aUser()`
- Defaults devem representar um estado válido (happy path)
- Nunca usar `null` como default — sempre um valor válido
- Localizar builders em pacote `testfixtures` ou diretório `test/`

---

## Fixtures

### Definição

Fixtures são dados estáticos reutilizáveis entre testes. Diferem de Builders por serem imutáveis.

### Padrão

```java
public final class TestFixtures {

    private TestFixtures() {} // não instanciar

    public static final String TENANT_ALPHA_ID = "tenant-alpha";
    public static final String TENANT_BETA_ID = "tenant-beta";

    public static final String VALID_EMAIL = "user@atlasops.test";
    public static final String VALID_CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";

    public static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
}
```

### Quando usar Fixtures vs Builders

| Situação                                      | Usar              |
| --------------------------------------------- | ----------------- |
| Objeto complexo com variações por teste       | Builder           |
| Constante reutilizável (ID, email, timestamp) | Fixture           |
| Cenário com múltiplos objetos relacionados    | Builder + Fixture |

---

## Isolamento

### Princípios

1. **Cada teste é independente** — a ordem de execução não importa
2. **Sem estado compartilhado** entre testes (exceto fixtures imutáveis)
3. **Mocking de ports** para isolar a camada testada
4. **Clock e IdGenerator** injetáveis e determinísticos em testes

### Clock determinístico

```java
// Em produção: injetar java.time.Clock.systemUTC()
// Em testes: injetar Clock fixo
Clock fixedClock = Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneOffset.UTC);
```

### IdGenerator determinístico

```java
// Em produção: injetar UUID.randomUUID()
// Em testes: injetar sequência previsível
IdGenerator testIdGen = new IdGenerator() {
    private int counter = 0;
    public String generate() { return "test-id-" + (++counter); }
};
```

### Isolamento por camada

| Camada testada   | O que mockar                           | Framework      |
| ---------------- | -------------------------------------- | -------------- |
| `domain`         | Nada (lógica pura)                     | JUnit 5        |
| `application`    | Ports (Repository, Clock, IdGenerator) | Mockito        |
| `infrastructure` | Serviços externos (via Testcontainers) | Testcontainers |
| `presentation`   | Application layer (MockMvc)            | Spring MockMvc |

---

## Mocking

### Framework: Mockito

### Regras de uso

- **Mockar ports** (interfaces), nunca classes concretas
- **Verificar interações** quando a saída não é diretamente observável
- **Preferir stubs** (`when().thenReturn()`) a verificações (`verify()`)
- **Nunca mockar** entidades de domínio, value objects ou records
- **Limite de mocks por teste:** máximo 3 — se precisar de mais, o design pode estar acoplado

### Padrão

```java
@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private Clock clock;

    @InjectMocks
    private CreateCustomerUseCase useCase;

    @Test
    void should_createCustomer_when_allFieldsValid() {
        // Arrange
        when(idGenerator.generate()).thenReturn("customer-001");
        when(clock.now()).thenReturn(TestFixtures.FIXED_NOW);

        var command = new CreateCustomerCommand("Empresa Alpha", "alpha@empresa.com", "tenant-alpha");

        // Act
        var result = useCase.execute(command);

        // Assert
        assertThat(result.id()).isEqualTo("customer-001");
        verify(customerRepository).save(any(Customer.class));
    }
}
```

### Proibições

- ❌ `@MockBean` em testes unitários (apenas em integration tests com Spring)
- ❌ Mockar classes do JDK (`String`, `List`, `Optional`)
- ❌ Mockar classes `final` ou `record`
- ❌ Deep stubs (`RETURNS_DEEP_STUBS`) — indicam design acoplado

---

## Property-Based Tests (jqwik)

### Configuração

```java
@Property(tries = 100)
@Tag("Feature: monorepo-sdd-harness, Property N: título")
```

### Geradores customizados

```java
@Provide
Arbitrary<String> validFeatureNames() {
    return Arbitraries.strings()
        .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                   'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                   'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
                   '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(1)
        .ofMaxLength(50)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
}
```

### Validação de referência

Todo teste de propriedade deve conter o comentário:

```java
/**
 * Validates: Requirements X.Y
 * Property N: Título da propriedade do design document
 */
```

---

## Estrutura de Arquivos de Teste

```
src/test/java/com/atlasops/{modulo}/
├── domain/
│   ├── {Entidade}Test.java           # Unit tests de entidades
│   └── {ValueObject}PropertyTest.java # PBT de value objects
├── application/
│   ├── {UseCase}Test.java            # Unit tests de use cases
│   └── {UseCase}PropertyTest.java    # PBT de use cases (quando aplicável)
├── infrastructure/
│   └── {Adapter}IntegrationTest.java # Integration tests com Testcontainers
├── presentation/
│   └── {Controller}Test.java         # MockMvc tests
└── testfixtures/
    ├── {Nome}Builder.java            # Builders
    └── TestFixtures.java             # Constantes e fixtures
```

---

## Testes de Integração

- Requerem Docker Compose ativo (`make compose-up`)
- Isolamento por tenant: cada teste usa tenant exclusivo
- Timeout máximo: **30 segundos** por teste
- Cleanup de dados entre suítes (não entre testes individuais)
- Usar `@Tag("integration")` para separar de unit tests
- Executar via `make test-integration` ou `./gradlew integrationTest`

---

## Cobertura (Jacoco)

| Escopo             | Meta linhas | Meta branches |
| ------------------ | ----------- | ------------- |
| Projeto total      | ≥ 75%       | ≥ 65%         |
| Módulos de domínio | ≥ 85%       | —             |

### Exclusões permitidas de cobertura

- `package-info.java`
- Classes de configuração Spring (`@Configuration`)
- DTOs e Records sem lógica
- Main class (`AtlasOpsApplication.java`)
