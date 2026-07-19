package com.atlasops.documents.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering document metadata.
 *
 * @param filename the document filename (1-255 characters)
 * @param contentType the MIME content type
 * @param fileSize the declared file size in bytes
 * @param checksum the SHA-256 checksum (64 hex characters)
 * @param requestId optional associated request identifier
 */
public record RegisterDocumentRequest(
    @NotBlank(message = "Filename must not be blank")
        @Size(max = 255, message = "Filename must not exceed 255 characters")
        String filename,
    @NotBlank(message = "Content type must not be blank") String contentType,
    @Positive(message = "File size must be greater than zero") long fileSize,
    @NotBlank(message = "Checksum must not be blank")
        @Size(min = 64, max = 64, message = "Checksum must be exactly 64 characters (SHA-256)")
        String checksum,
    String requestId) {}
