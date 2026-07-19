package com.atlasops.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final String USER_ID = "user-001";
  private static final String EMAIL = "john@atlasops.test";
  private static final String NAME = "John Doe";
  private static final String PASSWORD_HASH = "$2a$10$hashedpasswordvalue";
  private static final String TENANT_ID = "tenant-alpha";
  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  @Test
  void should_createUserWithActiveStatus_when_allFieldsAreValid() {
    User user = User.create(USER_ID, EMAIL, NAME, PASSWORD_HASH, UserRole.ANALYST, TENANT_ID, NOW);

    assertThat(user.getId()).isEqualTo(USER_ID);
    assertThat(user.getEmail()).isEqualTo(EMAIL);
    assertThat(user.getName()).isEqualTo(NAME);
    assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
    assertThat(user.getRole()).isEqualTo(UserRole.ANALYST);
    assertThat(user.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.getCreatedAt()).isEqualTo(NOW);
    assertThat(user.getUpdatedAt()).isEqualTo(NOW);
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void should_updateRole_when_newRoleIsProvided() {
    User user = User.create(USER_ID, EMAIL, NAME, PASSWORD_HASH, UserRole.ANALYST, TENANT_ID, NOW);
    Instant updateTime = Instant.parse("2025-01-16T12:00:00Z");

    user.updateRole(UserRole.ADMIN, updateTime);

    assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(user.getUpdatedAt()).isEqualTo(updateTime);
  }

  @Test
  void should_setStatusInactive_when_userIsDeactivated() {
    User user = User.create(USER_ID, EMAIL, NAME, PASSWORD_HASH, UserRole.ANALYST, TENANT_ID, NOW);
    Instant deactivationTime = Instant.parse("2025-01-17T08:00:00Z");

    user.deactivate(deactivationTime);

    assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    assertThat(user.isActive()).isFalse();
    assertThat(user.getUpdatedAt()).isEqualTo(deactivationTime);
  }

  @Test
  void should_returnTrue_when_userIsActive() {
    User user = User.create(USER_ID, EMAIL, NAME, PASSWORD_HASH, UserRole.CLIENT, TENANT_ID, NOW);

    assertThat(user.isActive()).isTrue();
  }

  @Test
  void should_returnFalse_when_userIsInactive() {
    User user = User.create(USER_ID, EMAIL, NAME, PASSWORD_HASH, UserRole.CLIENT, TENANT_ID, NOW);
    user.deactivate(NOW);

    assertThat(user.isActive()).isFalse();
  }

  @Test
  void should_preserveCreatedAt_when_roleIsUpdated() {
    User user = User.create(USER_ID, EMAIL, NAME, PASSWORD_HASH, UserRole.ANALYST, TENANT_ID, NOW);
    Instant updateTime = Instant.parse("2025-01-20T14:30:00Z");

    user.updateRole(UserRole.ADMIN, updateTime);

    assertThat(user.getCreatedAt()).isEqualTo(NOW);
    assertThat(user.getUpdatedAt()).isEqualTo(updateTime);
  }
}
