# Etadoor - Online Door Marketplace

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GraphQL](https://img.shields.io/badge/GraphQL-Enabled-e10098.svg)](https://graphql.org)
[![Version](https://img.shields.io/badge/version-1.0.5-blue.svg)](CHANGELOG.md)

## Overview
Etadoor is a door marketplace platform built with Spring Boot, offering both REST and GraphQL APIs. The platform allows users to browse and configure custom doors with various specifications.

## Key Features

### Core Functionality
- Door catalog with customization options
- Dynamic price calculation system
- User authentication with JWT
- Role-based access control (USER, ADMIN, SELLER)
- GraphQL API support

### Technical Features
- REST API with Swagger documentation
- GraphQL API with GraphiQL interface
- JWT-based authentication
- Name-based user identification
- PostgreSQL database integration

## Technology Stack
- Java Spring Boot 3.4.1
- Spring Security with JWT
- Spring GraphQL
- Spring Data JPA
- PostgreSQL
- Swagger UI
- Docker

## Getting Started

### Prerequisites
- JDK 17+
- PostgreSQL 12+
- Maven 3.6+
- Docker

### Setup
1. Clone the repository
```bash
git clone https://github.com/neKamita/G45-Project.git
cd etadoor
```

2. Configure database connection in `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/etadoor
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

4. Docker Setup
```bash
docker build -t etadoor:latest . 
docker run -p 8080:8080 etadoor:latest

```

## API Documentation
- REST API: `/swagger-uI/index.html`
- GraphQL API: `/graphiql`
- Render Web App: `/https://g45-project.onrender.com`

### GraphQL Example Queries
```graphql
# Get door details
query GetDoor {
  door(id: 1) {
    id
    name
    size
    color
    finalPrice
  }
}

# Configure door
mutation ConfigureDoor {
  configureDoor(input: {
    id: 1
    size: CUSTOM
    color: BLACK
    width: 250
    height: 220
  }) {
    id
    finalPrice
  }
}
```

## Project Structure
- `src/main/java/uz/pdp/controller` - REST and GraphQL controllers
- `src/main/java/uz/pdp/entity` - Domain entities
- `src/main/java/uz/pdp/service` - Business logic
- `src/main/java/uz/pdp/config` - Configuration classes
- `src/main/resources/graphql` - GraphQL schema
- `src/test/java/uz/pdp/service` - Unit tests for services

## Testing
The project includes comprehensive unit tests for core services:

### AuthService Tests
- Sign-in functionality
- Sign-up process
- Authentication validation
- JWT token generation

### DoorService Tests
- CRUD operations (Create, Read, Update, Delete)
- Door configuration
- Custom size handling
- Error scenarios

To run the tests:
```bash
./mvnw test
```

## Recent Updates
See [CHANGELOG.md](CHANGELOG.md) for detailed version history.


## Contact
For support or queries, please open an issue in the repository.
