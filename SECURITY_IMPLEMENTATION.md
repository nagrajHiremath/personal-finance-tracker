# Role-Based Security Implementation Guide

## Overview
This document describes the production-level role-based security implementation for the Finance Tracker API with JWT authentication, proper HTTP exception handling (401/403), and role-based access control.

## Architecture Components

### 1. User Roles
Two user roles are implemented:
- **USER**: Regular user with basic permissions (can manage own transactions)
- **ADMIN**: Administrator with full system permissions

User roles are defined in `UserRole` enum and stored in the database.

### 2. Authentication Flow

```
1. User Registration
   └─> POST /api/users/register
       └─> UserService.createUser()
           └─> Save to UserEntity with role

2. User Login
   └─> POST /api/users/login
       └─> AuthenticationManager validates credentials
       └─> JwtService.generateToken() with role
       └─> Return LoginResponse with JWT token

3. Token Validation
   └─> JwtFilter intercepts request
       └─> Extract token from Authorization header
       └─> JwtService.validateToken()
       └─> Set SecurityContext with user roles
```

## Key Files and Changes

### 1. Entity Layer

#### UserEntity (Enhanced)
**Location**: `src/main/java/com/finance/tracker/entity/UserEntity.java`

Changes:
- Added `UserRole` enum field (ROLE_USER, ROLE_ADMIN)
- Added `isActive` field for user account status
- Added `updatedAt` field for audit trails
- Added `@PrePersist` and `@PreUpdate` lifecycle hooks

```java
@Enumerated(EnumType.STRING)
@Column(name = "role", nullable = false)
private UserRole role = UserRole.USER;

@Column(name = "is_active", nullable = false)
private Boolean isActive = true;
```

#### UserRole Enum
**Location**: `src/main/java/com/finance/tracker/entity/enums/UserRole.java`

```java
public enum UserRole implements GrantedAuthority {
    USER("ROLE_USER", "Regular user with basic permissions"),
    ADMIN("ROLE_ADMIN", "Administrator with full permissions");
    
    @Override
    public String getAuthority() {
        return this.authority;
    }
}
```

### 2. DTO Layer

#### UserRequest (Enhanced)
**Location**: `src/main/java/com/finance/tracker/dto/UserRequest.java`

New fields:
- Added `role` field (defaults to USER)
- Added validation annotations
- Added Lombok builders

```java
@Builder.Default
private UserRole role = UserRole.USER;
```

#### LoginRequest (Enhanced)
**Location**: `src/main/java/com/finance/tracker/dto/LoginRequest.java`

Changes:
- Added validation annotations (@NotBlank, @Email)
- Added Lombok builders

#### LoginResponse (New)
**Location**: `src/main/java/com/finance/tracker/dto/LoginResponse.java`

```java
@Data
@Builder
public class LoginResponse {
    private String accessToken;      // JWT token
    private String tokenType;        // "Bearer"
    private Long userId;
    private String email;
    private String name;
    private String role;             // User's role (ROLE_USER/ROLE_ADMIN)
    private Long expiresIn;          // Token expiration in seconds
}
```

### 3. Exception Layer

#### AuthenticationException (New)
**Location**: `src/main/java/com/finance/tracker/exception/AuthenticationException.java`

- Extends `CommonServiceException`
- HTTP Status: **401 Unauthorized**
- Error Code: `0x0401`
- Thrown on: Invalid credentials, missing token, expired token

```java
throw new AuthenticationException("User not found with email: " + email);
```

#### AuthorizationException (New)
**Location**: `src/main/java/com/finance/tracker/exception/AuthorizationException.java`

- Extends `CommonServiceException`
- HTTP Status: **403 Forbidden**
- Error Code: `0x0403`
- Thrown on: Insufficient permissions/roles

```java
throw new AuthorizationException("Access denied: Admin role required");
```

#### GlobalExceptionHandler (Enhanced)
**Location**: `src/main/java/com/finance/tracker/exception/GlobalExceptionHandler.java`

New exception handlers:
- `handleAuthenticationException()` → 401 Unauthorized
- `handleAuthorizationException()` → 403 Forbidden
- `handleAccessDeniedException()` → 403 Forbidden (Spring Security)

### 4. Service Layer

#### UserService (Enhanced)
**Location**: `src/main/java/com/finance/tracker/service/UserService.java`

New methods:
- `createUser(UserRequest)` - Creates user with role (defaults to USER)
- `getUserByEmail(String)` - Retrieves user by email
- `validateCredentials(String, String)` - Validates login credentials
- `getUserById(Long)` - Retrieves user by ID

All methods throw `AuthenticationException` for 401 responses.

```java
@Transactional
public UserEntity createUser(UserRequest userRequest) throws CommonServiceException {
    // Creates user with role assignment
    user.setRole(userRequest.getRole() != null ? 
                 userRequest.getRole() : UserRole.USER);
    return userRepository.save(user);
}
```

#### JwtService (Enhanced)
**Location**: `src/main/java/com/finance/tracker/service/JwtService.java`

New features:
- Token now includes `userId` and `role` claims
- Proper exception handling for token validation
- Expiration time configurable via `application.yaml`

Methods:
- `generateToken(email, userId, role)` - Generates JWT with role
- `extractUsername(token)` - Extracts email from token (throws 401 on invalid)
- `extractUserId(token)` - Extracts user ID from token
- `extractRole(token)` - Extracts role from token
- `validateToken(token)` - Validates token (throws 401 on invalid/expired)

```java
public String generateToken(String userEmail, Long userId, UserRole role) {
    return Jwts.builder()
            .setSubject(userEmail)
            .claim("userId", userId)
            .claim("role", role.getAuthority())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS256, key)
            .compact();
}
```

### 5. Filter Layer

#### JwtFilter (Enhanced)
**Location**: `src/main/java/com/finance/tracker/util/JwtFilter.java`

Changes:
- Extracts both username and role from token
- Converts role string to `UserRole` enum
- Sets authorities in `SecurityContext` with roles
- Proper error handling with logging

```java
private void processJwtToken(String token) {
    String username = jwtService.extractUsername(token);
    String roleString = jwtService.extractRole(token);
    UserRole role = UserRole.fromAuthority(roleString);
    
    // Create auth token with role authorities
    List<GrantedAuthority> authorities = Collections.singletonList(role);
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(username, null, authorities);
    
    SecurityContextHolder.getContext().setAuthentication(auth);
}
```

### 6. Security Configuration

#### SecurityConfig (Enhanced)
**Location**: `src/main/java/com/finance/tracker/config/SecurityConfig.java`

Key features:
- **@EnableMethodSecurity** for method-level authorization (@PreAuthorize, @Secured)
- **Stateless sessions** for REST APIs (JWT only)
- **CORS enabled** for frontend integration
- **Endpoint-based authorization**:
  - Public: `/api/users/register`, `/api/users/login`
  - User role: `/api/transactions/**`
  - Admin role: `/api/admin/**`
- **Exception handling**:
  - 401 Unauthorized: `authenticationEntryPoint`
  - 403 Forbidden: `accessDeniedHandler`

```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/api/users/register", "/api/users/login").permitAll()
    .requestMatchers("/api/transactions/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
.exceptionHandling()
    .authenticationEntryPoint((request, response, authException) -> {
        response.setStatus(401);
        // ... write JSON error
    })
    .accessDeniedHandler((request, response, accessDeniedException) -> {
        response.setStatus(403);
        // ... write JSON error
    })
```

### 7. Controller Layer

#### UserController (Enhanced)
**Location**: `src/main/java/com/finance/tracker/controller/UserController.java`

Changes:
- `/register` endpoint returns HTTP 201 Created
- `/login` endpoint returns `LoginResponse` with token and user details
- Both endpoints include proper error handling
- Added logging for audit trails

```java
@PostMapping("/register")
public ResponseEntity<BaseResponse<String>> registerUser(
        @RequestBody @Valid UserRequest userRequest) 
        throws CommonServiceException {
    UserEntity user = userService.createUser(userRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.<String>builder()
                    .success(true)
                    .message("User successfully registered with role: " + 
                            user.getRole().getAuthority())
                    .build());
}

@PostMapping("/login")
public ResponseEntity<BaseResponse<LoginResponse>> login(
        @RequestBody @Valid LoginRequest loginRequest) 
        throws AuthenticationException {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            )
    );
    
    UserEntity user = userService.getUserByEmail(loginRequest.getEmail());
    String accessToken = jwtService.generateToken(
            user.getEmail(),
            user.getUserId(),
            user.getRole()
    );
    
    LoginResponse response = LoginResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .userId(user.getUserId())
            .email(user.getEmail())
            .role(user.getRole().getAuthority())
            .expiresIn(jwtService.getJwtExpiration() / 1000)
            .build();
    
    return ResponseEntity.ok(BaseResponse.<LoginResponse>builder()
            .success(true)
            .message("Login successful")
            .payload(response)
            .build());
}
```

## Usage Examples

### 1. User Registration

**Request**:
```http
POST /api/users/register HTTP/1.1
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "role": "USER"
}
```

**Success Response (201 Created)**:
```json
{
    "success": true,
    "timestamp": 1704067200000,
    "message": "User successfully registered with role: ROLE_USER"
}
```

**Error Response (409 Conflict)**:
```json
{
    "success": false,
    "timestamp": 1704067200000,
    "message": "User already exists with this email"
}
```

### 2. User Login

**Request**:
```http
POST /api/users/login HTTP/1.1
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "SecurePass123"
}
```

**Success Response (200 OK)**:
```json
{
    "success": true,
    "timestamp": 1704067200000,
    "message": "Login successful",
    "payload": {
        "access_token": "eyJhbGciOiJIUzI1NiJ9...",
        "token_type": "Bearer",
        "user_id": 1,
        "email": "john@example.com",
        "name": "John Doe",
        "role": "ROLE_USER",
        "expires_in": 3600
    }
}
```

**Error Response (401 Unauthorized)**:
```json
{
    "success": false,
    "timestamp": 1704067200000,
    "message": "Invalid email or password"
}
```

### 3. Authenticated Request (with role-based access)

**Request with valid token**:
```http
GET /api/transactions/filter HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response (200 OK)**:
```json
[
    {
        "transactionId": 1,
        "amount": 1000.00,
        ...
    }
]
```

**Error Response - Missing token (401 Unauthorized)**:
```json
{
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource"
}
```

**Error Response - Invalid token (401 Unauthorized)**:
```json
{
    "success": false,
    "timestamp": 1704067200000,
    "message": "Invalid token"
}
```

**Error Response - Insufficient role (403 Forbidden)**:
```json
{
    "error": "Forbidden",
    "message": "Access denied: Admin role required"
}
```

## Role-Based Access Control Examples

### Using @PreAuthorize in Controllers

```java
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<UserEntity>> getAllUsers() {
    // Only ADMIN users can access
}

@GetMapping("/transactions/{id}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
    // Both USER and ADMIN can access
}

@DeleteMapping("/admin/users/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<BaseResponse<String>> deleteUser(@PathVariable Long id) {
    // Only ADMIN users can access
}
```

### Using @Secured in Controllers

```java
@PostMapping("/admin/settings")
@Secured("ROLE_ADMIN")
public ResponseEntity<BaseResponse<String>> updateSettings(@RequestBody SettingsRequest request) {
    // Only ROLE_ADMIN can access
}
```

## Configuration

### application.yaml
```yaml
# JWT Configuration
jwt:
  expiration: 3600000  # Token expiration in milliseconds (1 hour)
```

## Security Best Practices Implemented

1. ✅ **Password Encryption**: BCrypt hashing for password storage
2. ✅ **JWT Tokens**: Stateless authentication using JWT with signature verification
3. ✅ **Role-Based Access Control**: Role-based authorization for endpoints
4. ✅ **Proper HTTP Status Codes**: 401 for auth failures, 403 for authorization failures
5. ✅ **Token Expiration**: Tokens expire after configured time
6. ✅ **Exception Handling**: Centralized exception handling with appropriate responses
7. ✅ **CSRF Protection**: CSRF disabled for stateless REST API
8. ✅ **Session Management**: Stateless sessions (no cookie-based sessions)
9. ✅ **Audit Logging**: Request logging for security monitoring
10. ✅ **Validation**: Input validation on all user inputs

## Database Schema Update

Run the following SQL to add new columns to existing `users` table:

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER' AFTER password;
ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true AFTER role;
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;
```

Or if using JPA with `ddl-auto: update`, the schema will be automatically updated on application startup.

## Testing the API

### Register User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"TestPass123","role":"USER"}'
```

### Login User
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"TestPass123"}'
```

### Access Protected Endpoint with Token
```bash
curl -X GET http://localhost:8080/api/transactions/filter \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Troubleshooting

### Token Expired (401)
- Solution: Request a new token via login endpoint
- Check JWT expiration time in `application.yaml`

### Invalid Token (401)
- Solution: Ensure token format is "Bearer {token}"
- Check token hasn't been modified

### Access Denied (403)
- Solution: Verify user has required role
- Check `SecurityConfig` for endpoint authorization rules

### User Not Found (401)
- Solution: Verify email address is correct
- Check user exists in database

---

## Next Steps

1. Add role assignment endpoints (admin can assign roles to users)
2. Implement refresh token mechanism
3. Add token blacklisting for logout functionality
4. Implement OAuth2 for third-party integrations
5. Add API key authentication for service-to-service calls
