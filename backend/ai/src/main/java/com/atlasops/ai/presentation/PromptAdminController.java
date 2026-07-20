package com.atlasops.ai.presentation;

import com.atlasops.ai.application.GetActivePromptUseCase;
import com.atlasops.ai.application.RegisterPromptVersionUseCase;
import com.atlasops.ai.domain.PromptTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for managing prompt versions.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /api/v1/admin/prompts/{name}/active — get active prompt for a name
 *   <li>POST /api/v1/admin/prompts/{name} — register a new prompt version
 * </ul>
 *
 * <p>Validates: P0.F.2 — Prompt Version Registry
 */
@RestController
@RequestMapping("/api/v1/admin/prompts")
public class PromptAdminController {

    private final GetActivePromptUseCase getActivePromptUseCase;
    private final RegisterPromptVersionUseCase registerPromptVersionUseCase;

    public PromptAdminController(
            GetActivePromptUseCase getActivePromptUseCase,
            RegisterPromptVersionUseCase registerPromptVersionUseCase) {
        this.getActivePromptUseCase = getActivePromptUseCase;
        this.registerPromptVersionUseCase = registerPromptVersionUseCase;
    }

    /** Returns the currently active prompt template for the given name. */
    @GetMapping("/{name}/active")
    public ResponseEntity<PromptResponse> getActive(@PathVariable String name) {
        PromptTemplate template = getActivePromptUseCase.execute(name);
        return ResponseEntity.ok(PromptResponse.from(template));
    }

    /** Registers a new prompt version. Pass {@code active: true} to promote it immediately. */
    @PostMapping("/{name}")
    public ResponseEntity<PromptResponse> register(
            @PathVariable String name,
            @Valid @RequestBody RegisterPromptRequest request) {

        PromptTemplate template = registerPromptVersionUseCase.execute(
                name, request.content(), request.active());
        URI location = URI.create("/api/v1/admin/prompts/" + name + "/v" + template.getVersion());
        return ResponseEntity.created(location).body(PromptResponse.from(template));
    }

    // ---- DTOs ----

    record RegisterPromptRequest(
            @NotBlank @Size(min = 10) String content,
            boolean active) {}

    record PromptResponse(
            String id,
            String name,
            int version,
            String versionIdentifier,
            boolean active,
            Instant createdAt) {

        static PromptResponse from(PromptTemplate t) {
            return new PromptResponse(
                    t.getId(), t.getName(), t.getVersion(),
                    t.getVersionIdentifier(), t.isActive(), t.getCreatedAt());
        }
    }
}
