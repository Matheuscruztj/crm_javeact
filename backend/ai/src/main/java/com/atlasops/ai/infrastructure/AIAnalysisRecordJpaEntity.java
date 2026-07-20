package com.atlasops.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for the ai_analysis_records table.
 * Validates: Requirement 4.8 — AI analysis persistence.
 */
@Entity
@Table(name = "ai_analysis_records")
public class AIAnalysisRecordJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion;

    @Column(name = "input_hash", nullable = false, length = 64)
    private String inputHash;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore;

    @Column(name = "fallback", nullable = false)
    private boolean fallback;

    @Column(name = "result", nullable = false, columnDefinition = "TEXT")
    private String result;

    @Column(name = "chunks_used", columnDefinition = "TEXT[]")
    private String[] chunksUsed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AIAnalysisRecordJpaEntity() {}

    public AIAnalysisRecordJpaEntity(
            String id, String tenantId, String model, String promptVersion,
            String inputHash, long durationMs, double confidenceScore,
            boolean fallback, String result, String[] chunksUsed, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.model = model;
        this.promptVersion = promptVersion;
        this.inputHash = inputHash;
        this.durationMs = durationMs;
        this.confidenceScore = confidenceScore;
        this.fallback = fallback;
        this.result = result;
        this.chunksUsed = chunksUsed;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getModel() { return model; }
    public String getPromptVersion() { return promptVersion; }
    public String getInputHash() { return inputHash; }
    public long getDurationMs() { return durationMs; }
    public double getConfidenceScore() { return confidenceScore; }
    public boolean isFallback() { return fallback; }
    public String getResult() { return result; }
    public String[] getChunksUsed() { return chunksUsed; }
    public Instant getCreatedAt() { return createdAt; }
}
