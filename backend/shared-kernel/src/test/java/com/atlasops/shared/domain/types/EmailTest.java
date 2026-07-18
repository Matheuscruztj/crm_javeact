package com.atlasops.shared.domain.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void should_createEmail_when_validFormat() {
    var email = new Email("user@example.com");
    assertThat(email.getValue()).isEqualTo("user@example.com");
  }

  @Test
  void should_lowercaseEmail_when_created() {
    var email = new Email("User@Example.COM");
    assertThat(email.getValue()).isEqualTo("user@example.com");
  }

  @Test
  void should_throwException_when_null() {
    assertThatThrownBy(() -> new Email(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or empty");
  }

  @Test
  void should_throwException_when_empty() {
    assertThatThrownBy(() -> new Email("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwException_when_noAtSymbol() {
    assertThatThrownBy(() -> new Email("invalid-email"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid email format");
  }

  @Test
  void should_throwException_when_noDomain() {
    assertThatThrownBy(() -> new Email("user@")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwException_when_noTld() {
    assertThatThrownBy(() -> new Email("user@domain")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_beEqual_when_sameEmailDifferentCase() {
    var email1 = new Email("User@Example.com");
    var email2 = new Email("user@example.com");
    assertThat(email1).isEqualTo(email2);
    assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
  }

  @Test
  void should_acceptEmail_when_subdomainsPresent() {
    var email = new Email("user@sub.domain.com");
    assertThat(email.getValue()).isEqualTo("user@sub.domain.com");
  }

  @Test
  void should_acceptEmail_when_plusAddressing() {
    var email = new Email("user+tag@example.com");
    assertThat(email.getValue()).isEqualTo("user+tag@example.com");
  }
}
