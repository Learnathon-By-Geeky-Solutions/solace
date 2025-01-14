#!/bin/bash

# Check required commands
for cmd in docker docker-compose mvn java curl; do
    if ! command -v $cmd >/dev/null 2>&1; then
        echo -e "${RED}Error: $cmd is required but not installed.${NC}"
        exit 1
   fi
done

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# Docker compose file paths
DOCKER_APP="docker/docker-app.yml"
DOCKER_SERVICES="docker/docker-services.yml"

# Function to check and apply code formatting
format_code() {
   echo -e "${CYAN}Checking if code formatting is necessary...${NC}"
   if ! mvn spotless:check; then
       echo -e "${YELLOW}Code formatting issues found. Applying formatting with Spotless...${NC}"
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
   echo -e "${CYAN}Building the application...${NC}"
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
   echo -e "${CYAN}Running the application from $JAR_FILE...${NC}"
   if ! java -jar "$JAR_FILE"; then
       echo -e "${RED}Application failed to start.${NC}"
       exit 1
   fi
}

# Function to build Docker image
build_docker() {
   echo -e "${CYAN}Building Docker image...${NC}"
   # First, build the application to ensure latest code is included
   format_code
   build_app
   # Build Docker image with no cache to ensure fresh build
   if ! docker build --no-cache -t twiggle-app -f docker/Dockerfile .; then
       echo -e "${RED}Docker build failed.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Docker image built successfully with latest code.${NC}"
}

# Function to start monitoring services
start_services() {
   echo -e "${CYAN}Starting monitoring services...${NC}"
   if ! docker-compose -f ${DOCKER_SERVICES} up -d; then
       echo -e "${RED}Failed to start monitoring services.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Monitoring services started successfully.${NC}"
   echo -e "${BLUE}Monitoring services available at:${NC}"
   echo -e "${CYAN}- Prometheus: http://localhost:9090${NC}"
   echo -e "${CYAN}- Grafana: http://localhost:3000 (admin/admin)${NC}"
}

# Function to start application
start_app() {
   echo -e "${CYAN}Starting application...${NC}"
   if ! docker-compose -f ${DOCKER_APP} up -d; then
       echo -e "${RED}Failed to start application service.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Application started successfully.${NC}"
   echo -e "${BLUE}Application available at:${NC}"
   echo -e "${CYAN}- Application: http://localhost:8080${NC}"
}

# Function to start all services
start_all() {
   start_services
   start_app
}

# Function to stop monitoring services
stop_services() {
   echo -e "${CYAN}Stopping monitoring services...${NC}"
   if ! docker-compose -f ${DOCKER_SERVICES} down; then
       echo -e "${RED}Failed to stop monitoring services.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Monitoring services stopped successfully.${NC}"
}

# Function to stop application
stop_app() {
   echo -e "${CYAN}Stopping application...${NC}"
   if ! docker-compose -f ${DOCKER_APP} down; then
       echo -e "${RED}Failed to stop application service.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Application stopped successfully.${NC}"
}

# Function to stop all services
stop_all() {
   stop_app
   stop_services
}

# Function to rebuild and restart all services
rebuild_docker_services() {
   echo -e "${CYAN}Starting rebuild process...${NC}"

   # Stop all services first
   stop_all

   # Build fresh application image
   build_docker

   # Start everything back up
   start_all

   echo -e "${GREEN}Rebuild completed successfully.${NC}"
}

# Function to run tests
run_tests() {
   echo -e "${CYAN}Running tests...${NC}"
   if ! mvn test; then
       echo -e "${RED}Tests failed.${NC}"
       exit 1
   fi
   echo -e "${GREEN}Tests completed successfully.${NC}"
}

# Function to run SonarQube analysis
run_sonar_check() {
    # Load the .env file
    if [ -f .env ]; then
        # Use 'set -a' to automatically export all variables
        set -a
        source .env
        set +a
    else
        echo -e "${RED}Error: .env file not found in docker/ directory.${NC}"
        exit 1
    fi

    echo -e "${CYAN}Starting SonarQube service...${NC}"
    if ! docker-compose -f ${DOCKER_SERVICES} up -d sonarqube; then
        echo -e "${RED}Failed to start SonarQube.${NC}"
        exit 1
    fi

    echo -e "${YELLOW}Waiting for SonarQube to start (this may take a minute)...${NC}"

    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}Error: curl is required but not installed.${NC}"
        exit 1
    fi

    # Wait for SonarQube to be ready
    max_attempts=30
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f http://localhost:9000/api/system/status > /dev/null; then
            echo -e "${GREEN}SonarQube is ready!${NC}"
            break
        fi
        echo -e "${YELLOW}Attempt $attempt/$max_attempts: SonarQube is not ready yet...${NC}"
        sleep 10
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        echo -e "${RED}SonarQube failed to start after $max_attempts attempts.${NC}"
        exit 1
    fi

    echo -e "${CYAN}Running SonarQube analysis...${NC}"
    if ! mvn clean verify sonar:sonar \
        -Dsonar.host.url=http://localhost:9000 \
        -Dsonar.token="${SONAR_TOKEN}"; then
        echo -e "${RED}SonarQube analysis failed.${NC}"
        exit 1
    fi

    echo -e "${GREEN}SonarQube analysis completed successfully.${NC}"
    echo -e "${BLUE}View the results at:${NC}"
    echo -e "${CYAN}http://localhost:9000${NC}"
}

# Function to clean all and rebuild
clean_all() {
   echo -e "${CYAN}Performing deep clean...${NC}"

   # Remove Maven target directories
   echo -e "${YELLOW}Cleaning Maven build directories...${NC}"
   if ! mvn clean; then
       echo -e "${RED}Maven clean failed.${NC}"
       exit 1
   fi

   # Remove Maven dependencies
   echo -e "${YELLOW}Cleaning Maven dependencies...${NC}"
   if ! rm -rf ~/.m2/repository/dev/solace/twiggle/; then
       echo -e "${RED}Failed to clean Maven dependencies.${NC}"
       exit 1
   fi

   # Clean Docker resources
   echo -e "${YELLOW}Cleaning Docker resources...${NC}"
   if docker images | grep -q 'twiggle-app'; then
       if ! docker rmi twiggle-app; then
           echo -e "${RED}Failed to remove Docker image.${NC}"
           exit 1
       fi
   fi

   echo -e "${GREEN}Deep clean completed successfully.${NC}"

   # Rebuild from scratch
   echo -e "${CYAN}Rebuilding from scratch...${NC}"
   format_code
   build_app
   echo -e "${GREEN}Project rebuilt successfully from scratch.${NC}"
}

# Function to rebuild Docker services with no cache
rebuild_docker_services_nocache() {
   echo -e "${CYAN}Rebuilding Docker services with no cache...${NC}"
   if ! docker-compose -f ${DOCKER_SERVICES} build --no-cache; then
       echo -e "${RED}Failed to rebuild monitoring services.${NC}"
       exit 1
   fi
   if ! docker-compose -f ${DOCKER_APP} build --no-cache; then
       echo -e "${RED}Failed to rebuild application service.${NC}"
       exit 1
   fi
   echo -e "${CYAN}Starting rebuilt services...${NC}"
   docker-compose -f ${DOCKER_SERVICES} up -d
   docker-compose -f ${DOCKER_APP} up -d
   echo -e "${GREEN}Docker services rebuilt and started successfully.${NC}"
   echo -e "${BLUE}Services available at:${NC}"
   echo -e "${CYAN}- Application: http://localhost:8080${NC}"
   echo -e "${CYAN}- Prometheus: http://localhost:9090${NC}"
   echo -e "${CYAN}- Grafana: http://localhost:3000 (admin/admin)${NC}"
}

# Help function with colored output
show_help() {
   echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
   echo -e "${BLUE}║             ${YELLOW}Available Commands${BLUE}             ║${NC}"
   echo -e "${BLUE}╠════════════════════════════════════════════╣${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}format${NC}     - Format code using Spotless    ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}build${NC}      - Build the application         ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}test${NC}       - Run tests                     ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}run${NC}        - Run the application locally   ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}docker${NC}     - Build fresh Docker image      ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}start-app${NC}  - Start application service     ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}stop-app${NC}   - Stop application service      ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}start-svc${NC}  - Start monitoring services     ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}stop-svc${NC}   - Stop monitoring services      ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}start${NC}      - Start all services            ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}stop${NC}       - Stop all services             ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}rebuild${NC}    - Rebuild and restart services  ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}rebuild-nocache${NC} - Rebuild with no cache    ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}clean${NC}      - Clean all and rebuild         ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}check${NC}      - Run SonarQube analysis        ${BLUE}║${NC}"
   echo -e "${BLUE}║${NC} ${CYAN}help${NC}       - Show this help message        ${BLUE}║${NC}"
   echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
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
   "start-app")
       start_app
       ;;
   "stop-app")
       stop_app
       ;;
   "start-svc")
       start_services
       ;;
   "stop-svc")
       stop_services
       ;;
   "start")
       start_all
       ;;
   "stop")
       stop_all
       ;;
   "rebuild")
       rebuild_docker_services
       ;;
   "rebuild-nocache")
       rebuild_docker_services_nocache
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