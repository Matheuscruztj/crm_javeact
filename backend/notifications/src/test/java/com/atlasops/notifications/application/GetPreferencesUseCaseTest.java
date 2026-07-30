package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import org.junit.jupiter.api.Test;

class GetPreferencesUseCaseTest {

  @Test
  void should_returnPreferencesFromRepository_whenPreferencesExist() {
    NotificationPreferencesRepository repository = mock(NotificationPreferencesRepository.class);
    GetPreferencesUseCase useCase = new GetPreferencesUseCase(repository);
    NotificationPreferences preferences =
        new NotificationPreferences("user-1", "tenant-1", false, java.util.List.of("ALERT"));

    when(repository.findOrDefault("user-1", "tenant-1")).thenReturn(preferences);

    assertThat(useCase.execute("user-1", "tenant-1")).isEqualTo(preferences);
  }
}
