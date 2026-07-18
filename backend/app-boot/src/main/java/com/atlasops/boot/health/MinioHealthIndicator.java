package com.atlasops.boot.health;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * Custom health indicator that checks MinIO/S3 bucket accessibility.
 *
 * <p>Performs a {@code headBucket} operation against the configured bucket with a 5-second timeout.
 * Reports UP if the bucket is accessible, DOWN otherwise with error details.
 */
@Component
public class MinioHealthIndicator extends AbstractHealthIndicator {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final S3Client s3Client;
  private final String bucketName;

  public MinioHealthIndicator(
      S3Client s3Client, @Value("${app.storage.bucket}") String bucketName) {
    super("MinIO health check failed");
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    try {
      HeadBucketRequest request =
          HeadBucketRequest.builder()
              .bucket(bucketName)
              .overrideConfiguration(cfg -> cfg.apiCallTimeout(TIMEOUT))
              .build();

      s3Client.headBucket(request);

      builder.up().withDetail("bucket", bucketName);
    } catch (SdkClientException ex) {
      builder.down().withDetail("bucket", bucketName).withDetail("error", ex.getMessage());
    } catch (Exception ex) {
      builder.down().withDetail("bucket", bucketName).withDetail("error", ex.getMessage());
    }
  }
}
