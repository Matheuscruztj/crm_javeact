package com.atlasops.boot.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * ArchUnit tests that enforce structural decoupling beyond hexagonal layering.
 *
 * <p>Validates W2 backend tasks:
 *
 * <ul>
 *   <li>inheritance rules
 *   <li>annotation rules
 *   <li>module decoupling
 *   <li>domain technology isolation
 * </ul>
 */
@Tag("architecture")
class StructuralDecouplingTest {

  private static JavaClasses allClasses;

  @BeforeAll
  static void importClasses() {
    allClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.atlasops");
  }

  @Nested
  @DisplayName("Inheritance Rules")
  class InheritanceRules {

    @Test
    @DisplayName("should_notExtendOrImplementFrameworkTypes_from_domainLayer")
    void should_notExtendOrImplementFrameworkTypes_from_domainLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..domain..")
              .and()
              .resideOutsideOfPackage("..domain.ports..")
              .should()
              .dependOnClassesThat()
              .resideInAnyPackage(
                  "org.springframework..",
                  "jakarta.persistence..",
                  "jakarta.transaction..",
                  "org.hibernate..",
                  "java.sql..");

      rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("should_notUseFieldInjection")
    void should_notUseFieldInjection() {
      ArchRule rule =
          noFields()
              .should()
              .beAnnotatedWith(Autowired.class);

      rule.allowEmptyShould(true).check(allClasses);
    }

  }

  @Nested
  @DisplayName("Annotation Rules")
  class AnnotationRules {

    @Test
    @DisplayName("should_notUseFrameworkAnnotations_in_domainLayer")
    void should_notUseFrameworkAnnotations_in_domainLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..domain..")
              .and()
              .resideOutsideOfPackage("..domain.ports..")
              .should()
              .beAnnotatedWith(Component.class)
              .orShould()
              .beAnnotatedWith(Service.class)
              .orShould()
              .beAnnotatedWith(Repository.class)
              .orShould()
              .beAnnotatedWith(Transactional.class)
              .orShould()
              .beAnnotatedWith(RestController.class)
              .orShould()
              .beAnnotatedWith(Entity.class);

      rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("should_notUseFrameworkAnnotations_in_applicationLayer")
    void should_notUseFrameworkAnnotations_in_applicationLayer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..application..")
              .should()
              .beAnnotatedWith(Component.class)
              .orShould()
              .beAnnotatedWith(Repository.class)
              .orShould()
              .beAnnotatedWith(Transactional.class)
              .orShould()
              .beAnnotatedWith(RestController.class)
              .orShould()
              .beAnnotatedWith(Entity.class);

      rule.allowEmptyShould(true).check(allClasses);
    }

    @Test
    @DisplayName("should_keep_web_annotations_with_presentation_layer")
    void should_keep_web_annotations_with_presentation_layer() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..presentation..")
              .and()
              .haveSimpleNameEndingWith("Controller")
              .should()
              .notBeAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

      rule.allowEmptyShould(true).check(allClasses);
    }
  }

  @Nested
  @DisplayName("Module Boundary Rules")
  class ModuleBoundaryRules {

    @Test
    @DisplayName("should_notAccessOtherModules_infrastructure_directly")
    void should_notAccessOtherModules_infrastructure_directly() {
      String[] modules = {
        "auth",
        "tenants",
        "users",
        "customers",
        "documents",
        "requests",
        "approvals",
        "activities",
        "notifications",
        "integrations",
        "search",
        "imports",
        "operations",
        "ai",
        "analytics",
        "audit"
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

    @Test
    @DisplayName("should_notDependOn_otherModules_domain_models_directly")
    void should_notDependOn_otherModules_domain_models_directly() {
      String[] modules = {
        "auth",
        "tenants",
        "users",
        "customers",
        "documents",
        "requests",
        "approvals",
        "activities",
        "notifications",
        "integrations",
        "search",
        "imports",
        "operations",
        "ai",
        "analytics",
        "audit"
      };

      for (String sourceModule : modules) {
        DescribedPredicate<JavaClass> otherModuleDomain =
            DescribedPredicate.describe(
                "classes from another module's domain package",
                javaClass -> {
                  String packageName = javaClass.getPackageName();
                  return packageName.startsWith("com.atlasops.")
                      && packageName.contains(".domain.")
                      && !packageName.startsWith("com.atlasops.shared.")
                      && !packageName.startsWith("com.atlasops." + sourceModule + ".");
                });

        ArchRule rule =
            noClasses()
                .that()
                .resideInAPackage("com.atlasops." + sourceModule + "..")
                .should()
                .dependOnClassesThat(otherModuleDomain);
        rule.allowEmptyShould(true).check(allClasses);
      }
    }

    @Test
    @DisplayName("should_notDependOn_otherModules_application_packages_directly")
    void should_notDependOn_otherModules_application_packages_directly() {
      String[] modules = {
        "auth",
        "tenants",
        "users",
        "customers",
        "documents",
        "requests",
        "approvals",
        "activities",
        "notifications",
        "integrations",
        "search",
        "imports",
        "operations",
        "ai",
        "analytics",
        "audit"
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
                  .resideInAPackage("com.atlasops." + targetModule + ".application..");

          rule.allowEmptyShould(true).check(allClasses);
        }
      }
    }

    @Test
    @DisplayName("should_notDependOn_framework_packages_from_domain")
    void should_notDependOn_framework_packages_from_domain() {
      ArchRule rule =
          noClasses()
              .that()
              .resideInAPackage("..domain..")
              .and()
              .resideOutsideOfPackage("..domain.ports..")
              .should()
              .dependOnClassesThat()
              .resideInAnyPackage(
                  "org.springframework..",
                  "jakarta.persistence..",
                  "jakarta.transaction..",
                  "jakarta.validation..",
                  "org.hibernate..",
                  "software.amazon.awssdk..");

      rule.allowEmptyShould(true).check(allClasses);
    }
  }
}
