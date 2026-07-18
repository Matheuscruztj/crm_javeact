package com.atlasops.shared.domain.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void should_createUserId_when_validString() {
    var userId = new UserId("user-abc");
    assertThat(userId.getValue()).isEqualTo("user-abc");
  }

  @Test
  void should_throwException_when_null() {
    assertThatThrownBy(() -> new UserId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }

  @Test
  void should_throwException_when_empty() {
    assertThatThrownBy(() -> new UserId("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwException_when_blank() {
    assertThatThrownBy(() -> new UserId("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_beEqual_when_sameValue() {
    var id1 = new UserId("user-1");
    var id2 = new UserId("user-1");
    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }
}
