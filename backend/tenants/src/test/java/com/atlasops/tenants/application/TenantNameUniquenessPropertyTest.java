package com.atlasops.tenants.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.ports.TenantRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.jqwik.api.*;

/**
 * Property-based tests for tenant name uniqueness with case-insensitive comparison.
 *
 * <p><b>Validates: Requirements 3.2, 3.3</b>
 *
 * <p>Property 8: Tenant Name Uniqueness (Case-Insensitive)
 *
 * <p>Requirement 3.2: THE Tenant_Module SHALL enforce uniqueness of tenant name across the platform
 * using case-insensitive comparison
 *
 * <p>Requirement 3.3: IF a tenant creation request uses a name already taken, THEN reject with
 * error indicating name is already in use
 */
@Tag("Feature: project-implementation-kickoff, Property 8: Tenant Name Uniqueness")
class TenantNameUniquenessPropertyTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  /**
   * Property: For ANY two tenant names that differ ONLY in case, creating the second MUST result in
   * a DuplicateResourceException.
   *
   * <p>Validates: Requirements 3.2, 3.3
   */
  @Property(tries = 100)
  void should_rejectCreation_when_tenantNameDiffersOnlyInCase(
      @ForAll("validTenantNames") String originalName,
      @ForAll("caseTransformations") CaseTransformation transformation) {

    // Arrange: create repository that already has the original name
    var repository = new InMemoryTenantRepository();
    var idGen = new SequentialIdGenerator();
    Clock clock = () -> NOW;

    var useCase = new CreateTenantUseCase(repository, idGen, clock);

    // First creation succeeds
    Tenant firstTenant = useCase.execute(originalName);
    assertThat(firstTenant).isNotNull();

    // Apply case transformation to produce a variant that differs only in case
    String caseVariant = transformation.apply(originalName);

    // Act: attempt to create tenant with a case-different name
    Throwable thrown = catchThrowable(() -> useCase.execute(caseVariant));

    // Assert: second creation must fail with DuplicateResourceException
    assertThat(thrown)
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("already exists");
  }

  /**
   * Property: For ANY valid tenant name, the uniqueness check performed by the use case is
   * case-insensitive — identical names in different cases are detected as duplicates.
   *
   * <p>Validates: Requirements 3.2, 3.3
   */
  @Property(tries = 100)
  void should_detectDuplicate_when_sameNameInUpperCase(@ForAll("validTenantNames") String name) {

    // Arrange
    var repository = new InMemoryTenantRepository();
    var idGen = new SequentialIdGenerator();
    Clock clock = () -> NOW;

    var useCase = new CreateTenantUseCase(repository, idGen, clock);

    // First creation succeeds
    useCase.execute(name);

    // Act: attempt to create with ALL UPPERCASE version
    String upperVariant = name.toUpperCase();
    Throwable thrown = catchThrowable(() -> useCase.execute(upperVariant));

    // Assert: must be rejected
    assertThat(thrown)
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("already exists");
  }

  /**
   * Property: For ANY valid tenant name, the uniqueness check performed by the use case is
   * case-insensitive — identical names in lowercase are detected as duplicates.
   *
   * <p>Validates: Requirements 3.2, 3.3
   */
  @Property(tries = 100)
  void should_detectDuplicate_when_sameNameInLowerCase(@ForAll("validTenantNames") String name) {

    // Arrange
    var repository = new InMemoryTenantRepository();
    var idGen = new SequentialIdGenerator();
    Clock clock = () -> NOW;

    var useCase = new CreateTenantUseCase(repository, idGen, clock);

    // First creation succeeds
    useCase.execute(name);

    // Act: attempt to create with ALL LOWERCASE version
    String lowerVariant = name.toLowerCase();
    Throwable thrown = catchThrowable(() -> useCase.execute(lowerVariant));

    // Assert: must be rejected
    assertThat(thrown)
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("already exists");
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validTenantNames() {
    // Generate valid tenant names: 3-50 chars, alphanumeric + hyphens + spaces
    // Must contain at least one letter to make case transformations meaningful
    return Combinators.combine(
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings()
                .withChars(
                    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                    'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
                    'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
                    'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', ' ')
                .ofMinLength(0)
                .ofMaxLength(44))
        .as((prefix, rest) -> prefix + rest)
        .filter(s -> s.trim().length() >= 3 && s.trim().length() <= 50)
        .filter(s -> s.trim().matches("^[a-zA-Z0-9\\- ]+$"))
        .map(String::trim);
  }

  @Provide
  Arbitrary<CaseTransformation> caseTransformations() {
    return Arbitraries.of(CaseTransformation.values());
  }

  // ---- Case Transformation Enum ----

  enum CaseTransformation {
    TO_UPPER {
      @Override
      String apply(String input) {
        return input.toUpperCase();
      }
    },
    TO_LOWER {
      @Override
      String apply(String input) {
        return input.toLowerCase();
      }
    },
    SWAP_CASE {
      @Override
      String apply(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
          if (Character.isUpperCase(c)) {
            sb.append(Character.toLowerCase(c));
          } else if (Character.isLowerCase(c)) {
            sb.append(Character.toUpperCase(c));
          } else {
            sb.append(c);
          }
        }
        return sb.toString();
      }
    };

    abstract String apply(String input);
  }

  // ---- In-Memory Test Doubles ----

  /**
   * In-memory TenantRepository that performs case-insensitive name lookups, simulating the real
   * repository behavior.
   */
  private static class InMemoryTenantRepository implements TenantRepository {

    private final Map<String, Tenant> tenantsById = new HashMap<>();
    private final Map<String, Tenant> tenantsByLowerName = new HashMap<>();

    @Override
    public Optional<Tenant> findById(String id) {
      return Optional.ofNullable(tenantsById.get(id));
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
      return tenantsByLowerName.containsKey(name.toLowerCase());
    }

    @Override
    public Tenant save(Tenant tenant) {
      tenantsById.put(tenant.getId(), tenant);
      tenantsByLowerName.put(tenant.getName().getValue().toLowerCase(), tenant);
      return tenant;
    }
  }

  /** Sequential ID generator for deterministic test behavior. */
  private static class SequentialIdGenerator implements IdGenerator {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String generate() {
      return "tenant-" + counter.incrementAndGet();
    }
  }
}
