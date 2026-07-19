package com.atlasops.documents.application;

/**
 * Command for registering document metadata before upload.
 *
 * @param filename the document filename (1-255 characters)
 * @param contentType the MIME type of the file (must be in AllowedContentType)
 * @param fileSize the declared file size in bytes (must be ≤ 2GB)
 * @param checksum the SHA-256 checksum declared at registration time
 * @param requestId optional associated request identifier
 * @param tenantId the tenant this document belongs to
 */
public record RegisterDocumentMetadataCommand(
    String filename,
    String contentType,
    long fileSize,
    String checksum,
    String requestId,
    String tenantId) {}
