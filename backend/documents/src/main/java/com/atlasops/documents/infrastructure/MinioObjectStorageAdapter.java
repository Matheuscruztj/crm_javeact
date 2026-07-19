package com.atlasops.documents.infrastructure;

import com.atlasops.documents.domain.ports.ObjectStoragePort;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * MinIO adapter implementing ObjectStoragePort using AWS S3 SDK. MinIO is S3-compatible, so this
 * adapter works with both MinIO and AWS S3.
 */
@Component
public class MinioObjectStorageAdapter implements ObjectStoragePort {

  private static final int BUFFER_SIZE = 8192;

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucketName;

  public MinioObjectStorageAdapter(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${app.storage.bucket-name:atlasops-documents}") String bucketName) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucketName = bucketName;
  }

  @Override
  public String generatePresignedUploadUrl(
      String storagePath, String contentType, int expiryMinutes) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(storagePath)
            .contentType(contentType)
            .build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expiryMinutes))
            .putObjectRequest(putObjectRequest)
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
    return presignedRequest.url().toString();
  }

  @Override
  public void deleteObject(String storagePath) {
    DeleteObjectRequest deleteRequest =
        DeleteObjectRequest.builder().bucket(bucketName).key(storagePath).build();

    s3Client.deleteObject(deleteRequest);
  }

  @Override
  public String getObjectChecksum(String storagePath) {
    GetObjectRequest getRequest =
        GetObjectRequest.builder().bucket(bucketName).key(storagePath).build();

    try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest)) {
      return computeSha256(response);
    } catch (Exception e) {
      throw new ObjectStorageException("Failed to compute checksum for object: " + storagePath, e);
    }
  }

  private String computeSha256(InputStream inputStream) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      digest.update(buffer, 0, bytesRead);
    }
    return HexFormat.of().formatHex(digest.digest());
  }
}
