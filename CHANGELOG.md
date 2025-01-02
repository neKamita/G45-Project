# Etadoor Changelog

All notable changes to this project will be documented in this file.

<details open>
<summary><strong>Version 1.0.2 (2024-01-02)</strong></summary>

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
<summary><strong>Version 1.0.1 (2024-01-02)</strong></summary>

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