plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Bulk data import processing"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    api(project(":backend:shared-kernel"))

    // Spring context (for component scanning)
    implementation("org.springframework:spring-context")

    // Spring Web (RestController, ResponseEntity, request annotations)
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webmvc")

    // Jakarta Validation (Valid, NotBlank)
    implementation("jakarta.validation:jakarta.validation-api")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${property("assertjVersion")}")
    testImplementation("org.mockito:mockito-core:${property("mockitoVersion")}")
    testImplementation("org.mockito:mockito-junit-jupiter:${property("mockitoVersion")}")
}
