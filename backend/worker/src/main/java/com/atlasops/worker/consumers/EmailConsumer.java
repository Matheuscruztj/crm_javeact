package com.atlasops.worker.consumers;

import com.atlasops.notifications.application.SendEmailNotificationCommand;
import com.atlasops.notifications.application.SendEmailNotificationUseCase;
import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import com.atlasops.worker.infrastructure.retry.RetryExecutor;
import com.atlasops.worker.infrastructure.retry.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Consumer for sending email notifications from the notifications.email stream. Sends emails via
 * SMTP (MailHog in dev) with retry logic (3x with 1s, 4s, 16s backoff). On failure after all
 * retries, moves to DLQ.
 *
 * <p>Validates: Requirements 16.1, 16.2, 16.3, 16.4, 16.5
 */
@Component
public class EmailConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
  private static final String STREAM_KEY = "notifications.email";

  private final SendEmailNotificationUseCase sendEmailUseCase;
  private final RetryExecutor retryExecutor;

  public EmailConsumer(SendEmailNotificationUseCase sendEmailUseCase, RetryExecutor retryExecutor) {
    this.sendEmailUseCase = sendEmailUseCase;
    this.retryExecutor = retryExecutor;
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String to = message.getRequired("to");
    String subject = message.getRequired("subject");
    String body = message.getRequired("body");
    String tenantName = message.get("tenantName");

    // Default tenant name if not provided
    if (tenantName == null || tenantName.isBlank()) {
      tenantName = "AtlasOps";
    }

    log.info("Processing email notification for {} (subject: {})", to, subject);

    final String finalTenantName = tenantName;

    TaskResult result =
        retryExecutor.executeWithRetry(
            "email:" + to + ":" + message.messageId(),
            message,
            () -> sendEmail(to, subject, body, finalTenantName));

    if (result instanceof TaskResult.MovedToDlq dlq) {
      log.error(
          "Email to {} failed after {} attempts, moved to DLQ. Subject: {}",
          to,
          dlq.totalAttempts(),
          subject);
    } else {
      log.info("Email sent successfully to {} (subject: {})", to, subject);
    }
  }

  private void sendEmail(String to, String subject, String body, String tenantName) {
    SendEmailNotificationCommand command =
        new SendEmailNotificationCommand(to, subject, body, tenantName);

    sendEmailUseCase.execute(command);
  }
}
