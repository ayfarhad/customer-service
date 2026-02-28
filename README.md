# Customer Service REST API

A production-ready Spring Boot 3 application managing customers with JWT security and PostgreSQL.

## Tech Stack
- Java 17
- Spring Boot 3.x
- Maven
- PostgreSQL
- Spring Data JPA (Hibernate)
- Spring Security with JWT
- Springdoc OpenAPI 3 (Swagger UI)
- Lombok
- Jakarta Validation

## Configuration

The application reads database and JWT settings from environment variables. See `application.yml` for defaults.

Environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/customerdb
export DB_USERNAME=postgres
export DB_PASSWORD=password
export JWT_SECRET=your-256-bit-secret-string
```

`ddl-auto=update` is configured so that JPA will create/update tables automatically.

## Running the Application

1. Ensure PostgreSQL is running and a database `customerdb` exists (or change DB_URL accordingly).
2. Set the environment variables above.
3. Build and run with Maven:

```bash
mvn clean package
java -jar target/customer-service-1.0.0.jar
```

The application will start on port `8080` by default.

## Default Users

| Username | Password  | Roles      |
|----------|-----------|------------|
| admin    | admin123  | ROLE_ADMIN |
| user     | user123   | ROLE_USER  |

## Testing with Swagger

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`.

1. Click "Authorize" and enter a JWT token prefixed with `Bearer `.
2. Use the `/api/v1/auth/login` endpoint to obtain a token.

## Example curl Commands

### Authenticate and obtain token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:
```json
{"token":"eyJhbGciOiJIUzI1NiJ9..."}
```

### Create customer (ADMIN only)
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"John Doe","email":"john.doe@example.com"}'
```

Sample response:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

### Get customer by ID (ADMIN or USER)
```bash
curl -X GET http://localhost:8080/api/v1/customers/1 \
  -H "Authorization: Bearer <token>"
```

### Get all customers (pagination + sorting)
```bash
curl -X GET "http://localhost:8080/api/v1/customers?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer <token>"
```

### Update customer (ADMIN only)
```bash
curl -X PUT http://localhost:8080/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Jane Doe","email":"jane.doe@example.com"}'
```

### Delete customer (ADMIN only)
```bash
curl -X DELETE http://localhost:8080/api/v1/customers/1 \
  -H "Authorization: Bearer <token>"
```

## Sample JSON Structures

`CustomerRequest`
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

`CustomerResponse`
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

**Sample error response**
```json
{
  "timestamp": "2026-02-27T12:34:56",
  "status": 404,
  "error": "Not Found",
  "message": "Customer with id 99 not found",
  "path": "/api/v1/customers/99"
}
```

`AuthenticationRequest`
```json
{
  "username": "admin",
  "password": "admin123"
}
```

`AuthenticationResponse`
```json
{
  "token": "<jwt-token>"
}
```

## Notes

- All `/api/v1/customers/**` endpoints require a valid Bearer token.
- ADMIN users can perform full CRUD; USER role is limited to GET operations.
- Unique constraint on email ensures duplicates are prevented.

---
