package com.atlasops.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.ai.domain.PromptTemplate;
import com.atlasops.ai.domain.ports.PromptTemplateRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for RegisterPromptVersionUseCase. Validates: P0.F.2 */
@ExtendWith(MockitoExtension.class)
class RegisterPromptVersionUseCaseTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private PromptTemplateRepository repository;
    @Mock private IdGenerator idGenerator;
    @Mock private Clock clock;

    private RegisterPromptVersionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterPromptVersionUseCase(repository, idGenerator, clock);
        when(idGenerator.generate()).thenReturn("pt-new");
        when(clock.now()).thenReturn(NOW);
    }

    @Test
    void should_assignVersion1_when_noExistingVersions() {
        when(repository.findAllByName("doc-analysis")).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PromptTemplate result = useCase.execute("doc-analysis", "Analyze: {{content}}", false);

        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("doc-analysis");
    }

    @Test
    void should_incrementVersion_when_existingVersionsPresent() {
        PromptTemplate existing = new PromptTemplate("pt-001", "doc-analysis", 2,
                "Old content", false, NOW.minusSeconds(3600));
        when(repository.findAllByName("doc-analysis")).thenReturn(List.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PromptTemplate result = useCase.execute("doc-analysis", "New content", true);

        assertThat(result.getVersion()).isEqualTo(3);
    }

    @Test
    void should_reject_when_nameIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, "content", false))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_reject_when_contentIsNull() {
        assertThatThrownBy(() -> useCase.execute("name", null, false))
                .isInstanceOf(NullPointerException.class);
    }
}
