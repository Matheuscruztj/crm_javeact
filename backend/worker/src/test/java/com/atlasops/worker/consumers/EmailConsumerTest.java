package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.application.SendEmailNotificationCommand;
import com.atlasops.notifications.application.SendEmailNotificationUseCase;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import com.atlasops.worker.infrastructure.retry.RetryExecutor;
import com.atlasops.worker.infrastructure.retry.RetryableTask;
import com.atlasops.worker.infrastructure.retry.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for EmailConsumer. Validates: Requirements 16.1, 16.2, 16.3, 16.4, 16.5 */
@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

  @Mock private SendEmailNotificationUseCase sendEmailUseCase;

  @Mock private RetryExecutor retryExecutor;

  private EmailConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new EmailConsumer(sendEmailUseCase, retryExecutor);
  }

  @Test
  void should_returnCorrectStreamKey() {
    assertThat(consumer.getStreamKey()).isEqualTo("notifications.email");
  }

  @Test
  void should_sendEmail_when_messageIsValid() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("to", "user@example.com");
    payload.put("subject", "Document Approved");
    payload.put("body", "Your document has been approved.");
    payload.put("tenantName", "Acme Corp");

    StreamMessage message = new StreamMessage("notifications.email", "msg-1", payload);

    when(retryExecutor.executeWithRetry(
            anyString(), any(StreamMessage.class), any(RetryableTask.class)))
        .thenAnswer(
            invocation -> {
              RetryableTask task = invocation.getArgument(2);
              task.execute();
              return new TaskResult.Success();
            });

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<SendEmailNotificationCommand> captor =
        ArgumentCaptor.forClass(SendEmailNotificationCommand.class);
    verify(sendEmailUseCase).execute(captor.capture());

    SendEmailNotificationCommand capturedCommand = captor.getValue();
    assertThat(capturedCommand.to()).isEqualTo("user@example.com");
    assertThat(capturedCommand.subject()).isEqualTo("Document Approved");
    assertThat(capturedCommand.body()).isEqualTo("Your document has been approved.");
    assertThat(capturedCommand.tenantName()).isEqualTo("Acme Corp");
  }

  @Test
  void should_useDefaultTenantName_when_notProvided() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("to", "user@example.com");
    payload.put("subject", "Test Subject");
    payload.put("body", "Test Body");
    // No tenantName field

    StreamMessage message = new StreamMessage("notifications.email", "msg-2", payload);

    when(retryExecutor.executeWithRetry(
            anyString(), any(StreamMessage.class), any(RetryableTask.class)))
        .thenAnswer(
            invocation -> {
              RetryableTask task = invocation.getArgument(2);
              task.execute();
              return new TaskResult.Success();
            });

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<SendEmailNotificationCommand> captor =
        ArgumentCaptor.forClass(SendEmailNotificationCommand.class);
    verify(sendEmailUseCase).execute(captor.capture());

    assertThat(captor.getValue().tenantName()).isEqualTo("AtlasOps");
  }

  @Test
  void should_handleFailure_when_retryExecutorReturnsDlq() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("to", "user@example.com");
    payload.put("subject", "Test Subject");
    payload.put("body", "Test Body");
    payload.put("tenantName", "Test Tenant");

    StreamMessage message = new StreamMessage("notifications.email", "msg-3", payload);

    when(retryExecutor.executeWithRetry(
            anyString(), any(StreamMessage.class), any(RetryableTask.class)))
        .thenReturn(new TaskResult.MovedToDlq(new RuntimeException("SMTP unavailable"), 3));

    // Act
    consumer.handle(message);

    // Assert - should complete without throwing (DLQ is logged, not thrown)
    verify(retryExecutor)
        .executeWithRetry(anyString(), any(StreamMessage.class), any(RetryableTask.class));
  }
}
