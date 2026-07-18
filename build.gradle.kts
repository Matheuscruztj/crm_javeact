plugins {
    java
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("com.github.spotbugs") version "6.0.9" apply false
    id("org.owasp.dependencycheck") version "9.1.0" apply false
    jacoco
}

allprojects {
    group = property("group") as String
    version = property("version") as String

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "org.owasp.dependencycheck")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(property("javaVersion") as String))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all"))
    }

    // ---- Testing Configuration ----
    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    // Source sets for integration tests
    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        }
    }

    val integrationTestImplementation by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly by configurations.getting {
        extendsFrom(configurations.testRuntimeOnly.get())
    }

    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter(tasks.test)
    }

    // ---- Jacoco Configuration ----
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    counter = "LINE"
                    minimum = "0.75".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "BRANCH"
                    minimum = "0.65".toBigDecimal()
                }
            }
        }
    }

    // ---- Spotless Configuration ----
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.19.2")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    // ---- Checkstyle Configuration ----
    configure<CheckstyleExtension> {
        toolVersion = property("checkstyleVersion") as String
        isIgnoreFailures = false
        maxWarnings = 0
    }

    // ---- SpotBugs Configuration ----
    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        effort.set(com.github.spotbugs.snom.Effort.MAX)
        reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
    }

    // ---- Common Dependencies ----
    dependencies {
        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter:${property("junitVersion")}")
        testImplementation("org.assertj:assertj-core:${property("assertjVersion")}")
        testImplementation("org.mockito:mockito-core:${property("mockitoVersion")}")
        testImplementation("org.mockito:mockito-junit-jupiter:${property("mockitoVersion")}")
        testImplementation("net.jqwik:jqwik:${property("jqwikVersion")}")
        testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunitVersion")}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    // ---- Verify Task ----
    tasks.register("verify") {
        description = "Runs all quality gates in sequence."
        group = "verification"
        dependsOn(
            "spotlessCheck",
            "checkstyleMain",
            "compileJava",
            "test",
            "spotbugsMain",
            "jacocoTestReport"
        )
    }
}
