package com.atlasops.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationTest {

  private static final String VALID_ID = "notif-001";
  private static final String VALID_RECIPIENT_USER_ID = "user-001";
  private static final String VALID_TENANT_ID = "tenant-001";
  private static final String VALID_TITLE = "Document Approved";
  private static final String VALID_MESSAGE = "Your document has been approved by the analyst.";
  private static final String VALID_LINK = "/documents/doc-123";
  private static final Instant VALID_CREATED_AT = Instant.parse("2025-01-15T10:30:00Z");

  @Test
  void should_createNotification_when_allFieldsValid() {
    Notification notification =
        Notification.create(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            VALID_TITLE,
            VALID_MESSAGE,
            VALID_LINK,
            VALID_CREATED_AT);

    assertThat(notification.getId()).isEqualTo(VALID_ID);
    assertThat(notification.getRecipientUserId()).isEqualTo(VALID_RECIPIENT_USER_ID);
    assertThat(notification.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(notification.getTitle()).isEqualTo(VALID_TITLE);
    assertThat(notification.getMessage()).isEqualTo(VALID_MESSAGE);
    assertThat(notification.isRead()).isFalse();
    assertThat(notification.getLink()).isEqualTo(VALID_LINK);
    assertThat(notification.getCreatedAt()).isEqualTo(VALID_CREATED_AT);
  }

  @Test
  void should_createNotificationWithReadFalse_when_usingCreateFactory() {
    Notification notification =
        Notification.create(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            VALID_TITLE,
            VALID_MESSAGE,
            VALID_LINK,
            VALID_CREATED_AT);

    assertThat(notification.isRead()).isFalse();
  }

  @Test
  void should_reconstitute_when_readIsTrue() {
    Notification notification =
        Notification.reconstitute(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            VALID_TITLE,
            VALID_MESSAGE,
            true,
            VALID_LINK,
            VALID_CREATED_AT);

    assertThat(notification.isRead()).isTrue();
  }

  @Test
  void should_allowNullLink_when_creating() {
    Notification notification =
        Notification.create(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            VALID_TITLE,
            VALID_MESSAGE,
            null,
            VALID_CREATED_AT);

    assertThat(notification.getLink()).isNull();
  }

  @Test
  void should_markAsRead_when_notificationIsUnread() {
    Notification notification =
        Notification.create(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            VALID_TITLE,
            VALID_MESSAGE,
            VALID_LINK,
            VALID_CREATED_AT);

    assertThat(notification.isRead()).isFalse();

    notification.markAsRead();

    assertThat(notification.isRead()).isTrue();
  }

  @Test
  void should_rejectCreation_when_idIsNull() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    null,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("id");
  }

  @Test
  void should_rejectCreation_when_recipientUserIdIsNull() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    null,
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("RecipientUserId");
  }

  @Test
  void should_rejectCreation_when_recipientUserIdIsBlank() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    "   ",
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RecipientUserId must not be blank");
  }

  @Test
  void should_rejectCreation_when_tenantIdIsNull() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    null,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_rejectCreation_when_tenantIdIsBlank() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    "   ",
                    VALID_TITLE,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId must not be blank");
  }

  @Test
  void should_rejectCreation_when_titleIsNull() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    null,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Title");
  }

  @Test
  void should_rejectCreation_when_titleIsBlank() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    "   ",
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Title must not be blank");
  }

  @Test
  void should_rejectCreation_when_titleExceedsMaxLength() {
    String longTitle = "A".repeat(151);

    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    longTitle,
                    VALID_MESSAGE,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Title must not exceed 150 characters");
  }

  @Test
  void should_acceptTitle_when_exactlyAtMaxLength() {
    String title150 = "A".repeat(150);

    Notification notification =
        Notification.create(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            title150,
            VALID_MESSAGE,
            VALID_LINK,
            VALID_CREATED_AT);

    assertThat(notification.getTitle()).hasSize(150);
  }

  @Test
  void should_rejectCreation_when_messageIsNull() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    null,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Message");
  }

  @Test
  void should_rejectCreation_when_messageIsBlank() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    "   ",
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message must not be blank");
  }

  @Test
  void should_rejectCreation_when_messageExceedsMaxLength() {
    String longMessage = "B".repeat(501);

    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    longMessage,
                    VALID_LINK,
                    VALID_CREATED_AT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message must not exceed 500 characters");
  }

  @Test
  void should_acceptMessage_when_exactlyAtMaxLength() {
    String message500 = "B".repeat(500);

    Notification notification =
        Notification.create(
            VALID_ID,
            VALID_RECIPIENT_USER_ID,
            VALID_TENANT_ID,
            VALID_TITLE,
            message500,
            VALID_LINK,
            VALID_CREATED_AT);

    assertThat(notification.getMessage()).hasSize(500);
  }

  @Test
  void should_rejectCreation_when_createdAtIsNull() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    VALID_ID,
                    VALID_RECIPIENT_USER_ID,
                    VALID_TENANT_ID,
                    VALID_TITLE,
                    VALID_MESSAGE,
                    VALID_LINK,
                    null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("CreatedAt");
  }
}
