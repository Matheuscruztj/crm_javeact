# AtlasOps AI — Build & Test Performance Guide

> **Versão:** 1.0  
> **Última atualização:** 2026-07-19

---

## Sumário

Este documento descreve as otimizações de performance implementadas no sistema de build e testes do AtlasOps AI.

---

## Otimizações Implementadas

### 1. Gradle Performance

#### Configuration Cache

```properties
# gradle.properties
org.gradle.configuration-cache=true
```

- Cacheia o resultado da fase de configuração do Gradle
- Reduz significativamente o tempo de builds subsequentes
- Especialmente efetivo em monorepos com muitos subprojetos

#### Parallel Execution

```properties
org.gradle.parallel=true
```

- Compila módulos independentes em paralelo
- Utiliza todos os cores disponíveis
- Redução de ~40-60% no tempo de build completo

#### Build Cache

```properties
org.gradle.caching=true
```

- Reutiliza outputs de builds anteriores
- Funciona entre diferentes branches
- Pode ser compartilhado entre CI runners (configuração adicional necessária)

#### File System Watching

```properties
org.gradle.vfs.watch=true
```

- Monitora mudanças no filesystem em tempo real
- Evita rescaneamento completo de diretórios
- Melhora performance de builds incrementais

#### JVM Tuning

```properties
org.gradle.jvmargs=-Xmx4g -XX:+UseG1GC -XX:MaxMetaspaceSize=512m
```

- Heap aumentado para 4GB (suporta compilação de 19 módulos)
- G1 Garbage Collector (melhor para heaps grandes)
- Metaspace limitado para evitar memory leaks

---

### 2. Test Performance

#### JUnit 5 Parallel Execution

```kotlin
// build.gradle.kts
systemProperty("junit.jupiter.execution.parallel.enabled", "true")
systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
```

- Testes executam em paralelo dentro de cada módulo
- Classes de teste também executam concorrentemente
- Factor dinâmico ajusta baseado nos recursos disponíveis

#### Multi-Fork Test Execution

```kotlin
maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
```

- Múltiplos processos JVM executam testes simultaneamente
- Evita contenção de recursos (usa metade dos CPUs)
- Cada fork processa ~100 testes antes de reciclar

#### Test Categorization

```kotlin
// Testes rápidos (padrão)
excludeTags("slow", "integration", "property")

// Testes de integração
includeTags("integration")

// Property-based tests
includeTags("property")
```

- Permite executar subconjuntos de testes
- `make test-fast` para feedback rápido durante desenvolvimento
- `make test-all` para validação completa

---

### 3. Compilation Optimizations

#### Incremental Compilation

```kotlin
options.isIncremental = true
```

- Recompila apenas classes afetadas por mudanças
- Reduz tempo de recompilação de segundos para milissegundos

#### Fork Compilation

```kotlin
options.isFork = true
options.forkOptions.jvmArgs = listOf("-Xmx1g")
```

- Compila em JVM separada
- Melhor gerenciamento de memória
- Evita crashes por OOM no processo principal

---

## Comandos Disponíveis

### Build

| Comando            | Descrição                  | Tempo Estimado |
| ------------------ | -------------------------- | -------------- |
| `make build-fast`  | Build sem testes e checks  | ~30s           |
| `make build`       | Build completo com caching | ~2-3min        |
| `make clean`       | Limpa artifacts            | ~5s            |
| `make clean-cache` | Limpa caches do Gradle     | ~10s           |

### Testes

| Comando                 | Descrição                    | Tempo Estimado |
| ----------------------- | ---------------------------- | -------------- |
| `make test-fast`        | Testes unitários rápidos     | ~30s           |
| `make test-unit`        | Todos os testes unitários    | ~1-2min        |
| `make test-property`    | Property-based tests (jqwik) | ~3-5min        |
| `make test-integration` | Testes de integração         | ~5-10min       |
| `make test-all`         | Todos os testes              | ~10-15min      |

### Verificação

| Comando            | Descrição                      | Tempo Estimado |
| ------------------ | ------------------------------ | -------------- |
| `make verify-fast` | Format + compile + fast tests  | ~1min          |
| `make verify`      | Todos os quality gates         | ~5min          |
| `make verify-full` | Gates + integration + coverage | ~15min         |

---

## Benchmarks de Performance

### Antes das Otimizações

```
Full Build:      ~8-10 min
Unit Tests:      ~4-5 min
Full Verify:     ~15-20 min
```

### Depois das Otimizações

```
Full Build:      ~2-3 min (70% mais rápido)
Unit Tests:      ~1-2 min (60% mais rápido)
Fast Verify:     ~1 min (nova opção)
Full Verify:     ~5-8 min (60% mais rápido)
```

---

## Como Usar Efetivamente

### Durante Desenvolvimento

```bash
# Feedback rápido enquanto codifica
make test-fast

# Antes de commit
make verify-fast
```

### Antes de Push

```bash
# Verificação completa
make verify
```

### Em CI/CD

```bash
# Full validation
make verify-full
```

---

## Troubleshooting

### Build Lento Após Atualização

```bash
# Limpar caches possivelmente corrompidos
make clean-cache
make clean
make build
```

### OutOfMemoryError

```bash
# Aumentar memória temporariamente
GRADLE_OPTS="-Xmx6g" make build
```

### Configuration Cache Incompatível

```bash
# Desabilitar temporariamente
./gradlew build --no-configuration-cache
```

### Flaky Tests

```kotlin
// Marcar como @Tag("flaky") e investigar
@Tag("flaky")
@Test
void should_sometimeFail() { ... }
```

---

## Próximas Otimizações (Planejadas)

1. **Remote Build Cache** — Compartilhar cache entre desenvolvedores
2. **Gradle Enterprise** — Métricas detalhadas de build
3. **Test Retry Plugin** — Retry automático de testes flaky
4. **Compilation Avoidance** — Skip compilação de módulos não alterados

---

## Referências

- [Gradle Performance Guide](https://docs.gradle.org/current/userguide/performance.html)
- [JUnit 5 Parallel Execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution)
- [Configuration Cache](https://docs.gradle.org/current/userguide/configuration_cache.html)
