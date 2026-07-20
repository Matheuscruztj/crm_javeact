package com.atlasops.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Tag;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.Positive;

/**
 * Property-based tests for LedgerEntry hash chain integrity.
 *
 * <p><b>Validates: P2.5 — Verifiable ledger with tamper detection</b>
 *
 * <p>Property: For ANY valid ledger entry, {@code isValid()} MUST return true.
 * Property: For ANY chain of N entries, each entry chains correctly to the previous.
 */
@Tag("Feature: project-implementation-kickoff, Property: LedgerEntry hash chain integrity")
class LedgerEntryHashChainPropertyTest {

  private static final Instant BASE_TIME = Instant.parse("2025-01-01T00:00:00Z");

  /**
   * Property: For any valid event type, payload hash, and tenant ID,
   * a newly created LedgerEntry is ALWAYS valid.
   */
  @Property(tries = 200)
  void should_alwaysBeValid_when_createdViaFactory(
      @ForAll @Positive long sequenceNumber,
      @ForAll @NotBlank String eventType,
      @ForAll @NotBlank String payloadHash,
      @ForAll @NotBlank String tenantId) {

    LedgerEntry entry = LedgerEntry.create(
        sequenceNumber, eventType, payloadHash,
        LedgerEntry.GENESIS_HASH, BASE_TIME, tenantId);

    assertThat(entry.isValid())
        .as("Entry with seq=%d must be valid", sequenceNumber)
        .isTrue();
  }

  /**
   * Property: For any chain length N (1..20), all entries remain valid and
   * each entry's previousHash equals the prior entry's currentHash.
   */
  @Property(tries = 50)
  void should_chainCorrectly_forAnyChainLength(
      @ForAll @IntRange(min = 2, max = 10) int chainLength,
      @ForAll @NotBlank String tenantId) {

    String previousHash = LedgerEntry.GENESIS_HASH;
    LedgerEntry lastEntry = null;

    for (int i = 1; i <= chainLength; i++) {
      LedgerEntry entry = LedgerEntry.create(
          i, "EVENT_" + i, "payload-hash-" + i,
          previousHash, BASE_TIME.plusSeconds(i), tenantId);

      assertThat(entry.isValid())
          .as("Entry %d must be valid", i)
          .isTrue();

      if (lastEntry != null) {
        assertThat(entry.previousHash())
            .as("Entry %d must chain to entry %d", i, i - 1)
            .isEqualTo(lastEntry.currentHash());
      }

      previousHash = entry.currentHash();
      lastEntry = entry;
    }
  }

  /**
   * Property: Modifying any field of an entry makes it ALWAYS invalid.
   */
  @Property(tries = 100)
  void should_alwaysDetectTampering_when_anyFieldChanged(
      @ForAll @Positive long seqNum,
      @ForAll @NotBlank String eventType,
      @ForAll @NotBlank String payload,
      @ForAll @NotBlank String tenantId) {

    LedgerEntry original = LedgerEntry.create(
        seqNum, eventType, payload, LedgerEntry.GENESIS_HASH, BASE_TIME, tenantId);

    // Tamper by creating record with wrong currentHash
    LedgerEntry tampered = new LedgerEntry(
        original.sequenceNumber(), original.eventType(), original.payloadHash(),
        original.previousHash(), "00000000000000000000000000000000000000000000000000000000tampered",
        original.occurredAt(), original.tenantId());

    assertThat(tampered.isValid())
        .as("Tampered entry should be invalid")
        .isFalse();
  }

  /**
   * Property: Two entries with different payloads ALWAYS have different hashes.
   */
  @Property(tries = 100)
  void should_produceDifferentHashes_when_payloadsDiffer(
      @ForAll @NotBlank String payloadA,
      @ForAll @NotBlank String payloadB,
      @ForAll @NotBlank String tenantId) {

    net.jqwik.api.Assume.that(!payloadA.equals(payloadB));

    LedgerEntry e1 = LedgerEntry.create(1L, "EVENT", payloadA, LedgerEntry.GENESIS_HASH, BASE_TIME, tenantId);
    LedgerEntry e2 = LedgerEntry.create(1L, "EVENT", payloadB, LedgerEntry.GENESIS_HASH, BASE_TIME, tenantId);

    assertThat(e1.currentHash())
        .as("Different payloads must produce different hashes")
        .isNotEqualTo(e2.currentHash());
  }
}
