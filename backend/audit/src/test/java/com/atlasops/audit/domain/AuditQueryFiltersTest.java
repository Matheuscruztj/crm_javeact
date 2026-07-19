package com.atlasops.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditQueryFiltersTest {

  private static final String TENANT_ID = "tenant-alpha";
  private static final Instant FROM = Instant.parse("2025-01-01T00:00:00Z");
  private static final Instant TO = Instant.parse("2025-01-31T23:59:59Z");

  @Test
  @DisplayName("should create filters when only tenantId is provided")
  void should_createFilters_when_onlyTenantIdProvided() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant(TENANT_ID);

    assertThat(filters.tenantId()).isEqualTo(TENANT_ID);
    assertThat(filters.actorId()).isNull();
    assertThat(filters.entityType()).isNull();
    assertThat(filters.entityId()).isNull();
    assertThat(filters.actionType()).isNull();
    assertThat(filters.fromTimestamp()).isNull();
    assertThat(filters.toTimestamp()).isNull();
  }

  @Test
  @DisplayName("should create filters with all optional fields")
  void should_createFilters_when_allFieldsProvided() {
    AuditQueryFilters filters =
        new AuditQueryFilters(
            TENANT_ID, "user-123", "CUSTOMER", "customer-456", "CREATED", FROM, TO);

    assertThat(filters.tenantId()).isEqualTo(TENANT_ID);
    assertThat(filters.actorId()).isEqualTo("user-123");
    assertThat(filters.entityType()).isEqualTo("CUSTOMER");
    assertThat(filters.entityId()).isEqualTo("customer-456");
    assertThat(filters.actionType()).isEqualTo("CREATED");
    assertThat(filters.fromTimestamp()).isEqualTo(FROM);
    assertThat(filters.toTimestamp()).isEqualTo(TO);
  }

  @Test
  @DisplayName("should reject null tenantId")
  void should_rejectCreation_when_tenantIdIsNull() {
    assertThatThrownBy(() -> new AuditQueryFilters(null, null, null, null, null, null, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId must not be null");
  }

  @Test
  @DisplayName("should reject blank tenantId")
  void should_rejectCreation_when_tenantIdIsBlank() {
    assertThatThrownBy(() -> new AuditQueryFilters("   ", null, null, null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId must not be blank");
  }

  @Test
  @DisplayName("should reject when fromTimestamp is after toTimestamp")
  void should_rejectCreation_when_fromTimestampIsAfterToTimestamp() {
    Instant later = Instant.parse("2025-02-01T00:00:00Z");
    Instant earlier = Instant.parse("2025-01-01T00:00:00Z");

    assertThatThrownBy(
            () -> new AuditQueryFilters(TENANT_ID, null, null, null, null, later, earlier))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("fromTimestamp must not be after toTimestamp");
  }

  @Test
  @DisplayName("should accept when fromTimestamp equals toTimestamp")
  void should_acceptCreation_when_fromTimestampEqualsToTimestamp() {
    Instant same = Instant.parse("2025-01-15T10:00:00Z");

    AuditQueryFilters filters =
        new AuditQueryFilters(TENANT_ID, null, null, null, null, same, same);

    assertThat(filters.fromTimestamp()).isEqualTo(same);
    assertThat(filters.toTimestamp()).isEqualTo(same);
  }

  @Test
  @DisplayName("should accept filters with only fromTimestamp")
  void should_acceptCreation_when_onlyFromTimestampProvided() {
    AuditQueryFilters filters =
        new AuditQueryFilters(TENANT_ID, null, null, null, null, FROM, null);

    assertThat(filters.fromTimestamp()).isEqualTo(FROM);
    assertThat(filters.toTimestamp()).isNull();
  }

  @Test
  @DisplayName("should accept filters with only toTimestamp")
  void should_acceptCreation_when_onlyToTimestampProvided() {
    AuditQueryFilters filters = new AuditQueryFilters(TENANT_ID, null, null, null, null, null, TO);

    assertThat(filters.fromTimestamp()).isNull();
    assertThat(filters.toTimestamp()).isEqualTo(TO);
  }
}
