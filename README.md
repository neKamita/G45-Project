# Etadoor - Online Door Marketplace

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GraphQL](https://img.shields.io/badge/GraphQL-Enabled-e10098.svg)](https://graphql.org)
[![Version](https://img.shields.io/badge/version-1.2.5-blue.svg)](CHANGELOG.md)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36.svg)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-Passing-success.svg)](README.md#testing)

## Overview
Etadoor is a comprehensive door marketplace platform built with Spring Boot, offering both REST and GraphQL APIs. The platform allows users to browse, configure, and purchase custom doors, accessories, and mouldings with advanced management features.

## Key Features

### Core Functionality
- Door catalog with advanced customization options
  - Color variants and custom color support
  - Size customization with dynamic pricing
  - Multiple image support with AWS S3 integration
- Door Accessories Management
  - Furniture and hardware options
  - Category-based organization
- Moulding System
  - Custom moulding configurations
  - Integrated pricing calculator
- Storage and Inventory Management
  - Multiple storage location support
  - Nearest storage finder
- Dynamic price calculation system
- Comprehensive contact management
  - Address geolocation support
  - Interactive map integration
- Advanced basket and checkout system
  - Multi-item checkout support
  - Checkout history tracking
- User authentication with JWT
- Role-based access control (USER, ADMIN, SELLER)

### Technical Features
- REST API with Swagger documentation
- GraphQL API with GraphiQL interface
- Redis Cloud caching integration
- AWS S3 for image storage
- Standardized EntityResponse pattern
- JWT-based authentication
- Name-based user identification
- PostgreSQL database integration
- Comprehensive test coverage
- Docker containerization

## Technology Stack
- Java Spring Boot 3.2.1
- Spring Security with JWT
- Spring GraphQL
- Spring Data JPA
- PostgreSQL
- Redis Cloud
- AWS S3
- Swagger UI
- Docker

## Getting Started

### Prerequisites
- JDK 17+
- PostgreSQL 12+
- Maven 3.6+
- Docker
- Redis (local or cloud)
- AWS Account (for S3)

### Setup
1. Clone the repository
```bash
git clone https://github.com/neKamita/G45-Project.git
cd etadoor
```

2. Configure environment variables
```properties
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/etadoor
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

# Redis Configuration
REDIS_HOST=your_redis_host
REDIS_PORT=your_redis_port
REDIS_PASSWORD=your_redis_password

# AWS Configuration
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_BUCKET_NAME=your_s3_bucket_name

# Email Configuration
MAIL_HOST=your_smtp_host
MAIL_PORT=your_smtp_port
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_email_password
```

3. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

4. Docker Setup
```bash
docker build -t etadoor:latest .
docker run -p 8080:8080 --env-file .env etadoor:latest
```

### Redis Setup

#### Local Setup (Arch Linux)
1. Install Redis:
```bash
sudo pacman -S redis
```
2. Start Redis:git
```bash
sudo systemctl start redis
sudo systemctl enable redis
```
3. Verify Redis:
```bash
redis-cli ping # SHOULD RETURN PONG
```

#### Windows Setup:
1. Download Redis for Windows from [GitHub Releases](https://github.com/microsoftarchive/redis/releases)
2. Install and start Redis service
3. Verify installation:
```cmd
redis-cli ping # SHOULD RETURN PONG
```

### Redis Cloud Setup (Production)
- Database: cache-M5NOIOBN
- Endpoint: redis-15073.crce175.eu-north-1-1.ec2.redns.redis-cloud.com:15073
- Password: [Configure in environment variables]

## API Documentation
- REST API: https://etadoor.koyeb.app/swagger-ui/index.html
- GraphQL API: https://etadoor.koyeb.app/graphiql

### REST API Examples

#### Door Management
```http
# Create Door
POST /api/doors
Content-Type: application/json

{
  "name": "Classic Wood Door",
  "material": "WOOD",
  "color": "BROWN",
  "size": "STANDARD"
}

# Add Color Variant
POST /api/doors/{id}/colors
Content-Type: application/json

{
  "color": "BLACK",
  "price": 299.99
}
```

#### Door Accessories
```http
# Create Accessory
POST /api/accessories
Content-Type: application/json

{
  "name": "Premium Handle",
  "type": "HANDLE",
  "price": 49.99
}
```

### GraphQL Examples
```graphql
# Get door with variants
query GetDoorWithVariants {
  door(id: 1) {
    id
    name
    size
    color
    variants {
      color
      price
    }
    finalPrice
  }
}

# Configure door with accessories
mutation ConfigureDoorWithAccessories {
  configureDoor(input: {
    id: 1
    size: CUSTOM
    color: BLACK
    width: 250
    height: 220
    accessories: [1, 2]
  }) {
    id
    finalPrice
    accessories {
      id
      name
      price
    }
  }
}
```

## Project Structure
```
src/main/java/uz/pdp/
├── config/          # Configuration classes
├── controller/      # REST and GraphQL controllers
├── dto/            # Data Transfer Objects
├── entity/         # Domain entities
├── enums/          # Enumerations
├── exception/      # Custom exceptions
├── mapper/         # Object mappers
├── repository/     # Data access layer
├── security/       # Security configurations
└── service/        # Business logic
```

### Key Components
- `controller/`: REST endpoints and GraphQL resolvers
  - DoorController: Door management
  - DoorAccessoryController: Accessories management
  - StorageController: Storage management
  - BasketController: Shopping operations
  - ContactController: Address management
- `service/`: Business logic implementation
  - DoorService: Door operations and variants
  - StorageService: Inventory management
  - BasketService: Shopping cart operations
  - ImageStorageService: AWS S3 integration

## Environment Variables
```env
# Database Configuration
DATABASE_URL=jdbc:postgresql://dpg-ctrrlibqf0us73djv36g-a.oregon-postgres.render.com:5432/g45_project
DATABASE_USERNAME=g45_project_user
DATABASE_PASSWORD=[Secured]

# Redis Configuration
REDIS_HOST=redis-15073.crce175.eu-north-1-1.ec2.redns.redis-cloud.com
REDIS_PORT=15073
REDIS_PASSWORD=[Secured]

# AWS Configuration
AWS_ACCESS_KEY=[Secured]
AWS_SECRET_KEY=[Secured]
AWS_BUCKET_NAME=etadoor-images

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=[Secured]
MAIL_PASSWORD=[Secured]

# Application URL
APP_URL=https://etadoor.koyeb.app
```

## Recent Updates
See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

## Contact
For support or queries, please open an issue in the repository.
