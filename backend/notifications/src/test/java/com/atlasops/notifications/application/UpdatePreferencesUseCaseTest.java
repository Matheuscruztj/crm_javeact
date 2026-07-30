package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdatePreferencesUseCaseTest {

  @Test
  void should_persistUpdatedPreferences_whenExecuteIsCalled() {
    NotificationPreferencesRepository repository = mock(NotificationPreferencesRepository.class);
    UpdatePreferencesUseCase useCase = new UpdatePreferencesUseCase(repository);
    NotificationPreferences saved =
        new NotificationPreferences("user-1", "tenant-1", true, List.of("COMMENT"));

    when(repository.save(saved)).thenReturn(saved);

    NotificationPreferences result = useCase.execute("user-1", "tenant-1", true, List.of("COMMENT"));

    assertThat(result).isEqualTo(saved);
  }
}
