package com.atlasops.imports.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.imports.domain.ImportJob;
import com.atlasops.imports.domain.ImportRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CsvImportAdapter.
 * Validates: P0.C.2 — Imports Module with CSV schema inference, preview, validation
 */
class CsvImportAdapterTest {

  private CsvImportAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new CsvImportAdapter();
  }

  @Test
  void should_inferSchema_when_csvHasHeader() {
    String csv = "name,email,phone\nJohn,john@test.com,123\nJane,jane@test.com,456";
    List<String> schema = adapter.inferSchema(csv);
    assertThat(schema).containsExactly("name", "email", "phone");
  }

  @Test
  void should_returnEmptySchema_when_csvIsEmpty() {
    assertThat(adapter.inferSchema("")).isEmpty();
    assertThat(adapter.inferSchema(null)).isEmpty();
  }

  @Test
  void should_returnPreview_when_csvHasRows() {
    String csv = "name,email\nAlice,alice@test.com\nBob,bob@test.com\nCarol,carol@test.com";
    List<List<String>> preview = adapter.preview(csv);
    assertThat(preview).hasSize(3);
    assertThat(preview.get(0)).containsExactly("Alice", "alice@test.com");
  }

  @Test
  void should_limitPreview_when_moreThanFiveRows() {
    StringBuilder csv = new StringBuilder("id,value\n");
    for (int i = 1; i <= 10; i++) {
      csv.append(i).append(",value").append(i).append("\n");
    }
    List<List<String>> preview = adapter.preview(csv.toString());
    assertThat(preview).hasSize(5);
  }

  @Test
  void should_returnNoErrors_when_csvIsValid() {
    String csv = "name,email\nAlice,alice@test.com\nBob,bob@test.com";
    List<String> errors = adapter.validate(csv);
    assertThat(errors).isEmpty();
  }

  @Test
  void should_returnError_when_rowHasWrongColumnCount() {
    String csv = "name,email,phone\nAlice,alice@test.com\nBob,bob@test.com,123456";
    List<String> errors = adapter.validate(csv);
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).contains("Row 2");
  }

  @Test
  void should_returnError_when_csvIsEmpty() {
    List<String> errors = adapter.validate("");
    assertThat(errors).isNotEmpty();
    assertThat(errors.get(0)).contains("empty");
  }

  @Test
  void should_startImport_when_requestIsValid() {
    ImportRequest request = new ImportRequest("CSV", null, "tenant-alpha", "user-001");
    ImportJob job = adapter.startImport(request);
    assertThat(job.jobId()).isNotBlank();
    assertThat(job.status()).isEqualTo("PENDING");
  }
}
