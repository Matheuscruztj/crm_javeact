package com.atlasops.boot.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration for S3-compatible object storage (MinIO).
 *
 * <p>Creates an {@link S3Client} bean configured to point to the MinIO endpoint defined in
 * application properties. Uses path-style access for MinIO compatibility.
 */
@Configuration
public class S3Config {

  @Value("${app.storage.endpoint}")
  private String endpoint;

  @Value("${app.storage.access-key}")
  private String accessKey;

  @Value("${app.storage.secret-key}")
  private String secretKey;

  @Value("${app.storage.region}")
  private String region;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .forcePathStyle(true)
        .build();
  }
}
