package com.atlasops.boot.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

  @Value("${app.storage.bucket}")
  private String bucketName;

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

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build();
  }

  @Bean
  @Profile("local")
  public ApplicationRunner ensureBucketExists(S3Client s3Client) {
    return args -> {
      try {
        s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      } catch (NoSuchBucketException ex) {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
      }
    };
  }
}
