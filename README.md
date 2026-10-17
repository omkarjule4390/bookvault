# Library Management System - REST API

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI_3.0-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge)](LICENSE)

A clean, scalable, and production-ready **Library Management System REST API** built with **Java 17**, **Spring Boot 3.x**, and **PostgreSQL**. Designed with strict enterprise architecture principles: Controller-Service-Repository multi-tier separation, DTO pattern, Jakarta Bean Validation, centralized exception handling, transactional consistency, and interactive OpenAPI (Swagger) documentation.

---

## Table of Contents
- [Architecture & Design](#architecture--design)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Key Features & Business Rules](#key-features--business-rules)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [PostgreSQL Setup (Docker Compose)](#postgresql-setup-docker-compose)
  - [Running the Application](#running-the-application)
- [API Documentation & Swagger UI](#api-documentation--swagger-ui)
- [REST API Endpoints Reference](#rest-api-endpoints-reference)
  - [1. Books API](#1-books-api)
  - [2. Members API](#2-members-api)
  - [3. Borrowing & Returns API](#3-borrowing--returns-api)
- [Sample curl Requests](#sample-curl-requests)
- [Testing](#testing)
- [Postman Collection](#postman-collection)

---

## Architecture & Design

The project strictly follows modern Spring Boot layered architecture:

```
                  ┌───────────────────────────────┐
                  │    HTTP Clients / Postman     │
                  │         Swagger UI            │
                  └───────────────┬───────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CONTROLLER LAYER                         │
│  - BookController, MemberController, BorrowController           │
│  - Jakarta Bean Validation (@Valid), HTTP Status Codes          │
│  - OpenAPI annotations (@Tag, @Operation, @ApiResponse)         │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ (Request / Response DTOs)
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                         SERVICE LAYER                           │
│  - BookService, MemberService, BorrowService                    │
│  - Transaction Management (@Transactional)                      │
│  - Business Invariant Enforcement & EntityMapper                │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ (Domain Entities)
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                       REPOSITORY LAYER                          │
│  - BookRepository, MemberRepository, BorrowRepository           │
│  - Spring Data JPA, Derived Queries, Pagination                 │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ (SQL / JDBC)
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      POSTGRESQL DATABASE                        │
│  - books, members, borrow_records tables                        │
│  - Indexes, Foreign Keys, Unique Constraints                    │
└─────────────────────────────────────────────────────────────────┘
```

### Core Architecture Highlights
- **DTO Pattern**: JPA entities (`Book`, `Member`, `BorrowRecord`) are **never** exposed directly to external clients. All endpoints consume `*Request` DTOs and return `*Response` DTOs.
- **Transactional Integrity**: Critical loan and return operations are wrapped in `@Transactional` to guarantee atomicity when updating book copy quantities and loan state.
- **Centralized Exception Handling**: `@RestControllerAdvice` catches domain exceptions, data conflicts, validation errors, and malformed inputs, returning standard JSON payloads (`ErrorResponse`).

---

## Technology Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 17 | Core programming language |
| **Spring Boot** | 3.3.3 | Enterprise application framework |
| **Spring Web (MVC)** | 3.3.3 | REST API controllers, content negotiation |
| **Spring Data JPA** | 3.3.3 | Relational database abstraction layer |
| **Hibernate** | 6.5.x | ORM engine & DDL schema generation |
| **PostgreSQL** | 16 | Production relational database |
| **Jakarta Bean Validation** | 3.0 | Declarative input validation |
| **Lombok** | 1.18.34 | Boilerplate reduction (@Getter, @Setter, @Builder) |
| **Springdoc OpenAPI** | 2.6.0 | OpenAPI 3.0 spec & Swagger UI |
| **H2 Database** | 2.2.x | In-memory database for fast, zero-setup testing |
| **Maven Wrapper** | 3.9.6 | Portable build tool |

---

## Project Structure

```
Library-Management-System/
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── postman/
│   └── Library-Management-System.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/librarymanagement/
    │   │   ├── LibraryManagementApplication.java
    │   │   ├── config/
    │   │   │   └── OpenApiConfig.java
    │   │   ├── controller/
    │   │   │   ├── BookController.java
    │   │   │   ├── BorrowController.java
    │   │   │   └── MemberController.java
    │   │   ├── dto/
    │   │   │   ├── BookRequest.java
    │   │   │   ├── BookResponse.java
    │   │   │   ├── BorrowRequest.java
    │   │   │   ├── BorrowResponse.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── MemberRequest.java
    │   │   │   └── MemberResponse.java
    │   │   ├── entity/
    │   │   │   ├── Book.java
    │   │   │   ├── BorrowRecord.java
    │   │   │   ├── BorrowStatus.java
    │   │   │   ├── Member.java
    │   │   │   └── MemberStatus.java
    │   │   ├── exception/
    │   │   │   ├── BusinessException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── mapper/
    │   │   │   └── EntityMapper.java
    │   │   ├── repository/
    │   │   │   ├── BookRepository.java
    │   │   │   ├── BorrowRepository.java
    │   │   │   └── MemberRepository.java
    │   │   └── service/
    │   │       ├── BookService.java
    │   │       ├── BorrowService.java
    │   │       ├── MemberService.java
    │   │       └── impl/
    │   │           ├── BookServiceImpl.java
    │   │           ├── BorrowServiceImpl.java
    │   │           └── MemberServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        ├── java/com/librarymanagement/
        │   ├── LibraryManagementApplicationTests.java
        │   ├── controller/
        │   │   ├── BookControllerTest.java
        │   │   ├── BorrowControllerTest.java
        │   │   └── MemberControllerTest.java
        │   └── service/
        │       ├── BookServiceTest.java
        │       ├── BorrowServiceTest.java
        │       └── MemberServiceTest.java
        └── resources/
            └── application-test.properties
```

---

## Key Features & Business Rules

1. **Book Inventory Management**:
   - Unique ISBN check upon creation and modification.
   - Dynamic inventory tracking: `availableCopies` cannot exceed `totalCopies` or drop below 0.
   - Deletion safety: Books with active borrowings cannot be deleted.

2. **Member Account Management**:
   - Unique email validation across member profiles.
   - Member statuses: `ACTIVE`, `SUSPENDED`, `INACTIVE`.
   - Deletion safety: Members holding unreturned books cannot be deleted.

3. **Borrowing Workflow**:
   - Member verification: Only `ACTIVE` members can borrow books.
   - Maximum limit: Maximum **5 active loans** per member.
   - Single active copy: A member cannot borrow two copies of the same book simultaneously.
   - Auto inventory decrement: `availableCopies` decrements by 1 on issue.
   - Configurable loan period: Default 14 days with custom override support.

4. **Return & Overdue Fine Workflow**:
   - Idempotent returns: Already returned loans cannot be returned twice.
   - Auto inventory increment: `availableCopies` increments by 1 on return.
   - Overdue calculation: If returned past `dueDate`, system automatically computes late fines ($1.00/day) and appends remarks.

---

## Getting Started

### Prerequisites
- **JDK 17** or higher installed (`java -version`)
- **Docker & Docker Compose** (optional, for running PostgreSQL container) or a local PostgreSQL 14+ instance.

### PostgreSQL Setup (Docker Compose)
A ready-to-run `docker-compose.yml` is included in the project root:

```bash
docker compose up -d
```

This launches a PostgreSQL 16 container with:
- **Database**: `library_db`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5432`

To check container health:
```bash
docker compose ps
```

### Running the Application

Build and run using the Maven Wrapper:

**On Windows:**
```cmd
mvnw.cmd clean spring-boot:run
```

**On Linux / macOS:**
```bash
./mvnw clean spring-boot:run
```

The application will start on **port 8080**.

#### Environment Variables Override (Optional)
You can configure database credentials via environment variables without modifying code:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=library_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

---

## API Documentation & Swagger UI

Once the application is running, access the interactive Swagger UI and OpenAPI 3.0 documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## REST API Endpoints Reference

### 1. Books API (`/api/v1/books`)

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/books` | Add a new book to inventory | `201 Created` |
| `GET` | `/api/v1/books/{id}` | Get book by ID | `200 OK` |
| `GET` | `/api/v1/books/isbn/{isbn}` | Get book by ISBN | `200 OK` |
| `GET` | `/api/v1/books` | List books (paginated: `page`, `size`, `sortBy`, `direction`) | `200 OK` |
| `GET` | `/api/v1/books/search?query=...` | Search books by title or author | `200 OK` |
| `GET` | `/api/v1/books/category/{category}` | Filter books by category | `200 OK` |
| `PUT` | `/api/v1/books/{id}` | Update book details | `200 OK` |
| `DELETE` | `/api/v1/books/{id}` | Delete book (if no active loans) | `204 No Content` |

### 2. Members API (`/api/v1/members`)

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/members` | Register a new member | `201 Created` |
| `GET` | `/api/v1/members/{id}` | Get member profile by ID | `200 OK` |
| `GET` | `/api/v1/members/email/{email}` | Get member profile by email | `200 OK` |
| `GET` | `/api/v1/members` | List members (paginated) | `200 OK` |
| `GET` | `/api/v1/members/search?query=...` | Search members by name or email | `200 OK` |
| `PUT` | `/api/v1/members/{id}` | Update member profile | `200 OK` |
| `DELETE` | `/api/v1/members/{id}` | Delete member (if no active loans) | `204 No Content` |

### 3. Borrowing & Returns API (`/api/v1/borrow`)

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/borrow` | Issue/borrow a book | `201 Created` |
| `PUT` | `/api/v1/borrow/{id}/return` | Return a borrowed book | `200 OK` |
| `GET` | `/api/v1/borrow/{id}` | Get borrow record by ID | `200 OK` |
| `GET` | `/api/v1/borrow` | List all borrow records (paginated) | `200 OK` |
| `GET` | `/api/v1/borrow/member/{memberId}` | Get borrowing history for a member | `200 OK` |
| `GET` | `/api/v1/borrow/member/{memberId}/active` | Get currently unreturned books for member | `200 OK` |
| `GET` | `/api/v1/borrow/status/{status}` | Filter loans by status (`BORROWED`, `RETURNED`) | `200 OK` |
| `GET` | `/api/v1/borrow/overdue` | Get all overdue borrow records | `200 OK` |

---

## Sample curl Requests

### 1. Add a Book
```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "category": "Software Engineering",
    "totalCopies": 5,
    "publicationYear": 2008
  }'
```

### 2. Register a Member
```bash
curl -X POST http://localhost:8080/api/v1/members \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice.johnson@example.com",
    "phone": "+1-555-0199",
    "status": "ACTIVE"
  }'
```

### 3. Borrow a Book
```bash
curl -X POST http://localhost:8080/api/v1/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "memberId": 1,
    "borrowDays": 14,
    "remarks": "Project preparation"
  }'
```

### 4. Return a Book
```bash
curl -X PUT http://localhost:8080/api/v1/borrow/1/return
```

### 5. Check Active Borrowings for a Member
```bash
curl -X GET http://localhost:8080/api/v1/borrow/member/1/active
```

---

## Testing

The project includes unit tests for all services and WebMvc slice tests for all controllers.

Automated tests use an in-memory H2 database with PostgreSQL compatibility, ensuring tests run isolated and fast without requiring external services:

```bash
# Run all automated tests
./mvnw test
```

### Test Suites Included:
- **`LibraryManagementApplicationTests`**: Spring application context loading test.
- **`BookServiceTest`**: Mockito unit tests for book inventory business rules, duplicate ISBN validation, and delete constraints.
- **`MemberServiceTest`**: Unit tests for member registration, duplicate email rejection, and active loan constraints.
- **`BorrowServiceTest`**: Unit tests for loan creation, copy decrement, inactive member rejection, max loan limit enforcement, and return/late-fee calculations.
- **`BookControllerTest`**: WebMvc slice tests with JSON validation and HTTP status codes.
- **`MemberControllerTest`**: WebMvc slice tests for member endpoints.
- **`BorrowControllerTest`**: WebMvc slice tests for borrow and return endpoints.

---

## Postman Collection

Import the included Postman collection to test all endpoints interactively:

1. Open Postman.
2. Click **Import**.
3. Select `postman/Library-Management-System.postman_collection.json`.
4. Run requests using the pre-configured collection variables (`baseUrl`: `http://localhost:8080`).
