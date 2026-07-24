# ============================================================
# TICKET-F108 (Day 10, Sprint 9) — Backend Dockerfile (Multi-Stage Build)
# ============================================================
#
# WHAT: A Dockerfile is a recipe for building a container image.
#       Think of it like a cooking recipe:
#         - Each instruction (FROM, COPY, RUN) is a step
#         - Each step creates a "layer" (like layers of a cake)
#         - The final image is all layers stacked together
#
#       A "multi-stage build" uses TWO separate recipes in ONE file:
#         Stage 1 (builder): Has all the TOOLS (Maven, JDK) to compile code
#         Stage 2 (runtime): Has only what's needed to RUN the app (JRE + JAR)
#         The builder stage is thrown away — only the runtime stage ships.
#
# WHY:  Without multi-stage, your image includes Maven (~300MB) + source code
#       + JDK (~400MB) — stuff you don't need at runtime. Total: ~700MB.
#       With multi-stage, you only ship JRE (~150MB) + JAR (~50MB) = ~200MB.
#       Smaller images = faster deployments, less storage, less attack surface.
#
# ANALOGY:
#       Building a house: Stage 1 is the construction site (cranes, scaffolding,
#       cement mixers). Stage 2 is the finished house (just the building).
#       You don't ship the cranes to the homeowner.
#
# HOW TO BUILD & RUN:
#   cd backend
#   docker build -t smartbudget-backend .
#   docker run -p 8080:8080 smartbudget-backend
#   curl http://localhost:8080/actuator/health   # {"status":"UP"}
# ============================================================

# syntax=docker/dockerfile:1.6

# -------------------------------------------------------
# Stage 1 — Build stage
# -------------------------------------------------------
# Uses Maven + JDK 25 to compile the Spring Boot project into a runnable JAR.
# The `AS builder` label lets Stage 2 reach back and grab the JAR artifact.
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy pom.xml first so Docker caches the dependency-download layer.
# Any change to source code will NOT invalidate the deps cache — only pom changes will.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:resolve

# Now copy the source and build the JAR.
# -DskipTests because tests run in CI (GitHub Actions), not in the Docker build.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests


# -------------------------------------------------------
# Stage 2 — Runtime stage
# -------------------------------------------------------
# A minimal container: JRE-only Alpine image + our JAR. No Maven, no source, no JDK.
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Run as non-root for security. If the JVM is ever compromised, the attacker
# does not automatically get root inside the container. Two-line cost, big win.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Reach back into the discarded Stage 1 and copy JUST the JAR.
# --chown makes the file owned by the non-root user we created above.
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Documentation only — actual port binding happens with `docker run -p 8080:8080 ...`
EXPOSE 8080

# Container-level healthcheck: docker-compose surfaces this as (healthy) in `docker ps`.
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Exec form (JSON array) is preferred — signals propagate correctly to the JVM.
# -XX:+ExitOnOutOfMemoryError ensures the container dies on OOM so Docker restarts it.
ENTRYPOINT ["java","-XX:+ExitOnOutOfMemoryError","-jar","app.jar"]
