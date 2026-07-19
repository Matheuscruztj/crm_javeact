package com.atlasops.notifications.infrastructure;

import com.atlasops.notifications.application.EmailDeliveryException;
import com.atlasops.notifications.domain.EmailNotification;
import com.atlasops.notifications.domain.ports.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * SMTP adapter implementing the EmailSenderPort. Sends emails via JavaMailSender (MailHog in
 * development).
 */
@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

  private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

  private final JavaMailSender mailSender;
  private final String fromAddress;

  public SmtpEmailSenderAdapter(
      JavaMailSender mailSender,
      @Value("${atlasops.mail.from:noreply@atlasops.local}") String fromAddress) {
    this.mailSender = mailSender;
    this.fromAddress = fromAddress;
  }

  @Override
  public void sendEmail(EmailNotification emailNotification) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(emailNotification.to());
    message.setSubject(formatSubject(emailNotification));
    message.setText(emailNotification.body());

    try {
      mailSender.send(message);
      log.info(
          "Email sent successfully to {} with subject '{}'",
          emailNotification.to(),
          message.getSubject());
    } catch (MailException e) {
      log.error("Failed to send email to {}: {}", emailNotification.to(), e.getMessage());
      throw new EmailDeliveryException("Failed to send email to " + emailNotification.to(), e);
    }
  }

  private String formatSubject(EmailNotification notification) {
    return String.format("[%s] %s", notification.tenantName(), notification.subject());
  }
}
