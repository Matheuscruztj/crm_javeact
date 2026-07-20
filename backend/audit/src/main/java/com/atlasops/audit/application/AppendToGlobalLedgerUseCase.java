package com.atlasops.audit.application;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.LedgerEntry;
import com.atlasops.audit.domain.ports.LedgerRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

/**
 * Use case that appends an audit entry to the global verifiable ledger.
 * Computes the SHA-256 of the audit entry JSON, chains it with the previous
 * hash, and persists the resulting {@link LedgerEntry}.
 *
 * <p>Validates: P2.5 — Append-only verifiable ledger with hash chain
 */
public class AppendToGlobalLedgerUseCase {

    private final LedgerRepository ledgerRepository;

    public AppendToGlobalLedgerUseCase(LedgerRepository ledgerRepository) {
        this.ledgerRepository = Objects.requireNonNull(ledgerRepository, "LedgerRepository must not be null");
    }

    /**
     * Appends an audit entry to the global ledger for the entry's tenant.
     *
     * @param auditEntry  the audit entry to record in the ledger
     * @param auditJson   the JSON serialization of the audit entry (for hashing)
     * @return the persisted {@link LedgerEntry}
     */
    public LedgerEntry execute(AuditEntry auditEntry, String auditJson) {
        Objects.requireNonNull(auditEntry, "AuditEntry must not be null");
        Objects.requireNonNull(auditJson, "auditJson must not be null");

        String tenantId = auditEntry.getTenantId();

        // Get previous hash for the chain
        Optional<LedgerEntry> last = ledgerRepository.findLast(tenantId);
        String previousHash = last.map(LedgerEntry::currentHash).orElse(LedgerEntry.GENESIS_HASH);

        // Compute SHA-256 of the audit entry JSON
        String payloadHash = sha256Hex(auditJson);

        // Get next sequence number
        long sequenceNumber = ledgerRepository.nextSequence(tenantId);

        // Create and append the ledger entry
        LedgerEntry entry = LedgerEntry.create(
                sequenceNumber,
                auditEntry.getActionType(),
                payloadHash,
                previousHash,
                auditEntry.getTimestamp(),
                tenantId);

        return ledgerRepository.append(entry);
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
