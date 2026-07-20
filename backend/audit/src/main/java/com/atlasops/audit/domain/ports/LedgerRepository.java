package com.atlasops.audit.domain.ports;

import com.atlasops.audit.domain.LedgerEntry;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for the append-only verifiable ledger.
 * Implementations must guarantee that entries are never modified or deleted.
 *
 * <p>Validates: P2.5 — Append-only verifiable ledger with hash chain
 */
public interface LedgerRepository {

    /**
     * Appends a new entry to the ledger. Implementations must persist the entry atomically.
     *
     * @param entry the ledger entry to append
     * @return the persisted entry (may include generated DB fields)
     */
    LedgerEntry append(LedgerEntry entry);

    /**
     * Finds the most recently appended ledger entry for a tenant.
     *
     * @param tenantId the owning tenant
     * @return the last entry, or empty if no entries exist (new ledger)
     */
    Optional<LedgerEntry> findLast(String tenantId);

    /**
     * Returns the next sequence number for a tenant's ledger.
     * Equivalent to {@code lastSequenceNumber + 1}, or 1 if the ledger is empty.
     *
     * @param tenantId the owning tenant
     * @return the next available sequence number (>= 1)
     */
    long nextSequence(String tenantId);

    /**
     * Returns the most recent ledger entries for a tenant.
     *
     * @param tenantId the owning tenant
     * @param limit    maximum number of entries to return (most recent first)
     * @return list of ledger entries ordered by {@code sequenceNumber} descending
     */
    List<LedgerEntry> findAll(String tenantId, int limit);
}
