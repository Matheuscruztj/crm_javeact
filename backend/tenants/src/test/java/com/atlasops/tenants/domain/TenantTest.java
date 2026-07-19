package com.atlasops.tenants.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tenant aggregate root")
class TenantTest {

  private static final String TENANT_ID = "550e8400-e29b-41d4-a716-446655440000";
  private static final TenantName VALID_NAME = new TenantName("Atlas Corp");
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");

  @Test
  @DisplayName("should create tenant with ACTIVE status and correct fields")
  void should_createTenantWithActiveStatus_when_allFieldsValid() {
    Tenant tenant = Tenant.create(TENANT_ID, VALID_NAME, FIXED_NOW);

    assertThat(tenant.getId()).isEqualTo(TENANT_ID);
    assertThat(tenant.getName()).isEqualTo(VALID_NAME);
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(tenant.getCreatedAt()).isEqualTo(FIXED_NOW);
    assertThat(tenant.getUpdatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  @DisplayName("should reject null id on create")
  void should_rejectCreation_when_idIsNull() {
    assertThatThrownBy(() -> Tenant.create(null, VALID_NAME, FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("id must not be null");
  }

  @Test
  @DisplayName("should reject null name on create")
  void should_rejectCreation_when_nameIsNull() {
    assertThatThrownBy(() -> Tenant.create(TENANT_ID, null, FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("name must not be null");
  }

  @Test
  @DisplayName("should reject null timestamp on create")
  void should_rejectCreation_when_timestampIsNull() {
    assertThatThrownBy(() -> Tenant.create(TENANT_ID, VALID_NAME, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp must not be null");
  }

  @Test
  @DisplayName("should deactivate tenant and update timestamp")
  void should_setStatusInactive_when_deactivated() {
    Tenant tenant = Tenant.create(TENANT_ID, VALID_NAME, FIXED_NOW);
    Instant deactivateTime = Instant.parse("2025-01-16T12:00:00Z");

    tenant.deactivate(deactivateTime);

    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    assertThat(tenant.getUpdatedAt()).isEqualTo(deactivateTime);
    assertThat(tenant.getCreatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  @DisplayName("should reject null timestamp on deactivate")
  void should_rejectDeactivation_when_timestampIsNull() {
    Tenant tenant = Tenant.create(TENANT_ID, VALID_NAME, FIXED_NOW);

    assertThatThrownBy(() -> tenant.deactivate(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp must not be null");
  }

  @Test
  @DisplayName("should have identity-based equality via Entity base class")
  void should_beEqual_when_sameId() {
    Tenant tenant1 = Tenant.create(TENANT_ID, VALID_NAME, FIXED_NOW);
    Tenant tenant2 = Tenant.create(TENANT_ID, new TenantName("Other Name"), FIXED_NOW);

    assertThat(tenant1).isEqualTo(tenant2);
    assertThat(tenant1.hashCode()).isEqualTo(tenant2.hashCode());
  }

  @Test
  @DisplayName("should not be equal when different id")
  void should_notBeEqual_when_differentId() {
    Tenant tenant1 = Tenant.create(TENANT_ID, VALID_NAME, FIXED_NOW);
    Tenant tenant2 = Tenant.create("660e8400-e29b-41d4-a716-446655440000", VALID_NAME, FIXED_NOW);

    assertThat(tenant1).isNotEqualTo(tenant2);
  }
}
