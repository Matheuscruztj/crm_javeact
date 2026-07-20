# ============================================================
# AtlasOps AI — Backend API Dockerfile
# Multi-stage build: builder + runtime
# Validates: P0.U.1 — Application Dockerfiles
# ============================================================

# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Gradle wrapper and build files first for layer caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/

# Copy all backend source (multi-project build)
COPY backend/ backend/

# Build the application (skip tests — tests run in CI)
RUN ./gradlew :backend:app-boot:bootJar --no-daemon -q

# Extract layered jar for optimized Docker layers
WORKDIR /workspace/backend/app-boot/build/libs
RUN java -Djarmode=layertools -jar *.jar extract

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S atlasops && adduser -S atlasops -G atlasops
USER atlasops

WORKDIR /app

# Copy layered artifacts (ordered by change frequency — least to most frequent)
COPY --from=builder /workspace/backend/app-boot/build/libs/dependencies/ ./
COPY --from=builder /workspace/backend/app-boot/build/libs/spring-boot-loader/ ./
COPY --from=builder /workspace/backend/app-boot/build/libs/snapshot-dependencies/ ./
COPY --from=builder /workspace/backend/app-boot/build/libs/application/ ./

# Expose API port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1

# Entry point with graceful shutdown support
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
