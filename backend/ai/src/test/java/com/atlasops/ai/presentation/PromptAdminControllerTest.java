package com.atlasops.ai.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.atlasops.ai.application.GetActivePromptUseCase;
import com.atlasops.ai.application.RegisterPromptVersionUseCase;
import com.atlasops.ai.domain.PromptTemplate;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for PromptAdminController.
 * Validates: P0.F.2 — Prompt Version Registry
 */
@ExtendWith(MockitoExtension.class)
class PromptAdminControllerTest {

    @Mock private GetActivePromptUseCase getActivePromptUseCase;
    @Mock private RegisterPromptVersionUseCase registerPromptVersionUseCase;

    private PromptAdminController controller;

    @BeforeEach
    void setUp() {
        controller = new PromptAdminController(getActivePromptUseCase, registerPromptVersionUseCase);
    }

    private PromptTemplate aTemplate(int version, boolean active) {
        return new PromptTemplate("pt-00" + version, "doc-analysis", version,
                "Analyze: {{content}}", active, Instant.parse("2025-01-15T10:00:00Z"));
    }

    @Test
    void should_returnActiveTemplate_when_nameHasActiveVersion() {
        when(getActivePromptUseCase.execute("doc-analysis")).thenReturn(aTemplate(2, true));

        ResponseEntity<?> response = controller.getActive("doc-analysis");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_returnNotFound_when_noActiveVersion() {
        when(getActivePromptUseCase.execute("missing")).thenThrow(
                new ResourceNotFoundException("No active prompt template found for name: missing"));

        // Controller should not catch the exception (Spring exception handler would)
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.getActive("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_registerNewVersion_when_validRequest() {
        PromptTemplate created = aTemplate(3, true);
        when(registerPromptVersionUseCase.execute(anyString(), anyString(), anyBoolean()))
                .thenReturn(created);

        var request = new PromptAdminController.RegisterPromptRequest(
                "Analyze this document: {{content}}", true);
        ResponseEntity<?> response = controller.register("doc-analysis", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
