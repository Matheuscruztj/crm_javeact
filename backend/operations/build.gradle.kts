plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "System operations and health monitoring"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    api(project(":backend:shared-kernel"))

    // Spring Web for REST controllers
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-context")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
