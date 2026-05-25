# Personal CRM

Personal CRM is a full-stack web application for managing personal and professional relationships. The backend provides authenticated user accounts, JWT-based authorization, user-scoped contact records, contact details, database migrations, validation, and automated tests.

The repository is structured as a full-stack product. The stack below lists technologies already present in the codebase.

## Features

- User registration and login
- JWT bearer authentication for protected routes
- Authenticated profile and credential updates
- User-owned contact CRUD operations
- Contact details with phone numbers, email addresses, address data, birthday, organization, job title, and notes
- Academic formation records linked to contacts
- Important dates linked to contacts
- Automatic synchronization between contact birthdays and important dates
- User-owned contact groups for organizing relationship segments
- Contact group memberships for assigning owned contacts to owned groups
- Email normalization and duplicate email protection
- BCrypt password hashing
- Flyway-managed relational schema
- Structured JSON responses for validation, authentication, conflict, and not found errors
- Automated backend tests with an in-memory H2 database

## Tech Stack

| Area | Technology |
| --- | --- |
| Product | Full-stack web application |
| Backend language | Java 21 |
| Backend framework | Spring Boot |
| Backend API | Spring Web |
| Backend persistence | Spring Data JPA |
| Backend security | Spring Security, OAuth2 JOSE |
| Backend validation | Bean Validation |
| Backend migrations | Flyway |
| Database | MySQL |
| Backend tests | JUnit, Spring Boot Test, H2 |
| Build tool | Maven |

## Architecture

The application is organized as a full-stack product with a Spring Boot backend and a frontend workspace. The implemented backend follows a layered structure that keeps HTTP handling, business rules, persistence, security, and shared error handling separated.

- Controllers expose the REST API and validate request bodies.
- Services own application rules, normalization, ownership checks, and persistence orchestration.
- Repositories provide database access through Spring Data JPA.
- Domain entities model users, contacts, contact details, academic formations, important dates, and groups.
- Flyway migrations keep schema changes explicit and repeatable.
- Security configuration protects private routes with JWT bearer tokens.
- Shared exception handling returns consistent JSON errors across the API.

## Persistence

The schema includes `users` for account identity and `contacts` for user-owned relationship records. Contact data is scoped by owner so each authenticated user can only access their own CRM records.

Contacts support optional phone numbers, email addresses, address fields, notes, birthday, organization, and job title. Phone numbers and email addresses are stored as ordered contact details, while address and note fields are stored with the contact record.

Academic formations are linked to contacts and protected through the same ownership rules as contact records. This keeps education history available as structured CRM data without allowing one user to access another user's contact details.

Important dates are linked to contacts and can store relationship-specific dates such as birthdays, anniversaries, work events, family dates, or other reminders. Each record keeps a title, date, category, and optional description under the same owner-scoped access rules as contacts.

Contact birthdays are synchronized with important dates so birthday changes remain available both as contact profile data and as date-based CRM reminders. Updating or removing a contact birthday updates the related `BIRTHDAY` important date, and changes to `BIRTHDAY` important dates are reflected back on the contact.

Contact groups are owned directly by users and can be used to organize relationship segments such as mentors, clients, hiring contacts, friends, or professional communities. The group resource manages reusable metadata such as name, description, and display color.

Group memberships connect user-owned groups to user-owned contacts through a join table with a uniqueness rule that prevents duplicate assignments.

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

Important date endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/contacts/{contactId}/important-dates` | Lists important dates for an owned contact. |
| `POST` | `/contacts/{contactId}/important-dates` | Creates an important date for an owned contact. |
| `GET` | `/contacts/{contactId}/important-dates/{importantDateId}` | Returns one important date by id. |
| `PUT` | `/contacts/{contactId}/important-dates/{importantDateId}` | Updates one important date by id. |
| `DELETE` | `/contacts/{contactId}/important-dates/{importantDateId}` | Deletes one important date by id. |

Valid important date types are `BIRTHDAY`, `WORK`, `FAMILY`, `ANNIVERSARY`, and `OTHER`.

Group endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/groups` | Lists groups owned by the authenticated user. |
| `POST` | `/groups` | Creates a group for the authenticated user. |
| `GET` | `/groups/{id}` | Returns one owned group by id. |
| `PUT` | `/groups/{id}` | Updates one owned group by id. |
| `DELETE` | `/groups/{id}` | Deletes one owned group by id. |

Group membership endpoints:

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/groups/{groupId}/contacts` | Lists contacts assigned to an owned group. |
| `POST` | `/groups/{groupId}/contacts/{contactId}` | Assigns an owned contact to an owned group. |
| `DELETE` | `/groups/{groupId}/contacts/{contactId}` | Removes an owned contact from an owned group. |

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

### Create Important Date

Request:

```json
{
  "title": "Birthday dinner",
  "date": "2026-06-15",
  "type": "BIRTHDAY",
  "description": "Dinner reservation."
}
```

Response:

```json
{
  "id": 35,
  "contactId": 10,
  "title": "Birthday dinner",
  "date": "2026-06-15",
  "type": "BIRTHDAY",
  "description": "Dinner reservation.",
  "createdAt": "2026-05-21T12:00:00",
  "updatedAt": "2026-05-21T12:00:00"
}
```

### List Important Dates

Response:

```json
[
  {
    "id": 35,
    "contactId": 10,
    "title": "Birthday dinner",
    "date": "2026-06-15",
    "type": "BIRTHDAY",
    "description": "Dinner reservation.",
    "createdAt": "2026-05-21T12:00:00",
    "updatedAt": "2026-05-21T12:00:00"
  }
]
```

### Create Group

Request:

```json
{
  "name": "Mentors",
  "description": "People who help with career decisions.",
  "colorHex": "#0EA5E9"
}
```

Response:

```json
{
  "id": 40,
  "name": "Mentors",
  "description": "People who help with career decisions.",
  "colorHex": "#0EA5E9",
  "createdAt": "2026-05-21T12:00:00",
  "updatedAt": "2026-05-21T12:00:00"
}
```

### List Groups

Response:

```json
[
  {
    "id": 40,
    "name": "Mentors",
    "description": "People who help with career decisions.",
    "colorHex": "#0EA5E9",
    "createdAt": "2026-05-21T12:00:00",
    "updatedAt": "2026-05-21T12:00:00"
  }
]
```

### Add Contact To Group

Request:

```text
POST /groups/40/contacts/10
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

### List Group Contacts

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

### Error Response

Validation, duplicate email, invalid login, invalid current password, missing contact, missing academic formation, missing important date, and missing group errors are returned in a consistent JSON format.

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

### Database Setup

Create the local database user before starting the backend:

```sql
CREATE DATABASE IF NOT EXISTS personal_crm;
CREATE USER IF NOT EXISTS 'personal_crm'@'localhost' IDENTIFIED BY 'personal_crm';
GRANT ALL PRIVILEGES ON personal_crm.* TO 'personal_crm'@'localhost';
FLUSH PRIVILEGES;
```

### Backend Environment

The backend reads database and JWT settings from environment variables. Set them in the same terminal session used to start Spring Boot.

PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/personal_crm?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="personal_crm"
$env:DB_PASSWORD="personal_crm"
$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:JWT_ISSUER="personal-crm"
$env:JWT_EXPIRATION_SECONDS="3600"
```

Bash:

```bash
export DB_URL="jdbc:mysql://localhost:3306/personal_crm?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="personal_crm"
export DB_PASSWORD="personal_crm"
export JWT_SECRET="replace-with-a-long-random-secret"
export JWT_ISSUER="personal-crm"
export JWT_EXPIRATION_SECONDS="3600"
```

Use a private, high-entropy value for `JWT_SECRET` outside local development.

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

### Frontend Workspace

The frontend workspace is `frontend/`. No frontend runtime is required to run the backend API.

When a frontend package is present, install dependencies and start the client from that workspace:

```bash
cd frontend
npm install
npm run dev
```

Frontend environment files should stay out of Git. The client API base URL should point to:

```text
http://localhost:8080/api
```

## Configuration

Backend environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/personal_crm?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `personal_crm` |
| `DB_PASSWORD` | `personal_crm` |
| `JWT_SECRET` | `personal-crm-local-jwt-secret-key-change-me` |
| `JWT_ISSUER` | `personal-crm` |
| `JWT_EXPIRATION_SECONDS` | `3600` |

## Testing

The backend test suite covers authentication, credential updates, JWT token handling, protected route authorization, contact persistence, contact API behavior, birthday synchronization, academic formation behavior, important date behavior, group behavior, group membership behavior, ownership isolation, request validation, repository persistence, password hashing, duplicate email protection, and Flyway migration startup with H2.

```bash
cd backend
mvn test
```

## License

This project is licensed under the MIT License.
