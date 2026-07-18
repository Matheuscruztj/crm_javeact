package com.atlasops.shared.domain.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CorrelationIdTest {

  @Test
  void should_createCorrelationId_when_validUuid() {
    var uuid = UUID.randomUUID().toString();
    var correlationId = new CorrelationId(uuid);
    assertThat(correlationId.getValue()).isEqualTo(uuid);
  }

  @Test
  void should_throwException_when_null() {
    assertThatThrownBy(() -> new CorrelationId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }

  @Test
  void should_throwException_when_empty() {
    assertThatThrownBy(() -> new CorrelationId("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwException_when_notUuidFormat() {
    assertThatThrownBy(() -> new CorrelationId("not-a-uuid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UUID format");
  }

  @Test
  void should_throwException_when_partialUuid() {
    assertThatThrownBy(() -> new CorrelationId("12345678-1234"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_generateValidCorrelationId_when_factoryMethod() {
    var correlationId = CorrelationId.generate();
    assertThat(correlationId).isNotNull();
    assertThat(correlationId.getValue())
        .matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  }

  @Test
  void should_generateUniqueIds_when_multipleGenerate() {
    var id1 = CorrelationId.generate();
    var id2 = CorrelationId.generate();
    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void should_beEqual_when_sameUuid() {
    var uuid = UUID.randomUUID().toString();
    var id1 = new CorrelationId(uuid);
    var id2 = new CorrelationId(uuid);
    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }
}
