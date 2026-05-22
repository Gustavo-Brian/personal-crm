# Personal CRM

Personal CRM is a Spring Boot backend API for managing personal and professional relationships. It provides authenticated user accounts, JWT-based authorization, user-scoped contact records, contact details, database migrations, validation, and automated tests.

## Features

- User registration and login
- JWT bearer authentication for protected routes
- Authenticated profile and credential updates
- User-owned contact CRUD operations
- Contact details with phone numbers, email addresses, address data, birthday, organization, job title, and notes
- Academic formation records linked to contacts
- Email normalization and duplicate email protection
- BCrypt password hashing
- Flyway-managed relational schema
- Structured JSON responses for validation, authentication, conflict, and not found errors
- Automated backend tests with an in-memory H2 database

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot |
| API | Spring Web |
| Persistence | Spring Data JPA |
| Security | Spring Security, OAuth2 JOSE |
| Validation | Bean Validation |
| Database migrations | Flyway |
| Database | MySQL |
| Tests | JUnit, Spring Boot Test, H2 |
| Build tool | Maven |

## Architecture

The backend follows a layered structure that keeps HTTP handling, business rules, persistence, security, and shared error handling separated.

- Controllers expose the REST API and validate request bodies.
- Services own application rules, normalization, ownership checks, and persistence orchestration.
- Repositories provide database access through Spring Data JPA.
- Domain entities model users, contacts, and contact detail data.
- Flyway migrations keep schema changes explicit and repeatable.
- Security configuration protects private routes with JWT bearer tokens.
- Shared exception handling returns consistent JSON errors across the API.

## Persistence

The schema includes `users` for account identity and `contacts` for user-owned relationship records. Contact data is scoped by owner so each authenticated user can only access their own CRM records.

Contacts support optional phone numbers, email addresses, address fields, notes, birthday, organization, and job title. Phone numbers and email addresses are stored as ordered contact details, while address and note fields are stored with the contact record.

Academic formations are linked to contacts and protected through the same ownership rules as contact records. This keeps education history available as structured CRM data without allowing one user to access another user's contact details.

## API Reference

Base URL:

```text
http://localhost:8080/api
```

Protected endpoints require an `Authorization` header:

```http
Authorization: Bearer jwt-token
```

Authentication endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/auth/register` | Creates a user account. |
| `POST` | `/auth/login` | Authenticates a user and returns a bearer token. |
| `PUT` | `/auth/credentials` | Updates the authenticated user's name, email, and optional password. |

Contact endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/contacts` | Lists contacts owned by the authenticated user. |
| `POST` | `/contacts` | Creates a contact for the authenticated user. |
| `GET` | `/contacts/{id}` | Returns one owned contact by id. |
| `PUT` | `/contacts/{id}` | Updates one owned contact by id. |
| `DELETE` | `/contacts/{id}` | Deletes one owned contact by id. |

Academic formation endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/contacts/{contactId}/academic-formations` | Lists academic formations for an owned contact. |
| `POST` | `/contacts/{contactId}/academic-formations` | Creates an academic formation for an owned contact. |
| `GET` | `/contacts/{contactId}/academic-formations/{formationId}` | Returns one academic formation by id. |
| `PUT` | `/contacts/{contactId}/academic-formations/{formationId}` | Updates one academic formation by id. |
| `DELETE` | `/contacts/{contactId}/academic-formations/{formationId}` | Deletes one academic formation by id. |

## API Examples

### Register

Request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "password123"
}
```

Response:

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

Response:

```json
{
  "id": 1,
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "token": "jwt-token",
  "tokenType": "Bearer"
}
```

### Update Credentials

Request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada.lovelace@example.com",
  "currentPassword": "password123",
  "newPassword": "new-password123"
}
```

Response:

```json
{
  "id": 1,
  "name": "Ada Lovelace",
  "email": "ada.lovelace@example.com",
  "token": "jwt-token",
  "tokenType": "Bearer"
}
```

### Create Contact

Request:

```json
{
  "name": "Grace Hopper",
  "organization": "US Navy",
  "jobTitle": "Computer Scientist",
  "birthday": "1906-12-09",
  "phoneNumbers": [
    {
      "label": "work",
      "number": "+1-555-0100"
    }
  ],
  "emailAddresses": [
    {
      "label": "primary",
      "email": "grace.hopper@example.com"
    }
  ],
  "address": {
    "street": "1 Navy Way",
    "city": "Arlington",
    "state": "VA",
    "postalCode": "22201",
    "country": "USA"
  },
  "notes": "COBOL pioneer and compiler leader."
}
```

Response:

```json
{
  "id": 10,
  "name": "Grace Hopper",
  "organization": "US Navy",
  "jobTitle": "Computer Scientist",
  "birthday": "1906-12-09",
  "phoneNumbers": [
    {
      "label": "work",
      "number": "+1-555-0100"
    }
  ],
  "emailAddresses": [
    {
      "label": "primary",
      "email": "grace.hopper@example.com"
    }
  ],
  "address": {
    "street": "1 Navy Way",
    "city": "Arlington",
    "state": "VA",
    "postalCode": "22201",
    "country": "USA"
  },
  "notes": "COBOL pioneer and compiler leader.",
  "createdAt": "2026-05-21T12:00:00",
  "updatedAt": "2026-05-21T12:00:00"
}
```

### List Contacts

Response:

```json
[
  {
    "id": 10,
    "name": "Grace Hopper",
    "organization": "US Navy",
    "jobTitle": "Computer Scientist",
    "birthday": "1906-12-09",
    "phoneNumbers": [
      {
        "label": "work",
        "number": "+1-555-0100"
      }
    ],
    "emailAddresses": [
      {
        "label": "primary",
        "email": "grace.hopper@example.com"
      }
    ],
    "address": {
      "street": "1 Navy Way",
      "city": "Arlington",
      "state": "VA",
      "postalCode": "22201",
      "country": "USA"
    },
    "notes": "COBOL pioneer and compiler leader.",
    "createdAt": "2026-05-21T12:00:00",
    "updatedAt": "2026-05-21T12:00:00"
  }
]
```

### Create Academic Formation

Request:

```json
{
  "institution": "Massachusetts Institute of Technology",
  "degree": "BSc",
  "fieldOfStudy": "Computer Science",
  "startDate": "2010-02-01",
  "endDate": "2014-12-15",
  "description": "Undergraduate studies."
}
```

Response:

```json
{
  "id": 30,
  "contactId": 10,
  "institution": "Massachusetts Institute of Technology",
  "degree": "BSc",
  "fieldOfStudy": "Computer Science",
  "startDate": "2010-02-01",
  "endDate": "2014-12-15",
  "description": "Undergraduate studies.",
  "createdAt": "2026-05-21T12:00:00",
  "updatedAt": "2026-05-21T12:00:00"
}
```

### List Academic Formations

Response:

```json
[
  {
    "id": 30,
    "contactId": 10,
    "institution": "Massachusetts Institute of Technology",
    "degree": "BSc",
    "fieldOfStudy": "Computer Science",
    "startDate": "2010-02-01",
    "endDate": "2014-12-15",
    "description": "Undergraduate studies.",
    "createdAt": "2026-05-21T12:00:00",
    "updatedAt": "2026-05-21T12:00:00"
  }
]
```

### Error Response

Validation, duplicate email, invalid login, invalid current password, missing contact, and missing academic formation errors are returned in a consistent JSON format.

```json
{
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address"
  }
}
```

Invalid login credentials return a generic authentication error without exposing whether the email exists.

## Local Setup

Requirements:

- Java 21
- Maven
- MySQL 8

Run the backend tests:

```bash
cd backend
mvn test
```

Start the backend API:

```bash
cd backend
mvn spring-boot:run
```

Default API URL:

```text
http://localhost:8080/api
```

## Configuration

The backend reads database and JWT settings from environment variables.

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/personal_crm?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `personal_crm` |
| `DB_PASSWORD` | `personal_crm` |
| `JWT_SECRET` | `personal-crm-local-jwt-secret-key-change-me` |
| `JWT_ISSUER` | `personal-crm` |
| `JWT_EXPIRATION_SECONDS` | `3600` |

## Testing

The backend test suite covers authentication, credential updates, JWT token handling, protected route authorization, contact persistence, contact API behavior, academic formation behavior, ownership isolation, request validation, repository persistence, password hashing, duplicate email protection, and Flyway migration startup with H2.

```bash
cd backend
mvn test
```

## License

This project is licensed under the MIT License.
