package com.atlasops.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Worker Process - Separate Spring Boot application for asynchronous processing.
 *
 * <p>Handles: document processing, AI analysis, imports, notifications, retries and DLQ.
 */
@SpringBootApplication
public class WorkerApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorkerApplication.class, args);
  }
}
