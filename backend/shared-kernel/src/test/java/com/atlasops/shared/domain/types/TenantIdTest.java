package com.atlasops.shared.domain.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TenantIdTest {

  @Test
  void should_createTenantId_when_validString() {
    var tenantId = new TenantId("tenant-alpha");
    assertThat(tenantId.getValue()).isEqualTo("tenant-alpha");
  }

  @Test
  void should_throwException_when_null() {
    assertThatThrownBy(() -> new TenantId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }

  @Test
  void should_throwException_when_empty() {
    assertThatThrownBy(() -> new TenantId("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwException_when_blank() {
    assertThatThrownBy(() -> new TenantId("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_beEqual_when_sameValue() {
    var id1 = new TenantId("tenant-1");
    var id2 = new TenantId("tenant-1");
    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentValue() {
    var id1 = new TenantId("tenant-1");
    var id2 = new TenantId("tenant-2");
    assertThat(id1).isNotEqualTo(id2);
  }
}
