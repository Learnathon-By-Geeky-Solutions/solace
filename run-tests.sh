#!/bin/bash

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "Docker is not running. Please start Docker and try again."
  exit 1
fi

# Start MongoDB container for testing if not already running
if ! docker ps | grep -q "mongodb-test"; then
  echo "Starting MongoDB container for testing..."
  docker run -d --name mongodb-test -p 27017:27017 mongo:7.0.5
  sleep 5 # Give MongoDB time to initialize
fi

# Run tests with integration tests enabled
echo "Running tests..."
export RUN_INTEGRATION_TESTS=true
./mvnw clean test

# Cleanup
echo "Cleaning up MongoDB container..."
docker stop mongodb-test
docker rm mongodb-test

echo "Tests completed!" 