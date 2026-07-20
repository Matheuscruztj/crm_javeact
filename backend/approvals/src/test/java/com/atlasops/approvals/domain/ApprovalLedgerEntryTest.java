package com.atlasops.approvals.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ApprovalLedgerEntry} hash chain integrity.
 * Validates: P0.F.1.5 — Integrity tests and tampering detection
 */
class ApprovalLedgerEntryTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createValidEntry_when_genesisEntry() {
        ApprovalLedgerEntry entry = ApprovalLedgerEntry.create(
                1L, "approval-001", "APPROVED", "analyst-001", NOW,
                "tenant-alpha", ApprovalLedgerEntry.GENESIS_HASH);

        assertThat(entry.isValid()).isTrue();
        assertThat(entry.entryHash()).hasSize(64); // SHA-256 hex = 64 chars
        assertThat(entry.previousHash()).isEqualTo(ApprovalLedgerEntry.GENESIS_HASH);
    }

    @Test
    void should_chainHashes_when_sequentialEntries() {
        ApprovalLedgerEntry first = ApprovalLedgerEntry.create(
                1L, "approval-001", "APPROVED", "analyst-001", NOW,
                "tenant-alpha", ApprovalLedgerEntry.GENESIS_HASH);

        ApprovalLedgerEntry second = ApprovalLedgerEntry.create(
                2L, "approval-002", "REJECTED", "analyst-001", NOW,
                "tenant-alpha", first.entryHash());

        assertThat(second.previousHash()).isEqualTo(first.entryHash());
        assertThat(second.isValid()).isTrue();
    }

    @Test
    void should_detectTampering_when_entryHashModified() {
        ApprovalLedgerEntry entry = ApprovalLedgerEntry.create(
                1L, "approval-001", "APPROVED", "analyst-001", NOW,
                "tenant-alpha", ApprovalLedgerEntry.GENESIS_HASH);

        // Simulate tampering by creating an entry with wrong hash
        ApprovalLedgerEntry tampered = new ApprovalLedgerEntry(
                entry.sequenceNumber(), entry.approvalId(), "REJECTED", // changed status
                entry.decisionBy(), entry.occurredAt(), entry.tenantId(),
                entry.previousHash(), entry.entryHash()); // hash unchanged!

        assertThat(tampered.isValid()).isFalse();
    }

    @Test
    void should_produceDifferentHashes_when_fieldsAreDifferent() {
        ApprovalLedgerEntry entry1 = ApprovalLedgerEntry.create(
                1L, "approval-001", "APPROVED", "analyst-001", NOW,
                "tenant-alpha", ApprovalLedgerEntry.GENESIS_HASH);

        ApprovalLedgerEntry entry2 = ApprovalLedgerEntry.create(
                1L, "approval-002", "APPROVED", "analyst-001", NOW,
                "tenant-alpha", ApprovalLedgerEntry.GENESIS_HASH);

        assertThat(entry1.entryHash()).isNotEqualTo(entry2.entryHash());
    }
}
