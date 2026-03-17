# Smart Employee Management System
## UC1 — Project Foundation

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Architecture Overview](#4-architecture-overview)
5. [Database Configuration](#5-database-configuration)
6. [Database Schema & Entity Details](#6-database-schema--entity-details)
7. [Relationships Between Entities](#7-relationships-between-entities)
8. [DTO Architecture](#8-dto-architecture)
9. [API Endpoints — Complete Reference](#9-api-endpoints--complete-reference)
10. [Security Configuration](#10-security-configuration)
11. [Custom UserDetails Service](#11-custom-userdetails-service)
12. [Password Encryption — BCrypt](#12-password-encryption--bcrypt)
13. [Global Exception Handling](#13-global-exception-handling)
14. [Error Response Structure](#14-error-response-structure)
15. [Sample API Requests & Responses](#15-sample-api-requests--responses)
16. [Git Workflow](#16-git-workflow)
17. [How to Run the Project](#17-how-to-run-the-project)
18. [UC1 Completion Checklist](#18-uc1-completion-checklist)
19. [Known Notes & Improvement Points for UC2+](#19-known-notes--improvement-points-for-uc2)

---

## 1. Project Overview

**Smart Employee Management System** is an enterprise-grade backend REST API built using Spring Boot. The goal of this system is to manage employees, departments, users, and roles with a clean layered architecture, proper security, and structured exception handling.

UC1 establishes the entire project foundation — database connectivity, all entities and their relationships, CRUD operations, user registration, login with BCrypt-encrypted passwords, Spring Security integration, and a global exception handler.

| Property | Value |
|---|---|
| Project Name | `smart-employee-management` |
| Base Package | `com.vishal.employeesystem` |
| Spring Boot Version | 3.x (Jakarta EE namespace) |
| Java Version | Java 17 |
| Build Tool | Maven |
| Database | MySQL |
| Architecture | Controller → DTO → Service → Repository → Entity → DB |

---

## 2. Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core programming language |
| Spring Boot 3.x | Application framework |
| Spring Web (MVC) | REST API layer |
| Spring Data JPA | ORM and database abstraction |
| Spring Security | Authentication and request authorization |
| Hibernate | JPA implementation / DDL management |
| MySQL 8.x | Relational database |
| BCrypt (via Spring Security) | Password hashing |
| Lombok | Boilerplate reduction (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) |
| Jakarta Validation | Bean validation annotations on entities and DTOs |
| Maven | Dependency management and build |
| Postman | API testing |

---

## 3. Project Structure

```
smart-employee-management/
└── src/
    └── main/
        └── java/
            └── com/vishal/employeesystem/
                ├── config/
                │   └── SecurityConfig.java               ← Spring Security configuration
                ├── controller/
                │   ├── AuthController.java               ← Login endpoint
                │   ├── DepartmentController.java         ← Department CRUD
                │   ├── EmployeeController.java           ← Employee CRUD
                │   ├── RoleController.java               ← Role management
                │   └── UserController.java               ← User registration
                ├── dto/
                │   ├── EmployeeRequestDTO.java           ← Employee create/update input
                │   ├── LoginRequestDTO.java              ← Login input
                │   └── RegisterRequestDTO.java           ← User registration input
                ├── entity/
                │   ├── Department.java                   ← Department table
                │   ├── Employee.java                     ← Employee table
                │   ├── Role.java                         ← Roles table
                │   └── User.java                         ← Users table
                ├── exception/
                │   ├── ErrorResponse.java                ← Structured API error body
                │   ├── GlobalExceptionHandler.java       ← Centralized exception handling
                │   └── ResourceNotFoundException.java    ← Custom 404 exception
                ├── repository/
                │   ├── DepartmentRepository.java         ← JPA repository for Department
                │   ├── EmployeeRepository.java           ← JPA repository for Employee
                │   ├── RoleRepository.java               ← JPA repository for Role
                │   └── UserRepository.java               ← JPA repository for User
                ├── security/
                │   └── CustomUserDetailsService.java     ← Loads user from DB for Spring Security
                └── service/
                    ├── DepartmentService.java            ← Department business logic
                    ├── EmployeeService.java              ← Employee business logic
                    ├── RoleService.java                  ← Role business logic
                    └── UserService.java                  ← User registration business logic
└── src/main/resources/
    └── application.properties                           ← DB config, JPA settings
```

---

## 4. Architecture Overview

```
HTTP Request
     │
     ▼
  Controller          ← Receives HTTP request, delegates to Service
     │
     ▼
    DTO               ← Input data object — validated, never the raw entity
     │
     ▼
  Service             ← All business logic lives here
     │
     ▼
 Repository           ← Spring Data JPA interface — talks to DB
     │
     ▼
  Entity              ← JPA-mapped class — mirrors database table
     │
     ▼
 MySQL Database
```

**Key Principles followed in UC1:**
- Controllers never talk to repositories directly
- Entities are never exposed as API response bodies in the Employee module (DTO used for input)
- Business logic is never placed in controllers
- Exception handling is centralised — no try/catch blocks inside services or controllers

---

## 5. Database Configuration

**File:** `src/main/resources/application.properties`

```properties
spring.application.name=smart-employee-management

# MySQL DataSource
spring.datasource.url=jdbc:mysql://localhost:3306/employee_db
spring.datasource.username=root
spring.datasource.password=Vishal@2004

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

| Property | Value | Meaning |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/employee_db` | Local MySQL, database name = `employee_db` |
| `spring.datasource.username` | `root` | MySQL user |
| `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate auto-creates/updates tables on startup |
| `spring.jpa.show-sql` | `true` | Prints every SQL query to console |
| `hibernate.dialect` | `MySQLDialect` | Tells Hibernate to generate MySQL-compatible SQL |

> **Note:** `ddl-auto=update` is appropriate for development. For production this should be set to `validate` and migrations managed via Flyway or Liquibase.

---

## 6. Database Schema & Entity Details

### 6.1 `employee` Table

**Entity:** `com.vishal.employeesystem.entity.Employee`

```java
@Entity
@Table(
    name = "employee",
    uniqueConstraints = { @UniqueConstraint(columnNames = "email") },
    indexes = { @Index(name = "idx_email", columnList = "email") }
)
```

| Column | Java Field | Type | Constraints |
|---|---|---|---|
| `id` | `id` | `Long` | Primary Key, Auto Increment (IDENTITY) |
| `name` | `name` | `String` | NOT NULL, max 100 chars |
| `email` | `email` | `String` | NOT NULL, UNIQUE, indexed, valid email format |
| `salary` | `salary` | `Double` | NOT NULL, must be positive |
| `department_id` | `department` | `Long` (FK) | Foreign Key → `department.id` |

**Annotations used:**
- `@NotNull` on `name`, `email`, `salary`
- `@Size(max=100)` on `name`
- `@Email` on `email`
- `@Positive` on `salary`
- `@ManyToOne` + `@JoinColumn(name="department_id")` for relationship
- `@UniqueConstraint` on `email` at table level
- `@Index` on `email` for fast lookups

---

### 6.2 `department` Table

**Entity:** `com.vishal.employeesystem.entity.Department`

```java
@Entity
@Table(name = "department")
```

| Column | Java Field | Type | Constraints |
|---|---|---|---|
| `id` | `id` | `Long` | Primary Key, Auto (GenerationType.AUTO) |
| `name` | `name` | `String` | No DB constraint currently |
| `location` | `location` | `String` | No DB constraint currently |

---

### 6.3 `roles` Table

**Entity:** `com.vishal.employeesystem.entity.Role`

```java
@Entity
@Table(name = "roles")
```

| Column | Java Field | Type | Constraints |
|---|---|---|---|
| `id` | `id` | `Long` | Primary Key, Auto (GenerationType.AUTO) |
| `role_name` | `roleName` | `String` | UNIQUE |

---

### 6.4 `users` Table

**Entity:** `com.vishal.employeesystem.entity.User`

```java
@Entity
@Table(name = "users")
```

| Column | Java Field | Type | Constraints |
|---|---|---|---|
| `id` | `id` | `Long` | Primary Key, Auto (GenerationType.AUTO) |
| `username` | `username` | `String` | UNIQUE |
| `email` | `email` | `String` | Valid email format |
| `password` | `password` | `String` | Stored BCrypt-hashed |
| `enable` | `enable` | `boolean` | Default = `true` |

---

### 6.5 `user_roles` Join Table (Many-to-Many)

Auto-created by JPA from `@JoinTable` on `User.java`:

| Column | References |
|---|---|
| `user_id` | `users.id` |
| `role_id` | `roles.id` |

---

## 7. Relationships Between Entities

| Relationship | From | To | Type | JPA Annotation | Join |
|---|---|---|---|---|---|
| Employee → Department | `Employee` | `Department` | Many-to-One | `@ManyToOne` + `@JoinColumn(name="department_id")` | FK column in `employee` table |
| User → Role | `User` | `Role` | Many-to-Many | `@ManyToMany` + `@JoinTable` | Join table `user_roles` |

**Many-to-One (Employee → Department):**
- Many employees can belong to one department
- The `department_id` foreign key column lives in the `employee` table
- When an employee is fetched, their department object is loaded via join

**Many-to-Many (User → Role):**
- One user can have multiple roles
- One role can be assigned to multiple users
- Managed through the `user_roles` join table
- Java type on `User` is `Set<Role>` to prevent duplicate role assignments

---

## 8. DTO Architecture

DTOs (Data Transfer Objects) are used as the input layer for API requests. Entities are never directly received from or sent to the client in write operations.

### 8.1 `EmployeeRequestDTO`

Used for: `POST /employees` and `PUT /employees/{id}`

| Field | Type | Validation |
|---|---|---|
| `name` | `String` | `@NotBlank`, `@Size(min=2, max=50)` |
| `email` | `String` | `@NotBlank`, `@Email` |
| `salary` | `Double` | `@Positive` |
| `departmentId` | `Long` | `@NotNull`, `@Positive` |

### 8.2 `LoginRequestDTO`

Used for: `POST /auth/login`

| Field | Type | Validation |
|---|---|---|
| `username` | `String` | `@NotBlank` |
| `password` | `String` | `@NotBlank` |

### 8.3 `RegisterRequestDTO`

Used for: `POST /users/register`

| Field | Type | Validation |
|---|---|---|
| `username` | `String` | `@NotBlank`, `@Size(min=3, max=30)` |
| `email` | `String` | `@NotBlank`, `@Email` |
| `password` | `String` | `@NotBlank`, `@Size(min=8, max=64)` |
| `roleIds` | `Set<Long>` | Optional — assigns roles by ID on registration |

> All DTOs use Lombok `@Data` for auto-generated getters, setters, equals, hashCode, and toString.

---

## 9. API Endpoints — Complete Reference

### 9.1 Authentication

| Method | URL | Description | Auth Required | Request Body |
|---|---|---|---|---|
| POST | `/auth/login` | Authenticate a registered user | No | `LoginRequestDTO` |

**`POST /auth/login`**
- Accepts `username` and `password`
- Internally calls `AuthenticationManager.authenticate()`
- Validates credentials against DB via `CustomUserDetailsService`
- Returns `"Login successful"` string on success
- Returns `401 Unauthorized` on bad credentials (handled by Spring Security)

---

### 9.2 User Management

| Method | URL | Description | Auth Required | Request Body |
|---|---|---|---|---|
| POST | `/users/register` | Register a new user with optional roles | No | `RegisterRequestDTO` |

**`POST /users/register`**
- Accepts username, email, password, and optional `roleIds`
- Password is BCrypt-hashed before saving to DB
- Roles are fetched by their IDs and assigned to the user
- Returns the saved `User` entity as response

---

### 9.3 Role Management

| Method | URL | Description | Auth Required | Request Body |
|---|---|---|---|---|
| POST | `/roles` | Create a new role | No (currently) | `Role` entity directly |
| GET | `/roles` | Fetch all roles | No (currently) | — |

> Note: `RoleController` currently accepts the `Role` entity directly as `@RequestBody`, not a DTO. This is an improvement point for future use cases.

---

### 9.4 Department Management

| Method | URL | Description | Auth Required | Request Body |
|---|---|---|---|---|
| POST | `/department` | Create a new department | No (currently) | `Department` entity directly |
| GET | `/department` | Fetch all departments | No (currently) | — |

> Note: `DepartmentController` currently accepts the `Department` entity directly as `@RequestBody`, not a DTO. This is an improvement point for future use cases.

---

### 9.5 Employee Management

| Method | URL | Description | Auth Required | Request Body / Params |
|---|---|---|---|---|
| POST | `/employees` | Create a new employee | No (currently) | `EmployeeRequestDTO` |
| GET | `/employees` | Fetch all employees | No (currently) | — |
| GET | `/employees/{id}` | Fetch employee by ID | No (currently) | Path variable: `id` |
| PUT | `/employees/{id}/{salary}` | Update employee salary by ID | No (currently) | Path variables: `id`, `salary` |
| DELETE | `/employees/delete/{id}` | Delete employee by ID | No (currently) | Path variable: `id` |

**`POST /employees`** — uses `EmployeeRequestDTO` with `@Valid`

**`PUT /employees/{id}/{salary}`** — updates only the salary field via path variables. This is a simplified implementation; a full update DTO will be used in later use cases.

**`DELETE /employees/delete/{id}`** — returns a confirmation string on success, throws `ResourceNotFoundException` if employee not found.

---

## 10. Security Configuration

**File:** `com.vishal.employeesystem.config.SecurityConfig`

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
        return auth.build();
    }
}
```

| Setting | Current Value | Explanation |
|---|---|---|
| CSRF | Disabled | Required for stateless REST APIs — no session cookie to protect |
| Authorization | `anyRequest().permitAll()` | All endpoints open during development/UC1 |
| PasswordEncoder | `BCryptPasswordEncoder` | Industry-standard adaptive hash for passwords |
| AuthenticationManager | Custom bean | Wired with `CustomUserDetailsService` + BCrypt encoder |

**Commented-out block (production intent):**
The actual production security rules are already written and commented in the file:
```java
.requestMatchers("/auth/**").permitAll()
.requestMatchers("/users/**").permitAll()
.requestMatchers("/roles/**").permitAll()
.anyRequest().authenticated()
```
These will be activated in a future use case (UC3 — JWT Security).

---

## 11. Custom UserDetails Service

**File:** `com.vishal.employeesystem.security.CustomUserDetailsService`

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
```

**How it works:**
- Spring Security calls `loadUserByUsername()` automatically during login
- It fetches the `User` record from MySQL by username via `UserRepository`
- If user is not found, throws `UsernameNotFoundException` → Spring Security returns `401`
- Returns a Spring `UserDetails` object built with the stored BCrypt-hashed password and a hardcoded `"USER"` authority
- Spring Security then compares the incoming plain-text password against the stored hash using BCrypt

---

## 12. Password Encryption — BCrypt

BCrypt is configured as the `PasswordEncoder` bean in `SecurityConfig`.

**Registration flow:**
```
Plain-text password (from RegisterRequestDTO)
        │
        ▼
  passwordEncoder.encode(password)   ← BCrypt generates a salted hash
        │
        ▼
  Stored in DB as: $2a$10$xxxxxxxxxxxxxxxxxxxxx...
```

**Login flow:**
```
Plain-text password (from LoginRequestDTO)
        │
        ▼
  AuthenticationManager.authenticate()
        │
        ▼
  CustomUserDetailsService.loadUserByUsername()  ← fetches hashed password from DB
        │
        ▼
  BCrypt.matches(plain, hashed)  ← Spring Security does this internally
        │
        ▼
  Match → authenticated / No match → 401
```

BCrypt properties:
- Each password gets a unique salt automatically — same password hashed twice gives different results
- Cost factor is 10 by default (configurable) — computationally expensive to brute-force
- The stored hash includes the algorithm version, cost, and salt embedded in the string

---

## 13. Global Exception Handling

**File:** `com.vishal.employeesystem.exception.GlobalExceptionHandler`

Annotated with `@RestControllerAdvice` — intercepts exceptions thrown anywhere in the application and converts them into structured JSON responses. No try/catch blocks are needed in controllers or services.

| Exception | HTTP Status | Handler Method | When triggered |
|---|---|---|---|
| `MethodArgumentNotValidException` | 400 Bad Request | `handleValidationErrors()` | `@Valid` constraint fails on a DTO |
| `ResourceNotFoundException` | 404 Not Found | `handleResourceNotFound()` | Entity not found in DB by ID |
| `Exception` (generic) | 500 Internal Server Error | `handleGenericException()` | Any unhandled runtime exception |

**Handler for validation errors** — collects ALL field errors into a map:
```java
ex.getBindingResult()
  .getFieldErrors()
  .forEach(error ->
      fieldErrors.put(error.getField(), error.getDefaultMessage())
  );
```

**`ResourceNotFoundException`** is a custom `RuntimeException`:
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```
Thrown from service layer like: `throw new ResourceNotFoundException("Employee not found with id: " + id)`

---

## 14. Error Response Structure

**File:** `com.vishal.employeesystem.exception.ErrorResponse`

All API error responses follow a single, consistent JSON structure. Fields are conditionally included using `@JsonInclude(NON_NULL)` — null fields are omitted from the JSON output.

```json
// Validation error (400) — has "errors" map, no "message"
{
  "timestamp": "2026-03-17T10:45:22.318",
  "status": 400,
  "errors": {
    "fieldName": "error message",
    "anotherField": "another error message"
  }
}

// Resource not found (404) — has "message", no "errors"
{
  "timestamp": "2026-03-17T10:47:01.104",
  "status": 404,
  "message": "Employee not found with id: 99"
}

// Server error (500) — has "message", no "errors"
{
  "timestamp": "2026-03-17T10:50:00.000",
  "status": 500,
  "message": "An unexpected error occurred. Please contact support."
}
```

**Static factory methods used:**
- `ErrorResponse.of(status, message)` — for 404, 500
- `ErrorResponse.ofValidation(status, errorsMap)` — for 400 validation errors

---

## 15. Sample API Requests & Responses

### Register a new user

```http
POST /users/register
Content-Type: application/json

{
  "username": "vishal",
  "email": "vishal@company.com",
  "password": "Vishal@2004",
  "roleIds": [1]
}
```

```json
// 200 OK
{
  "id": 1,
  "username": "vishal",
  "email": "vishal@company.com",
  "password": "$2a$10$...(BCrypt hash)...",
  "enable": true,
  "roles": [{ "id": 1, "roleName": "ROLE_ADMIN" }]
}
```

---

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "vishal",
  "password": "Vishal@2004"
}
```

```
200 OK
Login succesfull
```

---

### Create a Department

```http
POST /department
Content-Type: application/json

{
  "name": "Engineering",
  "location": "Pune"
}
```

```json
// 200 OK
{
  "id": 1,
  "name": "Engineering",
  "location": "Pune"
}
```

---

### Create an Employee

```http
POST /employees
Content-Type: application/json

{
  "name": "Arjun Sharma",
  "email": "arjun@company.com",
  "salary": 75000.0,
  "departmentId": 1
}
```

```json
// 200 OK
{
  "id": 1,
  "name": "Arjun Sharma",
  "email": "arjun@company.com",
  "salary": 75000.0,
  "department": {
    "id": 1,
    "name": "Engineering",
    "location": "Pune"
  }
}
```

---

### Validation failure — POST /employees with bad data

```http
POST /employees
Content-Type: application/json

{
  "name": "",
  "email": "not-an-email",
  "salary": -500,
  "departmentId": null
}
```

```json
// 400 Bad Request
{
  "timestamp": "2026-03-17T10:45:22.318",
  "status": 400,
  "errors": {
    "name": "Name is required ",
    "email": "Email should be valid ",
    "salary": "salary should be positive ",
    "departmentId": "Department ID is required "
  }
}
```

---

### Resource not found — GET /employees/999

```http
GET /employees/999
```

```json
// 404 Not Found
{
  "timestamp": "2026-03-17T10:47:01.104",
  "status": 404,
  "message": "Employee not found with id: 999"
}
```

---

## 16. Git Workflow

```
main          ← clean, production-ready only
  └── dev     ← integration branch
        └── feature/uc1-project-foundation  ← UC1 work (completed and merged)
```

**Commands followed:**

```bash
# Create feature branch
git checkout dev
git checkout -b feature/uc1-project-foundation

# After completing UC1
git add .
git commit -m "feat(uc1): project foundation - entities, CRUD, auth, security, exception handling"
git push origin feature/uc1-project-foundation

# Merge into dev (via PR or direct)
git checkout dev
git merge feature/uc1-project-foundation
git push origin dev
```

---

## 17. How to Run the Project

### Prerequisites

| Requirement | Version |
|---|---|
| Java | 17 or higher |
| Maven | 3.6+ |
| MySQL | 8.x |

### Step 1 — Create the Database

```sql
CREATE DATABASE employee_db;
```

### Step 2 — Update credentials (if different)

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### Step 3 — Build and Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

The server starts on: `http://localhost:8080`

Hibernate will auto-create all tables on first startup (`ddl-auto=update`).

### Step 4 — Test with Postman

Recommended test order:
1. `POST /roles` — create at least one role (e.g. `ROLE_ADMIN`)
2. `POST /department` — create at least one department
3. `POST /users/register` — register a user with the role ID from step 1
4. `POST /auth/login` — verify login works
5. `POST /employees` — create an employee with the department ID from step 2
6. `GET /employees` — verify list
7. `GET /employees/{id}` — verify single fetch
8. `PUT /employees/{id}/{salary}` — update salary
9. `DELETE /employees/delete/{id}` — delete and verify 404 on re-fetch

---

## 18. UC1 Completion Checklist

| # | Task | Status |
|---|---|---|
| 1 | Spring Boot project setup with Maven | ✅ Done |
| 2 | MySQL database connection configured | ✅ Done |
| 3 | Layered architecture implemented (Controller → Service → Repository → Entity) | ✅ Done |
| 4 | `Department` entity + CRUD API | ✅ Done |
| 5 | `Employee` entity + full CRUD API | ✅ Done |
| 6 | Employee → Department Many-to-One relationship | ✅ Done |
| 7 | `EmployeeRequestDTO` — input DTO with validation annotations | ✅ Done |
| 8 | `Role` entity + API | ✅ Done |
| 9 | `User` entity with `enable` flag | ✅ Done |
| 10 | User → Role Many-to-Many relationship with `user_roles` join table | ✅ Done |
| 11 | `RegisterRequestDTO` with role IDs support | ✅ Done |
| 12 | `POST /users/register` — user registration with BCrypt password | ✅ Done |
| 13 | `LoginRequestDTO` | ✅ Done |
| 14 | `POST /auth/login` — login via `AuthenticationManager` | ✅ Done |
| 15 | BCrypt `PasswordEncoder` bean configured | ✅ Done |
| 16 | `CustomUserDetailsService` — loads user from DB for Spring Security | ✅ Done |
| 17 | `SecurityConfig` — CSRF disabled, all routes open for dev | ✅ Done |
| 18 | `ResourceNotFoundException` custom exception | ✅ Done |
| 19 | `ErrorResponse` — structured error body with `@JsonInclude(NON_NULL)` | ✅ Done |
| 20 | `GlobalExceptionHandler` — handles 400, 404, 500 | ✅ Done |
| 21 | Git branching — feature branch created, completed, and merged to dev | ✅ Done |

---

## 19. Known Notes & Improvement Points for UC2+

These are not bugs — they are intentional simplifications in UC1 that will be addressed in future use cases.

| # | Current State | Planned Improvement |
|---|---|---|
| 1 | `SecurityConfig` has `anyRequest().permitAll()` — all endpoints are open | UC3: Activate role-based authorization with JWT |
| 2 | `DepartmentController` accepts raw `Department` entity as `@RequestBody` | UC2/UC3: Introduce `DepartmentRequestDTO` with validation |
| 3 | `RoleController` accepts raw `Role` entity as `@RequestBody` | UC2/UC3: Introduce `RoleRequestDTO` |
| 4 | Employee `PUT` updates only salary via path variables `/{id}/{salary}` | Future: Accept a full `EmployeeUpdateDTO` in request body |
| 5 | `CustomUserDetailsService` hardcodes authority as `"USER"` for all users | UC3: Map actual roles from DB to `GrantedAuthority` list |
| 6 | `User` entity exposes hashed password in register API response | Future: Introduce `UserResponseDTO` to exclude sensitive fields |
| 7 | Validation annotations exist on `Employee` entity — JPA validation | Keep DTOs as the validation boundary; entity-level annotations are redundant with DTO validation |
| 8 | No response DTOs (only request DTOs exist) | Future: Add `EmployeeResponseDTO`, `UserResponseDTO` to control what is returned |
| 9 | Login returns plain string `"Login succesfull"` | UC3: Login will return a JWT token instead |
| 10 | `GenerationType.AUTO` used for Department, Role, User IDs | Prefer `GenerationType.IDENTITY` for MySQL (AUTO can behave unexpectedly) |

---

*README authored for UC1 — Smart Employee Management System*
*Next: UC2 — Validation Layer | UC3 — JWT Security*
