package com.atlasops.documents.application;

/**
 * Command for confirming a document upload after the client has uploaded the file.
 *
 * @param documentId the document identifier
 * @param storagePath the storage path where the file was uploaded
 * @param tenantId the tenant identifier
 * @param correlationId the correlation ID for tracing
 */
public record ConfirmUploadCommand(
    String documentId, String storagePath, String tenantId, String correlationId) {}
