# ── Stage 1: Build ────────────────────────────────────────────
FROM gradle:8.14-jdk17 AS build
WORKDIR /app

# Cache dependencies before copying source
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon 2>/dev/null || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Run ──────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app
USER app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
