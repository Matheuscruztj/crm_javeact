package com.atlasops.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserRoleTest {

  @Test
  void should_returnAdmin_when_stringIsAdmin() {
    assertThat(UserRole.fromString("ADMIN")).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void should_returnAnalyst_when_stringIsAnalyst() {
    assertThat(UserRole.fromString("ANALYST")).isEqualTo(UserRole.ANALYST);
  }

  @Test
  void should_returnClient_when_stringIsClient() {
    assertThat(UserRole.fromString("CLIENT")).isEqualTo(UserRole.CLIENT);
  }

  @ParameterizedTest
  @ValueSource(strings = {"admin", "Admin", "aDmIn", "analyst", "Analyst", "client", "Client"})
  void should_returnCorrectRole_when_stringIsCaseInsensitive(String input) {
    UserRole result = UserRole.fromString(input);
    assertThat(result).isNotNull();
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsInvalid() {
    assertThatThrownBy(() -> UserRole.fromString("INVALID"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid role: INVALID");
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsNull() {
    assertThatThrownBy(() -> UserRole.fromString(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsEmpty() {
    assertThatThrownBy(() -> UserRole.fromString(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsBlank() {
    assertThatThrownBy(() -> UserRole.fromString("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }
}
