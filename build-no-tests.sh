#!/bin/bash

echo "Building Docker image without running tests..."
docker build -t twiggle:latest --build-arg SKIP_TESTS=true -f docker/Dockerfile .

echo "Build completed!" 