package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.EmailNotification;
import com.atlasops.notifications.domain.ports.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for sending email notifications asynchronously with retry logic. Retries up to 3 times
 * with exponential backoff (1s, 4s, 16s) before giving up.
 */
@Service
public class SendEmailNotificationUseCase {

  private static final Logger log = LoggerFactory.getLogger(SendEmailNotificationUseCase.class);
  private static final int MAX_RETRIES = 3;
  private static final long[] BACKOFF_DELAYS_MS = {1000L, 4000L, 16000L};

  private final EmailSenderPort emailSenderPort;

  public SendEmailNotificationUseCase(EmailSenderPort emailSenderPort) {
    this.emailSenderPort = emailSenderPort;
  }

  /**
   * Sends an email notification with retry logic.
   *
   * @param command the send email command
   * @throws EmailDeliveryException if all retry attempts fail
   */
  public void execute(SendEmailNotificationCommand command) {
    validateCommand(command);

    EmailNotification emailNotification =
        new EmailNotification(
            command.to(), command.subject(), command.body(), command.tenantName());

    RuntimeException lastException = null;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        emailSenderPort.sendEmail(emailNotification);
        log.info("Email sent successfully to {} on attempt {}", command.to(), attempt);
        return;
      } catch (RuntimeException e) {
        lastException = e;
        log.warn(
            "Email delivery attempt {}/{} failed for {}: {}",
            attempt,
            MAX_RETRIES,
            command.to(),
            e.getMessage());

        if (attempt < MAX_RETRIES) {
          sleep(BACKOFF_DELAYS_MS[attempt - 1]);
        }
      }
    }

    throw new EmailDeliveryException(
        "Failed to send email to " + command.to() + " after " + MAX_RETRIES + " attempts",
        lastException);
  }

  /**
   * Sleeps for the specified duration. Protected for testability.
   *
   * @param millis the duration in milliseconds
   */
  protected void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Email retry sleep interrupted", e);
    }
  }

  private void validateCommand(SendEmailNotificationCommand command) {
    if (command.to() == null || command.to().isBlank()) {
      throw new IllegalArgumentException("Email 'to' must not be null or empty");
    }
    if (command.subject() == null || command.subject().isBlank()) {
      throw new IllegalArgumentException("Email 'subject' must not be null or empty");
    }
    if (command.body() == null || command.body().isBlank()) {
      throw new IllegalArgumentException("Email 'body' must not be null or empty");
    }
    if (command.tenantName() == null || command.tenantName().isBlank()) {
      throw new IllegalArgumentException("Email 'tenantName' must not be null or empty");
    }
  }
}
