package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.atlasops.notifications.domain.EmailNotification;
import com.atlasops.notifications.domain.ports.EmailSenderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendEmailNotificationUseCaseTest {

  @Mock private EmailSenderPort emailSenderPort;

  private SendEmailNotificationUseCase useCase;

  @BeforeEach
  void setUp() {
    // Use a subclass that skips sleep for fast tests
    useCase =
        new SendEmailNotificationUseCase(emailSenderPort) {
          @Override
          protected void sleep(long millis) {
            // no-op for test speed
          }
        };
  }

  @Test
  void should_sendEmail_when_firstAttemptSucceeds() {
    doNothing().when(emailSenderPort).sendEmail(any(EmailNotification.class));

    var command =
        new SendEmailNotificationCommand(
            "user@example.com", "Subject", "Body content", "TenantName");

    useCase.execute(command);

    ArgumentCaptor<EmailNotification> captor = ArgumentCaptor.forClass(EmailNotification.class);
    verify(emailSenderPort, times(1)).sendEmail(captor.capture());

    EmailNotification sent = captor.getValue();
    assertThat(sent.to()).isEqualTo("user@example.com");
    assertThat(sent.subject()).isEqualTo("Subject");
    assertThat(sent.body()).isEqualTo("Body content");
    assertThat(sent.tenantName()).isEqualTo("TenantName");
  }

  @Test
  void should_retryAndSucceed_when_firstAttemptFailsButSecondSucceeds() {
    doThrow(new RuntimeException("SMTP error"))
        .doNothing()
        .when(emailSenderPort)
        .sendEmail(any(EmailNotification.class));

    var command =
        new SendEmailNotificationCommand("user@example.com", "Subject", "Body", "TenantName");

    useCase.execute(command);

    verify(emailSenderPort, times(2)).sendEmail(any(EmailNotification.class));
  }

  @Test
  void should_retryAndSucceed_when_firstTwoAttemptsFail() {
    doThrow(new RuntimeException("SMTP error"))
        .doThrow(new RuntimeException("SMTP error"))
        .doNothing()
        .when(emailSenderPort)
        .sendEmail(any(EmailNotification.class));

    var command =
        new SendEmailNotificationCommand("user@example.com", "Subject", "Body", "TenantName");

    useCase.execute(command);

    verify(emailSenderPort, times(3)).sendEmail(any(EmailNotification.class));
  }

  @Test
  void should_throwEmailDeliveryException_when_allThreeAttemptsFail() {
    doThrow(new RuntimeException("SMTP error"))
        .when(emailSenderPort)
        .sendEmail(any(EmailNotification.class));

    var command =
        new SendEmailNotificationCommand("user@example.com", "Subject", "Body", "TenantName");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(EmailDeliveryException.class)
        .hasMessageContaining("user@example.com")
        .hasMessageContaining("3 attempts")
        .hasCauseInstanceOf(RuntimeException.class);

    verify(emailSenderPort, times(3)).sendEmail(any(EmailNotification.class));
  }

  @Test
  void should_throwException_when_toIsNull() {
    var command = new SendEmailNotificationCommand(null, "Subject", "Body", "TenantName");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'to'");
  }

  @Test
  void should_throwException_when_subjectIsBlank() {
    var command = new SendEmailNotificationCommand("user@example.com", "  ", "Body", "TenantName");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'subject'");
  }

  @Test
  void should_throwException_when_bodyIsNull() {
    var command =
        new SendEmailNotificationCommand("user@example.com", "Subject", null, "TenantName");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'body'");
  }

  @Test
  void should_throwException_when_tenantNameIsBlank() {
    var command = new SendEmailNotificationCommand("user@example.com", "Subject", "Body", "  ");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'tenantName'");
  }
}
