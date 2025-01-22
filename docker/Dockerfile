# Build stage
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app

# Install Docker client for TestContainers
RUN yum update -y && \
    amazon-linux-extras install docker && \
    yum clean all && \
    rm -rf /var/cache/yum

# Add non-root user and add to docker group
# RUN groupadd -g 999 docker && \
#     useradd -u 1000 -g docker -m spring

COPY pom.xml .
COPY src ./src

# Run code formatting, tests, and package the application
# Use Maven cache mount to speed up builds
RUN --mount=type=cache,target=/root/.m2 \
    mvn spotless:apply && \
    mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine
WORKDIR /app

# Update Alpine packages and add required tools
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
    curl \
    docker \
    docker-cli \
    shadow

# Add non-root user and add to existing docker group
RUN adduser -u 1000 -G docker -D spring && \
    chown -R spring:docker /app

# Copy the jar file
COPY --from=build /app/target/*.jar app.jar

# Set default environment to dev
ARG ENV=dev

# Copy base configuration and environment-specific configuration
COPY --from=build /app/src/main/resources/application.yml ./
COPY --from=build /app/src/main/resources/application-${ENV}.yml ./application-${ENV}.yml
RUN chown spring:docker *.yml

# Set active profile through environment variable
ENV SPRING_PROFILES_ACTIVE=${ENV}

EXPOSE 8080

# Add health check with reasonable defaults
HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER spring

# Use the environment-aware configuration
ENTRYPOINT ["java", "-jar", "app.jar"]