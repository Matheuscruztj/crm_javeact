package com.atlasops.boot.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit tests for validating hexagonal architecture rules across all backend modules.
 *
 * <p>These tests enforce the following architectural constraints:
 *
 * <ul>
 *   <li>No dependency cycles between modules
 *   <li>Domain layer does not import infrastructure or presentation
 *   <li>Application layer does not import infrastructure or presentation
 *   <li>Presentation layer depends only on application and shared-kernel
 * </ul>
 *
 * <p>Validates: Requirements 2.4, 2.5, 2.7, 2.8, 10.2
 */
class HexagonalArchitectureTest {

  private static JavaClasses allClasses;

  @BeforeAll
  static void importClasses() {
    allClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.atlasops");
  }

  @Nested
  @DisplayName("Module Cycle Detection (Req 2.8)")
  class ModuleCycleTests {

    @Test
    @DisplayName("should_haveNoCycles_between_backendModules")
    void should_haveNoCycles_between_backendModules() {
      ArchRule rule =
          SlicesRuleDefinition.slices().matching("com.atlasops.(*)..").should().beFreeOfCycles();

      rule.check(allClasses);
    }
  }

  @Nested
  @DisplayName("Domain Layer Isolation (Req 2.4)")
  class DomainLayerIsolationTests {

    @Test
    @DisplayName("should_notImportInfrastructure_from_domainLayer")
    void should_notImportInfrastructure_from_domainLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..domain..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..infrastructure..");

      rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("should_notImportPresentation_from_domainLayer")
    void should_notImportPresentation_from_domainLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..domain..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..presentation..");

      rule.allowEmptyShould(true).check(allClasses);
    }
  }

  @Nested
  @DisplayName("Application Layer Isolation (Req 2.5)")
  class ApplicationLayerIsolationTests {

    @Test
    @DisplayName("should_notImportInfrastructure_from_applicationLayer")
    void should_notImportInfrastructure_from_applicationLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..application..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..infrastructure..");

      rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("should_notImportPresentation_from_applicationLayer")
    void should_notImportPresentation_from_applicationLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..application..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..presentation..");

      rule.allowEmptyShould(true).check(allClasses);
    }
  }

  @Nested
  @DisplayName("Presentation Layer Dependencies (Req 2.7, 10.2)")
  class PresentationLayerDependencyTests {

    @Test
    @DisplayName("should_notAccessInfrastructureDirectly_from_presentationLayer")
    void should_notAccessInfrastructureDirectly_from_presentationLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..presentation..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..infrastructure..");

      rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("should_onlyDependOnApplicationAndSharedKernel_from_presentationLayer")
    void should_onlyDependOnApplicationAndSharedKernel_from_presentationLayer() {
      // Presentation layer should not access domain packages directly
      // (except shared-kernel which contains shared types).
      // It should go through the application layer.
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..presentation..")
              .should()
              .dependOnClassesThat(
                  com.tngtech.archunit.base.DescribedPredicate.describe(
                      "reside in a module's domain package (not shared-kernel)",
                      javaClass -> {
                        String name = javaClass.getPackageName();
                        // Allow shared-kernel domain types
                        if (name.startsWith("com.atlasops.shared")) {
                          return false;
                        }
                        // Block direct access to module-specific domain packages
                        // (the module's own domain is also blocked — use application layer)
                        return name.contains(".domain.");
                      }));

      rule.allowEmptyShould(true).check(allClasses);
    }
  }

  @Nested
  @DisplayName("Inter-Module Access Rules (Req 2.7)")
  class InterModuleAccessTests {

    @Test
    @DisplayName("should_notAccessInternalInfrastructure_ofOtherModules")
    void should_notAccessInternalInfrastructure_ofOtherModules() {
      // No module should access another module's infrastructure package directly.
      // Modules should interact via ports/interfaces in the domain layer.
      String[] modules = {
        "auth", "tenants", "users", "customers", "documents",
        "requests", "pipeline", "tasks", "workflows", "ai",
        "analytics", "audit"
      };

      for (String sourceModule : modules) {
        for (String targetModule : modules) {
          if (sourceModule.equals(targetModule)) {
            continue;
          }
          ArchRule rule =
              noClasses()
                  .that()
                  .resideInAPackage("com.atlasops." + sourceModule + "..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAPackage("com.atlasops." + targetModule + ".infrastructure..");

          rule.allowEmptyShould(true).check(allClasses);
        }
      }
    }
  }
}
