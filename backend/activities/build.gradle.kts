plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Activity logging and timeline events"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))

    // Spring Data Commons for Pageable/Page types in ports
    implementation("org.springframework.data:spring-data-commons")

    // Spring Web for presentation layer
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA for infrastructure layer
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
