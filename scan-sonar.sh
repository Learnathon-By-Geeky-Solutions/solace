#!/bin/bash

# Set the directory containing your docker compose files
DIR="docker"
cd "$(dirname "$0")"

# Create network if it doesn't exist
if ! docker network ls | grep -q monitoring-network; then
  echo "Creating monitoring-network..."
  docker network create monitoring-network
fi

# Check if SonarQube is already running
if ! curl -s http://localhost:9000 > /dev/null; then
  echo "Starting SonarQube..."
  docker compose -f ${DIR}/docker-services.yml up -d sonarqube
  
  # Wait for SonarQube to be ready
  echo "Waiting for SonarQube to start (this may take a minute)..."
  while ! curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; do
    echo -n "."
    sleep 5
  done
  echo -e "\nSonarQube is up and running!"
fi

# Generate token if not already set
if [ -z "$SONAR_TOKEN" ]; then
  echo "SONAR_TOKEN not set. Please set it in your .env file."
  echo "You can generate a token from SonarQube UI:"
  echo "1. Go to http://localhost:9000"
  echo "2. Log in (default: admin/admin)"
  echo "3. Go to User > My Account > Security"
  echo "4. Generate a new token and add it to your .env file as SONAR_TOKEN=your_token"
  exit 1
fi

# Build the application with Maven in Docker if target directory doesn't exist
if [ ! -d "target/classes" ]; then
  echo "Building the application with Maven in Docker..."
  docker run --rm -v "$(pwd)":/app -w /app maven:3.9-amazoncorretto-21 mvn clean package -DskipTests
fi

# Ensure dependencies are resolved
if [ ! -d ".m2" ]; then
  echo "Resolving Maven dependencies..."
  docker run --rm -v "$(pwd)":/app -v "$(pwd)/.m2":/root/.m2 -w /app maven:3.9-amazoncorretto-21 mvn dependency:resolve
fi

# Run SonarQube Scanner
echo "Running SonarQube analysis..."
docker compose -f ${DIR}/sonar-scanner.yml up

echo "Analysis complete! Check results at http://localhost:9000/dashboard?id=twiggle"