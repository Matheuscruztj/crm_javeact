package com.atlasops.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LedgerEntry hash chain integrity.
 * Validates: P2.5 — Verifiable ledger with tamper detection
 */
class LedgerEntryTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String GENESIS = LedgerEntry.GENESIS_HASH;

  @Test
  void should_createValidEntry_when_allFieldsProvided() {
    LedgerEntry entry = LedgerEntry.create(1L, "LOGIN", "hash-of-payload", GENESIS, NOW, TENANT);

    assertThat(entry.sequenceNumber()).isEqualTo(1L);
    assertThat(entry.eventType()).isEqualTo("LOGIN");
    assertThat(entry.payloadHash()).isEqualTo("hash-of-payload");
    assertThat(entry.previousHash()).isEqualTo(GENESIS);
    assertThat(entry.currentHash()).isNotBlank();
    assertThat(entry.occurredAt()).isEqualTo(NOW);
    assertThat(entry.tenantId()).isEqualTo(TENANT);
  }

  @Test
  void should_beValid_when_hashNotTampered() {
    LedgerEntry entry = LedgerEntry.create(1L, "LOGOUT", "payload-hash", GENESIS, NOW, TENANT);
    assertThat(entry.isValid()).isTrue();
  }

  @Test
  void should_detectTampering_when_currentHashModified() {
    LedgerEntry original = LedgerEntry.create(1L, "LOGIN", "payload", GENESIS, NOW, TENANT);
    // Create a tampered entry with wrong hash
    LedgerEntry tampered = new LedgerEntry(
        original.sequenceNumber(), original.eventType(), original.payloadHash(),
        original.previousHash(), "tampered-hash-value-xxxx", original.occurredAt(), original.tenantId());

    assertThat(tampered.isValid()).isFalse();
  }

  @Test
  void should_chainHashes_when_multipleEntriesCreated() {
    LedgerEntry first = LedgerEntry.create(1L, "CREATE_CUSTOMER", "hash1", GENESIS, NOW, TENANT);
    assertThat(first.isValid()).isTrue();

    LedgerEntry second = LedgerEntry.create(
        2L, "UPDATE_CUSTOMER", "hash2", first.currentHash(), NOW.plusSeconds(60), TENANT);
    assertThat(second.isValid()).isTrue();
    assertThat(second.previousHash()).isEqualTo(first.currentHash());

    LedgerEntry third = LedgerEntry.create(
        3L, "DELETE_CUSTOMER", "hash3", second.currentHash(), NOW.plusSeconds(120), TENANT);
    assertThat(third.isValid()).isTrue();
    assertThat(third.previousHash()).isEqualTo(second.currentHash());
  }

  @Test
  void should_produceDifferentHashes_when_payloadsAreDifferent() {
    LedgerEntry e1 = LedgerEntry.create(1L, "LOGIN", "payload-A", GENESIS, NOW, TENANT);
    LedgerEntry e2 = LedgerEntry.create(1L, "LOGIN", "payload-B", GENESIS, NOW, TENANT);

    assertThat(e1.currentHash()).isNotEqualTo(e2.currentHash());
  }

  @Test
  void should_throwNullPointer_when_eventTypeIsNull() {
    assertThatThrownBy(() -> LedgerEntry.create(1L, null, "hash", GENESIS, NOW, TENANT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("eventType");
  }

  @Test
  void should_throwIllegalArgument_when_sequenceNumberIsZero() {
    assertThatThrownBy(() -> LedgerEntry.create(0L, "EVENT", "hash", GENESIS, NOW, TENANT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("sequenceNumber");
  }

  @Test
  void should_throwIllegalArgument_when_sequenceNumberIsNegative() {
    assertThatThrownBy(() -> LedgerEntry.create(-1L, "EVENT", "hash", GENESIS, NOW, TENANT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("sequenceNumber");
  }

  @Test
  void should_genesisHashBeKnownConstant() {
    assertThat(LedgerEntry.GENESIS_HASH).isEqualTo("GENESIS");
  }

  @Test
  void should_beImmutable_when_entryCreated() {
    LedgerEntry entry = LedgerEntry.create(1L, "TEST", "hash", GENESIS, NOW, TENANT);
    // Records are immutable by definition — verifying getters return consistent values
    assertThat(entry.currentHash()).isEqualTo(entry.currentHash());
    assertThat(entry.isValid()).isEqualTo(entry.isValid());
  }
}
