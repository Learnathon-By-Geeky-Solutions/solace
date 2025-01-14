# Build stage
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Run code formatting, tests, and package the application
# Use Maven cache mount to speed up builds
RUN --mount=type=cache,target=/root/.m2 \
    mvn spotless:apply && \
    mvn clean verify

# Run stage
FROM amazoncorretto:21-alpine
WORKDIR /app

# Update Alpine packages and add curl for health checks
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl

# Copy the jar file
COPY --from=build /app/target/*.jar app.jar
# Explicitly copy both config files from resources
COPY --from=build /app/src/main/resources/application.yml ./
COPY --from=build /app/src/main/resources/application-dev.yml ./
# add application-pro.yml later here and clean it up a bit

EXPOSE 8080

# Add health check with reasonable defaults
# - Start checking after 30s (application startup)
# - Check every 30s
# - Set 3s timeout for each check
# - Allow 3 retries before considering unhealthy
HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
    CMD ["curl", "-f", "http://localhost:8080/actuator/health"]

# No need to specify profile as it's in application.yml
ENTRYPOINT ["java", "-jar", "app.jar"]