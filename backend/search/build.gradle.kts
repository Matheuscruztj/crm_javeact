plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Unified search across entities"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    api(project(":backend:shared-kernel"))

    // Spring Data for Pageable/Page types in ports
    implementation("org.springframework.data:spring-data-commons")

    // Spring Data JPA for PostgreSQL full-text search adapter
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
