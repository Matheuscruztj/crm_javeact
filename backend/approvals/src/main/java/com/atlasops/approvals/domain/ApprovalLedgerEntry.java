package com.atlasops.approvals.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Immutable ledger entry for an approval decision, forming a verifiable hash chain.
 *
 * <p>Each entry contains a SHA-256 hash of: previousHash + sequenceNumber + approvalId +
 * status + decisionBy + occurredAt + tenantId
 *
 * <p>Validates: P0.F.1 — Approval Ledger (append-only hash chain)
 *
 * @param sequenceNumber monotonically increasing sequence within a tenant
 * @param approvalId     the approval this entry refers to
 * @param status         the decision status (APPROVED, REJECTED, CANCELLED)
 * @param decisionBy     the actor who made the decision
 * @param occurredAt     when the decision was made
 * @param tenantId       the tenant context
 * @param previousHash   the hash of the previous ledger entry (or "GENESIS" for the first)
 * @param entryHash      SHA-256 hash of all fields above
 */
public record ApprovalLedgerEntry(
        long sequenceNumber,
        String approvalId,
        String status,
        String decisionBy,
        Instant occurredAt,
        String tenantId,
        String previousHash,
        String entryHash) {

    public static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    public ApprovalLedgerEntry {
        Objects.requireNonNull(approvalId, "ApprovalId must not be null");
        Objects.requireNonNull(status, "Status must not be null");
        Objects.requireNonNull(decisionBy, "DecisionBy must not be null");
        Objects.requireNonNull(occurredAt, "OccurredAt must not be null");
        Objects.requireNonNull(tenantId, "TenantId must not be null");
        Objects.requireNonNull(previousHash, "PreviousHash must not be null");
        Objects.requireNonNull(entryHash, "EntryHash must not be null");
    }

    /**
     * Creates a new ledger entry computing its hash from the given previous hash.
     */
    public static ApprovalLedgerEntry create(
            long sequenceNumber,
            String approvalId,
            String status,
            String decisionBy,
            Instant occurredAt,
            String tenantId,
            String previousHash) {

        String hash = computeHash(sequenceNumber, approvalId, status, decisionBy, occurredAt, tenantId, previousHash);
        return new ApprovalLedgerEntry(sequenceNumber, approvalId, status, decisionBy, occurredAt, tenantId, previousHash, hash);
    }

    /**
     * Verifies this entry's integrity by recomputing its hash.
     *
     * @return true if the stored hash matches the computed hash
     */
    public boolean isValid() {
        String expected = computeHash(sequenceNumber, approvalId, status, decisionBy, occurredAt, tenantId, previousHash);
        return expected.equals(entryHash);
    }

    private static String computeHash(
            long sequenceNumber, String approvalId, String status,
            String decisionBy, Instant occurredAt, String tenantId, String previousHash) {
        String payload = sequenceNumber + "|" + approvalId + "|" + status + "|"
                + decisionBy + "|" + occurredAt + "|" + tenantId + "|" + previousHash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
