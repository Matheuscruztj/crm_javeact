package com.atlasops.notifications.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.atlasops.notifications.application.EmailDeliveryException;
import com.atlasops.notifications.domain.EmailNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpEmailSenderAdapterResilienceTest {

  @Test
  @DisplayName("should_wrapMailException_when_smtpSendFails")
  void should_wrapMailException_when_smtpSendFails() {
    JavaMailSender mailSender = mock(JavaMailSender.class);
    doThrow(new MailException("smtp unavailable") {}).when(mailSender).send(any(SimpleMailMessage.class));

    SmtpEmailSenderAdapter adapter = new SmtpEmailSenderAdapter(mailSender, "noreply@atlasops.local");
    EmailNotification notification =
        new EmailNotification("user@example.com", "Subject", "Body", "Tenant Alpha");

    assertThatThrownBy(() -> adapter.sendEmail(notification))
        .isInstanceOf(EmailDeliveryException.class)
        .hasMessageContaining("user@example.com")
        .hasCauseInstanceOf(MailException.class);

    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("should_sendEmail_when_smtpIsAvailable")
  void should_sendEmail_when_smtpIsAvailable() {
    JavaMailSender mailSender = mock(JavaMailSender.class);
    SmtpEmailSenderAdapter adapter = new SmtpEmailSenderAdapter(mailSender, "noreply@atlasops.local");
    EmailNotification notification =
        new EmailNotification("user@example.com", "Subject", "Body", "Tenant Alpha");

    adapter.sendEmail(notification);

    verify(mailSender).send(any(SimpleMailMessage.class));
  }
}
