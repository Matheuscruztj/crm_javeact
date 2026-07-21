plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Worker Process - Async document processing, AI analysis, imports, notifications"

dependencies {
    implementation(project(":backend:shared-kernel"))
    implementation(project(":backend:documents"))
    implementation(project(":backend:ai"))
    implementation(project(":backend:notifications"))
    implementation(project(":backend:imports"))
    implementation(project(":backend:approvals"))
    implementation(project(":backend:activities"))

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Micrometer (Prometheus metrics — MeterRegistry, Gauge)
    implementation("io.micrometer:micrometer-core")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Database
    runtimeOnly("org.postgresql:postgresql:${property("postgresqlDriverVersion")}")
    runtimeOnly("com.h2database:h2")

    // AWS S3 SDK (MinIO)
    implementation(platform("software.amazon.awssdk:bom:${property("awsSdkVersion")}"))
    implementation("software.amazon.awssdk:s3")

    // Apache Tika for text extraction
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")

    // Apache PDFBox for PDF preview generation
    implementation("org.apache.pdfbox:pdfbox:3.0.1")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
