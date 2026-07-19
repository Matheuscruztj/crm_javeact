package com.atlasops.documents.presentation;

/**
 * Response DTO containing the presigned upload URL and storage path.
 *
 * @param uploadUrl the presigned URL for uploading the file
 * @param storagePath the storage path where the file will be stored
 */
public record UploadUrlResponse(String uploadUrl, String storagePath) {}
