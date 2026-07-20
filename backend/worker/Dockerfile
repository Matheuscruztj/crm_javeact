# ============================================================
# AtlasOps AI — Worker Process Dockerfile
# Multi-stage build: builder + runtime
# Validates: P0.U.1 — Application Dockerfiles
# ============================================================

# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
COPY backend/ backend/

RUN ./gradlew :backend:worker:bootJar --no-daemon -q

WORKDIR /workspace/backend/worker/build/libs
RUN java -Djarmode=layertools -jar *.jar extract

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: non-root user
RUN addgroup -S atlasops && adduser -S atlasops -G atlasops
USER atlasops

WORKDIR /app

COPY --from=builder /workspace/backend/worker/build/libs/dependencies/ ./
COPY --from=builder /workspace/backend/worker/build/libs/spring-boot-loader/ ./
COPY --from=builder /workspace/backend/worker/build/libs/snapshot-dependencies/ ./
COPY --from=builder /workspace/backend/worker/build/libs/application/ ./

# Worker does not expose HTTP port by default (background processor)
# Expose management port for health checks
EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=70.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
