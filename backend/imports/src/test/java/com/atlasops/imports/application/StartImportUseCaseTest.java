package com.atlasops.imports.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.imports.domain.ImportJob;
import com.atlasops.imports.domain.ImportRequest;
import com.atlasops.imports.domain.ports.ImportPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link StartImportUseCase}.
 * Validates: P0.C.2 — Imports module
 */
@ExtendWith(MockitoExtension.class)
class StartImportUseCaseTest {

    @Mock
    private ImportPort importPort;

    @InjectMocks
    private StartImportUseCase useCase;

    @Test
    void should_startImport_when_requestIsValid() {
        ImportRequest request = new ImportRequest("CSV", "/data/customers.csv", "tenant-alpha", "user-001");
        ImportJob expected = new ImportJob("job-001", "PENDING", -1L, 0L);
        when(importPort.startImport(any())).thenReturn(expected);

        ImportJob result = useCase.execute(request);

        assertThat(result.jobId()).isEqualTo("job-001");
        assertThat(result.status()).isEqualTo("PENDING");
        verify(importPort).startImport(request);
    }

    @Test
    void should_rejectImport_when_tenantIdIsBlank() {
        ImportRequest request = new ImportRequest("CSV", "/data/file.csv", "", "user-001");

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantId");
    }

    @Test
    void should_rejectImport_when_sourceTypeIsBlank() {
        ImportRequest request = new ImportRequest("", "/data/file.csv", "tenant-alpha", "user-001");

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SourceType");
    }
}
