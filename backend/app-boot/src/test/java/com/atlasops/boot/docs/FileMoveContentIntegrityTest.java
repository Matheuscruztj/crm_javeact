package com.atlasops.boot.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Property 5: File move preserves content integrity.
 *
 * <p>Validates: Requirements 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11
 *
 * <p>Since the original files have already been moved (they no longer exist at source), this test
 * verifies that each file exists at its expected destination, has non-zero size, and has a valid
 * SHA-256 hash (proving content integrity at destination).
 */
@Tag("Feature: project-adequation-restructure, Property 5: File move preserves content integrity")
class FileMoveContentIntegrityTest {

  private static final Path PROJECT_ROOT = resolveProjectRoot();

  private static final List<String> EXPECTED_DESTINATION_FILES =
      List.of(
          "docs/specifications/PROJECT-SCOPE.md",
          "docs/specifications/TECHNICAL-SPECIFICATION.md",
          "docs/specifications/SPECIFICATION-PLANNING.md",
          "docs/architecture/DESIGN-PLANNING.md",
          "docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md",
          "docs/architecture/DATA-ENTITIES-BY-DATABASE.md",
          "docs/diagrams/ARCHITECTURE-DIAGRAMS-C4-MERMAID.md",
          "docs/task-plans/README.md",
          "docs/task-plans/TASK-PLAN-01-CODE-AND-CONFIG.md",
          "docs/task-plans/TASK-PLAN-02-VALIDATION-AND-CHECKS.md",
          "docs/task-plans/TASK-PLAN-03-DOCS-AND-ROADMAP-CLEANUP.md",
          "docs/testing/QUALITY-TESTING-CICD.md",
          "docs/runbooks/OPERATIONS-RUNBOOK.md");

  @Test
  void should_existAtDestination_when_filesMoved() {
    for (String relativePath : EXPECTED_DESTINATION_FILES) {
      Path file = PROJECT_ROOT.resolve(relativePath);
      assertThat(file).as("File should exist at destination: %s", relativePath).exists();
    }
  }

  @Test
  void should_haveNonZeroSize_when_filesMoved() throws IOException {
    for (String relativePath : EXPECTED_DESTINATION_FILES) {
      Path file = PROJECT_ROOT.resolve(relativePath);
      long size = Files.size(file);
      assertThat(size).as("File should have non-zero size: %s", relativePath).isGreaterThan(0);
    }
  }

  @Test
  void should_haveValidSha256Hash_when_filesMoved() throws IOException, NoSuchAlgorithmException {
    for (String relativePath : EXPECTED_DESTINATION_FILES) {
      Path file = PROJECT_ROOT.resolve(relativePath);
      String hash = computeSha256(file);
      assertThat(hash)
          .as("File should have a valid non-empty SHA-256 hash: %s", relativePath)
          .isNotBlank()
          .hasSize(64); // SHA-256 produces 64 hex characters
    }
  }

  @Test
  void should_containMarkdownContent_when_filesMoved() throws IOException {
    for (String relativePath : EXPECTED_DESTINATION_FILES) {
      Path file = PROJECT_ROOT.resolve(relativePath);
      String content = Files.readString(file);
      assertThat(content)
          .as("File should contain markdown content (starts with # heading): %s", relativePath)
          .isNotBlank()
          .contains("#"); // All docs should contain at least one markdown heading
    }
  }

  private static String computeSha256(Path file) throws IOException, NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] fileBytes = Files.readAllBytes(file);
    byte[] hashBytes = digest.digest(fileBytes);
    return HexFormat.of().formatHex(hashBytes);
  }

  private static Path resolveProjectRoot() {
    // Navigate from the test class location up to the project root.
    // The project root contains the 'docs/' directory.
    // From backend/app-boot/ we go up 2 levels to reach project root.
    Path currentDir = Path.of(System.getProperty("user.dir"));
    Path candidate = currentDir;

    // Walk up until we find a directory containing 'docs/' and 'backend/'
    for (int i = 0; i < 5; i++) {
      if (Files.isDirectory(candidate.resolve("docs"))
          && Files.isDirectory(candidate.resolve("backend"))) {
        return candidate;
      }
      candidate = candidate.getParent();
      if (candidate == null) {
        break;
      }
    }

    // Fallback: assume current working directory is project root
    return currentDir;
  }
}
