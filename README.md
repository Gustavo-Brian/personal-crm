# Personal CRM

A backend API for a personal CRM focused on authenticated user accounts and relationship management data.

## Overview

Personal CRM organizes personal and professional relationship data in an authenticated application. The backend provides the account foundation for user-owned CRM data, including persistence, database migrations, request validation, password hashing, and structured API error responses.

## Capabilities

- User account registration
- Email normalization and duplicate email protection
- BCrypt password hashing
- User persistence with Spring Data JPA
- Flyway-managed database schema
- Validation errors and conflict responses in JSON
- Test configuration with an in-memory H2 database

## Architecture

The application is organized with a Spring Boot backend and a frontend workspace. The backend follows a layered structure with controllers, services, repositories, configuration classes, database migrations, and shared exception handling.

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security Crypto
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

Authentication:

- `POST /auth/register` creates a user account and returns the registered user id, name, and email.

Example request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "password123"
}
```

Example response:

```json
{
  "id": 1,
  "name": "Ada Lovelace",
  "email": "ada@example.com"
}
```

Validation and duplicate email errors are returned as JSON responses.

## Testing

The backend test suite covers the registration flow, password hashing, duplicate email protection, request validation errors, repository persistence, and Flyway migration startup with H2.

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

The database settings can be changed with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
