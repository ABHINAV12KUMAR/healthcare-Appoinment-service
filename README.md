# Healthcare Appointment Service

A Spring Boot microservice that handles appointment booking in a healthcare system. This service validates patient information, books appointments, and coordinates with other services through event-driven architecture.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [System Integration](#system-integration)
- [CI/CD Pipeline](#cicd-pipeline)
- [Circuit Breaker](#circuit-breaker)
- [Monitoring & Observability](#monitoring--observability)

## Overview

The Appointment Service is a critical component of a microservices-based healthcare platform.
It enables patients to book appointments with doctors by managing available time slots, validating patient information,
and publishing appointment events to other services for further processing (notifications, billing, etc.).

### Key Responsibilities

- Book appointments between patients and doctors
- Validate patient information through external Patient Query Service
- Publish appointment events to Kafka for downstream processing
- Ensure data consistency with transactional operations
- Handle service failures with circuit breaker pattern

## Features

### Core Functionality

- **Appointment Booking**: RESTful API for booking appointments with validation
- **Patient Validation**: Integration with Patient Query Service for patient verification
- **Event Publishing**: Asynchronous event publishing via Kafka
- **Duplicate Prevention**: Database-level constraints to prevent double booking

### Enterprise Features

- **Circuit Breaker**: Resilience4j for fault tolerance against external service failures
- **Distributed Tracing**: MDC-based trace correlation across microservices
- **Pessimistic Locking**: Database row locking for concurrency control
- **Transaction Management**: Atomic operations with after-commit event publishing
- **API Documentation**: OpenAPI 3 (Swagger) for interactive API exploration
- **Health Monitoring**: Spring Boot Actuator endpoints for observability

## Technology Stack

### Core Technologies

- **Java 17** - Programming language
- **Spring Boot 3.5.15** - Application framework
- **Spring Data JPA** - Database abstraction
- **Spring Kafka** - Event streaming
- **Spring Validation** - Request validation

### Database & Messaging

- **MySQL** - Relational database
- **Apache Kafka** - Event streaming platform

### Build & Deployment

- **Maven** - Build tool and dependency management
- **Jenkins** - CI/CD pipeline
- **SonarQube** - Code quality analysis

### Libraries & Tools

- **Lombok 1.18.46** - Reduce boilerplate code
- **MapStruct 1.6.3** - Type-safe bean mapping
- **Resilience4j 2.2.0** - Fault tolerance patterns
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **Spring Boot Actuator** - Application monitoring

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│     Controllers (REST API)          │
│  - AppointmentController           │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│        Service Layer                │
│  - AppointmentServiceImpl           │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│      Repository Layer               │
│  - AppointmentRepository            │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│         Database (MySQL)            │
│  - appointments table               │
└─────────────────────────────────────┘
```

### Key Components

#### Entities
- **Appointment** - Stores appointment details (patient, doctor, date, time, status)

#### Controllers
- **AppointmentController** - `POST /api/v1/appointments` for booking appointments

#### Services
- **AppointmentServiceImpl** - Core business logic for appointment booking

#### External Integration
- **PatientQueryClient** - REST client for Patient Query Service with circuit breaker
- **AppointmentEventProducer** - Kafka event producer for appointment events

### Business Logic Flow

1. Validate appointment request (patient ID, doctor, date, time)
2. Fetch patient details from Patient Query Service (with circuit breaker)
3. Check for duplicate patient bookings at same time
4. Save appointment with SCHEDULED status
5. Publish appointment event to Kafka AFTER database commit
6. Return appointment response

## Prerequisites

Before running this application, ensure you have:

- **Java 17** or higher installed
- **Maven 3.6+** for building the project
- **MySQL 8.0+** running on localhost:3306
- **Apache Kafka** running on localhost:9092
- **Patient Query Service** running on localhost:8082

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd appointment_service
```

2. Create MySQL database:
```sql
CREATE DATABASE appointment_db;
```

3. Update database credentials in `src/main/resources/application.yaml` if needed:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/appointment_db
    username: root
    password: admin123
```

4. Build the project:
```bash
mvn clean install
```

## Configuration

### Application Configuration

Key configuration in `src/main/resources/application.yaml`:

- **Server Port**: 8084
- **Database**: MySQL connection settings
- **Kafka**: Bootstrap servers and producer configuration
- **Patient Query Service**: URL and timeout settings
- **Circuit Breaker**: Resilience4j configuration
- **Logging**: Debug level for application logs

### Environment Variables

The following can be configured via environment variables:

- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap servers

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using JAR

```bash
java -jar target/appointment_service-0.0.1-SNAPSHOT.jar
```

The application will start on port 8084.

### Verify Health Check

```bash
curl http://localhost:8084/actuator/health
```

## API Documentation

### Swagger UI

Interactive API documentation is available at:
```
http://localhost:8084/swagger-ui.html
```

### OpenAPI JSON

OpenAPI specification is available at:
```
http://localhost:8084/api-docs
```

### Key Endpoints

#### Book Appointment
```http
POST /api/v1/appointments
Content-Type: application/json

{
  "patientId": 1,
  "doctorName": "Dr. Smith",
  "appointmentDate": "2026-07-15",
  "appointmentTime": "10:00",
  "reason": "Regular checkup"
}
```

**Response Codes:**
- `201` - Appointment booked successfully
- `400` - Invalid request data
- `409` - Slot already booked or duplicate appointment
- `503` - Patient service unavailable

## Database Schema

### ER Diagram

```mermaid
erDiagram
    DOCTOR ||--o{ APPOINTMENT : "has"

    DOCTOR {
        bigint id PK "Auto Increment"
        varchar doctor_name UK "NOT NULL"
        varchar specialization
        varchar email
        varchar phone
        boolean is_available
        timestamp created_at
        timestamp updated_at
    }

    APPOINTMENT {
        bigint id PK "Auto Increment"
        bigint patient_id
        varchar patient_name
        varchar patient_email
        varchar doctor_name FK "NOT NULL"
        date appointment_date "NOT NULL"
        time appointment_time "NOT NULL"
        varchar reason
        enum status "PENDING, SCHEDULED, CANCELLED, COMPLETED"
        timestamp created_at
        timestamp updated_at
    }

    APPOINTMENT {
        UK "doctor_name, appointment_date, appointment_time"
    }
```

### Appointments Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| patient_id | BIGINT | NOT NULL |
| patient_name | VARCHAR | - |
| patient_email | VARCHAR | - |
| doctor_name | VARCHAR | NOT NULL |
| appointment_date | DATE | NOT NULL |
| appointment_time | TIME | NOT NULL |
| reason | VARCHAR | - |
| status | ENUM | PENDING, SCHEDULED, CANCELLED, COMPLETED |
| created_at | TIMESTAMP | - |
| updated_at | TIMESTAMP | - |

**Unique Constraint:** (doctor_name, appointment_date, appointment_time)

### Doctors Table

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| doctor_name | VARCHAR | NOT NULL, UNIQUE |
| specialization | VARCHAR | - |
| email | VARCHAR | - |
| phone | VARCHAR | - |
| is_available | BOOLEAN | - |
| created_at | TIMESTAMP | - |
| updated_at | TIMESTAMP | - |


## System Integration

### External Services

#### Patient Query Service
- **URL**: http://localhost:8082
- **Purpose**: Validate patient existence and retrieve patient details
- **Integration**: REST client with circuit breaker protection
- **Timeout**: 2s connect, 3s read

#### Kafka
- **Bootstrap Servers**: localhost:9092
- **Topics**: appointment-topic
- **Purpose**: Publish/consume events for inter-service communication
- **Event Types**: APPOINTMENT_BOOKED

### Event Schema

Appointment events contain:
- appointmentId
- patientId
- patientName
- appointmentDate
- appointmentTime
- status
- eventType
- timestamp

## CI/CD Pipeline

### Jenkins Pipeline Stages

The `Jenkinsfile` defines the following stages:

1. **Checkout** - Pulls source code from repository
2. **Check Maven** - Verifies Maven installation
3. **List Workspace Files** - Lists project files
4. **Build Appointment Service** - Compiles and packages with Maven
5. **SonarQube Analysis** - Runs code quality analysis
6. **Archive Artifact** - Archives JAR files

### Pipeline Configuration

- **Build Timeout**: 15 minutes
- **Build Retention**: Last 10 builds
- **Concurrent Builds**: Disabled
- **SonarQube**: Integrated for code quality

## Circuit Breaker

### Why We Need Circuit Breaker

In a microservices architecture, services depend on each other through network calls. 
When an external service (like the Patient Query Service) becomes unavailable, slow, or experiences high latency,
it can cause cascading failures throughout the system:

- **Resource Exhaustion**: Threads get blocked waiting for unresponsive services, leading to thread pool exhaustion
- **Cascading Failures**: One service's failure can propagate to dependent services, causing system-wide outages
- **Poor User Experience**: Users experience long timeouts and errors when services are degraded
- **Increased Load**: Retrying failed requests puts additional load on already struggling services

The Circuit Breaker pattern prevents these issues by:
- **Failing Fast**: When a service is known to be down, calls fail immediately instead of waiting for timeouts
- **Load Reduction**: Prevents overwhelming a struggling service with requests during outages
- **Automatic Recovery**: Allows the system to detect when the service recovers and resume normal operations
- **Graceful Degradation**: Enables fallback mechanisms to provide alternative responses when services are unavailable

### Circuit Breaker Implementation

This application uses **Resilience4j** (version 2.2.0) to implement the Circuit Breaker pattern for the Patient Query Service integration.

#### Configuration

The circuit breaker is configured in `application.yaml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      patientQueryService:
        register-health-indicator: true
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
```

#### Configuration Parameters Explained

- **register-health-indicator: true** - Exposes circuit breaker state in Spring Boot Actuator health endpoint
- **sliding-window-type: COUNT_BASED** - Uses call count (not time-based) for the sliding window
- **sliding-window-size: 10** - Tracks the last 10 calls to determine failure rate
- **minimum-number-of-calls: 5** - Requires at least 5 calls before calculating failure rate
- **failure-rate-threshold: 50** - Circuit opens if 50% of calls fail
- **wait-duration-in-open-state: 10s** - Waits 10 seconds before transitioning from OPEN to HALF_OPEN
- **permitted-number-of-calls-in-half-open-state: 3** - Allows 3 test calls in HALF_OPEN state
- **automatic-transition-from-open-to-half-open-enabled: true** - Automatically transitions to HALF_OPEN after wait duration

#### Circuit Breaker States

The circuit breaker operates in three states:

1. **CLOSED (Normal Operation)**
   - All requests pass through to the external service
   - Success/failure rates are tracked in the sliding window
   - If failure rate exceeds threshold (50%), circuit transitions to OPEN

2. **OPEN (Circuit Tripped)**
   - All requests are blocked from reaching the external service
   - Fallback method is called immediately for all requests
   - No actual calls are made to the external service
   - After wait duration (10s), transitions to HALF_OPEN

3. **HALF_OPEN (Recovery Testing)**
   - Limited number of requests (3) are allowed through to test if service has recovered
   - If these test calls succeed, circuit transitions back to CLOSED
   - If test calls fail, circuit transitions back to OPEN

#### Implementation Details

The circuit breaker is implemented in `PatientQueryClient.java`:

```java
@CircuitBreaker(
    name = CIRCUIT_BREAKER_PATIENT_QUERY_SERVICE,
    fallbackMethod = "getPatientByIdFallback"
)
public PatientInfoResponse getPatientById(Long patientId) {
    // Calls Patient Query Service
}

public PatientInfoResponse getPatientByIdFallback(Long patientId, Throwable exception) {
    // Fallback logic when circuit is open or service fails
    throw new PatientServiceUnavailableException(
        "Patient validation service is temporarily unavailable. Please try again later."
    );
}
```

#### Fallback Mechanism

When the circuit breaker is OPEN or when the external service fails:
- The `getPatientByIdFallback` method is automatically called
- It throws a `PatientServiceUnavailableException` with a user-friendly message
- This prevents the application from hanging and provides immediate feedback to users
- The exception is caught by the global exception handler and returned as a 503 Service Unavailable response

#### Monitoring Circuit Breaker State

Monitor the circuit breaker through Actuator endpoints:

```bash
# Health check includes circuit breaker state
curl http://localhost:8084/actuator/health

# Circuit breaker specific metrics
curl http://localhost:8084/actuator/metrics/resilience4j.circuitbreaker.state
```

#### Benefits in This Application

1. **Patient Service Protection**: Prevents cascading failures when Patient Query Service is down
2. **Better User Experience**: Users get immediate feedback instead of long timeouts
3. **Resource Conservation**: Frees up threads and resources during service outages
4. **Automatic Recovery**: System automatically detects when Patient Query Service recovers
5. **Observability**: Circuit breaker state is exposed through health endpoints for monitoring

## Monitoring & Observability

### Actuator Endpoints

- **Health**: `/actuator/health` - Application health status
- **Info**: `/actuator/info` - Application information
- **Metrics**: `/actuator/metrics` - Application metrics

### Logging

- **Framework**: SLF4J with Logback
- **Level**: DEBUG for application package
- **Structured Logging**: MDC includes traceId, patientId, doctorName
- **Trace Correlation**: UUID-based trace IDs for distributed tracing

### Circuit Breaker Metrics

Circuit breaker state and metrics are available through Actuator endpoints when the circuit is triggered.

## Error Handling

### Global Exception Handler

Centralized exception handling with `GlobalExceptionHandler` provides consistent error responses:

- **AppointmentBookingException** - Business validation errors
- **PatientServiceUnavailableException** - External service failures
- **Generic exceptions** - Unexpected errors

### Circuit Breaker Fallback

When Patient Query Service is unavailable:
- Circuit breaker opens after threshold
- Fallback method returns graceful error message
- Automatic recovery after wait duration

## Development

### Code Structure

```
src/main/java/com/healthcare/appointment/
├── AppointmentServiceApplication.java
├── client/
│   └── PatientQueryClient.java
├── config/
│   ├── KafkaTopicConfig.java
│   └── PatientQueryServiceProperties.java
│   └── RestClientConfig.java
├── controller/
│   └── AppointmentController.java
├── dto/
│   └── PatientInfoResponse.java
├── entity/
│   └── Appointment.java
├── exception/
│   ├── ApiErrorResponse.java
│   ├── AppointmentBookingException.java
│   ├── GlobalExceptionHandler.java
│   └── PatientServiceUnavailableException.java
├── kafka/
│   └── AppointmentEventProducer.java
├── mapper/
│   └── AppointmentMapper.java
├── repository/
│   └── AppointmentRepository.java
└── service/
    ├── AppointmentService.java
    └── imp/
        └── AppointmentServiceImpl.java
```

### Testing

Run tests with:
```bash
mvn test
```

## License

[Specify your license here]

## Contact

[Specify contact information here]
