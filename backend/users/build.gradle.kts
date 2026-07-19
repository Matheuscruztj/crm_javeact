plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Users module - user management"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))

    // Spring Data JPA for persistence adapters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-context")

    // Security - bcrypt password hashing
    implementation("org.springframework.security:spring-security-crypto")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
