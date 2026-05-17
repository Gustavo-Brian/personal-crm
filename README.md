# Personal CRM

A backend API for a personal CRM focused on authenticated user accounts and relationship management data.

## Overview

Personal CRM organizes personal and professional relationship data in an authenticated application. The backend provides the account foundation for user-owned CRM data, including persistence, database migrations, request validation, password hashing, and structured API error responses.

## Capabilities

- User account registration
- User login with credential validation
- JWT-secured protected API routes
- Email normalization and duplicate email protection
- BCrypt password hashing
- User persistence with Spring Data JPA
- Flyway-managed database schema
- Validation, conflict, and authentication errors in JSON
- Test configuration with an in-memory H2 database

## Architecture

The application is organized with a Spring Boot backend and a frontend workspace. The backend follows a layered structure with controllers, services, repositories, configuration classes, database migrations, and shared exception handling.

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Security OAuth2 JOSE
- Bean Validation
- Flyway
- MySQL
- H2 for tests
- Maven

## Data Model

The database schema is managed with Flyway. The user model stores account identity and authentication data:

- `id`
- `name`
- `email`
- `password_hash`
- `created_at`
- `updated_at`

## API

Base path: `/api`

Authentication endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/auth/register` | Creates a user account with name, email, and password. |
| `POST` | `/auth/login` | Authenticates a user and returns a bearer token. |

### Register

Request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "password123"
}
```

Success response:

```json
{
  "id": 1,
  "name": "Ada Lovelace",
  "email": "ada@example.com"
}
```

### Login

Request:

```json
{
  "email": "ada@example.com",
  "password": "password123"
}
```

Success response:

```json
{
  "id": 1,
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "token": "jwt-token",
  "tokenType": "Bearer"
}
```

Protected endpoints require the token in the `Authorization` header:

```http
Authorization: Bearer jwt-token
```

### Error Format

Validation, duplicate email, and invalid credential errors are returned as JSON responses.

```json
{
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address"
  }
}
```

Invalid login credentials return an authentication error without exposing whether the email exists.

## Testing

The backend test suite covers registration, login, JWT token handling, protected route authorization, password hashing, duplicate email protection, request validation errors, repository persistence, and Flyway migration startup with H2.

## Local Setup

Requirements:

- Java 21
- Maven
- MySQL 8

Backend:

```bash
cd backend
mvn test
mvn spring-boot:run
```

Default backend configuration:

- API URL: `http://localhost:8080/api`
- Database URL: `jdbc:mysql://localhost:3306/personal_crm`
- Database user: `personal_crm`
- Database password: `personal_crm`
- JWT issuer: `personal-crm`
- JWT expiration: `3600` seconds

The database and JWT settings can be changed with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ISSUER`, and `JWT_EXPIRATION_SECONDS`.
