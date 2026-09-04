# FundooNotes Backend

A secure and scalable **Notes Management REST API** built using Spring Boot, PostgreSQL, Redis, JWT, JMS, ActiveMQ Artemis, and Java 21.

---

## 1. Project Overview

FundooNotes is a backend application that allows users to:

* Register and login
* Authenticate using JWT
* Manage personal notes
* Pin and unpin notes
* Archive and unarchive notes
* Move notes to trash
* Restore notes
* Permanently delete notes
* Search and filter notes
* Paginate notes
* Create and manage labels
* Assign labels to notes
* Create reminders
* Process reminders asynchronously using JMS
* Upload note attachments
* Download attachments
* Delete attachments
* Logout and revoke JWT tokens using Redis

The application follows a layered architecture with Controller, Service, Repository, Entity, DTO, Security, Exception, Redis, JMS, and AOP components.

---

## 2. Technology Stack

| Technology        | Purpose                            |
| ----------------- | ---------------------------------- |
| Java 21           | Programming language               |
| Spring Boot 4.1.1 | Backend framework                  |
| Maven             | Build tool                         |
| Spring Web        | REST APIs                          |
| Spring Data JPA   | Database access                    |
| PostgreSQL        | Relational database                |
| Spring Security   | Authentication and authorization   |
| JWT               | Token-based authentication         |
| Redis             | JWT token state and revocation     |
| JMS               | Asynchronous messaging             |
| ActiveMQ Artemis  | JMS message broker                 |
| Docker            | Infrastructure containers          |
| Lombok            | Boilerplate reduction              |
| Bean Validation   | Request validation                 |
| Spring AOP        | Logging and cross-cutting concerns |
| Swagger/OpenAPI   | API documentation                  |
| JUnit 5           | Unit testing                       |
| Mockito           | Mock-based testing                 |
| Apache Maven      | Dependency/build management        |

---

## 3. Architecture

```text
                         ┌─────────────────┐
                         │     Postman     │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │  Spring Boot    │
                         │   REST API      │
                         └────────┬────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
       ┌────────────┐      ┌────────────┐      ┌────────────┐
       │ PostgreSQL │      │   Redis    │      │   Security │
       │            │      │            │      │    + JWT   │
       └────────────┘      └────────────┘      └────────────┘
                                  │
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ JMS / Artemis   │
                         │ Docker Broker   │
                         └────────┬────────┘
                                  │
                                  ▼
                         NotificationConsumer
```

---

## 4. Project Structure

```text
src/
├── main/
│   ├── java/com/fundoo/notes/
│   │
│   ├── config/
│   │   ├── JpaConfig.java
│   │   ├── RedisConfig.java
│   │   ├── JmsConfig.java
│   │   └── OpenApiConfig.java
│   │
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── NoteController.java
│   │   ├── LabelController.java
│   │   ├── ReminderController.java
│   │   └── AttachmentController.java
│   │
│   ├── dto/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── note/
│   │   ├── label/
│   │   ├── reminder/
│   │   └── attachment/
│   │
│   ├── entity/
│   │   ├── User.java
│   │   ├── Note.java
│   │   ├── Label.java
│   │   ├── Reminder.java
│   │   └── Attachment.java
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── NoteRepository.java
│   │   ├── LabelRepository.java
│   │   ├── ReminderRepository.java
│   │   └── AttachmentRepository.java
│   │
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── NoteService.java
│   │   ├── LabelService.java
│   │   ├── ReminderService.java
│   │   └── AttachmentService.java
│   │
│   ├── security/
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── SecurityConfig.java
│   │
│   ├── redis/
│   │   └── TokenCacheService.java
│   │
│   ├── jms/
│   │   ├── NotificationMessage.java
│   │   ├── NotificationProducer.java
│   │   └── NotificationConsumer.java
│   │
│   ├── aspect/
│   │   └── LoggingAspect.java
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── UserNotFoundException.java
│   │   ├── NoteNotFoundException.java
│   │   ├── LabelNotFoundException.java
│   │   ├── AttachmentNotFoundException.java
│   │   ├── DuplicateEmailException.java
│   │   ├── DuplicateLabelException.java
│   │   ├── InvalidTokenException.java
│   │   └── InvalidPasswordException.java
│   │
│   └── enums/
│       └── ReminderStatus.java
│
└── test/
    └── java/com/fundoo/notes/
```

---

## 5. Database

The application uses PostgreSQL.

Default development configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fundoo_notes
    username: postgres
    password: postgres
```

Main tables:

```text
users
notes
labels
note_labels
reminders
attachments
```

---

## 6. Redis

Redis is used for JWT token management.

Token format:

```text
auth:token:<JWT_TOKEN>
```

Redis is used to:

* Store active JWT tokens
* Apply token TTL
* Validate active sessions
* Revoke tokens during logout

Logout flow:

```text
Login
  ↓
Generate JWT
  ↓
Store JWT in Redis
  ↓
JWT expires / Logout
  ↓
Remove JWT from Redis
  ↓
Protected API returns 401
```

---

## 7. JMS and ActiveMQ Artemis

ActiveMQ Artemis is used as the JMS message broker.

The broker runs through Docker.

Application connection:

```yaml
spring:
  artemis:
    broker-url: tcp://localhost:61616
    user: admin
    password: admin
```

Notification queue:

```text
fundoo.notification.queue
```

Reminder flow:

```text
Reminder
   ↓
Scheduler
   ↓
NotificationProducer
   ↓
ActiveMQ Artemis
   ↓
fundoo.notification.queue
   ↓
NotificationConsumer
```

When a reminder is processed:

```text
PENDING → SENT
```

---

## 8. Attachments

Attachments use a two-part storage approach.

### PostgreSQL

Stores metadata:

```text
id
fileName
fileType
fileSize
filePath
noteId
userId
createdDate
```

### Local storage

Actual files are stored inside:

```text
uploads/
```

Example:

```text
uploads/
└── 1756890000000_document.pdf
```

Upload flow:

```text
Postman
   ↓
Multipart File
   ↓
AttachmentController
   ↓
AttachmentService
   ├── Save physical file
   └── Save metadata
          ↓
      PostgreSQL
```

---

## 9. Authentication

Protected APIs require:

```text
Authorization: Bearer <JWT_TOKEN>
```

Public endpoints:

```text
POST /api/auth/register
POST /api/auth/login
```

Protected endpoints require authentication.

---

## 10. Main API Endpoints

### Authentication

```text
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
```

### Users

```text
GET    /api/users/me
```

### Notes

```text
POST   /api/notes
GET    /api/notes
GET    /api/notes/{id}
PUT    /api/notes/{id}
DELETE /api/notes/{id}

POST   /api/notes/{id}/pin
POST   /api/notes/{id}/unpin
POST   /api/notes/{id}/archive
POST   /api/notes/{id}/unarchive
POST   /api/notes/{id}/trash
POST   /api/notes/{id}/restore
DELETE /api/notes/{id}/permanent

GET    /api/notes/search
```

### Labels

```text
POST   /api/labels
GET    /api/labels
GET    /api/labels/{id}
PUT    /api/labels/{id}
DELETE /api/labels/{id}

POST   /api/notes/{noteId}/labels/{labelId}
DELETE /api/notes/{noteId}/labels/{labelId}
GET    /api/notes/{noteId}/labels
```

### Reminders

```text
POST   /api/reminders/notes/{noteId}
GET    /api/reminders
GET    /api/reminders/{id}
DELETE /api/reminders/{id}
```

### Attachments

```text
POST   /api/attachments/notes/{noteId}
GET    /api/attachments/notes/{noteId}
GET    /api/attachments/{attachmentId}/download
DELETE /api/attachments/{attachmentId}
```

---

## 11. HTTP Status Codes

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

---

## 12. Swagger

After starting the application, open:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

## 13. Running the Application

### Start PostgreSQL

Make sure PostgreSQL is running and create:

```text
fundoo_notes
```

### Start Redis

Make sure Redis is running on:

```text
localhost:6379
```

### Start ActiveMQ Artemis

Make sure the Docker Artemis container is running and exposes:

```text
61616
```

Check Docker:

```bash
docker ps
```

### Start Spring Boot

Run:

```bash
mvn clean spring-boot:run
```

Or:

```bash
mvn clean package
java -jar target/fundoo-notes-*.jar
```

---

## 14. Running Tests

Run all JUnit tests:

```bash
mvn clean test
```

The test suite covers:

```text
Authentication
Notes
Note status operations
Search
Pagination
Labels
Redis token management
Reminders
Attachments
Controllers
Services
Exceptions
```

---

## 15. Postman Testing Order

Recommended testing order:

```text
1. Register
2. Login
3. Test protected API
4. Create Note
5. Get Notes
6. Update Note
7. Pin Note
8. Archive Note
9. Trash Note
10. Restore Note
11. Permanent Delete
12. Search Notes
13. Pagination
14. Create Label
15. Add Label to Note
16. Get Note Labels
17. Remove Label
18. Create Reminder
19. Wait for Reminder
20. Verify JMS Notification
21. Upload Attachment
22. Get Attachments
23. Download Attachment
24. Test Attachment Ownership
25. Delete Attachment
26. Logout
27. Verify old JWT returns 401
```

---

## 16. Security and Ownership

Every user-owned resource is validated against the authenticated user's email.

Examples:

```text
User A cannot access User B's notes.
User A cannot access User B's labels.
User A cannot access User B's reminders.
User A cannot access User B's attachments.
```

Unauthorized resource access returns:

```text
404 Not Found
```

This prevents exposing whether another user's resource exists.

---

## 17. Logging

Spring AOP is used for service-layer logging.

The application logs:

* Method entry
* Method completion
* Execution time
* Exceptions

Sensitive information such as passwords and JWT tokens should not be logged.

Logs are written to:

```text
logs/fundoo-notes.log
```

---

## 18. Exception Handling

Centralized exception handling is provided by:

```text
GlobalExceptionHandler.java
```

Handled exceptions include:

```text
UserNotFoundException
NoteNotFoundException
LabelNotFoundException
AttachmentNotFoundException
DuplicateEmailException
DuplicateLabelException
InvalidTokenException
InvalidPasswordException
Validation exceptions
IllegalArgumentException
Generic exceptions
```

---

## 19. Current Development Status

```text
Phase 1 — Project Setup + Authentication       ✅
Phase 2 — Note CRUD                            ✅
Phase 3 — Pin/Archive/Trash/Restore            ✅
Phase 4 — Search/Filter/Pagination              ✅
Phase 5 — Labels                               ✅
Phase 6 — Redis Token Management                ✅
Phase 7 — Reminder + JMS                       ✅
Phase 8 — Attachments                          ✅
```

---

## 20. Final Verification

Before considering the project complete:

```bash
mvn clean test
```

Verify:

```text
JUnit tests             → PASS
PostgreSQL              → CONNECTED
Redis                   → CONNECTED
ActiveMQ Artemis        → CONNECTED
JWT authentication      → WORKING
JWT logout/revocation   → WORKING
Notes CRUD              → WORKING
Search/Pagination       → WORKING
Labels                  → WORKING
Reminders               → WORKING
JMS notifications       → WORKING
Attachments             → WORKING
Swagger                 → WORKING
Exception handling      → WORKING
Logging/AOP             → WORKING
```

---

## 21. Project Goal

FundooNotes demonstrates a production-style Spring Boot backend with:

* RESTful API design
* JWT security
* Redis session/token management
* PostgreSQL persistence
* JPA relationships
* Validation
* Global exception handling
* AOP logging
* JMS asynchronous processing
* ActiveMQ Artemis
* Docker infrastructure
* File attachment management
* Swagger/OpenAPI
* Unit testing with JUnit and Mockito
* User-level resource ownership
