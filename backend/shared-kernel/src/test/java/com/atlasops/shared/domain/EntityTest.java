package com.atlasops.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EntityTest {

  static class TestEntity extends Entity<String> {
    TestEntity(String id) {
      super(id);
    }
  }

  @Test
  void should_storeId_when_validIdProvided() {
    var entity = new TestEntity("abc-123");
    assertThat(entity.getId()).isEqualTo("abc-123");
  }

  @Test
  void should_throwException_when_nullId() {
    assertThatThrownBy(() -> new TestEntity(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("must not be null");
  }

  @Test
  void should_beEqual_when_sameIdAndType() {
    var entity1 = new TestEntity("id-1");
    var entity2 = new TestEntity("id-1");
    assertThat(entity1).isEqualTo(entity2);
    assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentId() {
    var entity1 = new TestEntity("id-1");
    var entity2 = new TestEntity("id-2");
    assertThat(entity1).isNotEqualTo(entity2);
  }

  @Test
  void should_notBeEqual_when_comparedToNull() {
    var entity = new TestEntity("id-1");
    assertThat(entity).isNotEqualTo(null);
  }
}
