# Overview

A REST API for managing payment cards and retrieving exchange rates from third-party APIs.

## Tech Stack

- Java 21
- Spring Boot 4.x with Spring Data JPA
- SQL Server (MS SQL) for database
- Liquibase for database migrations
- Resilience4j Circuit Breaker for fault tolerance
- Lombok for code generation
- JUnit 5 and Mockito for testing
- Gradle as build tool

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Gradle 7+
- SQL Server database running and accessible

### Installation Steps

1. Clone and navigate to the project
2. Install mssql server & configure the database connection in application.yml
3. Add Exchange/ Use existing temporary key Rate API key in application.yml
4. Build the project
```bash
./gradlew clean build
```

5. Run the application
```bash
./gradlew bootRun
```

6. Use the postman collection to test APIs in the root directory
```
/postman-collection.json
```

7. The server starts on
```
http://localhost:8080
```

## API Endpoints

### Cards Management
- POST /api/v1/cards - Create a new card
- GET /api/v1/cards/{id} - Get card by ID
- GET /api/v1/cards?page=0&size=10 - Get cards with pagination
- PUT /api/v1/cards/{id} - Update card details

### Exchange Rates
- GET /api/v1/exchange-rates?baseCurrency=USD - Get exchange rates


## Future Improvements Required

- Complete sensitive data masking utility implementation for request/response bodies
- Add API authentication using JWT tokens
- Secure APIs with spring security
- Improve the current implementation with a comprehensive error handling with custom exception messages
- Add integration tests for third-party API interactions
- Performance optimization for paginated queries
- Implement backend caching
- Add API documentation with Swagger/OpenAPI
- Implement health check endpoints for monitoring
- Add metrics collection for API performance tracking
- Cover Unit test coverage at least 80%




    

