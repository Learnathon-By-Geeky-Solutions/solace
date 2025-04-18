# Twiggle: System Architecture Document

## Document Information
- **Document Title**: Twiggle System Architecture
- **Version**: 1.0
- **Date**: November 25, 2024
- **Status**: Approved
- **Prepared By**: Md Rafi from Team Solace
- **Approved By**: Mentor

## 1. Introduction

### 1.1 Purpose
This document provides a comprehensive overview of the Twiggle system architecture, detailing the components, their interactions, and the technologies used to implement the platform.

### 1.2 Scope
This architecture document covers the entire Twiggle ecosystem, including backend services, databases, external integrations, and deployment infrastructure.

### 1.3 Audience
This document is intended for:
- Software developers
- System architects
- DevOps engineers
- Technical stakeholders
- Project managers

### 1.4 Definitions and Acronyms
- **API**: Application Programming Interface
- **JWT**: JSON Web Token
- **REST**: Representational State Transfer
- **ORM**: Object-Relational Mapping
- **DTO**: Data Transfer Object
- **UUID**: Universally Unique Identifier
- **CDN**: Content Delivery Network
- **CI/CD**: Continuous Integration/Continuous Deployment
- **AWS**: Amazon Web Services
- **ECS**: Elastic Container Service
- **EKS**: Elastic Kubernetes Service
- **RDS**: Relational Database Service
- **S3**: Simple Storage Service

## 2. System Overview

### 2.1 System Purpose
Twiggle is a comprehensive gardening and plant management platform that helps users plan, maintain, and track their gardens while connecting with a community of fellow gardening enthusiasts.

### 2.2 System Context
Twiggle operates as a cloud-based SaaS (Software as a Service) platform, accessible via web browsers and mobile applications. The system integrates with external weather services, plant databases, and social media platforms to provide a complete gardening experience.

### 2.3 Key Stakeholders
- **End Users**: Gardeners and plant enthusiasts using the platform
- **Administrators**: Platform managers and support staff
- **Experts**: Gardening professionals providing advice
- **Third-Party Integrations**: Weather services, plant databases, etc.
- **Development Team**: Engineers building and maintaining the platform

## 3. Architectural Drivers

### 3.1 Functional Requirements
- User authentication and authorization
- Garden planning and visualization
- Plant database and recommendations
- Weather monitoring and alerts
- Maintenance reminders and tracking
- Community sharing and social features
- Expert advice and educational content

### 3.2 Non-Functional Requirements
- **Scalability**: Support for 100,000+ users with 10,000+ concurrent sessions
- **Performance**: API response time < 200ms for 95% of requests
- **Availability**: 99.9% uptime with planned maintenance windows
- **Security**: GDPR and CCPA compliance, data encryption, secure authentication
- **Maintainability**: Modular design, comprehensive documentation, automated testing
- **Extensibility**: Ability to add new features and integrate with new services

### 3.3 Constraints
- Budget constraints for third-party services
- Regulatory requirements for data protection
- Technical limitations of mobile devices
- Internet connectivity requirements for real-time features

## 4. System Architecture

### 4.1 High-Level Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│                      Client Layer                              │
│                                                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │             │  │             │  │             │             │
│  │  Web App    │  │  iOS App    │  │ Android App │             │
│  │             │  │             │  │             │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                │                │                    │
│         ▼                ▼                ▼                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                                                         │   │
│  │                    API Gateway                          │   │
│  │                                                         │   │
│  └──────────────────────────┬──────────────────────────────┘   │
│                             │                                  │
└─────────────────────────────┼──────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                    Application Layer                            │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │             │  │             │  │             │              │
│  │  Auth       │  │  User       │  │  Garden     │              │
│  │  Service    │  │  Service    │  │  Service    │              │
│  │             │  │             │  │             │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │             │  │             │  │             │              │
│  │  Plant      │  │  Weather    │  │  Reminder   │              │
│  │  Service    │  │  Service    │  │  Service    │              │
│  │             │  │             │  │             │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐                               │
│  │             │  │             │                               │
│  │  Community  │  │  Activity   │                               │
│  │  Service    │  │  Service    │                               │
│  │             │  │             │                               │
│  └─────────────┘  └─────────────┘                               │
│                                                                 │
└─────────────────────────┬──────────────────────────────────────-┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                    Data Layer                                   │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │             │  │             │  │             │              │
│  │ PostgreSQL  │  │  MongoDB    │  │   Redis     │              │
│  │             │  │             │  │             │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐                               │
│  │             │  │             │                               │
│  │    S3       │  │  ElastiCache│                               │
│  │             │  │             │                               │
│  └─────────────┘  └─────────────┘                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Component Architecture

#### 4.2.1 Client Layer
- **Web Application**: React-based SPA with responsive design
- **iOS Application**: Native Swift application
- **Android Application**: Native Kotlin application
- **API Gateway**: AWS API Gateway for request routing and throttling

#### 4.2.2 Application Layer
- **Authentication Service**: User authentication and authorization
- **User Service**: User profile management
- **Garden Service**: Garden planning and management
- **Plant Service**: Plant database and recommendations
- **Weather Service**: Weather data and forecasts
- **Reminder Service**: Maintenance reminders and notifications
- **Community Service**: Social features and sharing
- **Activity Service**: User activity tracking

#### 4.2.3 Data Layer
- **PostgreSQL**: Primary relational database for user data, gardens, plants
- **MongoDB**: Document database for weather data, garden images
- **Redis**: In-memory cache for sessions, rate limiting, frequently accessed data
- **S3**: Object storage for images and static assets
- **ElastiCache**: Managed Redis service for caching

### 4.3 Deployment Architecture

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

#### 4.3.1 AWS Services
- **Route 53**: DNS management
- **CloudFront**: CDN for static assets
- **API Gateway**: API management and throttling
- **ECS/EKS**: Container orchestration
- **Lambda**: Serverless functions for event processing
- **RDS**: Managed PostgreSQL database
- **ElastiCache**: Managed Redis service
- **S3**: Object storage
- **CloudWatch**: Monitoring and logging
- **CloudTrail**: Audit logging
- **IAM**: Identity and access management
- **VPC**: Network isolation
- **WAF**: Web application firewall

### 4.4 Communication Architecture

#### 4.4.1 Synchronous Communication
- RESTful APIs for client-server communication
- gRPC for service-to-service communication
- WebSockets for real-time updates

#### 4.4.2 Asynchronous Communication
- RabbitMQ for message queuing
- Event-driven architecture for decoupled services
- Pub/sub pattern for notifications

#### 4.4.3 API Design
- RESTful principles
- JSON for request/response format
- JWT for authentication
- OpenAPI 3.0 for documentation
- Versioning via URL path

## 5. Component Details

### 5.1 Authentication Service

#### 5.1.1 Purpose
Handle user authentication, authorization, and session management.

#### 5.1.2 Key Components
- JWT token generation and validation
- OAuth 2.0 integration for social login
- Password hashing and verification
- Session management
- Role-based access control

#### 5.1.3 Technologies
- Spring Security
- JWT library
- bcrypt for password hashing
- Redis for session storage

### 5.2 User Service

#### 5.2.1 Purpose
Manage user profiles, preferences, and account settings.

#### 5.2.2 Key Components
- User profile CRUD operations
- Preference management
- Account settings
- User search and discovery

#### 5.2.3 Technologies
- Spring Boot
- Hibernate ORM
- PostgreSQL
- Redis for caching

### 5.3 Garden Service

#### 5.3.1 Purpose
Manage garden plans, layouts, and plant arrangements.

#### 5.3.2 Key Components
- Garden CRUD operations
- Garden layout management
- Plant placement and arrangement
- Garden sharing and privacy settings

#### 5.3.3 Technologies
- Spring Boot
- Hibernate ORM
- PostgreSQL
- Spatial extensions for garden layout

### 5.4 Plant Service

#### 5.4.1 Purpose
Provide plant database, search, and recommendations.

#### 5.4.2 Key Components
- Plant database management
- Plant search and filtering
- Plant recommendations
- Plant care information

#### 5.4.3 Technologies
- Spring Boot
- Hibernate ORM
- PostgreSQL
- Elasticsearch for search
- Redis for caching

### 5.5 Weather Service

#### 5.5.1 Purpose
Provide weather data, forecasts, and gardening advice.

#### 5.5.2 Key Components
- Current weather data
- Weather forecasts
- Garden-specific weather advice
- Weather alerts and notifications

#### 5.5.3 Technologies
- Spring Boot
- MongoDB for weather data
- Redis for caching
- OpenWeatherMap API integration

### 5.6 Reminder Service

#### 5.6.1 Purpose
Manage maintenance reminders and notifications.

#### 5.6.2 Key Components
- Reminder creation and management
- Notification scheduling
- Reminder completion tracking
- Notification delivery

#### 5.6.3 Technologies
- Spring Boot
- Hibernate ORM
- PostgreSQL
- RabbitMQ for notifications
- Firebase Cloud Messaging

### 5.7 Community Service

#### 5.7.1 Purpose
Handle social features, sharing, and community interaction.

#### 5.7.2 Key Components
- Garden image management
- Comments and likes
- User following
- Activity feed

#### 5.7.3 Technologies
- Spring Boot
- MongoDB for images and social data
- Redis for caching
- S3 for image storage

## 6. Data Architecture

### 6.1 Data Models

#### 6.1.1 User Data Model
```
User
├── id (UUID)
├── username (String)
├── email (String)
├── passwordHash (String)
├── firstName (String)
├── lastName (String)
├── bio (String)
├── profileImageUrl (String)
├── createdAt (DateTime)
├── updatedAt (DateTime)
├── lastLogin (DateTime)
├── isActive (Boolean)
└── role (String)
```

#### 6.1.2 Garden Data Model
```
Garden
├── id (UUID)
├── userId (UUID)
├── name (String)
├── description (String)
├── location (String)
├── latitude (Decimal)
├── longitude (Decimal)
├── width (Decimal)
├── height (Decimal)
├── createdAt (DateTime)
├── updatedAt (DateTime)
└── isPublic (Boolean)
```

#### 6.1.3 Plant Data Model
```
Plant
├── id (UUID)
├── name (String)
├── scientificName (String)
├── description (String)
├── type (String)
├── wateringFrequency (String)
├── sunlightRequirements (String)
├── soilType (String)
├── hardinessZone (String)
├── height (Decimal)
├── spread (Decimal)
├── imageUrl (String)
├── createdAt (DateTime)
└── updatedAt (DateTime)
```

#### 6.1.4 Garden Plant Data Model
```
GardenPlant
├── id (UUID)
├── gardenId (UUID)
├── plantId (UUID)
├── positionX (Decimal)
├── positionY (Decimal)
├── plantedDate (Date)
├── notes (String)
├── createdAt (DateTime)
└── updatedAt (DateTime)
```

#### 6.1.5 Reminder Data Model
```
Reminder
├── id (UUID)
├── userId (UUID)
├── gardenPlantId (UUID)
├── type (String)
├── frequency (String)
├── nextDueDate (DateTime)
├── notes (String)
├── createdAt (DateTime)
├── updatedAt (DateTime)
└── isActive (Boolean)
```

#### 6.1.6 Weather Data Model
```
Weather
├── location
│   ├── name (String)
│   ├── latitude (Number)
│   └── longitude (Number)
├── current
│   ├── temperature (Number)
│   ├── humidity (Number)
│   ├── windSpeed (Number)
│   ├── precipitation (Number)
│   ├── description (String)
│   ├── icon (String)
│   └── timestamp (DateTime)
├── forecast (Array)
│   └── [Forecast Day]
├── gardenAdvice (Array)
│   └── [Advice]
├── hazards (Array)
│   └── [Hazard]
└── updatedAt (DateTime)
```

### 6.2 Database Design

#### 6.2.1 PostgreSQL Schema
- Users table
- Gardens table
- Plants table
- Garden_Plants table
- Reminders table
- Activities table

#### 6.2.2 MongoDB Collections
- Weather data collection
- Garden images collection
- User activities collection

#### 6.2.3 Redis Data Structures
- User sessions
- Rate limiting
- Weather cache
- Plant recommendations cache

### 6.3 Data Flow

#### 6.3.1 User Registration Flow
1. User submits registration form
2. Authentication service validates input
3. User service creates user record
4. Authentication service generates JWT token
5. Token returned to client

#### 6.3.2 Garden Creation Flow
1. User submits garden details
2. Garden service validates input
3. Garden service creates garden record
4. Garden service returns garden data
5. Client updates UI with new garden

#### 6.3.3 Weather Data Flow
1. Client requests weather for location
2. Weather service checks cache
3. If cache miss, weather service calls external API
4. Weather service processes and stores data
5. Weather service returns data to client

#### 6.3.4 Reminder Notification Flow
1. Reminder service checks for due reminders
2. Reminder service creates notification
3. Notification service sends via preferred channel
4. User receives notification
5. User marks reminder as complete

## 7. Security Architecture

### 7.1 Authentication and Authorization
- JWT-based authentication
- OAuth 2.0 for social login
- Role-based access control
- Fine-grained permissions

### 7.2 Data Protection
- TLS 1.3 for data in transit
- AES-256 for data at rest
- Secure password hashing
- Regular security audits

### 7.3 API Security
- Input validation and sanitization
- Protection against common attacks
- Rate limiting
- IP-based blocking

### 7.4 Compliance
- GDPR compliance
- CCPA compliance
- Data retention policies
- Privacy by design

## 8. Scalability and Performance

### 8.1 Scalability Strategy
- Horizontal scaling of microservices
- Database read replicas
- Caching at multiple levels
- Stateless services

### 8.2 Performance Optimization
- CDN for static assets
- Database query optimization
- Connection pooling
- Asynchronous processing

### 8.3 Load Balancing
- Application load balancing
- Database load balancing
- Geographic distribution
- Auto-scaling

### 8.4 Caching Strategy
- Browser caching
- CDN caching
- Application caching
- Database caching

## 9. Monitoring and Observability

### 9.1 Monitoring
- Health checks
- Performance metrics
- Resource utilization
- Error rates

### 9.2 Logging
- Centralized logging
- Structured log format
- Log levels
- Request tracing

### 9.3 Alerting
- Real-time alerts
- Escalation policies
- On-call rotation
- Incident response

### 9.4 Dashboards
- System status
- Performance metrics
- User activity
- Error tracking

## 10. Deployment and DevOps

### 10.1 CI/CD Pipeline
- Automated testing
- Code quality checks
- Security scanning
- Automated deployment

### 10.2 Infrastructure as Code
- Terraform for AWS
- Docker for containers
- Kubernetes for orchestration
- Helm for packaging

### 10.3 Environments
- Development
- Staging
- Production
- Disaster recovery

### 10.4 Deployment Strategies
- Blue-green deployment
- Canary releases
- Rollback capability
- Database migrations

## 11. Disaster Recovery

### 11.1 Backup Strategy
- Database backups
- Configuration backups
- Code repository backups
- Regular testing of backups

### 11.2 Recovery Procedures
- Service recovery
- Database recovery
- Configuration recovery
- Communication plan

### 11.3 High Availability
- Multi-AZ deployment
- Database replication
- Failover automation
- Load balancing

### 11.4 Business Continuity
- Service level agreements
- Recovery time objectives
- Recovery point objectives
- Incident response plan

## 12. Future Considerations

### 12.1 Scalability Enhancements
- Event-driven architecture
- GraphQL API
- Serverless functions
- Edge computing

### 12.2 Feature Enhancements
- Machine learning
- Augmented reality
- IoT integration
- Blockchain

### 12.3 Technical Debt
- Code refactoring
- Technology updates
- Performance optimization
- Security hardening

## 13. Appendix

### 13.1 Technology Stack Details
- Version information
- Configuration details
- Dependencies
- Third-party services

### 13.2 Architecture Decision Records
- Key architectural decisions
- Alternatives considered
- Rationale
- Consequences

### 13.3 Reference Architectures
- AWS Well-Architected Framework
- Microservices patterns
- Security patterns
- Data patterns 