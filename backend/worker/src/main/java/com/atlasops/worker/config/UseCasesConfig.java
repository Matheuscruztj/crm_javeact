package com.atlasops.worker.config;

import com.atlasops.activities.application.RecordActivityUseCase;
import com.atlasops.activities.domain.ports.ActivityRepository;
import com.atlasops.approvals.application.CreatePendingApprovalUseCase;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.notifications.application.CreateNotificationUseCase;
import com.atlasops.notifications.application.PushSSEEventUseCase;
import com.atlasops.notifications.application.SendEmailNotificationUseCase;
import com.atlasops.notifications.domain.ports.EmailSenderPort;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import com.atlasops.notifications.domain.ports.SSEConnectionPort;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration providing use case beans for worker consumers. Wires domain ports to application
 * layer use cases.
 */
@Configuration
public class UseCasesConfig {

  @Bean
  public CreatePendingApprovalUseCase createPendingApprovalUseCase(
      ApprovalRepository approvalRepository, IdGenerator idGenerator, Clock clock) {
    return new CreatePendingApprovalUseCase(approvalRepository, idGenerator, clock);
  }

  @Bean
  public RecordActivityUseCase recordActivityUseCase(
      ActivityRepository activityRepository, IdGenerator idGenerator, Clock clock) {
    return new RecordActivityUseCase(activityRepository, idGenerator, clock);
  }

  @Bean
  public CreateNotificationUseCase createNotificationUseCase(
      NotificationRepository notificationRepository, IdGenerator idGenerator, Clock clock) {
    return new CreateNotificationUseCase(notificationRepository, idGenerator, clock);
  }

  @Bean
  public SendEmailNotificationUseCase sendEmailNotificationUseCase(
      EmailSenderPort emailSenderPort) {
    return new SendEmailNotificationUseCase(emailSenderPort);
  }

  @Bean
  public PushSSEEventUseCase pushSSEEventUseCase(SSEConnectionPort sseConnectionPort) {
    return new PushSSEEventUseCase(sseConnectionPort);
  }
}
