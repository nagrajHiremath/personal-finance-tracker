# Role-Based Security System - Quick Start Guide

## Summary of Implementation

Your Finance Tracker API now has production-level role-based security with proper HTTP exception handling. Here's what was implemented:

### ✅ What's New

1. **Two User Roles**
   - `USER` - Regular users (access transactions)
   - `ADMIN` - Administrators (full system access)

2. **Proper HTTP Status Codes**
   - `401 Unauthorized` - Authentication fails
   - `403 Forbidden` - User lacks permissions
   - `201 Created` - User registration successful
   - `200 OK` - Login successful

3. **JWT Tokens with Role Information**
   - Tokens now include user ID and role
   - 1 hour expiration (configurable)
   - Automatic validation on every request

4. **Database Changes**
   - `UserEntity` now has `role`, `isActive`, and `updatedAt` fields
   - Run application and JPA will auto-create columns

---

## API Endpoints

### 1. Register User (Public)
```bash
POST /api/users/register
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "role": "USER"
}
```

**Response (201 Created)**:
```json
{
    "success": true,
    "timestamp": 1704067200000,
    "message": "User successfully registered with role: ROLE_USER"
}
```

### 2. Login (Public)
```bash
POST /api/users/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "SecurePass123"
}
```

**Response (200 OK)**:
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

### 3. Access Protected Endpoints
```bash
GET /api/transactions/filter
Authorization: Bearer <ACCESS_TOKEN>
```

---

## Using Roles in Your Controllers

### Example 1: USER and ADMIN Can Access
```java
@GetMapping("/transactions/{id}")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
    // Both USER and ADMIN can access
    return ResponseEntity.ok(transactionService.getTransaction(id));
}
```

### Example 2: ADMIN Only
```java
@DeleteMapping("/admin/users/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<BaseResponse<String>> deleteUser(@PathVariable Long id) {
    // Only ADMIN can access
    return ResponseEntity.ok(new BaseResponse<>(...));
}
```

### Example 3: Using @Secured
```java
@PostMapping("/admin/settings")
@Secured("ROLE_ADMIN")
public ResponseEntity<BaseResponse<String>> updateSettings(@RequestBody SettingsRequest request) {
    // Only ROLE_ADMIN
    return ResponseEntity.ok(...);
}
```

---

## Database Schema

The following columns were added to the `users` table (auto-created by JPA):

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER';
ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
```

---

## Configuration

The JWT expiration time is configurable in `application.yaml`:

```yaml
jwt:
  expiration: 3600000  # milliseconds (1 hour)
```

To change to 24 hours: `86400000`  
To change to 7 days: `604800000`

---

## Error Examples

### Invalid Credentials (401)
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"wrong"}'
```

Response:
```json
{
    "success": false,
    "timestamp": 1704067200000,
    "message": "Invalid email or password"
}
```

### Missing Token (401)
```bash
curl -X GET http://localhost:8080/api/transactions/filter
```

Response:
```json
{
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource"
}
```

### Insufficient Permissions (403)
```bash
# USER tries to access ADMIN endpoint
curl -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer <USER_TOKEN>"
```

Response:
```json
{
    "error": "Forbidden",
    "message": "Access denied: Admin role required"
}
```

---

## File Changes Summary

### New Files Created
- ✅ `entity/enums/UserRole.java` - Role enumeration
- ✅ `exception/AuthenticationException.java` - 401 exceptions
- ✅ `exception/AuthorizationException.java` - 403 exceptions
- ✅ `dto/LoginResponse.java` - Login response DTO
- ✅ `SECURITY_IMPLEMENTATION.md` - Detailed documentation

### Modified Files
- ✅ `entity/UserEntity.java` - Added role, isActive, updatedAt
- ✅ `dto/UserRequest.java` - Added role with validation
- ✅ `dto/LoginRequest.java` - Added validation
- ✅ `service/UserService.java` - Added role management
- ✅ `service/JwtService.java` - Added role in tokens
- ✅ `util/JwtFilter.java` - Extract and set roles
- ✅ `config/SecurityConfig.java` - Role-based authorization
- ✅ `exception/GlobalExceptionHandler.java` - Added 401/403 handlers
- ✅ `controller/UserController.java` - Enhanced login/register
- ✅ `application.yaml` - Added JWT expiration config

---

## Testing with cURL

### Full Auth Flow

```bash
# 1. Register user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "TestPass123",
    "role": "USER"
  }'

# 2. Login and extract token
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123"
  }' | jq -r '.payload.access_token')

# 3. Access protected endpoint
curl -X GET http://localhost:8080/api/transactions/filter \
  -H "Authorization: Bearer $TOKEN"
```

---

## Next Steps

1. **Add Admin User Creation Endpoint**
   - Only existing admins can create new admin users
   
2. **Implement Token Refresh**
   - Add refresh token endpoint for better UX
   
3. **Add Logout Endpoint**
   - Implement token blacklisting

4. **Add Role Management**
   - Allow admins to change user roles

5. **Add Audit Logging**
   - Log all authentication and authorization events

---

## Production Checklist

- ✅ Password encryption (BCrypt)
- ✅ JWT token validation
- ✅ Role-based access control
- ✅ Proper HTTP status codes
- ✅ Exception handling
- ✅ CSRF protection disabled (for REST API)
- ✅ Stateless sessions
- ✅ Request validation
- ⚠️ TODO: Enable HTTPS in production
- ⚠️ TODO: Configure environment-specific secrets
- ⚠️ TODO: Add rate limiting
- ⚠️ TODO: Add request logging/monitoring
