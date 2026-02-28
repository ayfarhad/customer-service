# Functional Specification

## Overview

`customer-service` is a Spring Boot 3 REST API that manages customer records. The service is secured with JWT-based authentication and stores data in a PostgreSQL database. It exposes endpoints for creating, retrieving, listing, updating, and deleting customers. The application is designed for deployment in production environments and includes OpenAPI documentation.

## Actors & Roles

- **Admin**: full CRUD on customers.
- **User**: read-only access to customer data.
- **System**: the application itself, handling authentication and data persistence.

## Functional Requirements

### Customer Entity

- Fields:
  - `id`: auto-generated Long primary key.
  - `name`: String, required, max 150 characters.
  - `email`: String, required, unique, valid email format.

### Endpoints `/api/v1/customers`

1. **Create customer**
   - `POST /api/v1/customers`
   - Requires role `ADMIN`.
   - Request body: `CustomerRequest` (name, email).
   - Validates fields; returns 400 on violation.
   - Returns 201 with `CustomerResponse`.
   - Handles duplicate email with 409.

2. **Get customer by ID**
   - `GET /api/v1/customers/{id}`
   - Requires role `ADMIN` or `USER`.
   - Returns 200 with `CustomerResponse` or 404 if not found.

3. **List customers**
   - `GET /api/v1/customers` with paging & sorting.
   - Requires role `ADMIN` or `USER`.
   - Returns paged results converting entities to DTOs.

4. **Update customer**
   - `PUT /api/v1/customers/{id}`
   - Requires role `ADMIN`.
   - Body: `CustomerRequest`; validates.
   - Returns 200 updated customer or 404/409.

5. **Delete customer**
   - `DELETE /api/v1/customers/{id}`
   - Requires role `ADMIN`.
   - Returns 204 or 404.

### Authentication & Authorization

- **Login**
  - `POST /api/v1/auth/login`
  - Accepts username/password.
  - Authenticates against in-memory users.
  - On success returns JWT token valid for 1 hour with username & roles in claims.
  - Logged authentication attempts.

- **JWT requirements**
  - Stateless; CSRF disabled; sessions disabled.
  - Secret key configured via `application.yml` or environment variable.
  - Signed with HS256.
  - `JwtService` generates/validates tokens; `JwtAuthenticationFilter` processes each request.
  - Invalid/expired tokens result in 401.

- **User storage**
  - In-memory with two accounts:
      - `admin/admin123` (ROLE_ADMIN)
      - `user/user123` (ROLE_USER)

- **Authorization rules**
  - `/api/v1/auth/login`: permit all.
  - Swagger endpoints: permit all.
  - All other endpoints require authentication.
  - Method-level security (`@PreAuthorize`) enforces role-based access.

### Data Layer

- Uses Spring Data JPA with PostgreSQL.
- Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- `hibernate.ddl-auto=update`.
- Logs SQL statements.
- Unique constraint on `email` at database level.

### Validation and Error Handling

- Input validation with Jakarta Validation annotations.
- Global exception handler returns structured JSON with timestamp, status, error, message, path.
- Specific exceptions: `CustomerNotFoundException`, `EmailAlreadyExistsException`.
- Generic handler for unexpected errors.

### Logging

- SLF4J used throughout.
- Log business operations (create/update/delete) and authentication events.

### API Documentation

- Springdoc OpenAPI generates Swagger UI at `/swagger-ui.html`.
- JWT bearer scheme configured.
- Each controller method annotated with `@Operation` and `@ApiResponse`.

## Non-functional Requirements

- Java 17 compatibility.
- No deprecated APIs.
- Build with Maven; produces an executable jar.
- Clean layered architecture; separation of concerns.
- Production-ready error handling and security.

## Deployment & Configuration

- Application reads config from `application.yml` or env vars.
- Database connectivity via env variables.
- JWT secret provided via env var for security.
- Run with `java -jar target/customer-service-1.0.0.jar` after building.

## Example Usage

Refer to README for curl commands and JSON samples.

---
*Document generated on 2026-02-27.*
