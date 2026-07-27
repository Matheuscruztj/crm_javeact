plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.flywaydb.flyway") version "10.11.0"
}

description = "App Boot module - Spring Boot application entry point (aggregator)"

dependencies {
    // All domain modules
    implementation(project(":backend:shared-kernel"))
    implementation(project(":backend:auth"))
    implementation(project(":backend:tenants"))
    implementation(project(":backend:users"))
    implementation(project(":backend:customers"))
    implementation(project(":backend:documents"))
    implementation(project(":backend:requests"))
    implementation(project(":backend:approvals"))
    implementation(project(":backend:activities"))
    implementation(project(":backend:notifications"))
    implementation(project(":backend:integrations"))
    implementation(project(":backend:search"))
    implementation(project(":backend:imports"))
    implementation(project(":backend:operations"))
    implementation(project(":backend:ai"))
    implementation(project(":backend:analytics"))
    implementation(project(":backend:audit"))

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Database
    runtimeOnly("org.postgresql:postgresql:${property("postgresqlDriverVersion")}")
    runtimeOnly("com.h2database:h2")
    implementation("org.flywaydb:flyway-core:${property("flywayVersion")}")
    implementation("org.flywaydb:flyway-database-postgresql:${property("flywayVersion")}")

    // AWS S3 SDK (MinIO)
    implementation(platform("software.amazon.awssdk:bom:${property("awsSdkVersion")}"))
    implementation("software.amazon.awssdk:s3")

    // Observability
    implementation("io.micrometer:micrometer-registry-prometheus:${property("micrometerVersion")}")

    // OpenAPI / Swagger documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Logging - structured JSON
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // Resilience4j circuit breakers (P0.J.2)
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
    implementation("io.github.resilience4j:resilience4j-micrometer:2.1.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:postgresql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:junit-jupiter:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:toxiproxy:${property("testcontainersVersion")}")
    // DataFaker for realistic fake data generation in E2E / integration tests
    testImplementation("net.datafaker:datafaker:2.2.2")

    // Architecture testing
    testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunitVersion")}")
}

// Separate integration tests using JUnit 5 tags
tasks.named<Test>("test") {
    filter {
        excludeTestsMatching("*IntegrationTest")
    }
}

// Override the root integrationTest task for app-boot to use src/test (not src/integrationTest)
// since integration tests in app-boot live in src/test with @Tag("integration")
tasks.named<Test>("integrationTest") {
    description = "Runs integration tests with Testcontainers"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("*IntegrationTest")
    }
    shouldRunAfter(tasks.named("test"))
    jvmArgs("-Dtestcontainers.reuse.enable=true")
}

tasks.register<Test>("architectureTest") {
    description = "Runs architecture-focused tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("architecture")
    }
    shouldRunAfter(tasks.named("test"))
}

tasks.register<Test>("openApiContractExportTest") {
    description = "Exports the live OpenAPI contract artifact"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter {
        includeTestsMatching("com.atlasops.boot.integration.OpenApiContractExportIntegrationTest")
    }
}

tasks.register<Test>("resilienceTest") {
    description = "Runs resilience and dependency-failure tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    environment("DOCKER_HOST", "unix:///var/run/docker.sock")
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
    jvmArgs("-Ddocker.host=unix:///var/run/docker.sock")
    filter {
        includeTestsMatching("*ResilienceTest")
        includeTestsMatching("*FailureTest")
        includeTestsMatching("*Resilience*")
    }
}
