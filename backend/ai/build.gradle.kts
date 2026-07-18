plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "AI module - Spring AI integration with RAG (Ollama + pgvector)"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

dependencies {
    // Inter-module
    api(project(":backend:shared-kernel"))

    // Spring AI - Ollama integration
    implementation("org.springframework.ai:spring-ai-ollama-spring-boot-starter:${property("springAiOllamaVersion")}")

    // Spring AI - pgvector for embeddings
    implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter:${property("springAiPgvectorVersion")}")

    // Spring Data JPA for persistence adapters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-context")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
