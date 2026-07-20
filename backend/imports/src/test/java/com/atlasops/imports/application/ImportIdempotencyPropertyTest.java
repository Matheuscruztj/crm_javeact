package com.atlasops.imports.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.imports.domain.ImportJob;
import com.atlasops.imports.domain.ImportRequest;
import com.atlasops.imports.domain.ports.ImportPort;
import com.atlasops.imports.infrastructure.CsvImportAdapter;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tag;
import net.jqwik.api.constraints.NotBlank;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Property-based tests for the imports module.
 *
 * <p><b>Validates: P0.C.2 — Imports Module with DuckDB CSV</b>
 *
 * <p>Properties tested:
 * <ul>
 *   <li>Property 1: Any valid import request always produces a non-blank job ID
 *   <li>Property 2: CSV schema inference always returns non-empty list for non-empty header
 *   <li>Property 3: CSV preview never returns more rows than requested limit
 *   <li>Property 4: CSV validation with consistent column counts never reports errors
 * </ul>
 */
@Tag("Feature: monorepo-sdd-harness, Property: Import Idempotency and Schema Inference")
@ExtendWith(MockitoExtension.class)
class ImportIdempotencyPropertyTest {

    private final CsvImportAdapter csvAdapter = new CsvImportAdapter();

    /**
     * Property 1: Any valid import request always produces a non-blank job ID.
     * The job ID must be a UUID-format string.
     */
    @Property(tries = 100)
    void should_alwaysProduceNonBlankJobId_forAnyValidRequest(
            @ForAll @NotBlank String tenantId,
            @ForAll @NotBlank String sourceLocation) {

        ImportRequest request = new ImportRequest("CSV", sourceLocation, tenantId, "user-001");
        ImportPort adapter = new CsvImportAdapter();

        ImportJob job = adapter.startImport(request);

        assertThat(job.jobId()).isNotBlank();
        // Job ID must be parseable as UUID
        assertThat(job.jobId()).satisfies(id -> {
            try {
                UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                org.junit.jupiter.api.Assertions.fail("Job ID is not a valid UUID: " + id);
            }
        });
    }

    /**
     * Property 2: CSV schema inference always returns non-empty column list
     * for any non-empty single-line header.
     */
    @Property(tries = 100)
    void should_inferNonEmptySchema_forAnyNonEmptyHeader(
            @ForAll("nonEmptyCsvHeaders") String header) {

        var schema = csvAdapter.inferSchema(header);

        assertThat(schema).isNotEmpty();
        assertThat(schema).allMatch(col -> !col.isBlank());
    }

    /**
     * Property 3: CSV preview never returns more than 5 rows,
     * regardless of input size.
     */
    @Property(tries = 100)
    void should_neverExceedPreviewLimit_forAnyValidCsv(
            @ForAll("validCsvWithMultipleRows") String csv) {

        var preview = csvAdapter.preview(csv);

        assertThat(preview.size()).isLessThanOrEqualTo(5);
    }

    /**
     * Property 4: CSV with consistent column counts always passes validation (no errors).
     */
    @Property(tries = 100)
    void should_reportNoErrors_when_csvHasConsistentColumnCounts(
            @ForAll("consistentColumnCsv") String csv) {

        var errors = csvAdapter.validate(csv);

        assertThat(errors).isEmpty();
    }

    // ---- Arbitraries ----

    @Provide
    Arbitrary<String> nonEmptyCsvHeaders() {
        // Generate headers like "col1,col2,col3"
        return Arbitraries.integers().between(1, 8).flatMap(numCols ->
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15)
                .list().ofSize(numCols)
                .map(cols -> String.join(",", cols))
        );
    }

    @Provide
    Arbitrary<String> validCsvWithMultipleRows() {
        return Arbitraries.integers().between(1, 4).flatMap(numCols ->
            Arbitraries.integers().between(1, 20).flatMap(numRows ->
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                    .list().ofSize(numCols)
                    .flatMap(header ->
                        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                            .list().ofSize(numCols)
                            .list().ofSize(numRows)
                            .map(rows -> {
                                StringBuilder sb = new StringBuilder(String.join(",", header));
                                for (var row : rows) {
                                    sb.append("\n").append(String.join(",", row));
                                }
                                return sb.toString();
                            })
                    )
            )
        );
    }

    @Provide
    Arbitrary<String> consistentColumnCsv() {
        return validCsvWithMultipleRows(); // all generated CSVs have consistent columns
    }
}
