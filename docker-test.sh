#!/bin/bash

echo "Setting up Docker test environment..."
cd "$(dirname "$0")" || { echo "ERROR: FAILED TO CHANGE DIRECTORY"; exit 1; }
# Build and run the test containers
docker compose -f docker/docker-test.yml down -v
docker compose -f docker/docker-test.yml up --build

# Cleanup
docker compose -f docker/docker-test.yml down -v 