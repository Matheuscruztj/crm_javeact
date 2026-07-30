package com.atlasops.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.atlasops")
@EnableJpaRepositories(basePackages = "com.atlasops")
@EntityScan(basePackages = "com.atlasops")
@EnableScheduling
public class AtlasOpsApplication {

  public static void main(String[] args) {
    SpringApplication.run(AtlasOpsApplication.class, args);
  }
}
