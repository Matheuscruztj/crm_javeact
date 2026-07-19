package com.atlasops.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailNotificationTest {

  private static final String VALID_TO = "client@example.com";
  private static final String VALID_SUBJECT = "Document Approved";
  private static final String VALID_BODY = "Your document has been approved.";
  private static final String VALID_TENANT_NAME = "Acme Corp";

  @Test
  void should_createEmailNotification_when_allFieldsValid() {
    EmailNotification email =
        new EmailNotification(VALID_TO, VALID_SUBJECT, VALID_BODY, VALID_TENANT_NAME);

    assertThat(email.to()).isEqualTo(VALID_TO);
    assertThat(email.subject()).isEqualTo(VALID_SUBJECT);
    assertThat(email.body()).isEqualTo(VALID_BODY);
    assertThat(email.tenantName()).isEqualTo(VALID_TENANT_NAME);
  }

  @Test
  void should_rejectCreation_when_toIsNull() {
    assertThatThrownBy(
            () -> new EmailNotification(null, VALID_SUBJECT, VALID_BODY, VALID_TENANT_NAME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("to");
  }

  @Test
  void should_rejectCreation_when_toIsBlank() {
    assertThatThrownBy(
            () -> new EmailNotification("   ", VALID_SUBJECT, VALID_BODY, VALID_TENANT_NAME))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'to' must not be blank");
  }

  @Test
  void should_rejectCreation_when_subjectIsNull() {
    assertThatThrownBy(() -> new EmailNotification(VALID_TO, null, VALID_BODY, VALID_TENANT_NAME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("subject");
  }

  @Test
  void should_rejectCreation_when_subjectIsBlank() {
    assertThatThrownBy(() -> new EmailNotification(VALID_TO, "   ", VALID_BODY, VALID_TENANT_NAME))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'subject' must not be blank");
  }

  @Test
  void should_rejectCreation_when_bodyIsNull() {
    assertThatThrownBy(
            () -> new EmailNotification(VALID_TO, VALID_SUBJECT, null, VALID_TENANT_NAME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("body");
  }

  @Test
  void should_rejectCreation_when_bodyIsBlank() {
    assertThatThrownBy(
            () -> new EmailNotification(VALID_TO, VALID_SUBJECT, "   ", VALID_TENANT_NAME))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'body' must not be blank");
  }

  @Test
  void should_rejectCreation_when_tenantNameIsNull() {
    assertThatThrownBy(() -> new EmailNotification(VALID_TO, VALID_SUBJECT, VALID_BODY, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("tenantName");
  }

  @Test
  void should_rejectCreation_when_tenantNameIsBlank() {
    assertThatThrownBy(() -> new EmailNotification(VALID_TO, VALID_SUBJECT, VALID_BODY, "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'tenantName' must not be blank");
  }
}
