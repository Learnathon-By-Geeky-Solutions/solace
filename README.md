# Twiggle - Urban Garden Planner (Backend)

Twiggle is a web application designed to help urban gardeners plan and manage their gardens. It provides personalized gardening suggestions, space-optimized layout designing, and resource management for urban environments, making gardening accessible and enjoyable for everyone.

---

## Project Setup

This project uses **Spring Boot** to build the backend application. Below are the steps to set up the backend on your local machine.

---

### Prerequisites

Ensure the following tools are installed on your system:

- **Java 21** or above
- **JDK** (version 21 or 21+ recommended) with JAVA_HOME environment variable set
- **Maven** (version 3.6+ recommended)
---

### Getting Started

1. **Clone the Repository**

   ```bash
   git clone --branch develop-backend https://github.com/Learnathon-By-Geeky-Solutions/solace.git
   cd solace
   ```
   Note: If you want to clone the other branches, use the following command:
   ```bash
   git clone --branch <branch-name> https://github.com/Learnathon-By-Geeky-Solutions/solace.git
   cd solace
    ```

2. **Install Dependencies & Setup**

   This project uses Maven as the build tool. To install the necessary dependencies, run:

   ```bash
   mvn clean install
   ```
   Note: Alternatively, you can use the shell script provided to clean and build the project:
   ```bash
    ./run.sh clean
   ```

3. **Configure Application Properties**

   The application properties are stored in `src/main/resources/application.properties`. You can modify these properties as needed.

   For sensitive information (e.g., database credentials, API keys), use environment variables or a `.env` file. Create a `.env` file in the project root and add the required environment variables. A `.env.example` file is provided as a template.

   Example `.env` file:
   ```bash
   GRAFANA_USER=username
   GRAFANA_PASSWORD=password
   
   SONARQUBE_USER=username
   SONARQUBE_PASSWORD=password
   SONAR_TOKEN=token
   ```

   The application reads these environment variables during startup.

4. **Start Docker Services**

   If you want to run the application using Docker, start the Docker services and application using the provided shell script:

   ```bash
   ./run.sh start
   ```

   This command starts the application, Prometheus, and Grafana services etc. Access the application at `http://localhost:8080`.

   To stop the services, run:

   ```bash
   ./run.sh stop
   ```
5. **Run the Application Locally**

   Use the provided shell script to run the application:

   ```bash
   ./run.sh run
   ```

   Alternatively, you can start the application manually:

   ```bash
   mvn spring-boot:run
   ```

6. **Access the Application**

   Once the application starts, access the backend at:

   ```
   http://localhost:8080
   ```

   For API documentation, access Swagger UI:

   ```
   http://localhost:8080/swagger-ui/index.html
   ```

---

### Current Code Structure

```
twiggle/
├── src/                    # Source code
│   ├── main
│   │   ├── java
│   │   │   ├── dev.solace
│   │   │   │   ├── twiggle
│   │   │   │   │   ├── controller            # API endpoints (REST controllers)
│   │   │   │   │   ├── model                 # Domain models (e.g., Plant, Layout)
│   │   │   │   │   ├── repository            # Database repositories (JPA repositories)
│   │   │   │   │   ├── service               # Business logic (service layer)
│   │   │   │   │   ├── dto                   # Data Transfer Objects (DTOs for API requests/responses)
│   │   │   │   │   ├── exception             # Custom exception handling (global exception handler)
│   │   │   │   │   ├── config                # Configuration classes (security, Swagger, etc.)
│   │   │   │   │   ├── util                  # Utility classes (e.g., date, validation)
│   │   │   │   │   ├── security              # Security (JWT, OAuth2, etc.)
│   │   │   │   │   ├── ai                    # AI/ML integration (plant disease detection, recommendations)
│   │   │   │   │   ├── scheduler             # Scheduling tasks (for watering, fertilizing)
│   │   ├── resources
│   │   │   ├── application.properties        # Application config properties
│   │   │   ├── static                       # Static files (if needed, e.g., images for plant database)
│   │   │   ├── templates                    # Thymeleaf templates (if using for server-side rendering)
│   └── test
│       ├── java
│       │   ├── dev.solace
│       │   │   ├── twiggle
│       │   │   │   ├── controller            # Tests for controllers (API layer)
│       │   │   │   ├── service               # Tests for services (business logic layer)
│       │   │   │   ├── repository            # Tests for repositories (data layer)
│       │   │   │   ├── util                  # Tests for utility classes
├── docker/                 # Docker configuration
│   ├── Dockerfile         # Application Dockerfile
│   ├── docker-compose.yml # Docker services configuration
│   ├── prometheus/        # Prometheus configuration
│   └── grafana/           # Grafana configuration
├── .env                    # Environment variables
├── pom.xml                # Maven dependencies
└── run.sh                 # Script for various operations
```

---

### Maven Dependencies

The project includes the following dependencies for backend development:

- **Spring Web**: Build REST APIs
- **Spring Data MongoDB**: MongoDB integration
- **Spring Boot DevTools**: Developer tools
- **Spring Security**: Authentication and authorization
- **JWT Support**: JSON Web Token integration
- **Spring Boot Actuator**: Monitoring and metrics
- **Spring Boot Starter Test**: Testing framework
- **Spring Boot Starter Validation**: Input validation
- **Springdoc OpenAPI**: API documentation (Swagger UI)
- **Prometheus**: Monitoring and alerting
- **Lombok**: Boilerplate code reduction
- **Spring Boot Configuration Processor**: Configuration metadata generation
- **Dotenv**: Environment variable management
- **Mapstruct**: Object mapping
- **Resilience4j**: Resilience patterns (circuit breaker, rate limiter)
- **Spotless Plugin**: Code formatting and linting
- **Jocco Plugin**: Code documentation generation
- **Git Commit ID Plugin**: Git commit ID generation
- **SonarQube Plugin**: Code quality analysis

---

### Available Commands

The `run.sh` script provides various commands to manage the application. 

```bash
./run.sh [command]
```
Use the following commands to display the list of available commands:

```bash
./run.sh help
```
---

### Testing

Run unit and integration tests using:

```bash
mvn test
```

Alternatively, use the provided shell script to run the tests:

```bash
./run.sh test
```

---

### Logging and Monitoring

The application uses Spring Boot Actuator for health checks and monitoring. Access these endpoints at:

- **Health Check**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`

---

### Deployment

Package the application as a JAR file:

```bash
mvn clean package
```

Deploy the `twiggle.jar` file to your preferred environment.

---

### Contact

For any questions or inquiries, contact us at:

**Email**: [Tasriad Ahmed Tias](mailto:trisn.eclipse@gmail.com)

**Repository**: [Twiggle GitHub](https://github.com/Learnathon-By-Geeky-Solutions/solace.git)

