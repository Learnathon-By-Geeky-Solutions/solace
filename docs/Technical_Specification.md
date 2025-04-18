# Twiggle: Technical Specification Document

## Document Information
- **Document Title**: Twiggle Technical Specification
- **Version**: 1.0
- **Date**: April 18, 2023
- **Status**: Approved
- **Prepared By**: Technical Team
- **Approved By**: Technical Lead, Product Owner

## 1. Introduction

### 1.1 Purpose
This document provides a detailed technical specification for the Twiggle application, outlining the architecture, technologies, and implementation details for the development team.

### 1.2 Scope
This specification covers the backend architecture, API design, database schema, and integration points for the Twiggle platform.

### 1.3 Definitions and Acronyms
- **API**: Application Programming Interface
- **JWT**: JSON Web Token
- **REST**: Representational State Transfer
- **ORM**: Object-Relational Mapping
- **DTO**: Data Transfer Object
- **UUID**: Universally Unique Identifier
- **CDN**: Content Delivery Network
- **CI/CD**: Continuous Integration/Continuous Deployment

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Mobile Clients │────▶│  API Gateway    │────▶│  Load Balancer │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Web Clients    │────▶│  API Gateway    │────▶│  Load Balancer │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Third-Party    │────▶│  API Gateway    │────▶│  Load Balancer │
│  Integrations   │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Authentication │◀───▶│  Microservices  │◀───▶│  Databases    │
│  Service        │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        ▲                       ▲                        ▲
        │                       │                        │
        ▼                       ▼                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  User Service   │     │  Plant Service  │     │  PostgreSQL     │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        ▲                       ▲                        ▲
        │                       │                        │
        ▼                       ▼                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Garden Service │     │  Weather Service│     │  MongoDB        │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        ▲                       ▲                        ▲
        │                       │                        │
        ▼                       ▼                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Reminder       │     │  Community      │     │  Redis Cache    │
│  Service        │     │  Service        │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 2.2 Technology Stack

#### 2.2.1 Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **API Style**: RESTful
- **Authentication**: JWT with OAuth 2.0
- **API Documentation**: OpenAPI 3.0 (Swagger)

#### 2.2.2 Databases
- **Primary Database**: PostgreSQL 15
- **Document Database**: MongoDB 6.0
- **Caching**: Redis 7.0
- **ORM**: Hibernate 6.0

#### 2.2.3 Infrastructure
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions
- **Cloud Provider**: AWS
- **CDN**: CloudFront
- **Message Queue**: RabbitMQ

#### 2.2.4 External Services
- **Weather Data**: OpenWeatherMap API
- **Plant Database**: Trefle API
- **Image Storage**: AWS S3
- **Email Service**: SendGrid
- **Push Notifications**: Firebase Cloud Messaging

### 2.3 Deployment Architecture

```
┌────────────────────────────────────────────────────────────────────────────┐
│                                AWS Cloud                                   │
│                                                                            │
│  ┌────────────┐      ┌────────────┐      ┌────────────┐                    │
│  │  Route 53  │────▶ │ CloudFront │────▶│ API Gateway│                    │
│  └────────────┘      └────────────┘      └────┬───────┘                    │
│                                               ▼                            │
│  ┌────────────┐      ┌────────────┐     ┌────────────┐                     │
│  │    ECS     │────▶│    EKS     │────▶│   Lambda   │                     │
│  └────────────┘      └────────────┘     └────┬───────┘                     │
│                                              ▼                             │
│  ┌────────────┐      ┌────────────┐     ┌────────────┐                     │
│  │    RDS     │────▶│ ElastiCache│────▶│     S3     │                     │
│  └────────────┘      └────────────┘     └────────────┘                     │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

## 3. API Design

### 3.1 API Overview
The Twiggle API follows RESTful principles and is organized into the following domains:

1. **Authentication API**: User registration, login, and token management
2. **User API**: User profile management
3. **Garden API**: Garden planning and management
4. **Plant API**: Plant database and recommendations
5. **Weather API**: Weather data and forecasts
6. **Reminder API**: Maintenance reminders and notifications
7. **Community API**: Social features and sharing
8. **Activity API**: User activity tracking

### 3.2 API Versioning
- API versioning will be implemented using URL path: `/api/v1/...`
- Major version changes will increment the version number
- Backward compatibility will be maintained for at least one major version

### 3.3 Authentication and Authorization
- JWT-based authentication
- Token expiration: 24 hours for access tokens, 30 days for refresh tokens
- Role-based access control (RBAC) with the following roles:
  - `USER`: Standard user permissions
  - `ADMIN`: Administrative permissions
  - `EXPERT`: Gardening expert permissions

### 3.4 Rate Limiting
- Standard rate limit: 100 requests per minute per user
- Burst rate limit: 200 requests per minute per user
- Rate limit headers will be included in responses:
  - `X-RateLimit-Limit`: Maximum requests per window
  - `X-RateLimit-Remaining`: Remaining requests in current window
  - `X-RateLimit-Reset`: Time when the rate limit resets

### 3.5 Error Handling
- Standardized error response format:
  ```json
  {
    "success": false,
    "message": "Error message",
    "error": {
      "code": "ERROR_CODE",
      "message": "Detailed error message",
      "details": {}
    }
  }
  ```
- HTTP status codes will be used appropriately:
  - 200: Success
  - 201: Created
  - 400: Bad Request
  - 401: Unauthorized
  - 403: Forbidden
  - 404: Not Found
  - 429: Too Many Requests
  - 500: Internal Server Error

## 4. Database Design

### 4.1 PostgreSQL Schema

#### 4.1.1 Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    profile_image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);
```

#### 4.1.2 Gardens Table
```sql
CREATE TABLE gardens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    width DECIMAL(10, 2),
    height DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_public BOOLEAN NOT NULL DEFAULT FALSE
);
```

#### 4.1.3 Plants Table
```sql
CREATE TABLE plants (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    scientific_name VARCHAR(100),
    description TEXT,
    type VARCHAR(50),
    watering_frequency VARCHAR(50),
    sunlight_requirements VARCHAR(50),
    soil_type VARCHAR(50),
    hardiness_zone VARCHAR(20),
    height DECIMAL(10, 2),
    spread DECIMAL(10, 2),
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### 4.1.4 Garden_Plants Table
```sql
CREATE TABLE garden_plants (
    id UUID PRIMARY KEY,
    garden_id UUID NOT NULL REFERENCES gardens(id),
    plant_id UUID NOT NULL REFERENCES plants(id),
    position_x DECIMAL(10, 2),
    position_y DECIMAL(10, 2),
    planted_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### 4.1.5 Reminders Table
```sql
CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    garden_plant_id UUID REFERENCES garden_plants(id),
    type VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    next_due_date TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

#### 4.1.6 Activities Table
```sql
CREATE TABLE activities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 4.2 MongoDB Collections

#### 4.2.1 Weather Data Collection
```json
{
  "_id": ObjectId,
  "location": {
    "name": "string",
    "latitude": number,
    "longitude": number
  },
  "current": {
    "temperature": number,
    "humidity": number,
    "windSpeed": number,
    "precipitation": number,
    "description": "string",
    "icon": "string",
    "timestamp": ISODate
  },
  "forecast": [
    {
      "date": ISODate,
      "temperature": {
        "min": number,
        "max": number
      },
      "humidity": number,
      "windSpeed": number,
      "precipitation": number,
      "description": "string",
      "icon": "string"
    }
  ],
  "gardenAdvice": [
    {
      "type": "string",
      "description": "string",
      "priority": number
    }
  ],
  "hazards": [
    {
      "type": "string",
      "description": "string",
      "severity": "string",
      "startTime": ISODate,
      "endTime": ISODate
    }
  ],
  "updatedAt": ISODate
}
```

#### 4.2.2 Garden Images Collection
```json
{
  "_id": ObjectId,
  "gardenId": "uuid",
  "userId": "uuid",
  "url": "string",
  "thumbnailUrl": "string",
  "description": "string",
  "tags": ["string"],
  "likes": number,
  "comments": [
    {
      "id": "uuid",
      "userId": "uuid",
      "content": "string",
      "createdAt": ISODate
    }
  ],
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

### 4.3 Redis Data Structures

#### 4.3.1 User Sessions
- Key: `session:{userId}`
- Value: JWT token
- Expiration: 24 hours

#### 4.3.2 Rate Limiting
- Key: `ratelimit:{userId}`
- Value: Counter
- Expiration: 1 minute

#### 4.3.3 Weather Cache
- Key: `weather:{locationId}`
- Value: Serialized weather data
- Expiration: 30 minutes

#### 4.3.4 Plant Recommendations Cache
- Key: `recommendations:{userId}:{gardenId}`
- Value: Serialized plant recommendations
- Expiration: 24 hours

## 5. Microservices Design

### 5.1 Authentication Service
- **Purpose**: Handle user authentication and authorization
- **Endpoints**:
  - `/api/v1/auth/register`: User registration
  - `/api/v1/auth/login`: User login
  - `/api/v1/auth/refresh`: Token refresh
  - `/api/v1/auth/logout`: User logout
  - `/api/v1/auth/reset-password`: Password reset

### 5.2 User Service
- **Purpose**: Manage user profiles and preferences
- **Endpoints**:
  - `/api/v1/users`: User CRUD operations
  - `/api/v1/users/{id}/profile`: Profile management
  - `/api/v1/users/{id}/preferences`: User preferences

### 5.3 Garden Service
- **Purpose**: Manage garden plans and layouts
- **Endpoints**:
  - `/api/v1/gardens`: Garden CRUD operations
  - `/api/v1/gardens/{id}/plants`: Plants in a garden
  - `/api/v1/gardens/{id}/layout`: Garden layout management

### 5.4 Plant Service
- **Purpose**: Provide plant database and recommendations
- **Endpoints**:
  - `/api/v1/plants`: Plant CRUD operations
  - `/api/v1/plants/search`: Plant search
  - `/api/v1/plants/recommendations`: Plant recommendations

### 5.5 Weather Service
- **Purpose**: Provide weather data and forecasts
- **Endpoints**:
  - `/api/v1/weather/current`: Current weather
  - `/api/v1/weather/forecast`: Weather forecast
  - `/api/v1/weather/garden`: Garden-specific weather advice
  - `/api/v1/weather/hazards`: Weather hazards

### 5.6 Reminder Service
- **Purpose**: Manage maintenance reminders
- **Endpoints**:
  - `/api/v1/reminders`: Reminder CRUD operations
  - `/api/v1/reminders/upcoming`: Upcoming reminders
  - `/api/v1/reminders/complete`: Mark reminder as complete

### 5.7 Community Service
- **Purpose**: Handle social features and sharing
- **Endpoints**:
  - `/api/v1/images`: Garden image management
  - `/api/v1/images/{id}/comments`: Image comments
  - `/api/v1/images/{id}/likes`: Image likes
  - `/api/v1/activities`: User activities

## 6. Security Considerations

### 6.1 Authentication
- JWT tokens with RSA-256 signing
- Token expiration and refresh mechanism
- Secure password hashing using bcrypt
- Multi-factor authentication option for sensitive operations

### 6.2 Authorization
- Role-based access control (RBAC)
- Fine-grained permissions for resources
- API key authentication for third-party integrations

### 6.3 Data Protection
- All data in transit encrypted using TLS 1.3
- Sensitive data at rest encrypted using AES-256
- Regular security audits and penetration testing
- GDPR and CCPA compliance

### 6.4 API Security
- Input validation and sanitization
- Protection against common attacks (SQL injection, XSS, CSRF)
- Rate limiting to prevent abuse
- IP-based blocking for suspicious activity

## 7. Performance Considerations

### 7.1 Caching Strategy
- Redis for frequently accessed data
- CDN for static assets and images
- Browser caching for API responses
- Database query caching

### 7.2 Database Optimization
- Indexing on frequently queried fields
- Partitioning for large tables
- Query optimization and monitoring
- Connection pooling

### 7.3 API Performance
- Pagination for large result sets
- Field filtering to reduce response size
- Compression for responses
- Asynchronous processing for long-running operations

### 7.4 Scalability
- Horizontal scaling of microservices
- Database read replicas
- Load balancing across multiple instances
- Auto-scaling based on demand

## 8. Monitoring and Logging

### 8.1 Application Monitoring
- Health checks for all services
- Performance metrics collection
- Error tracking and alerting
- User activity monitoring

### 8.2 Logging Strategy
- Centralized logging using ELK stack
- Structured logging in JSON format
- Log levels: ERROR, WARN, INFO, DEBUG
- Request ID tracking across services

### 8.3 Alerting
- Real-time alerts for critical issues
- Escalation policies for unresolved alerts
- Dashboard for system status
- Regular reporting on system health

## 9. Deployment and DevOps

### 9.1 CI/CD Pipeline
- Automated testing (unit, integration, e2e)
- Code quality checks (SonarQube)
- Security scanning
- Automated deployment to environments

### 9.2 Environments
- Development
- Staging
- Production
- Disaster Recovery

### 9.3 Deployment Strategy
- Blue-green deployment
- Canary releases for critical changes
- Rollback capability
- Database migration management

### 9.4 Infrastructure as Code
- Terraform for AWS infrastructure
- Docker for containerization
- Kubernetes for orchestration
- Helm for package management

## 10. Testing Strategy

### 10.1 Testing Levels
- Unit testing (JUnit, Mockito)
- Integration testing (Spring Test)
- API testing (REST Assured)
- End-to-end testing (Selenium)
- Performance testing (JMeter)

### 10.2 Test Coverage
- Minimum 80% code coverage
- Critical paths 100% covered
- Automated test execution in CI pipeline
- Regular regression testing

### 10.3 Test Environments
- Local development environment
- CI test environment
- Staging environment
- Production-like environment for final testing

## 11. Documentation

### 11.1 API Documentation
- OpenAPI 3.0 specification
- Interactive Swagger UI
- Postman collection
- Example requests and responses

### 11.2 Code Documentation
- Javadoc for all public APIs
- README files for each service
- Architecture decision records (ADRs)
- Code style guide

### 11.3 Operational Documentation
- Deployment procedures
- Monitoring and alerting guide
- Troubleshooting guide
- Disaster recovery procedures

## 12. Future Considerations

### 12.1 Scalability Enhancements
- Event-driven architecture using Kafka
- GraphQL API for flexible queries
- Serverless functions for specific operations
- Edge computing for global performance

### 12.2 Feature Enhancements
- Machine learning for plant recommendations
- Augmented reality for garden visualization
- IoT integration for smart garden devices
- Blockchain for plant provenance tracking

### 12.3 Technical Debt
- Regular code refactoring
- Technology stack updates
- Performance optimization
- Security hardening

## 13. Appendix

### 13.1 API Endpoints Reference
- Complete list of all API endpoints
- Request/response examples
- Error codes and handling

### 13.2 Database Schema Diagrams
- Entity-relationship diagrams
- Table schemas
- Index definitions

### 13.3 Third-Party Service Integration
- API documentation for external services
- Integration patterns
- Error handling and fallbacks 