package com.atlasops.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for the golden_dataset table.
 * Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
@Entity
@Table(name = "golden_dataset")
public class GoldenExampleJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "query", nullable = false, columnDefinition = "TEXT")
    private String query;

    @Column(name = "expected_answer", nullable = false, columnDefinition = "TEXT")
    private String expectedAnswer;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected GoldenExampleJpaEntity() {}

    public GoldenExampleJpaEntity(String id, String tenantId, String query,
            String expectedAnswer, String category, String createdBy, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.query = query;
        this.expectedAnswer = expectedAnswer;
        this.category = category;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getQuery() {
        return query;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public String getCategory() {
        return category;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
