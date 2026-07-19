package com.atlasops.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.ports.PasswordHashPort;
import com.atlasops.users.domain.ports.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.jqwik.api.*;

/**
 * Property-based tests for email uniqueness within a tenant.
 *
 * <p><b>Validates: Requirements 5.3, 6.2</b>
 *
 * <p>Property 9: Email Uniqueness Within Tenant
 *
 * <p>Requirement 5.3: THE User_Module SHALL enforce email uniqueness within a tenant
 * (case-insensitive comparison)
 *
 * <p>Requirement 6.2: THE Customer_Module SHALL enforce customer email uniqueness within a tenant
 */
@Tag("Feature: project-implementation-kickoff, Property 9: Email Uniqueness Within Tenant")
class EmailUniquenessWithinTenantPropertyTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String HASHED_PASSWORD = "$2a$10$hashedPasswordValue";

  /**
   * Property: For ANY valid email, creating two users with the SAME email (case-insensitive) in the
   * SAME tenant SHALL throw DuplicateResourceException on the second attempt.
   *
   * <p>Validates: Requirement 5.3
   */
  @Property(tries = 100)
  void should_throwDuplicateResourceException_when_sameEmailCreatedTwiceInSameTenant(
      @ForAll("validEmails") String email,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("emailCaseVariations") CaseVariation caseVariation) {

    // Arrange
    var repository = new InMemoryUserRepository();
    var useCase = buildUseCase(repository);

    String firstEmail = email.toLowerCase();
    String secondEmail = applyCaseVariation(email, caseVariation);

    var firstCommand =
        new CreateUserCommand(firstEmail, "First User", "password123", "ANALYST", tenantId);

    var secondCommand =
        new CreateUserCommand(secondEmail, "Second User", "password456", "CLIENT", tenantId);

    // Act: create first user successfully
    User firstUser = useCase.execute(firstCommand);
    assertThat(firstUser).isNotNull();

    // Act: try creating second user with same email (possibly different case) in same tenant
    Throwable thrown = catchThrowable(() -> useCase.execute(secondCommand));

    // Assert: second creation must fail with DuplicateResourceException
    assertThat(thrown)
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining(email.toLowerCase());
  }

  /**
   * Property: For ANY valid email, creating users with the SAME email in DIFFERENT tenants SHALL
   * succeed without conflict.
   *
   * <p>Validates: Requirements 5.3, 6.2 (uniqueness is scoped to tenant, not global)
   */
  @Property(tries = 100)
  void should_succeedWithoutConflict_when_sameEmailUsedInDifferentTenants(
      @ForAll("validEmails") String email, @ForAll("distinctTenantPairs") String[] tenantPair) {

    // Arrange
    var repository = new InMemoryUserRepository();
    var useCase = buildUseCase(repository);

    String tenantA = tenantPair[0];
    String tenantB = tenantPair[1];

    var commandTenantA =
        new CreateUserCommand(email, "User In Tenant A", "password123", "ADMIN", tenantA);

    var commandTenantB =
        new CreateUserCommand(email, "User In Tenant B", "password456", "ANALYST", tenantB);

    // Act: create user in tenant A
    User userA = useCase.execute(commandTenantA);

    // Act: create user with same email in tenant B
    User userB = useCase.execute(commandTenantB);

    // Assert: both creations succeed
    assertThat(userA).isNotNull();
    assertThat(userB).isNotNull();
    assertThat(userA.getEmail()).isEqualTo(email.toLowerCase());
    assertThat(userB.getEmail()).isEqualTo(email.toLowerCase());
    assertThat(userA.getTenantId()).isEqualTo(tenantA);
    assertThat(userB.getTenantId()).isEqualTo(tenantB);
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validEmails() {
    Arbitrary<String> localPart =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '_')
            .ofMinLength(2)
            .ofMaxLength(15)
            .filter(s -> s.matches("^[a-z][a-z0-9._]*[a-z0-9]$"));

    Arbitrary<String> domain =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(2)
            .ofMaxLength(10)
            .filter(s -> s.matches("^[a-z]+$"));

    Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "dev");

    return Combinators.combine(localPart, domain, tld)
        .as((local, dom, t) -> local + "@" + dom + "." + t);
  }

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(5)
        .ofMaxLength(20)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  Arbitrary<String[]> distinctTenantPairs() {
    return validTenantIds()
        .flatMap(
            tenantA ->
                validTenantIds()
                    .filter(tenantB -> !tenantB.equals(tenantA))
                    .map(tenantB -> new String[] {tenantA, tenantB}));
  }

  @Provide
  Arbitrary<CaseVariation> emailCaseVariations() {
    return Arbitraries.of(CaseVariation.values());
  }

  // ---- Helper types ----

  enum CaseVariation {
    ALL_UPPERCASE,
    ALL_LOWERCASE,
    MIXED_CASE
  }

  private String applyCaseVariation(String email, CaseVariation variation) {
    return switch (variation) {
      case ALL_UPPERCASE -> email.toUpperCase();
      case ALL_LOWERCASE -> email.toLowerCase();
      case MIXED_CASE -> toMixedCase(email);
    };
  }

  private String toMixedCase(String input) {
    char[] chars = input.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (i % 2 == 0) {
        chars[i] = Character.toUpperCase(chars[i]);
      } else {
        chars[i] = Character.toLowerCase(chars[i]);
      }
    }
    return new String(chars);
  }

  // ---- Helper methods ----

  private CreateUserUseCase buildUseCase(InMemoryUserRepository repository) {
    PasswordHashPort passwordHashPort =
        new PasswordHashPort() {
          @Override
          public String hash(String rawPassword) {
            return HASHED_PASSWORD;
          }

          @Override
          public boolean verify(String rawPassword, String hashedPassword) {
            return false;
          }
        };

    AtomicInteger idCounter = new AtomicInteger(0);
    IdGenerator idGenerator = () -> "user-" + idCounter.incrementAndGet();

    Clock clock = () -> NOW;

    return new CreateUserUseCase(repository, passwordHashPort, idGenerator, clock);
  }

  // ---- In-Memory Test Double ----

  /**
   * In-memory implementation of UserRepository that simulates real persistence with
   * case-insensitive email uniqueness per tenant.
   */
  private static class InMemoryUserRepository implements UserRepository {

    // Key: "lowercase-email:tenantId" → User
    private final Map<String, User> store = new HashMap<>();

    @Override
    public Optional<User> findById(String id) {
      return store.values().stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<User> findByEmailAndTenantId(String email, String tenantId) {
      String key = buildKey(email, tenantId);
      return Optional.ofNullable(store.get(key));
    }

    @Override
    public boolean existsByEmailAndTenantId(String email, String tenantId) {
      String key = buildKey(email, tenantId);
      return store.containsKey(key);
    }

    @Override
    public User save(User user) {
      String key = buildKey(user.getEmail(), user.getTenantId());
      store.put(key, user);
      return user;
    }

    private String buildKey(String email, String tenantId) {
      return email.toLowerCase() + ":" + tenantId;
    }
  }
}
