package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.shared.sdd.SpecValidator.SpecValidationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;

/**
 * Property-based tests for spec completeness detection.
 *
 * <p><b>Validates: Requirements 6.8</b>
 *
 * <p>Property 11: For any spec directory under {@code .kiro/specs/}, if any of the four required
 * files ({@code requirements.md}, {@code design.md}, {@code tasks.md}, {@code .config.kiro}) is
 * missing, the validation SHALL flag the spec as incomplete and list exactly the missing file
 * names.
 */
@Tag("Feature: monorepo-sdd-harness, Property 11: Spec Completeness Detection")
class SpecValidatorPropertyTest {

  private Path tempDir;

  @BeforeTry
  void setUp() throws IOException {
    tempDir = Files.createTempDirectory("spec-completeness-test");
  }

  // ─── Property: When ALL 4 required files are present, spec is complete ───────

  @Property(tries = 100)
  void allFilesPresent_shouldAlwaysMarkSpecAsComplete(
      @ForAll("validFeatureNames") String featureName) throws IOException {

    Path specDir = tempDir.resolve(featureName);
    Files.createDirectories(specDir);
    createAllRequiredFiles(specDir);

    SpecValidationResult result = SpecValidator.validateCompleteness(specDir);

    assertThat(result.isComplete())
        .as("Spec '%s' with all 4 files should be marked as complete", featureName)
        .isTrue();
    assertThat(result.missingFiles())
        .as("No files should be reported as missing when all are present")
        .isEmpty();
    assertThat(result.errors())
        .as("No errors should be reported when all files are present")
        .isEmpty();
  }

  // ─── Property: Any non-empty subset of missing files flags incomplete ────────

  @Property(tries = 100)
  void anyMissingSubset_shouldAlwaysFlagIncomplete(
      @ForAll("nonEmptySubsetsOfRequiredFiles") Set<String> filesToOmit) throws IOException {

    Path specDir = tempDir.resolve("test-feature");
    Files.createDirectories(specDir);

    // Create only the files NOT in the omission set
    Set<String> presentFiles = new HashSet<>(SpecValidator.REQUIRED_FILES);
    presentFiles.removeAll(filesToOmit);
    for (String file : presentFiles) {
      Files.writeString(specDir.resolve(file), "# " + file);
    }

    SpecValidationResult result = SpecValidator.validateCompleteness(specDir);

    assertThat(result.isComplete())
        .as("Spec with missing files %s should be flagged as incomplete", filesToOmit)
        .isFalse();
    assertThat(result.missingFiles())
        .as("Missing files list should not be empty when files are omitted")
        .isNotEmpty();
  }

  // ─── Property: Reported missing files match exactly what's absent ─────────────

  @Property(tries = 100)
  void reportedMissingFiles_shouldMatchExactlyWhatIsAbsent(
      @ForAll("nonEmptySubsetsOfRequiredFiles") Set<String> filesToOmit) throws IOException {

    Path specDir = tempDir.resolve("exact-match-feature");
    Files.createDirectories(specDir);

    // Create only the files NOT in the omission set
    Set<String> presentFiles = new HashSet<>(SpecValidator.REQUIRED_FILES);
    presentFiles.removeAll(filesToOmit);
    for (String file : presentFiles) {
      Files.writeString(specDir.resolve(file), "# " + file);
    }

    SpecValidationResult result = SpecValidator.validateCompleteness(specDir);

    // The reported missing files should contain EXACTLY the omitted files
    // No false positives (reporting present files as missing)
    // No false negatives (failing to report absent files)
    assertThat(new HashSet<>(result.missingFiles()))
        .as("Reported missing files should exactly match omitted files %s", filesToOmit)
        .isEqualTo(filesToOmit);

    // Additionally verify count matches
    assertThat(result.missingFiles())
        .as("Count of reported missing files should equal count of omitted files")
        .hasSize(filesToOmit.size());

    // Verify no duplicates in the reported list
    assertThat(result.missingFiles())
        .as("No duplicate entries in missing files list")
        .doesNotHaveDuplicates();
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validFeatureNames() {
    // Generate valid kebab-case names for spec directories
    Arbitrary<String> segment =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(8)
            .filter(s -> !s.isEmpty());

    return segment
        .list()
        .ofMinSize(1)
        .ofMaxSize(3)
        .map(segments -> String.join("-", segments))
        .filter(name -> name.length() <= 50 && name.length() >= 1);
  }

  @Provide
  Arbitrary<Set<String>> nonEmptySubsetsOfRequiredFiles() {
    // Generate all possible non-empty subsets of the 4 required files
    // There are 15 non-empty subsets of a 4-element set (2^4 - 1)
    List<String> allFiles = new ArrayList<>(SpecValidator.REQUIRED_FILES);

    return Arbitraries.integers()
        .between(1, (1 << allFiles.size()) - 1)
        .map(
            bitmask -> {
              Set<String> subset = new HashSet<>();
              for (int i = 0; i < allFiles.size(); i++) {
                if ((bitmask & (1 << i)) != 0) {
                  subset.add(allFiles.get(i));
                }
              }
              return subset;
            })
        .filter(s -> !s.isEmpty());
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  private void createAllRequiredFiles(Path dir) throws IOException {
    for (String file : SpecValidator.REQUIRED_FILES) {
      Files.writeString(dir.resolve(file), "# " + file);
    }
  }
}
