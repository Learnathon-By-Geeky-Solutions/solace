#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Function to check and apply code formatting
format_code() {
   echo "Checking if code formatting is necessary..."
   if ! mvn spotless:check; then
       echo "Code formatting issues found. Applying formatting with Spotless..."
       if ! mvn spotless:apply; then
           echo -e "${RED}Spotless formatting failed. Please check your code.${NC}"
           exit 1
       fi
       echo -e "${GREEN}Code formatting completed successfully.${NC}"
   else
       echo -e "${GREEN}Code formatting is already up to date.${NC}"
   fi
}

# Function to build the application
build_app() {
   echo "Building the application..."
   if ! mvn clean install; then
       echo -e "${RED}Build failed. Fix the errors and try again.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Build completed successfully.${NC}"
}

# Function to run the application locally
run_local() {
   JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" | head -n 1)
   if [ -z "$JAR_FILE" ]; then
       echo -e "${RED}No JAR file found in the target directory. Build might have failed.${NC}"
       exit 1
   fi
   echo "Running the application from $JAR_FILE..."
   if ! java -jar "$JAR_FILE"; then
       echo -e "${RED}Application failed to start.${NC}"
       exit 1
   fi
}

# Function to build Docker image
build_docker() {
   echo "Building Docker image..."
   if ! docker build -t twiggle-app -f docker/Dockerfile .; then
       echo -e "${RED}Docker build failed.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Docker image built successfully.${NC}"
}

# Function to start Docker services
start_docker_services() {
   echo "Starting Docker services..."
   if ! docker-compose -f docker/docker-compose.yml up -d; then
       echo -e "${RED}Failed to start Docker services.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Docker services started successfully.${NC}"
   echo "Services available at:"
   echo "- Application: http://localhost:8080"
   echo "- Prometheus: http://localhost:9090"
   echo "- Grafana: http://localhost:3000 (admin/admin)"
}

# Function to rebuild Docker services with no cache
rebuild_docker_services() {
   echo "Rebuilding Docker services with no cache..."
   if ! docker-compose -f docker/docker-compose.yml build --no-cache; then
       echo -e "${RED}Failed to rebuild Docker services.${NC}"
       exit 1
   fi
   echo "Starting rebuilt services..."
   if ! docker-compose -f docker/docker-compose.yml up -d; then
       echo -e "${RED}Failed to start Docker services.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Docker services rebuilt and started successfully.${NC}"
   echo "Services available at:"
   echo "- Application: http://localhost:8080"
   echo "- Prometheus: http://localhost:9090"
   echo "- Grafana: http://localhost:3000 (admin/admin)"
}

# Function to stop Docker services
stop_docker_services() {
   echo "Stopping Docker services..."
   if ! docker-compose -f docker/docker-compose.yml down; then
       echo -e "${RED}Failed to stop Docker services.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Docker services stopped successfully.${NC}"
}

# Function to run tests
run_tests() {
   echo "Running tests..."
   if ! mvn test; then
       echo -e "${RED}Tests failed.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Tests completed successfully.${NC}"
}

# Function to run SonarQube analysis
run_sonar_check() {
   echo "Starting SonarQube service..."
   if ! docker-compose -f docker/docker-compose.yml up -d sonarqube; then
       echo -e "${RED}Failed to start SonarQube.${NC}"
       exit 1
   fi

   echo "Waiting for SonarQube to start (this may take a minute)..."
   sleep 60

   echo "Running SonarQube analysis..."
   if ! mvn clean verify sonar:sonar \
       -Dsonar.host.url=http://localhost:9000 \
       -Dsonar.login=admin \
       -Dsonar.password=admin; then
       echo -e "${RED}SonarQube analysis failed.${NC}"
       exit 1
   fi

   echo -e "${GREEN}SonarQube analysis completed successfully.${NC}"
   echo "View the results at http://localhost:9000"
}

# Function to clean all and rebuild
clean_all() {
   echo "Performing deep clean..."

   # Remove Maven target directories
   echo "Cleaning Maven build directories..."
   if ! mvn clean; then
       echo -e "${RED}Maven clean failed.${NC}"
       exit 1
   fi

   # Remove Maven dependencies
   echo "Cleaning Maven dependencies..."
   if ! rm -rf ~/.m2/repository/dev/solace/twiggle/; then
       echo -e "${RED}Failed to clean Maven dependencies.${NC}"
       exit 1
   fi

   # Clean Docker resources
   echo "Cleaning Docker resources..."
   if docker images | grep -q 'twiggle-app'; then
       if ! docker rmi twiggle-app; then
           echo -e "${RED}Failed to remove Docker image.${NC}"
           exit 1
       fi
   fi

   echo -e "${GREEN}Deep clean completed successfully.${NC}"

   # Rebuild from scratch
   echo "Rebuilding from scratch..."
   format_code
   build_app
   echo -e "${GREEN}Project rebuilt successfully from scratch.${NC}"
}

# Help function
show_help() {
   echo "Usage: ./run.sh [command]"
   echo "Commands:"
   echo "  format    - Format code using Spotless"
   echo "  build     - Build the application"
   echo "  test      - Run tests"
   echo "  run       - Run the application locally"
   echo "  docker    - Build Docker image"
   echo "  start     - Start all Docker services"
   echo "  rebuild   - Rebuild and start Docker services with no cache"
   echo "  stop      - Stop all Docker services"
   echo "  clean     - Clean all packages and rebuild from scratch"
   echo "  check     - Run SonarQube analysis"
   echo "  help      - Show this help message"
}

# Main script
case "$1" in
   "format")
       format_code
       ;;
   "build")
       format_code
       build_app
       ;;
   "test")
       run_tests
       ;;
   "run")
       format_code
       build_app
       run_local
       ;;
   "docker")
       build_docker
       ;;
   "start")
       start_docker_services
       ;;
   "rebuild")
       rebuild_docker_services
       ;;
   "stop")
       stop_docker_services
       ;;
   "check")
       run_sonar_check
       ;;
   "clean")
       clean_all
       ;;
   "help"|"")
       show_help
       ;;
   *)
       echo -e "${RED}Invalid command. Use './run.sh help' for usage information.${NC}"
       exit 1
       ;;
esac