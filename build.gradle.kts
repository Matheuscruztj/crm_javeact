plugins {
    java
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("com.github.spotbugs") version "6.0.9" apply false
    id("org.owasp.dependencycheck") version "9.1.0" apply false
    jacoco
}

import java.time.Duration

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

    // ========================================================================
    // Compilation Optimizations
    // ========================================================================
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all"))
        // Enable incremental compilation
        options.isIncremental = true
        // Fork compilation to separate JVM for better memory management
        options.isFork = true
        options.forkOptions.jvmArgs = listOf("-Xmx1g")
    }

    // ========================================================================
    // Test Performance Optimizations
    // ========================================================================
    tasks.withType<Test> {
        useJUnitPlatform {
            // Exclude slow tests from normal runs (tag with @Tag("slow"))
            excludeTags("slow", "integration")
        }
        
        // JVM args for tests
        jvmArgs(
            "-XX:+EnableDynamicAgentLoading",
            "-Xmx1g",
            "-XX:+UseG1GC"
        )
        
        // ---- Parallelization Configuration ----
        // Run tests in parallel using all available CPUs
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        
        // Run test classes in parallel (not just methods)
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
        systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
        // Limit parallelism to avoid resource contention
        systemProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic")
        systemProperty("junit.jupiter.execution.parallel.config.dynamic.factor", "0.5")
        
        // ---- Performance Tweaks ----
        // Reuse JVM forks for faster test execution
        setForkEvery(100)
        
        // Fail fast on first test failure (optional, enable for CI)
        // failFast = true
        
        // Better test reporting
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
            showExceptions = true
            showCauses = true
            showStackTraces = true
            // Only show full details on failure
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        
        // Timeout per test class (prevent hanging tests)
        timeout.set(Duration.ofMinutes(5))
        
        // Retry flaky tests (requires test-retry plugin)
        // retry {
        //     maxRetries.set(2)
        //     failOnPassedAfterRetry.set(false)
        // }
        
        // Report generation
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        
        // Finalize test outputs
        finalizedBy(tasks.named("jacocoTestReport"))
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
        
        // Integration tests include the integration tag
        useJUnitPlatform {
            includeTags("integration")
        }
        
        // Integration tests run sequentially (shared resources)
        maxParallelForks = 1
        
        // Longer timeout for integration tests
        timeout.set(Duration.ofMinutes(10))
    }
    
    // ========================================================================
    // Fast Unit Test Task (skip slow tests)
    // ========================================================================
    tasks.register<Test>("testFast") {
        description = "Runs fast unit tests only (excludes slow and property-based tests)."
        group = "verification"
        
        useJUnitPlatform {
            excludeTags("slow", "integration", "property")
        }
        
        // Maximum parallelization for fast tests
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        
        // Fail fast
        failFast = true
    }
    
    // ========================================================================
    // Property-Based Tests Task
    // ========================================================================
    tasks.register<Test>("testProperty") {
        description = "Runs property-based tests (jqwik)."
        group = "verification"
        
        useJUnitPlatform {
            includeTags("property")
        }
        
        // PBT can be slow, run with moderate parallelism
        maxParallelForks = 2
        
        // Longer timeout for PBT
        timeout.set(Duration.ofMinutes(15))
    }

    // ========================================================================
    // Jacoco Configuration (Optimized)
    // ========================================================================
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false) // Disable CSV for faster execution
        }
        
        // Exclude generated code and configuration classes
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/config/**",
                        "**/configuration/**",
                        "**/*Config.class",
                        "**/*Configuration.class",
                        "**/Application.class",
                        "**/package-info.class"
                    )
                }
            })
        )
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

    // ========================================================================
    // Spotless Configuration (Optimized)
    // ========================================================================
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.19.2")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            // Target only src directories (exclude build)
            target("src/**/*.java")
        }
        // Enable ratchet mode (only check changed files)
        ratchetFrom("origin/main")
    }

    // ========================================================================
    // Checkstyle Configuration (Optimized)
    // ========================================================================
    configure<CheckstyleExtension> {
        toolVersion = property("checkstyleVersion") as String
        isIgnoreFailures = false
        maxWarnings = 0
        // Use configuration caching
        configDirectory.set(rootProject.file("backend/config/checkstyle"))
    }
    
    // Run checkstyle only on main sources (skip tests for speed)
    tasks.named("checkstyleTest") {
        enabled = false
    }

    // ========================================================================
    // SpotBugs Configuration (Optimized)
    // ========================================================================
    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        effort.set(com.github.spotbugs.snom.Effort.MAX)
        reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
        excludeFilter.set(rootProject.file("backend/config/spotbugs/exclude.xml"))
    }
    
    // Use HTML reports only (faster than XML)
    tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
        reports.create("html") {
            required.set(true)
        }
        reports.create("xml") {
            required.set(false)
        }
    }
    
    // Disable SpotBugs for tests (focus on main code)
    tasks.named("spotbugsTest") {
        enabled = false
    }

    // ========================================================================
    // Common Dependencies
    // ========================================================================
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

    // ========================================================================
    // OWASP Dependency-Check (P0.R.2)
    // Fails build if any dependency has a CVSS score >= 9.0 (CRITICAL)
    // ========================================================================
    configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
        failBuildOnCVSS = 9.0f
        format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML.toString()
        outputDirectory = "${project.layout.buildDirectory.get()}/reports/dependency-check"
        // Use NVD API key if provided (avoids 403/rate-limiting without a key)
        val nvdApiKey = System.getenv("NVD_API_KEY")
        if (!nvdApiKey.isNullOrBlank()) {
            nvd.setApiKey(nvdApiKey)
        }
        // Suppress false positives (add to backend/config/dependency-check/suppression.xml)
        val suppressionFile = rootProject.file("backend/config/dependency-check/suppression.xml")
        if (suppressionFile.exists()) {
            suppressionFiles = listOf(suppressionFile.absolutePath)
        }
    }

    // ========================================================================
    // Verify Task (Optimized for Speed)
    // ========================================================================
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
    
    // ========================================================================
    // Fast Verify Task (Parallel, Skip Slow Checks)
    // ========================================================================
    tasks.register("verifyFast") {
        description = "Runs fast quality gates only (skip SpotBugs and coverage verification)."
        group = "verification"
        dependsOn(
            "spotlessCheck",
            "compileJava",
            "testFast"
        )
    }
    
    // ========================================================================
    // Full Verify Task (All Checks Including Integration)
    // ========================================================================
    tasks.register("verifyFull") {
        description = "Runs all quality gates including integration tests."
        group = "verification"
        dependsOn(
            "verify",
            "integrationTest",
            "jacocoTestCoverageVerification"
        )
    }
}

// ============================================================================
// Root Project Build Scan and Reporting
// ============================================================================

// Aggregate test reports from all subprojects
tasks.register<TestReport>("aggregateTestReport") {
    description = "Aggregates test reports from all subprojects."
    group = "reporting"
    destinationDirectory.set(layout.buildDirectory.dir("reports/allTests"))
    testResults.from(subprojects.map { it.tasks.withType<Test>().map { test -> test.binaryResultsDirectory } })
}

// Aggregate Jacoco reports
tasks.register<JacocoReport>("aggregateJacocoReport") {
    description = "Aggregates Jacoco coverage reports from all subprojects."
    group = "reporting"
    
    dependsOn(subprojects.map { it.tasks.named("test") })
    
    val jacocoReportTasks = subprojects.mapNotNull { 
        it.tasks.findByName("jacocoTestReport") as? JacocoReport 
    }
    
    executionData.setFrom(files(jacocoReportTasks.map { it.executionData }))
    
    sourceDirectories.setFrom(files(subprojects.map { "${it.projectDir}/src/main/java" }))
    classDirectories.setFrom(files(subprojects.map { "${it.layout.buildDirectory.get()}/classes/java/main" }))
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated"))
    }
}

