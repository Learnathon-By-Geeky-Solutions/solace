# Twiggle - Urban Garden Planner (Backend)

Twiggle is a web application designed to help urban gardeners plan and manage their gardens. It provides personalized gardening suggestions, space-optimized layout designing, and resource management for urban environments, making gardening accessible and enjoyable for everyone.

---

## Project Setup

This project uses **Spring Boot** to build the backend application. Below are the steps to set up the backend on your local machine.

---

### Prerequisites

Ensure the following tools are installed on your system:

- **Java 21** or above
- **Maven** (version 3.6+ recommended)
- **MongoDB** (for NoSQL database)
- **PostgreSQL** (for Supabase database)

---

### Getting Started

1. **Clone the Repository**

   ```bash
   git clone https://github.com/Learnathon-By-Geeky-Solutions/solace.git
   cd solace
   ```

2. **Install Dependencies**

   This project uses Maven as the build tool. To install the necessary dependencies, run:

   ```bash
   mvn clean install
   ```

3. **Configure Application Properties**

   Configure your database connection settings and other properties in `src/main/resources/application.properties` or `src/main/resources/application.yml`.

   - Example for MongoDB:

     ```properties
     spring.data.mongodb.uri=mongodb://localhost:27017/twiggle
     ```

   - Example for PostgreSQL:

     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/twiggle
     spring.datasource.username=your-username
     spring.datasource.password=your-password
     ```

4. **Run the Application**

   Use the provided shell script to run the application:

   ```bash
   ./run.sh
   ```

   Alternatively, you can start the application manually:

   ```bash
   mvn spring-boot:run
   ```

5. **Access the Application**

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
/twiggle
├── src
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
└── pom.xml                                    # Maven build file
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
- **Lombok**: Boilerplate code reduction
- **Spring Boot Configuration Processor**: Configuration metadata generation
- **Spotless Plugin**: Code formatting and linting

---

### Testing

Run unit and integration tests using:

```bash
mvn test
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

### Contribution

We welcome contributions! Please fork the repository, make your changes, and submit a pull request.

---

### License

This project is licensed under the MIT License - see the LICENSE file for details.

---

### Contact

For any questions or inquiries, contact us at:

**Email**: [contact@solace.dev](mailto:contact@solace.dev)

**Repository**: [Twiggle GitHub](https://github.com/Learnathon-By-Geeky-Solutions/solace.git)

