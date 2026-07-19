package com.atlasops.documents.application;

/**
 * Result of the initiate upload use case, containing the presigned URL and storage path.
 *
 * @param uploadUrl the presigned URL for uploading the file
 * @param storagePath the storage path where the file will be stored
 */
public record InitiateUploadResult(String uploadUrl, String storagePath) {}
