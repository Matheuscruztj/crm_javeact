plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Documents module - document management and ingestion"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))
    implementation(project(":backend:ai"))

    // Spring Data JPA for persistence adapters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-context")

    // AWS S3 SDK for MinIO object storage adapter
    implementation(platform("software.amazon.awssdk:bom:${property("awsSdkVersion")}"))
    implementation("software.amazon.awssdk:s3")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
