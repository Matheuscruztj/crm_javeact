plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Notification delivery and preferences"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))
    implementation(project(":backend:auth"))

    // Spring Data Commons for Pageable/Page types in ports
    implementation("org.springframework.data:spring-data-commons")

    // Spring Data JPA for repository adapter
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Web for controllers and SSE
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Mail for SMTP adapter
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Jakarta Validation for request DTOs
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Data Redis for SSE event store
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Jackson for JSON serialization
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
