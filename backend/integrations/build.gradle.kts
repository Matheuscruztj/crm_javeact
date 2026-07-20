plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "External system connectors and webhooks"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    api(project(":backend:shared-kernel"))

    // Spring Web for HTTP client and REST controllers
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-context")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-junit-jupiter:${property("mockitoVersion")}")
}
