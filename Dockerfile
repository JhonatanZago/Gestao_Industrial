# Build da aplicação
FROM gradle:9.7.1-jdk17 AS build

WORKDIR /workspace
COPY . .

RUN gradle clean bootJar --no-daemon

# Runtime enxuto
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
