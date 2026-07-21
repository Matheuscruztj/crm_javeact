package com.atlasops.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for the prompt_versions table.
 * Validates: P0.F.2 — Prompt Version Registry
 */
@Entity
@Table(name = "prompt_versions")
public class PromptVersionJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "ab_weight", nullable = false)
    private int abWeight;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PromptVersionJpaEntity() {}

    public PromptVersionJpaEntity(
            String id, String tenantId, String name, String version,
            String tag, String template, boolean active, int abWeight,
            String createdBy, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.version = version;
        this.tag = tag;
        this.template = template;
        this.active = active;
        this.abWeight = abWeight;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getTag() {
        return tag;
    }

    public String getTemplate() {
        return template;
    }

    public boolean isActive() {
        return active;
    }

    public int getAbWeight() {
        return abWeight;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
