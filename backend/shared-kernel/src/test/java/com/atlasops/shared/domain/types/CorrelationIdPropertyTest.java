package com.atlasops.shared.domain.types;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for correlation ID generation.
 *
 * <p><b>Validates: Requirements 3.11, 11.4, 11.5</b>
 */
@Tag("Feature: monorepo-sdd-harness, Property 3: Correlation ID Propagation")
class CorrelationIdPropertyTest {

  /**
   * Property: Every generated correlation ID SHALL be a valid UUID v4 format.
   *
   * <p>A valid UUID v4 string must match the pattern: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx where x
   * is any hex digit and y is one of [8, 9, a, b].
   */
  @Property(tries = 100)
  void generatedId_shouldAlwaysBeValidUuidV4Format() {
    CorrelationId correlationId = CorrelationId.generate();

    String value = correlationId.getValue();

    // Must be parseable as a UUID
    UUID uuid = UUID.fromString(value);
    assertThat(uuid).isNotNull();

    // Must match UUID string format (lowercase hex with dashes)
    assertThat(value)
        .matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  }

  /**
   * Property: Every generated correlation ID SHALL have correct UUID version 4 bits.
   *
   * <p>UUID v4 requires version bits to be 0100 (version = 4) at bits 48-51, and variant bits to be
   * 10xx (variant = 2) at bits 62-63.
   */
  @Property(tries = 100)
  void generatedId_shouldAlwaysHaveCorrectVersionAndVariantBits() {
    CorrelationId correlationId = CorrelationId.generate();

    UUID uuid = UUID.fromString(correlationId.getValue());

    // Version must be 4
    assertThat(uuid.version()).isEqualTo(4);

    // Variant must be 2 (RFC 4122 / Leach-Salz)
    assertThat(uuid.variant()).isEqualTo(2);
  }

  /**
   * Property: A batch of generated correlation IDs SHALL contain no duplicates.
   *
   * <p>Given the UUID v4 space (122 random bits), collisions in any reasonable batch should be
   * statistically impossible.
   */
  @Property(tries = 100)
  void generatedIds_shouldBeUniqueInBatch(@ForAll @IntRange(min = 10, max = 50) int batchSize) {
    Set<String> ids = new HashSet<>();

    for (int i = 0; i < batchSize; i++) {
      CorrelationId correlationId = CorrelationId.generate();
      ids.add(correlationId.getValue());
    }

    assertThat(ids).hasSize(batchSize);
  }
}
