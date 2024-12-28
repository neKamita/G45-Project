# Etadoor Changelog

All notable changes to this project will be documented in this file.

<details open>
<summary><strong>Version 1.0.0 (2024-28-12)</strong></summary>

### 🚀 New Features
- JWT authentication with Bearer token support
- Swagger UI integration at `/swagger-ui.html`
- Test endpoints for public/user/admin access
- User entity with role-based authorization
- Company entity with social media list support
- Door catalog basic structure
- Basic security configuration in `MyConf`
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