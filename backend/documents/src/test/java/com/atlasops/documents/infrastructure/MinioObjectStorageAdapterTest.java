package com.atlasops.documents.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** Unit tests for MinioObjectStorageAdapter. Validates: Requirements 10.1, 10.3, 10.5 */
@ExtendWith(MockitoExtension.class)
class MinioObjectStorageAdapterTest {

  private static final String BUCKET_NAME = "atlasops-documents";
  private static final String STORAGE_PATH = "tenant-alpha/2025/01/doc-001/contract.pdf";
  private static final String CONTENT_TYPE = "application/pdf";
  private static final int EXPIRY_MINUTES = 60;

  @Mock private S3Client s3Client;

  @Mock private S3Presigner s3Presigner;

  private MinioObjectStorageAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MinioObjectStorageAdapter(s3Client, s3Presigner, BUCKET_NAME);
  }

  // --- generatePresignedUploadUrl ---

  @Test
  void should_returnPresignedUrl_when_validPathAndContentType() throws Exception {
    // Arrange
    URL expectedUrl =
        URI.create(
                "https://minio.local/atlasops-documents/tenant-alpha/2025/01/doc-001/contract.pdf?signed=true")
            .toURL();

    PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
    when(presignedRequest.url()).thenReturn(expectedUrl);

    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(presignedRequest);

    // Act
    String url = adapter.generatePresignedUploadUrl(STORAGE_PATH, CONTENT_TYPE, EXPIRY_MINUTES);

    // Assert
    assertThat(url).contains("minio.local");
    assertThat(url).contains(STORAGE_PATH);

    ArgumentCaptor<PutObjectPresignRequest> captor =
        ArgumentCaptor.forClass(PutObjectPresignRequest.class);
    verify(s3Presigner).presignPutObject(captor.capture());

    PutObjectPresignRequest captured = captor.getValue();
    assertThat(captured.signatureDuration()).isEqualTo(Duration.ofMinutes(EXPIRY_MINUTES));
    assertThat(captured.putObjectRequest().bucket()).isEqualTo(BUCKET_NAME);
    assertThat(captured.putObjectRequest().key()).isEqualTo(STORAGE_PATH);
    assertThat(captured.putObjectRequest().contentType()).isEqualTo(CONTENT_TYPE);
  }

  // --- deleteObject ---

  @Test
  void should_invokeDeleteOnS3_when_deleteObjectCalled() {
    // Act
    adapter.deleteObject(STORAGE_PATH);

    // Assert
    ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(captor.capture());

    DeleteObjectRequest captured = captor.getValue();
    assertThat(captured.bucket()).isEqualTo(BUCKET_NAME);
    assertThat(captured.key()).isEqualTo(STORAGE_PATH);
  }

  // --- getObjectChecksum ---

  @Test
  void should_returnSha256Checksum_when_objectExists() throws Exception {
    // Arrange
    byte[] fileContent = "Hello, World! This is test content.".getBytes(StandardCharsets.UTF_8);
    String expectedChecksum = computeExpectedSha256(fileContent);

    ResponseInputStream<GetObjectResponse> responseStream =
        new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(new ByteArrayInputStream(fileContent)));

    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

    // Act
    String checksum = adapter.getObjectChecksum(STORAGE_PATH);

    // Assert
    assertThat(checksum).isEqualTo(expectedChecksum);
    assertThat(checksum).hasSize(64);
    assertThat(checksum).matches("^[a-f0-9]{64}$");

    ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObject(captor.capture());

    GetObjectRequest captured = captor.getValue();
    assertThat(captured.bucket()).isEqualTo(BUCKET_NAME);
    assertThat(captured.key()).isEqualTo(STORAGE_PATH);
  }

  @Test
  void should_throwObjectStorageException_when_s3ClientFails() {
    // Arrange
    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenThrow(new RuntimeException("S3 connection failed"));

    // Act & Assert
    assertThatThrownBy(() -> adapter.getObjectChecksum(STORAGE_PATH))
        .isInstanceOf(ObjectStorageException.class)
        .hasMessageContaining("Failed to compute checksum")
        .hasMessageContaining(STORAGE_PATH);
  }

  // --- Helper ---

  private String computeExpectedSha256(byte[] content) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(content);
    return HexFormat.of().formatHex(hash);
  }
}
