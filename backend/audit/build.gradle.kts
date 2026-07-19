plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Audit module - audit logging and compliance"

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

    // Spring Data JPA for infrastructure adapter
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Web for REST controller
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring AOP for audit aspect
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Jackson for JSON handling in aspect
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Logging (used in use cases for retry/error logging)
    implementation("org.slf4j:slf4j-api")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("net.jqwik:jqwik:1.9.2")
    testRuntimeOnly("ch.qos.logback:logback-classic")
}
