package com.atlasops.tenants.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TenantName value object")
class TenantNameTest {

  @Test
  @DisplayName("should create valid TenantName when name is within bounds and valid chars")
  void should_createValidTenantName_when_nameIsValid() {
    var name = new TenantName("Atlas Corp");

    assertThat(name.getValue()).isEqualTo("Atlas Corp");
  }

  @Test
  @DisplayName("should trim whitespace from name")
  void should_trimWhitespace_when_nameHasLeadingOrTrailingSpaces() {
    var name = new TenantName("  Atlas Corp  ");

    assertThat(name.getValue()).isEqualTo("Atlas Corp");
  }

  @Test
  @DisplayName("should accept name with minimum length of 3 characters")
  void should_acceptName_when_lengthIsExactlyThree() {
    var name = new TenantName("Abc");

    assertThat(name.getValue()).isEqualTo("Abc");
  }

  @Test
  @DisplayName("should accept name with maximum length of 100 characters")
  void should_acceptName_when_lengthIsExactlyHundred() {
    String hundredChars = "a".repeat(100);
    var name = new TenantName(hundredChars);

    assertThat(name.getValue()).isEqualTo(hundredChars);
  }

  @Test
  @DisplayName("should accept name with hyphens")
  void should_acceptName_when_containsHyphens() {
    var name = new TenantName("my-tenant");

    assertThat(name.getValue()).isEqualTo("my-tenant");
  }

  @Test
  @DisplayName("should accept name with numbers")
  void should_acceptName_when_containsNumbers() {
    var name = new TenantName("Tenant 123");

    assertThat(name.getValue()).isEqualTo("Tenant 123");
  }

  @Test
  @DisplayName("should reject null name")
  void should_rejectName_when_null() {
    assertThatThrownBy(() -> new TenantName(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null");
  }

  @Test
  @DisplayName("should reject name shorter than 3 characters")
  void should_rejectName_when_tooShort() {
    assertThatThrownBy(() -> new TenantName("ab"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least 3 characters");
  }

  @Test
  @DisplayName("should reject name longer than 100 characters")
  void should_rejectName_when_tooLong() {
    String longName = "a".repeat(101);

    assertThatThrownBy(() -> new TenantName(longName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at most 100 characters");
  }

  @Test
  @DisplayName("should reject name with invalid characters (underscore)")
  void should_rejectName_when_containsUnderscore() {
    assertThatThrownBy(() -> new TenantName("invalid_name"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("alphanumeric characters, hyphens, and spaces");
  }

  @Test
  @DisplayName("should reject name with special characters")
  void should_rejectName_when_containsSpecialChars() {
    assertThatThrownBy(() -> new TenantName("name@corp"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("alphanumeric characters, hyphens, and spaces");
  }

  @Test
  @DisplayName("should reject name with only whitespace that results in empty after trim")
  void should_rejectName_when_onlyWhitespace() {
    assertThatThrownBy(() -> new TenantName("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should be equal for case-insensitive comparison")
  void should_beEqual_when_namesMatchCaseInsensitively() {
    var name1 = new TenantName("Atlas Corp");
    var name2 = new TenantName("atlas corp");

    assertThat(name1).isEqualTo(name2);
    assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
  }

  @Test
  @DisplayName("should not be equal for different names")
  void should_notBeEqual_when_namesDiffer() {
    var name1 = new TenantName("Atlas Corp");
    var name2 = new TenantName("Beta Corp");

    assertThat(name1).isNotEqualTo(name2);
  }

  @Test
  @DisplayName("should preserve original case in getValue")
  void should_preserveOriginalCase_when_gettingValue() {
    var name = new TenantName("Atlas Corp");

    assertThat(name.getValue()).isEqualTo("Atlas Corp");
  }
}
