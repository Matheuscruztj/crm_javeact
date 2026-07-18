package com.atlasops.boot.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/** Unit tests for {@link MinioHealthIndicator}. */
class MinioHealthIndicatorTest {

  private static final String BUCKET_NAME = "atlasops-local";

  private S3Client s3Client;
  private MinioHealthIndicator healthIndicator;

  @BeforeEach
  void setUp() {
    s3Client = mock(S3Client.class);
    healthIndicator = new MinioHealthIndicator(s3Client, BUCKET_NAME);
  }

  @Test
  void should_reportUp_when_bucketIsAccessible() {
    HeadBucketResponse response = HeadBucketResponse.builder().build();
    when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(response);

    Health health = healthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("bucket", BUCKET_NAME);
  }

  @Test
  void should_reportDown_when_sdkClientExceptionOccurs() {
    when(s3Client.headBucket(any(HeadBucketRequest.class)))
        .thenThrow(SdkClientException.builder().message("Unable to connect to MinIO").build());

    Health health = healthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("bucket", BUCKET_NAME);
    assertThat(health.getDetails().get("error").toString()).contains("Unable to connect to MinIO");
  }

  @Test
  void should_reportDown_when_bucketDoesNotExist() {
    when(s3Client.headBucket(any(HeadBucketRequest.class)))
        .thenThrow(NoSuchBucketException.builder().message("Bucket does not exist").build());

    Health health = healthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("bucket", BUCKET_NAME);
    assertThat(health.getDetails().get("error").toString()).contains("Bucket does not exist");
  }

  @Test
  void should_reportDown_when_unexpectedExceptionOccurs() {
    when(s3Client.headBucket(any(HeadBucketRequest.class)))
        .thenThrow(new RuntimeException("Connection timed out"));

    Health health = healthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("bucket", BUCKET_NAME);
    assertThat(health.getDetails().get("error").toString()).contains("Connection timed out");
  }
}
