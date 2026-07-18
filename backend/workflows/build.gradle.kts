plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Workflows module - workflow orchestration"

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
    implementation("org.springframework:spring-context")

    // Redis for workflow state caching
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
