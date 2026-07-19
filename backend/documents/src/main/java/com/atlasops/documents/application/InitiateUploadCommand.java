package com.atlasops.documents.application;

/**
 * Command for initiating a document upload by generating a presigned URL.
 *
 * @param documentId the document identifier
 * @param tenantId the tenant identifier
 */
public record InitiateUploadCommand(String documentId, String tenantId) {}
