package com.atlasops.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.atlasops.operations.domain.ProjectionStatus;
import com.atlasops.operations.domain.ports.ProjectionRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProjectionsUseCaseTest {

  @Mock private ProjectionRepository projectionRepository;
  private GetProjectionsUseCase useCase;

  @BeforeEach
  void setUp() { useCase = new GetProjectionsUseCase(projectionRepository); }

  @Test
  void should_returnAllProjections_sortedByName() {
    var search = ProjectionStatus.create("search-index");
    var vector = ProjectionStatus.create("vector-index");
    var analytics = ProjectionStatus.create("analytics");
    when(projectionRepository.findAll()).thenReturn(List.of(search, vector, analytics));

    List<ProjectionStatus> result = useCase.execute();

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getName()).isEqualTo("analytics");
    assertThat(result.get(1).getName()).isEqualTo("search-index");
    assertThat(result.get(2).getName()).isEqualTo("vector-index");
  }

  @Test
  void should_returnEmpty_when_noProjections() {
    when(projectionRepository.findAll()).thenReturn(List.of());
    assertThat(useCase.execute()).isEmpty();
  }
}
