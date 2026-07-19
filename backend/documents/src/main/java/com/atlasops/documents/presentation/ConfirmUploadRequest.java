package com.atlasops.documents.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for confirming a document upload.
 *
 * @param storagePath the storage path where the file was uploaded
 */
public record ConfirmUploadRequest(
    @NotBlank(message = "Storage path must not be blank") String storagePath) {}
