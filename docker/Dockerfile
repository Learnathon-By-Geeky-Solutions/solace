# Build stage
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Format code and package the application in a single layer
RUN mvn spotless:apply && \
    mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine
WORKDIR /app

# Update Alpine packages to get the latest security fixes
RUN apk update && \
    apk upgrade

# Copy the jar file
COPY --from=build /app/target/*.jar app.jar
# Explicitly copy both config files from resources
COPY --from=build /app/src/main/resources/application.yml ./
COPY --from=build /app/src/main/resources/application-dev.yml ./
# add application-pro.yml later here and clean it up a bit

EXPOSE 8080
# No need to specify profile as it's in application.yml
ENTRYPOINT ["java", "-jar", "app.jar"]