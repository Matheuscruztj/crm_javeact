package com.atlasops.boot.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.documents.infrastructure.MinioObjectStorageAdapter;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class MinioResilienceIntegrationTest {

  @Test
  @DisplayName("should_failAndRecover_when_minioDependencyFailsThenRecovers")
  void should_failAndRecover_when_minioDependencyFailsThenRecovers() {
    S3Client s3Client = mock(S3Client.class);
    S3Presigner presigner = mock(S3Presigner.class);
    MinioObjectStorageAdapter adapter =
        new MinioObjectStorageAdapter(s3Client, presigner, "atlasops-resilience");

    ResponseInputStream<GetObjectResponse> objectStream =
        new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            new ByteArrayInputStream("resilience-payload".getBytes(StandardCharsets.UTF_8)));

    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(objectStream);
    doAnswer(
            invocation -> {
              throw new RuntimeException("minio unavailable");
            })
        .when(s3Client)
        .deleteObject(any(DeleteObjectRequest.class));

    assertThat(adapter.getObjectChecksum("docs/sample.txt")).isNotBlank();

    assertThatThrownBy(() -> adapter.deleteObject("docs/sample.txt"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("minio unavailable");

    when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);

    adapter.deleteObject("docs/sample.txt");
  }
}
