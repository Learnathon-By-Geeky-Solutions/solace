# 🌱 Twiggle - Your Gardening Companion

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

[![Deploy Status](https://img.shields.io/badge/Deploy-Status-success.svg)](https://twiggle.com)
[![Documentation](https://img.shields.io/badge/Docs-API-blue.svg)](docs/API.md)
[![Tests](https://img.shields.io/badge/Tests-Passing-brightgreen.svg)](https://github.com/your-org/twiggle/actions)

</div>

## 📑 Table of Contents
- [Overview](#-overview)
- [Team](#-team)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Quick Links](#-quick-links)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Documentation](#-documentation)
- [Resources](#-resources)
- [Contributing](#-contributing)
- [License](#-license)

## 🌟 Overview

Twiggle is a comprehensive gardening and plant management platform that helps users plan, maintain, and track their gardens while connecting with a community of fellow gardening enthusiasts. Built with Spring Boot, it provides a robust backend for managing gardens, plants, weather data, and community interactions.

<div align="center">
  <img src="docs/assets/indoor-garden.jpg" alt="Beautiful indoor garden with hanging plants and natural lighting" width="800"/>
  <p><em>Transform your space into a thriving indoor garden with Twiggle's plant management features</em></p>
</div>

### Project Context
- **Purpose**: Simplify garden management and plant care
- **Target Users**: Home gardeners, plant enthusiasts, and gardening professionals
- **Key Value**: All-in-one solution for garden planning and maintenance

## 👥 Team

| Name                  | GitHub                                                    |
|-----------------------|-----------------------------------------------------------|
| Tasriad Ahmed Tias    | [@tasriad](https://github.com/tasriad)                    |
| MD. AS-AID RAHMAN     | [@aar-rafi](https://github.com/aar-rafi)                  |
| Munim Thahmid         | [@munimthahmid](https://github.com/munimthahmid)          |
| Moonwar AL Wardiful (Mentor) | [@moonwarnishan](https://github.com/moonwarnishan) |


## ✨ Features

- 🌿 **Garden Planning**
  - Create and manage multiple gardens
  - Visual garden layout designer
  - Plant placement and arrangement
  - Garden sharing and privacy settings

- 🌱 **Plant Management**
  - Comprehensive plant database
  - Smart plant recommendations
  - Growth tracking and maintenance
  - Care instructions and tips

- 🌤️ **Weather Integration**
  - Real-time weather monitoring
  - Garden-specific weather advice
  - Weather-based maintenance alerts
  - Forecast integration

- 🔔 **Smart Reminders**
  - Customizable maintenance schedules
  - Multi-channel notifications
  - Task completion tracking
  - Seasonal care reminders

- 👥 **Community Features**
  - Garden photo sharing
  - Expert advice system
  - Community discussions
  - Progress tracking

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.4.4
- **Language**: Java 21
- **API Style**: RESTful
- **Authentication**: JWT with OAuth 2.0

### Databases
- **Primary**: PostgreSQL 15
- **Document Store**: MongoDB 6.0
- **Cache**: Redis 7.0
- **ORM**: Hibernate 6.0

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions
- **Cloud**: AWS
- **CDN**: CloudFront

### External Services
- **Weather**: OpenWeatherMap API
- **Plant Database**: Trefle API
- **Storage**: AWS S3
- **Email**: SendGrid
- **Push Notifications**: Firebase Cloud Messaging

## 📁 Project Structure

```
twiggle/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/solace/twiggle/
│   │   │       ├── config/        # Configuration classes
│   │   │       ├── controller/    # REST controllers
│   │   │       ├── dto/          # Data Transfer Objects
│   │   │       ├── exception/    # Custom exceptions
│   │   │       ├── mapper/       # Object mappers
│   │   │       ├── model/        # Entity models
│   │   │       ├── repository/   # Data access layer
│   │   │       ├── service/      # Business logic
│   │   │       └── util/         # Utility classes
│   │   └── resources/
│   │       ├── application.yml   # Main configuration
│   │       └── db/              # Database migrations
│   └── test/                    # Test classes
├── docs/                        # Documentation
├── bruno/                       # API collections
└── docker/                      # Docker configuration
```

## 🔗 Quick Links

- 🌐 [Live Application](https://twiggle.com)
- 📚 [API Documentation](docs/API.md)
- 📋 [Product Requirements](docs/Product_Requirements_Document.md)
- 🏗️ [Technical Specification](docs/Technical_Specification.md)
- 📐 [System Architecture](docs/System_Architecture.md)
- 🐳 [Docker Hub](https://hub.docker.com/r/your-org/twiggle)

## 🚀 Installation

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL 15
- MongoDB 6.0
- Redis 7.0

### 🚀 Local Setup

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

**Access the application**

Backend API: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

## ⚙️ Configuration

### Environment Variables

| Variable                    | Description                         | Example                                   |
|-----------------------------|-------------------------------------|-------------------------------------------|
| `GRAFANA_USER`              | Grafana admin username              | `admin`                                   |
| `GRAFANA_PASSWORD`          | Grafana admin password              | `securepassword`                          |
| `SONARQUBE_USER`            | SonarQube login username            | `admin`                                   |
| `SONARQUBE_PASSWORD`        | SonarQube login password            | `sonarpass`                               |
| `SONAR_TOKEN`               | SonarQube API token                 | `123abc456xyz789token`                    |
| `SUPABASE_URL`              | Supabase project URL                | `https://xyzcompany.supabase.co`          |
| `SUPABASE_USERNAME`         | Supabase DB username                | `postgres`                                |
| `SUPABASE_PASSWORD`         | Supabase DB password                | `supabasepass`                            |
| `SUPABASE_API_URL`          | Supabase API base URL               | `https://xyzcompany.supabase.co/rest/v1`  |
| `SUPABASE_DB_URL`           | Supabase JDBC DB URL                | `jdbc:postgresql://xyz.supabase.co:5432/postgres` |
| `SUPABASE_ANON_KEY`         | Supabase public anon key            | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`  |
| `SUPABASE_SERVICE_ROLE_KEY`| Supabase service role key           | `service-role-key-goes-here`              |
| `OPENAI_API_KEY`            | OpenAI API key                      | `sk-abc123xyz456789...`                   |
| `UNSPLASH_ACCESS_KEY`       | Unsplash API access key             | `unsplash-access-key`                     |
| `RESEND_API_KEY`            | Resend email API key                | `re_abc123xyz456...`                      |
| `RESEND_FROM_EMAIL`         | Default from email for Resend       | `no-reply@example.com`                    |
| `WEATHER_API_KEY`           | Weather API key                     | `weather-123-api-key`                     |
| `GOOGLE_CLIENT_ID`          | Google OAuth client ID              | `12345-abc.apps.googleusercontent.com`    |
| `GOOGLE_CLIENT_SECRET`      | Google OAuth client secret          | `google-client-secret-here`               |

### Configuration Files

- `application.yml`: Main Spring Boot configuration
- `application-dev.yml`: Development profile settings
- `application-prod.yml`: Production profile settings
- `logback-spring.xml`: Logging configuration

## 📚 Documentation

- [API Documentation](docs/API.md)
- [Product Requirements](docs/Product_Requirements_Document.md)
- [Technical Specification](docs/Technical_Specification.md)
- [System Architecture](docs/System_Architecture.md)

## 📖 Resources

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


### External Services
- [OpenWeatherMap API](https://openweathermap.org/api)
- [Trefle API](https://trefle.io/)
- [SendGrid](https://sendgrid.com/)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
Made with ❤️ by Team Solace
</div>

