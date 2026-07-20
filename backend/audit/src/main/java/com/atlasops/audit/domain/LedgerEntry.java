package com.atlasops.audit.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable ledger entry forming a cryptographic hash chain.
 * Each entry includes the hash of its predecessor, making any tampering detectable.
 *
 * <p>Hash formula: {@code SHA256(previousHash | seqNum | eventType | payloadHash | occurredAt | tenantId)}
 *
 * <p>Validates: P2.5 — Append-only verifiable ledger with hash chain
 *
 * @param sequenceNumber monotonically increasing per-tenant sequence
 * @param eventType      the type of audited event
 * @param payloadHash    SHA-256 hex of the original AuditEntry payload
 * @param previousHash   the {@code currentHash} of the immediately preceding entry
 *                       (or "GENESIS" for the first entry)
 * @param currentHash    SHA-256 of the concatenated chain fields
 * @param occurredAt     when the original event occurred
 * @param tenantId       the owning tenant
 */
public record LedgerEntry(
        long sequenceNumber,
        String eventType,
        String payloadHash,
        String previousHash,
        String currentHash,
        Instant occurredAt,
        String tenantId) {

    /** Sentinel previous hash used for the first entry in a ledger. */
    public static final String GENESIS_HASH = "GENESIS";

    public LedgerEntry {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(payloadHash, "payloadHash must not be null");
        Objects.requireNonNull(previousHash, "previousHash must not be null");
        Objects.requireNonNull(currentHash, "currentHash must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("sequenceNumber must be >= 1, got: " + sequenceNumber);
        }
    }

    /**
     * Factory method that computes {@code currentHash} from the provided fields.
     *
     * @param sequenceNumber monotonically increasing sequence number
     * @param eventType      event type string
     * @param payloadHash    SHA-256 of the original payload
     * @param previousHash   hash of the predecessor entry (or {@link #GENESIS_HASH})
     * @param occurredAt     event timestamp
     * @param tenantId       owning tenant
     * @return a new LedgerEntry with a computed hash
     */
    public static LedgerEntry create(
            long sequenceNumber,
            String eventType,
            String payloadHash,
            String previousHash,
            Instant occurredAt,
            String tenantId) {
        String raw = previousHash
                + "|" + sequenceNumber
                + "|" + eventType
                + "|" + payloadHash
                + "|" + occurredAt
                + "|" + tenantId;
        String currentHash = sha256Hex(raw);
        return new LedgerEntry(sequenceNumber, eventType, payloadHash,
                previousHash, currentHash, occurredAt, tenantId);
    }

    /**
     * Verifies the integrity of this entry by recomputing its hash.
     *
     * @return true if the stored {@code currentHash} matches the computed hash
     */
    public boolean isValid() {
        String raw = previousHash
                + "|" + sequenceNumber
                + "|" + eventType
                + "|" + payloadHash
                + "|" + occurredAt
                + "|" + tenantId;
        String expected = sha256Hex(raw);
        return MessageDigest.isEqual(
                currentHash.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
