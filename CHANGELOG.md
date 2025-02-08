# Etadoor Changelog

All notable changes to this project will be documented in this file.

<details>
<summary><strong>Version 1.2.5 (2025-01-22)</strong></summary>

### 🎨 Enhanced
- Added comprehensive door color system with 25+ realistic options
- Organized colors into logical categories (classic, wood tones, modern finishes)
- Added display names for colors to improve UI presentation
- Enhanced seller notification system with beautiful HTML emails

### 📧 Added
- Automatic email notifications to sellers for new door orders
- Detailed HTML email templates with order information
- Non-blocking email notification system
- Comprehensive error handling for email failures

### 💻 Technical
- Integrated order notifications in OrderService
- Enhanced EmailService with new notification methods
- Added proper error handling and logging
- Implemented fail-safe order creation (succeeds even if notification fails)

### 🎯 User Experience
- More intuitive color selection with organized categories
- Professional-looking email notifications
- Better color display names in UI
- Enhanced seller dashboard notifications
</details>

<details>
<summary><strong>Version 1.2.4 (2025-01-21)</strong></summary>

### 🛠 Enhanced
- Improved checkout process reliability with native SQL operations
- Optimized basket item deletion with direct database queries
- Added detailed logging for basket operations
- Enhanced concurrent operation handling

### 🐛 Fixed
- Resolved version conflicts during basket clearing
- Fixed concurrent modification issues in checkout process
- Eliminated race conditions in multi-item basket operations
- Improved error handling for basket item deletions

### 💻 Technical
- Implemented CustomBasketItemRepository for native queries
- Removed Hibernate version checking for basket operations
- Added transaction boundaries for atomic operations
- Enhanced logging for better debugging capabilities

### 📝 Documentation
- Updated repository documentation with clear examples
- Added fun, descriptive comments to new implementations
- Enhanced error messages with user-friendly content
</details>

<details>
<summary><strong>Version 1.2.3 (2025-01-20)</strong></summary>

### Added
- Strategic database indexes for core entities 🚀
- Composite indexes for frequently filtered columns
- Enhanced similar doors search algorithm
- Optimized pagination support for large datasets

### Changed
- Replaced MySQL-specific syntax with JPA standard indexing
- Optimized JPQL queries for better execution plans
- Enhanced query performance with better WHERE clause ordering
- Updated repository documentation with fun comments

### Fixed
- Query validation error in [`DoorRepository`](src/main/java/uz/pdp/repository/DoorRepository.java)
- Similar doors search query syntax issues
- Performance bottlenecks in large result sets
- Database query execution plans

### Technical
- Added unique index for user email lookups
- Added composite indexes for order status and dates
- Added indexes for door material and price filtering
- Improved database query optimization
</details>

<details>
<summary><strong>Version 1.2.2 (2025-01-19)</strong></summary>

### Added
- Multi-image support for furniture doors 📸
- New database tables for storing multiple images per door
- Enhanced image upload endpoint with content type detection
- Improved error handling for image uploads

### Changed
- Split door creation and image upload into separate endpoints
- Updated FurnitureDoor entity to support multiple images
- Enhanced mapper and DTOs with fun, descriptive comments
- Improved error messages with emojis for better UX

### Fixed
- Content type validation for image uploads
- Image URL persistence in database
- Error handling for unsupported media types

### Technical
- Added JPA @ElementCollection for image URLs and filenames
- Enhanced GlobalExceptionHandler with HttpMediaTypeNotSupportedException handling
- Improved image content type detection from file extensions
</details>

<details>
<summary><strong>Version 1.2.1 (2025-01-18)</strong></summary>

### Added
- New endpoint for image deletion
- Improved logging system for authentication failures

### Changed
- Simplified authentication error messages
- Optimized Address and Door repository queries
- Updated door history storage logic
- Made `/api/doors/{id}` endpoint public

### Fixed
- Address repository city search functionality
- Door endpoint authentication issues
- User authentication response format
- PUT request handling for images

### Security
- Standardized authentication error messages
- Improved authentication failure logging
</details>

<details>
<summary><strong>Version 1.2.0 (2025-01-16)</strong></summary>

### Added
- Implemented `DoorHistory` entity to track door access history.
- Added `DoorHistoryRepository` for database operations related to `DoorHistory`.
- Created `DoorHistoryService` to manage door history logic.
- Added `DoorHistory` related methods in `DoorService` to save and retrieve door access history.
- Introduced `DoorDto` for door data transfer object.
- Added `AddressDTO` for address data transfer object.
- Implemented `SellerRequestDto` for seller request data transfer object.
- Added `DoorConfigInput` and `AddressConfigInput` for GraphQL mutations.
- Introduced `Page_Door` and `Page` types in GraphQL schema for paginated door queries.

### Changed
- Updated `Door` entity to include `@PrePersist` and `@PreUpdate` methods for price calculation and status updates.
- Modified `DoorService` to include caching and transactional annotations.
- Enhanced `EmailService` to support HTML email content and improved email validation.
- Updated `UserService` to handle seller request verification and email sending.
- Improved `AuthService` to include detailed validation and error handling for sign-up and sign-in processes.
- Enhanced `AddressService` to provide detailed logging and error handling for address operations.

### Fixed
- Resolved issues with email verification logic in `EmailVerificationRepository`.
- Fixed bugs related to door price calculation and custom size handling in `Door` entity.
- Corrected validation logic in `SignUpRequest` and `SignInRequest` handling in `AuthService`.
- Addressed issues with Redis configuration and caching in `application.yaml`.

### Removed
- Deprecated old email verification methods in `EmailService`.
- Removed unused imports and redundant code across various service classes.
</details>

<details>
<summary><strong>Version 1.1.9 (2025-01-13)</strong></summary>

### 🚀 New Features
- Added GraphQL schema for improved API interactions
- Implemented comprehensive GraphQL query/mutation support
- Added GraphiQL interface at `/graphiql` path

### 🛠 Technical Updates
- Added GraphQL schema with Door, User, and Address types
- Implemented query resolvers for core entities
- Enhanced mutation support for door configuration
- Added support for address geolocation queries
- Added comprehensive GraphQL documentation

### 🔒 Security
- Added GraphQL depth limiting
- Implemented field-level security
- Added authentication checks for GraphQL endpoints

- Added GraphQL query/mutation examples
- Updated API documentation with GraphQL section
- Added GraphiQL usage instructions
</details>

<details>
<summary><strong>Version 1.1.8 (2025-01-12)</strong></summary>

### 🐛 Bug Fixes
- Fixed multiple `@PreUpdate` annotations in Door entity
- Consolidated pre-update logic for door status
- Fixed JPA entity lifecycle callbacks

### 🔒 Security
- Added account deactivation functionality
- Enhanced admin controls for user management
- Added automatic door deactivation for suspended sellers

### 🛠 Technical Updates
- Added account status tracking in [`User`](src/main/java/uz/pdp/entity/User.java)
- Added door status tracking in [`Door`](src/main/java/uz/pdp/entity/Door.java)
- Enhanced [`AdminService`](src/main/java/uz/pdp/service/AdminService.java) with deactivation logic
- Updated [`DoorService`](src/main/java/uz/pdp/service/DoorService.java) to handle inactive doors
- Changed seller request and verification endpoints to use path variables
- Simplified API endpoints for better REST compliance
- Improved API usability and documentation

### 📝 Documentation
- Added account deactivation endpoint documentation
- Updated API documentation with new endpoints
- Added entity lifecycle documentation
</details>

<details>
<summary><strong>Version 1.1.7 (2025-01-12)</strong></summary>

### 🔒 Security
- Added email verification requirement for seller registration
- Enhanced seller approval process with admin-only access
- Implemented rate limiting for verification attempts
- Added Redis-based verification tracking

### 🛠 Technical Updates
- Added [`RedisConfig`](src/main/java/uz/pdp/config/RedisConfig.java) for verification tracking
- Enhanced [`UserService`](src/main/java/uz/pdp/service/UserService.java) with verification logic
- Updated [`AdminService`](src/main/java/uz/pdp/service/AdminService.java) with seller approval flow
- Added verification endpoints in [`UserController`](src/main/java/uz/pdp/controller/UserController.java)
- Implemented rate limiting with Resilience4j

### 📧 Email Features
- Added HTML email templates for verification codes
- Enhanced email service with better formatting
- Improved email delivery tracking and logging

### 🎯 Improvements
- Better verification code handling
- Enhanced security for seller registration
- Improved error messaging for verification
- Added proper rate limiting for security

### 📝 Documentation
- Added seller verification flow documentation
- Updated API documentation with new endpoints
- Added Redis configuration guide
- Enhanced environment variables documentation

</details>

<details>
<summary><strong>Version 1.1.6 (2025-01-12)</strong></summary>

### 🚀 New Features
- Added AWS S3 integration for door image storage
- Implemented secure image upload functionality
- Added support for multiple images per door

### 🛠 Technical Updates
- Added [`ImageStorageService`](src/main/java/uz/pdp/service/ImageStorageService.java) for S3 operations
- Enhanced [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java) with image upload endpoint
- Updated [`DoorService`](src/main/java/uz/pdp/service/DoorService.java) with image handling
- Configured AWS S3 client with proper security settings

### 🔒 Security
- Implemented secure S3 bucket policies
- Added proper IAM user permissions
- Protected S3 credentials using environment variables

### 📝 Documentation
- Added AWS S3 setup instructions
- Updated API documentation with image upload endpoints
- Added environment variables documentation for AWS credentials

</details>

<details>
<summary><strong>Version 1.1.5 (2025-01-11)</strong></summary>

### 🔄 Major Changes
- Standardized response format across all controllers using EntityResponse pattern
- Enhanced error handling and validation in all services
- Comprehensive test coverage for Controllers and Services
- Improved logging and monitoring across application

### 🛠 Technical Updates
- Refactored controllers to use EntityResponse pattern:
  - [`AdminController`](src/main/java/uz/pdp/controller/AdminController.java)
  - [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java)
  - [`ContactController`](src/main/java/uz/pdp/controller/ContactController.java)
  - [`AuthController`](src/main/java/uz/pdp/controller/AuthController.java)
- Added comprehensive test suites:
  - [`DoorControllerTest`](src/test/java/uz/pdp/controller/DoorControllerTest.java)
  - [`AdminServiceTest`](src/test/java/uz/pdp/service/AdminServiceTest.java)
  - [`ContactControllerTest`](src/test/java/uz/pdp/controller/ContactControllerTest.java)
  - [`AuthControllerTest`](src/test/java/uz/pdp/controller/AuthControllerTest.java)
- Enhanced service layer error handling:
  - [`DoorService`](src/main/java/uz/pdp/service/DoorService.java)
  - [`AdminService`](src/main/java/uz/pdp/service/AdminService.java)
  - [`AddressService`](src/main/java/uz/pdp/service/AddressService.java)
- Streamlined DTOs and removed redundant fields
- Added SLF4J logging across all controllers and services

### 🎯 Improvements
- Consistent response format across all API endpoints
- Standardized error handling and messaging
- Better validation error responses
- Enhanced logging for debugging and monitoring
- Increased test coverage for critical components
- Reduced data redundancy in DTOs

</details>

<details>
<summary><strong>Version 1.1.4 (2025-01-10)</strong></summary>

### 🔄 Major Changes
- Standardized all controller responses with EntityResponse pattern
- Enhanced error handling across all services
- Improved authentication response format

### 🛠 Technical Updates
- Refactored [`AuthService`](src/main/java/uz/pdp/service/AuthService.java) with standardized response format
- Updated [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java) with improved error handling
- Enhanced [`ContactController`](src/main/java/uz/pdp/controller/ContactController.java) with consistent response patterns
- Improved logging across all controllers and services

### 🔒 Security
- Enhanced authentication error messages
- Improved token handling in auth responses
- Added better validation error handling

### 📝 Documentation
- Updated API documentation with new response formats
- Added examples for standardized error responses
- Enhanced authentication endpoint documentation

### 🎯 Improvements
- Consistent error handling across all endpoints
- Better logging for debugging and monitoring
- Standardized success/error message format across all controllers
- Enhanced validation error responses
</details>

<details>
<summary><strong>Version 1.1.3 (2025-01-09)</strong></summary>

### 🚀 New Features
- Added Contacts management system with map integration
- Enhanced access control for Door management (Seller-specific)
- Added address management with geolocation support

### 🛠 Technical Updates
- Added [`AddressService`](src/main/java/uz/pdp/service/AddressService.java) for managing contact locations
- Added [`ContactController`](src/main/java/uz/pdp/controller/ContactController.java) for address endpoints
- Updated [`DoorService`](src/main/java/uz/pdp/service/DoorService.java) with seller-specific access control
- Added [`DoorSecurityService`](src/main/java/uz/pdp/service/DoorSecurityService.java) for door access management
- Simplified map point handling by removing redundant marker storage

### 🔒 Security
- Implemented role-based access control for door management
- Added seller-specific permissions for door operations
- Enhanced security configuration in [`MyConf`](src/main/java/uz/pdp/config/MyConf.java)

### 🔄 Changes
- Moved from Render to Koyeb deployment platform
- Updated base URL to etadoor.koyeb.app
- Enhanced documentation with new deployment details

### 📝 Documentation
- Added contact management API documentation
- Updated deployment instructions for Koyeb
- Added new environment variables documentation
- Enhanced API documentation with role-based access examples
</details>

<details>
<summary><strong>Version 1.1.2 (2025-01-08)</strong></summary>

### 🔄 Changes
- Introduced standardized API response format with `EntityResponse`
- Enhanced error handling with global exception management
- Improved response consistency across all endpoints

### 🛠 Technical Updates
- Added [`EntityResponse`](src/main/java/uz/pdp/payload/EntityResponse.java) for unified API responses
- Enhanced [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java) with standardized response format
- Removed redundant exception handlers from controllers
- Updated all REST endpoints to use the new response format

### 📝 Documentation
- Updated API documentation with new response format examples
- Updated Swagger UI documentation

### 🎯 Improvements
- Consistent HTTP status codes across all endpoints
- Standardized success/error message format
- Better client-side error handling support
</details>

<details>
<summary><strong>Version 1.1.1 (2025-01-08)</strong></summary>

### 🔧 Bug Fixes
- Fixed foreign key constraint issue between `doors` and `door_images` tables
- Resolved table naming inconsistency (door → doors)
- Fixed Redis Cloud integration issues with proper connection handling
- Added proper transaction management for door creation with images

### 🚀 Improvements
- Enhanced Redis configuration with RESP3 protocol support
- Added Redis Cloud support for production environment
- Improved error handling in DoorService for cache failures
- Added proper database indexing for door_images
- Implemented automatic final price calculation for doors

### 🔒 Security
- Secured Redis Cloud connection with SSL/TLS
- Moved Redis credentials to environment variables
- Protected sensitive configuration data in documentation

### 📝 Documentation
- Added Redis Cloud setup instructions for production
- Updated deployment guide for render.com
- Added troubleshooting section for common Redis issues
- Updated environment variables documentation

</details>

<details >
<summary><strong>Version 1.1.0 (2025-01-07)</strong></summary>

### Technical Updates
- Refactored the `configureDoor` REST endpoint in [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java) to accept a `DoorConfigInput` DTO instead of individual parameters.
- Added SLF4j logging to the `signIn` and `signUp` methods in [`AuthService`](src/main/java/uz/pdp/service/AuthService.java) for security auditing.
- Added SLF4j logging to various methods in [`DoorService`](src/main/java/uz/pdp/service/DoorService.java) to track data operations.
- Added SLF4j logging to the `signIn` and `signUp` methods in [`AuthController`](src/main/java/uz/pdp/controller/AuthController.java) to track incoming requests.

</details>
<details >
<summary><strong>Version 1.0.9 (2025-01-07)</strong></summary>

### New Features
- Added Redis caching support

### Technical Updates
- Implemented Redis caching for door operations
- Added Redis configuration
- Updated DoorService with caching annotations

### Documentation
- Updated README.md with Redis setup instructions

</details>

<details >
<summary><strong>Version 1.0.8 (2025-01-06)</strong></summary>

### New Features
- Added unit tests for `AuthService` and `DoorService`

### Technical Updates
- Implemented unit tests for `AuthService` covering sign-in and sign-up scenarios
- Implemented unit tests for `DoorService` covering CRUD operations and failure scenarios

### Documentation
- Updated `README.md` with instructions on running unit tests

</details>

<details>
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
- **Added SLF4j logging to [`AuthController`](src/main/java/uz/pdp/controller/AuthController.java), [`AuthService`](src/main/java/uz/pdp/service/AuthService.java), and [`DoorController`](src/main/java/uz/pdp/controller/DoorController.java) for improved monitoring and debugging.**

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

## [Unreleased]

### Changed
- Enhanced email template system for better maintainability
  - Moved HTML email template to EmailService for centralized management
  - Updated EmailService to use a consistent, modern design across all emails
  - Improved separation of concerns between email formatting and content generation

### Added
- Modern, responsive email template with:
  - Gradient headers and clean typography
  - Card-based layout with subtle shadows
  - Color-coded pricing information
  - Interactive hover effects
  - Emoji icons for better visual hierarchy
  - Mobile-friendly design

### Fixed
- Standardized EntityResponse usage in EmailService
- Corrected ItemType enum usage (DOOR_ACCESSORY instead of FURNITURE_ACCESSORY)
- Improved error messages with more context

### Technical Debt
- Consolidated duplicate email styling code
- Enhanced code organization in email-related services
