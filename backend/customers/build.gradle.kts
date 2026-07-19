plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Customers module - customer management"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))

    // Spring Web for REST controllers
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Spring Data JPA for persistence adapters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-context")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
