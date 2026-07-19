plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Auth module - authentication and authorization"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))

    // Spring Web (provides jakarta.servlet for filters in presentation layer)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Web & Security & JWT
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwtVersion")}")

    // Spring context for DI in infrastructure layer
    implementation("org.springframework:spring-context")

    // Spring Web for servlet filters in presentation layer
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Web for servlet filters in presentation layer
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Jakarta Validation for request DTOs
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Data Redis for refresh tokens and lockout
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Jackson for JSON serialization in Redis adapters
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
