plugins {
    `java-library`
}

description = "Shared kernel - base types, value objects, and shared ports"

dependencies {
    // Annotations only - no Spring runtime in shared-kernel domain
    compileOnly("jakarta.validation:jakarta.validation-api:3.0.2")
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
}
