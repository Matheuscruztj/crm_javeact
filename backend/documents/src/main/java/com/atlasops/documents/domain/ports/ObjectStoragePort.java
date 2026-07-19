package com.atlasops.documents.domain.ports;

/**
 * Port defining object storage operations for document files. Abstracts the underlying storage
 * implementation (MinIO/S3-compatible).
 */
public interface ObjectStoragePort {

  /**
   * Generates a presigned URL for uploading an object.
   *
   * @param storagePath the path where the object will be stored
   * @param contentType the MIME content type of the object
   * @param expiryMinutes number of minutes until the URL expires
   * @return the presigned PUT URL as a string
   */
  String generatePresignedUploadUrl(String storagePath, String contentType, int expiryMinutes);

  /**
   * Deletes an object from storage.
   *
   * @param storagePath the path of the object to delete
   */
  void deleteObject(String storagePath);

  /**
   * Retrieves the SHA-256 checksum of a stored object.
   *
   * @param storagePath the path of the object
   * @return the SHA-256 checksum as a hex string
   */
  String getObjectChecksum(String storagePath);
}
