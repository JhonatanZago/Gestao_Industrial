# Build da aplicação
FROM gradle:9.7.1-jdk17 AS build

WORKDIR /workspace
COPY . .

RUN gradle clean bootJar --no-daemon

# Runtime enxuto
FROM eclipse-temurin:17-jre

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/build/libs/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl --fail --silent --show-error http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
