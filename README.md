# Etadoor - Online Door Marketplace

[![Changelog](https://img.shields.io/badge/changelog-View%20Recent%20Changes-blue.svg)](CHANGELOG.md)

## Overview
Etadoor is a modern e-commerce platform specialized in door sales, connecting buyers with sellers in a seamless marketplace environment. The platform enables users to browse, purchase doors, leave reviews, and even become sellers themselves.

## Features

### For Buyers
- Browse extensive door catalog with detailed specifications
- Advanced search and filtering options
- Secure checkout process
- Review system for purchased products
- User profile management
- Order tracking and history

### For Sellers
- Seller account registration and verification
- Product listing management
- Order management dashboard
- Sales analytics and reporting
- Inventory management

## Technology Stack

### Backend
- Java Spring Boot
- Spring Security for authentication
- Spring Data JPA for data persistence
- PostgreSQL database

### Security
- JWT based authentication
- Role-based access control (RBAC)
- Password encryption
- Secure API endpoints

## Prerequisites
- JDK 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+
- Git

## Installation and Setup

1. Clone the repository
```bash
git clone https://github.com/neKamita/etadoor.git
cd etadoor
```

2. Configure PostgreSQL
```bash
# Create database
createdb etadoor

# Update application.properties with your database credentials
spring.datasource.url=jdbc:postgresql://localhost:5432/etadoor
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build the project
```bash
mvn clean install
```

4. Run the application
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## API Documentation
Once the application is running, access the Swagger UI documentation at:
`http://localhost:8080/swagger-ui.html`

## Database Schema

Main entities:
- Users (Buyers/Sellers)
- Products (Doors)
- Orders
- Reviews
- Categories
- Cart Items

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE.md file for details

## Contact
Project Link: https://github.com/neKamita/etadoor

## Acknowledgments
- Spring Boot Documentation
- PostgreSQL Documentation
- Maven Documentation

## Recent Changes
See our [CHANGELOG](CHANGELOG.md) for detailed version history and updates.
