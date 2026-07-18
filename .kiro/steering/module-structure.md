# Estrutura de Módulos — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Visão Geral

Cada módulo backend segue arquitetura hexagonal com pacotes padronizados. O domínio é o centro — isolado de frameworks, persistência e transporte.

---

## Template de Novo Módulo

Ao criar um novo módulo, seguir esta estrutura exata:

```
backend/{nome-modulo}/
├── build.gradle.kts
└── src/
    ├── main/java/com/atlasops/{modulo}/
    │   ├── package-info.java
    │   ├── domain/
    │   │   ├── package-info.java
    │   │   ├── ports/
    │   │   │   └── package-info.java
    │   │   ├── {Entidade}.java
    │   │   └── {ValueObject}.java
    │   ├── application/
    │   │   ├── package-info.java
    │   │   └── {Verbo}{Substantivo}UseCase.java
    │   ├── infrastructure/
    │   │   ├── package-info.java
    │   │   └── {Tecnologia}{Nome}Adapter.java
    │   └── presentation/
    │       ├── package-info.java
    │       └── {Nome}Controller.java
    └── test/java/com/atlasops/{modulo}/
        ├── domain/
        │   └── {Entidade}Test.java
        ├── application/
        │   └── {UseCase}Test.java
        └── infrastructure/
            └── {Adapter}Test.java
```

---

## Pacotes Obrigatórios

| Pacote            | Responsabilidade                                         | Dependências permitidas                      |
| ----------------- | -------------------------------------------------------- | -------------------------------------------- |
| `domain/`         | Entidades, Value Objects, Domain Events, Domain Services | Apenas `shared-kernel`                       |
| `domain/ports/`   | Interfaces de entrada e saída (ports)                    | Apenas tipos do `domain/`                    |
| `application/`    | Use Cases, Commands, Queries, DTOs                       | `domain/`, `shared-kernel`                   |
| `infrastructure/` | Adapters JPA, Redis, S3, Messaging, AI                   | `domain/ports/`, `shared-kernel`, frameworks |
| `presentation/`   | REST Controllers, Event Consumers                        | `application/`, `shared-kernel`              |

---

## Regras de Dependência

```
presentation → application → domain ← infrastructure
                                ↑
                          shared-kernel
```

### O que cada camada pode importar:

- **domain**: Apenas `shared-kernel` (tipos base, value objects compartilhados)
- **application**: `domain`, `shared-kernel`
- **infrastructure**: `domain.ports`, `shared-kernel`, bibliotecas externas (JPA, Redis, etc.)
- **presentation**: `application`, `shared-kernel`

### Proibições (validadas por ArchUnit):

- ❌ `domain` importando `infrastructure` ou `presentation`
- ❌ `application` importando `infrastructure` ou `presentation`
- ❌ `presentation` acessando `domain` diretamente (deve passar por `application`)
- ❌ Ciclos entre módulos
- ❌ Import de adapter concreto pela camada de API

---

## build.gradle.kts do Módulo

```kotlin
plugins {
    id("java-library")
}

dependencies {
    // Dependência inter-módulo
    implementation(project(":backend:shared-kernel"))

    // Dependências Spring (apenas em infrastructure e presentation)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Testes
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("net.jqwik:jqwik")
    testImplementation("org.assertj:assertj-core")
}
```

**Nota:** Apenas o módulo `app-boot` aplica o plugin `org.springframework.boot`. Demais módulos usam `java-library`.

---

## Testes Mínimos por Módulo

Cada módulo deve conter no mínimo:

| Camada            | Teste Obrigatório                                | Framework                |
| ----------------- | ------------------------------------------------ | ------------------------ |
| `domain/`         | Validação de entidades e value objects           | JUnit 5 + jqwik          |
| `application/`    | Cada Use Case com cenário happy-path e erro      | JUnit 5 + Mockito        |
| `infrastructure/` | Adapter com teste de integração (Testcontainers) | JUnit 5 + Testcontainers |
| Arquitetura       | Regras ArchUnit no módulo                        | ArchUnit                 |

### Cobertura mínima:

- Módulos de domínio: **85% linhas**
- Projeto total: **75% linhas**, **65% branches**

---

## Checklist de Novo Módulo

- [ ] Diretório criado em `backend/{nome-modulo}/`
- [ ] `build.gradle.kts` com dependências corretas
- [ ] Módulo incluído em `settings.gradle.kts` via `include()`
- [ ] Pacotes obrigatórios criados com `package-info.java`
- [ ] Pelo menos uma interface de port em `domain/ports/`
- [ ] Pelo menos um teste unitário
- [ ] Compila com `./gradlew :backend:{nome-modulo}:build`
- [ ] Regras ArchUnit aplicáveis ao módulo

---

## Comunicação entre Módulos

- Módulos se comunicam exclusivamente via interfaces públicas no pacote raiz ou `domain/ports/`
- Nunca acessar pacotes internos (`infrastructure/`, `persistence/`) de outro módulo
- Para eventos assíncronos: usar `EventPublisher` do `shared-kernel`
- Para queries síncronas: expor interface no módulo provedor, implementar no consumidor

---

## Módulo shared-kernel

O `shared-kernel` é especial — contém tipos base e interfaces compartilhadas:

```
backend/shared-kernel/src/main/java/com/atlasops/shared/
├── domain/
│   ├── Entity.java
│   ├── AggregateRoot.java
│   ├── ValueObject.java
│   └── DomainEvent.java
├── ports/
│   ├── Clock.java
│   ├── IdGenerator.java
│   └── EventPublisher.java
├── types/
│   ├── TenantId.java
│   ├── UserId.java
│   ├── Email.java
│   └── CorrelationId.java
└── errors/
    └── StandardError.java
```

Todos os módulos dependem de `shared-kernel`. Nenhum módulo depende diretamente de outro módulo de negócio (exceto `app-boot` que agrega todos).
