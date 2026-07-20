package com.atlasops.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.ai.domain.PromptTemplate;
import com.atlasops.ai.domain.ports.PromptTemplateRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for GetActivePromptUseCase. Validates: P0.F.2 — Prompt Version Registry */
@ExtendWith(MockitoExtension.class)
class GetActivePromptUseCaseTest {

    @Mock private PromptTemplateRepository repository;

    private GetActivePromptUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetActivePromptUseCase(repository);
    }

    @Test
    void should_returnTemplate_when_activeVersionExists() {
        PromptTemplate active = new PromptTemplate(
                "pt-001", "document-analysis", 2, "Analyze: {{content}}", true,
                Instant.parse("2025-01-15T10:00:00Z"));
        when(repository.findActiveByName("document-analysis")).thenReturn(Optional.of(active));

        PromptTemplate result = useCase.execute("document-analysis");

        assertThat(result.getName()).isEqualTo("document-analysis");
        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void should_throwNotFound_when_noActiveVersionExists() {
        when(repository.findActiveByName("missing-template")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing-template"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing-template");
    }

    @Test
    void should_reject_when_nameIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }
}
