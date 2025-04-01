# Testing Guide for Twiggle Application

This guide explains how to run tests for the Twiggle application, with a focus on handling the MongoDB dependency correctly.

## Overview

The Twiggle application uses MongoDB for data storage, and some tests rely on MongoDB being available. When running tests, especially in Docker environments, you may encounter issues related to Docker-in-Docker access.

## Running Tests

### Basic Test Execution

To run basic tests without integration tests:

```bash
mvn test
```

### Docker-based Testing

For proper integration testing with MongoDB, use one of these approaches:

#### 1. Using the run-tests.sh script

```bash
./run-tests.sh
```

This script will:
- Check if Docker is available
- Start a MongoDB container
- Run the tests with integration tests enabled
- Clean up the MongoDB container when done

#### 2. Using Docker Compose for tests

```bash
./docker-test.sh
```

This runs tests in a dedicated Docker environment with proper MongoDB configuration, using the `docker/docker-test.yml` configuration.

#### 3. Manual testing with Docker

```bash
# Start MongoDB container
docker run -d --name mongodb-test -p 27017:27017 mongo:7.0.5

# Run tests with integration flag
RUN_INTEGRATION_TESTS=true mvn test

# Clean up
docker stop mongodb-test
docker rm mongodb-test
```

## Test Configurations

### Test Profile

The application includes a dedicated `test` profile in `application-test.yml` that's configured to work with a local MongoDB instance during testing.

### Docker Test Configuration

The Docker test configuration in `docker/docker-test.yml` sets up:
- A MongoDB container for testing
- The application container with proper test configuration
- Health checks to ensure MongoDB is ready before tests run

## CI/CD Environment Testing

For CI/CD pipelines, the Docker build supports skipping tests:

```bash
docker build -t twiggle:latest --build-arg SKIP_TESTS=true -f docker/Dockerfile .
```

To run tests in a CI environment, use the Docker Compose test configuration:

```bash
docker-compose -f docker/docker-test.yml up --build
```

## Notes for Test Authors

1. Use the `@ActiveProfiles("test")` annotation on test classes
2. For MongoDB integration tests, mark them with `@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")`
3. Handle MongoDB connection gracefully with appropriate null checks 