plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Approval workflows and decision tracking"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    api(project(":backend:shared-kernel"))

    // Spring Data Commons for Page/Pageable in port interfaces
    implementation("org.springframework.data:spring-data-commons")

    // JPA for infrastructure layer entities and adapters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Validation for presentation layer request DTOs
    implementation("jakarta.validation:jakarta.validation-api")

    // Spring Web for controllers
    implementation("org.springframework.boot:spring-boot-starter-web")
}
