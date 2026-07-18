package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlasops.shared.sdd.SpecValidator.SpecValidationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpecValidatorTest {

  @TempDir Path tempDir;

  private Path specDir;

  @BeforeEach
  void setUp() throws IOException {
    specDir = tempDir.resolve("my-feature");
    Files.createDirectories(specDir);
  }

  // --- Full validation (validate) ---

  @Test
  void should_beComplete_when_allFilesPresent() throws IOException {
    createAllRequiredFiles(specDir);

    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isTrue();
    assertThat(result.featureName()).isEqualTo("my-feature");
    assertThat(result.errors()).isEmpty();
    assertThat(result.missingFiles()).isEmpty();
  }

  @Test
  void should_reportMissingFiles_when_requirementsMdAbsent() throws IOException {
    createFile(specDir, "design.md");
    createFile(specDir, "tasks.md");
    createFile(specDir, ".config.kiro");

    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles()).containsExactly("requirements.md");
  }

  @Test
  void should_reportMissingFiles_when_designMdAbsent() throws IOException {
    createFile(specDir, "requirements.md");
    createFile(specDir, "tasks.md");
    createFile(specDir, ".config.kiro");

    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles()).containsExactly("design.md");
  }

  @Test
  void should_reportMissingFiles_when_tasksMdAbsent() throws IOException {
    createFile(specDir, "requirements.md");
    createFile(specDir, "design.md");
    createFile(specDir, ".config.kiro");

    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles()).containsExactly("tasks.md");
  }

  @Test
  void should_reportMissingFiles_when_configKiroAbsent() throws IOException {
    createFile(specDir, "requirements.md");
    createFile(specDir, "design.md");
    createFile(specDir, "tasks.md");

    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles()).containsExactly(".config.kiro");
  }

  @Test
  void should_reportAllMissingFiles_when_directoryEmpty() {
    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles())
        .containsExactlyInAnyOrder("requirements.md", "design.md", "tasks.md", ".config.kiro");
  }

  @Test
  void should_reportMultipleMissing_when_twoFilesAbsent() throws IOException {
    createFile(specDir, "requirements.md");
    createFile(specDir, "design.md");

    SpecValidationResult result = SpecValidator.validate(specDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles()).containsExactlyInAnyOrder("tasks.md", ".config.kiro");
  }

  @Test
  void should_beInvalid_when_directoryDoesNotExist() {
    Path nonExistent = tempDir.resolve("non-existent-spec");

    SpecValidationResult result = SpecValidator.validate(nonExistent);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("does not exist"));
  }

  @Test
  void should_beInvalid_when_featureNameNotKebabCase() throws IOException {
    Path badNameDir = tempDir.resolve("MyFeature");
    Files.createDirectories(badNameDir);
    createAllRequiredFiles(badNameDir);

    SpecValidationResult result = SpecValidator.validate(badNameDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("Invalid feature-name"));
  }

  @Test
  void should_beInvalid_when_featureNameTooLong() throws IOException {
    Path longNameDir = tempDir.resolve("a".repeat(51));
    Files.createDirectories(longNameDir);
    createAllRequiredFiles(longNameDir);

    SpecValidationResult result = SpecValidator.validate(longNameDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("Invalid feature-name"));
  }

  @Test
  void should_reportBothErrors_when_badNameAndMissingFiles() throws IOException {
    Path badNameDir = tempDir.resolve("BadName");
    Files.createDirectories(badNameDir);
    createFile(badNameDir, "requirements.md");

    SpecValidationResult result = SpecValidator.validate(badNameDir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.errors()).hasSize(2);
    assertThat(result.errors()).anyMatch(e -> e.contains("Invalid feature-name"));
    assertThat(result.errors()).anyMatch(e -> e.contains("Missing required files"));
  }

  @Test
  void should_throwNPE_when_specDirIsNull() {
    assertThatThrownBy(() -> SpecValidator.validate(null)).isInstanceOf(NullPointerException.class);
  }

  // --- Completeness-only validation (validateCompleteness) ---

  @Test
  void should_beComplete_when_allFilesPresentIgnoringName() throws IOException {
    Path badNameDir = tempDir.resolve("NotKebabCase");
    Files.createDirectories(badNameDir);
    createAllRequiredFiles(badNameDir);

    SpecValidationResult result = SpecValidator.validateCompleteness(badNameDir);

    assertThat(result.isComplete()).isTrue();
    assertThat(result.featureName()).isEqualTo("NotKebabCase");
  }

  @Test
  void should_reportMissing_when_filesAbsentInCompletenessCheck() throws IOException {
    Path dir = tempDir.resolve("some-dir");
    Files.createDirectories(dir);
    createFile(dir, "requirements.md");

    SpecValidationResult result = SpecValidator.validateCompleteness(dir);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.missingFiles())
        .containsExactlyInAnyOrder("design.md", "tasks.md", ".config.kiro");
  }

  @Test
  void should_beInvalid_when_directoryDoesNotExistForCompleteness() {
    Path nonExistent = tempDir.resolve("does-not-exist");

    SpecValidationResult result = SpecValidator.validateCompleteness(nonExistent);

    assertThat(result.isComplete()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("does not exist"));
  }

  // --- REQUIRED_FILES constant ---

  @Test
  void should_haveExactlyFourRequiredFiles() {
    assertThat(SpecValidator.REQUIRED_FILES).hasSize(4);
    assertThat(SpecValidator.REQUIRED_FILES)
        .containsExactlyInAnyOrder("requirements.md", "design.md", "tasks.md", ".config.kiro");
  }

  // --- Helpers ---

  private void createAllRequiredFiles(Path dir) throws IOException {
    for (String file : SpecValidator.REQUIRED_FILES) {
      createFile(dir, file);
    }
  }

  private void createFile(Path dir, String fileName) throws IOException {
    Files.writeString(dir.resolve(fileName), "# " + fileName);
  }
}
