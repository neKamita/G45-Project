# Etadoor Changelog

All notable changes to this project will be documented in this file.
<details open>
<summary><strong>Version 1.0.7 (2025-01-05)</strong></summary>

### Technical Updates
- Updated `application.yaml` to change the base URL to `https://g45-project.onrender.com`
- Updated Swagger configuration to use the new base URL

</details>

<details>
<summary><strong>Version 1.0.6 (2025-01-04)</strong></summary>

### New Features
- Implemented full CRUD operations for the `Door` entity  

### Technical Updates
- Added endpoints for creating, reading, updating, and deleting doors

### Documentation
- Updated `README.md` with new API endpoints for door management

</details>

<details>
<summary><strong>Version 1.0.5 (2025-01-03)</strong></summary>

### 🚀 New Features
- Added Docker support for the application

### 🛠 Technical Updates
- Created a Dockerfile for building and running the application
- Updated `application.yaml` with hardcoded database connection details

### 📝 Documentation
- Updated `README.md` with Docker setup instructions

</details>

<details>
<summary><strong>Version 1.0.4 (2025-01-03)</strong></summary>

### 🚀 New Features
- Added Render PostgreSQL database configuration

### 🛠 Technical Updates
- Updated `application.yaml` with Render database connection details

### 📝 Documentation
- Updated `README.md` with Render PostgreSQL setup instructions

</details>

<details>
<summary><strong>Version 1.0.3 (2024-01-02)</strong></summary>

### 🚀 New Features
- Added GraphQL API support with Spring GraphQL
- Implemented Door entity queries and mutations
- Added GraphiQL UI for testing at `/graphiql`
- Added schema-based type definitions

### 🛠 Technical Updates
- Added GraphQL schema in [schema.graphqls](src/main/resources/graphql/schema.graphqls)
- Enhanced [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java) with GraphQL support
- Implemented query resolvers for door management
- Added mutation support for door configuration

### 📝 Documentation
- Added GraphQL query examples below
- Updated API documentation with GraphQL section

#### GraphQL Query Examples:
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
</details>

<details>
<summary><strong>Version 1.0.2 (2025-01-02)</strong></summary>

### 🔄 Changes
- Simplified [`Door`](src/main/java/uz/pdp/entity/Door.java) entity structure
- Enhanced [`Size`](src/main/java/uz/pdp/enums/Size.java) enum with dimension data
- Added price calculation logic for custom orders
- Improved door configuration system

### 🛠 Technical Updates
- Removed redundant width/height fields from Door entity
- Added standard size definitions in Size enum
- Implemented dynamic price calculation based on customizations
- Optimized door customization workflow
</details>



<details>
<summary><strong>Version 1.0.1 (2025-01-02)</strong></summary>

### 🔄 Changes
- Switched from username to name-based authentication in [`User`](src/main/java/uz/pdp/entity/User.java)
- Added lastname field support in user registration
- Implemented [`CustomUserDetailsService`](src/main/java/uz/pdp/config/CustomUserDetailsService.java) for database authentication
- Enhanced JWT token generation to use name instead of username

### 🐛 Bug Fixes
- Fixed userRepository null pointer exception in CustomUserDetailsService
- Resolved JWT token validation with name-based lookup
- Fixed user authentication flow in [`AuthService`](src/main/java/uz/pdp/service/AuthService.java)

### 🔒 Security
- Enhanced user lookup security with proper name-based queries
- Improved token generation with proper user identification
</details>

<details>
<summary><strong>Version 1.0.0 (2023-12-28)</strong></summary>

### 🚀 New Features
- JWT authentication with Bearer token support
- Swagger UI integration at `/swagger-ui.html`
- Test endpoints for public/user/admin access
- User entity with role-based authorization
- Company entity with social media list support
- Door catalog basic structure
- Basic security configuration in [`MyConf`](src/main/java/uz/pdp/config/MyConf.java)
- PostgreSQL database integration

### 🔄 Changes
- Updated security configuration to use JWT instead of basic auth
- Modified User entity to implement UserDetails
- Enhanced Company entity with proper JPA mappings
- Improved filter chain configuration

### 🐛 Bug Fixes
- Company social media list persistence issue
- JWT token validation in MyFilter
- Role-based access control
- User authentication provider configuration

### 🔒 Security
- Added JWT token authentication
- Implemented role-based authorization (USER, ADMIN)
- Secured endpoints with proper authentication
- Added CORS configuration
</details>