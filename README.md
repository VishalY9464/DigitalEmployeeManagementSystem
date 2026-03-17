# Smart Employee Management System
## UC2 — Validation Layer

---

## Table of Contents

1. [UC2 Overview](#1-uc2-overview)
2. [What Changed from UC1](#2-what-changed-from-uc1)
3. [Dependency Added](#3-dependency-added)
4. [Validation Annotations — Quick Reference](#4-validation-annotations--quick-reference)
5. [DTO Validation — Complete Detail](#5-dto-validation--complete-detail)
   - 5.1 [EmployeeRequestDTO](#51-employeerequestdto)
   - 5.2 [RegisterRequestDTO](#52-registerrequestdto)
   - 5.3 [LoginRequestDTO](#53-loginrequestdto)
6. [How @Valid Works in Controllers](#6-how-valid-works-in-controllers)
7. [Controllers Updated with @Valid](#7-controllers-updated-with-valid)
8. [ErrorResponse — Redesigned](#8-errorresponse--redesigned)
9. [GlobalExceptionHandler — Updated](#9-globalexceptionhandler--updated)
10. [How the Validation Pipeline Works End-to-End](#10-how-the-validation-pipeline-works-end-to-end)
11. [Error Response Structure — All Scenarios](#11-error-response-structure--all-scenarios)
12. [Sample API Requests & Responses](#12-sample-api-requests--responses)
13. [What Was NOT Changed](#13-what-was-not-changed)
14. [Architecture Principle — Validation Boundary](#14-architecture-principle--validation-boundary)
15. [Git Workflow](#15-git-workflow)
16. [UC2 Completion Checklist](#16-uc2-completion-checklist)
17. [Known Notes & Improvement Points for UC3](#17-known-notes--improvement-points-for-uc3)

---

## 1. UC2 Overview

UC2 introduces a formal **Validation Layer** into the Smart Employee Management System using **Jakarta Bean Validation (JSR-380)**. The goal is to reject invalid input at the API boundary before it ever reaches the service layer or database.

| Property | Value |
|---|---|
| Use Case | UC2 — Validation Layer |
| Specification | Jakarta Bean Validation (JSR-380) |
| Implementation | Hibernate Validator (reference implementation) |
| Trigger | `@Valid` annotation on controller method parameters |
| Error Handler | `GlobalExceptionHandler` → `MethodArgumentNotValidException` |
| Response Format | Structured JSON with `timestamp`, `status`, `errors` map |
| Validation Target | **DTOs only** — entities are never validated via API input |
| Branch | `feature/uc2-validation-layer` |

---

## 2. What Changed from UC1

| File | UC1 State | UC2 Change |
|---|---|---|
| `pom.xml` | No explicit validation dependency | Added `spring-boot-starter-validation` |
| `EmployeeRequestDTO.java` | Had validation annotations (partial) | Completed and confirmed — all fields covered |
| `RegisterRequestDTO.java` | Had validation annotations | Completed and confirmed — all fields covered |
| `LoginRequestDTO.java` | Had `@NotBlank` annotations | Confirmed complete |
| `EmployeeController.java` | Had `@Valid` on `POST` | Confirmed — `@Valid` on POST; PUT uses path variables (no DTO) |
| `AuthController.java` | Had `@Valid` on login | Confirmed complete |
| `UserController.java` | Had `@Valid` on register | Confirmed complete |
| `ErrorResponse.java` | Basic class | Redesigned with static factories + `@JsonInclude(NON_NULL)` |
| `GlobalExceptionHandler.java` | Had `ResourceNotFoundException` handler only | Added `MethodArgumentNotValidException` handler + generic `Exception` handler |

---

## 3. Dependency Added

**File:** `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**What this brings in:**
- `jakarta.validation-api` — the JSR-380 specification interfaces and annotations
- `hibernate-validator` — the reference implementation that executes the validation rules
- Version is managed automatically by Spring Boot's BOM — no explicit version needed

> **Why declare it explicitly?** In some Spring Boot versions `spring-boot-starter-web` pulls it transitively, but explicit declaration makes the intent clear, prevents issues on version upgrades, and is the production standard.

---

## 4. Validation Annotations — Quick Reference

All annotations used in UC2 come from the `jakarta.validation.constraints` package.

| Annotation | Applies To | What It Validates |
|---|---|---|
| `@NotBlank` | `String` | Not null + not empty + not whitespace-only (all three in one) |
| `@NotNull` | Any type | Not null — used on `Long`, `Double`, `Integer`, etc. |
| `@Email` | `String` | Must match a valid email format (contains `@`, valid domain) |
| `@Size(min, max)` | `String`, `Collection` | Length must be within the specified min/max range |
| `@Positive` | Numeric types | Value must be strictly greater than zero |

**Important rules:**
- `@NotBlank` includes null-check — you do NOT need `@NotNull` additionally on a `String`
- `@Positive` does NOT check for null — combine with `@NotNull` on `Double` fields if null is also invalid
- `@Email` does NOT check for blank — combine with `@NotBlank` when the field is required
- Use `@NotNull` (never `@NotBlank`) on `Long`, `Integer`, `Double` fields

---

## 5. DTO Validation — Complete Detail

### 5.1 `EmployeeRequestDTO`

**File:** `com.vishal.employeesystem.dto.EmployeeRequestDTO`
**Used by:** `POST /employees`

```java
@Data
public class EmployeeRequestDTO {

    @NotBlank(message = "Name is required ")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "Email is required ")
    @Email(message = "Email should be valid ")
    private String email;

    @Positive(message = "salary should be positive ")
    private Double salary;

    @NotNull(message = "Department ID is required ")
    @Positive(message = "Department ID must be a positive number ")
    private Long departmentId;
}
```

**Field-by-field breakdown:**

| Field | Type | Annotations | Valid Example | Invalid Example | Error Message |
|---|---|---|---|---|---|
| `name` | `String` | `@NotBlank` + `@Size(min=2,max=50)` | `"Arjun Sharma"` | `""` or `" "` | `"Name is required "` |
| `name` | `String` | `@Size(min=2,max=50)` | `"Ar"` | `"A"` | Default size message |
| `email` | `String` | `@NotBlank` + `@Email` | `"a@b.com"` | `"notanemail"` | `"Email should be valid "` |
| `salary` | `Double` | `@Positive` | `75000.0` | `-500.0` or `0.0` | `"salary should be positive "` |
| `departmentId` | `Long` | `@NotNull` + `@Positive` | `1` | `null` | `"Department ID is required "` |

> **Note:** `salary` has `@Positive` but no `@NotNull`. This means `null` salary will pass validation. This is an intentional design choice — salary may be optional at creation in this system. Flag for UC3+ review.

---

### 5.2 `RegisterRequestDTO`

**File:** `com.vishal.employeesystem.dto.RegisterRequestDTO`
**Used by:** `POST /users/register`

```java
@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Username is required ")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters ")
    private String username;

    @NotBlank(message = "Email is required ")
    @Email(message = "Email must be a valid address ")
    private String email;

    @NotBlank(message = "Password is required ")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters ")
    private String password;

    private Set<Long> roleIds;   // Optional — no validation constraint
}
```

**Field-by-field breakdown:**

| Field | Type | Annotations | Valid Example | Invalid Example | Error Message |
|---|---|---|---|---|---|
| `username` | `String` | `@NotBlank` + `@Size(min=3,max=30)` | `"vishal"` | `"ab"` | `"Username must be between 3 and 30 characters "` |
| `email` | `String` | `@NotBlank` + `@Email` | `"v@co.com"` | `"vishal"` | `"Email must be a valid address "` |
| `password` | `String` | `@NotBlank` + `@Size(min=8,max=64)` | `"Vishal@2004"` | `"abc"` | `"Password must be between 8 and 64 characters "` |
| `roleIds` | `Set<Long>` | None | `[1, 2]` or `null` | — | No validation — optional field |

---

### 5.3 `LoginRequestDTO`

**File:** `com.vishal.employeesystem.dto.LoginRequestDTO`
**Used by:** `POST /auth/login`

```java
@Data
public class LoginRequestDTO {

    @NotBlank(message = "Username is required ")
    private String username;

    @NotBlank(message = "Password is required ")
    private String password;
}
```

**Field-by-field breakdown:**

| Field | Type | Annotation | Valid Example | Invalid Example | Error Message |
|---|---|---|---|---|---|
| `username` | `String` | `@NotBlank` | `"vishal"` | `""` or `null` | `"Username is required "` |
| `password` | `String` | `@NotBlank` | `"Vishal@2004"` | `""` or `" "` | `"Password is required "` |

> Login DTO intentionally has no `@Size` or `@Email` on username — the system supports username-based login, not email-based. Credential validation (wrong password) is handled by Spring Security's `AuthenticationManager`, not Bean Validation.

---

## 6. How `@Valid` Works in Controllers

When `@Valid` is placed on a `@RequestBody` parameter, Spring Boot triggers the following pipeline automatically:

```
HTTP POST /employees  (JSON body arrives)
         │
         ▼
  Jackson deserializes JSON → EmployeeRequestDTO object
         │
         ▼
  Hibernate Validator runs all @NotBlank, @Email, @Size, etc.
  on every field of the DTO
         │
         ├─── All constraints PASS?
         │         │
         │         ▼
         │    Controller method executes normally
         │    → Service called → DB saved → 200/201 returned
         │
         └─── Any constraint FAILS?
                   │
                   ▼
             MethodArgumentNotValidException is thrown
             BEFORE the controller method body executes
                   │
                   ▼
             GlobalExceptionHandler.handleValidationErrors()
             collects all field errors → returns 400 JSON
```

**Key point:** The controller method body **never executes** if validation fails. The service layer is never called. The database is never touched.

---

## 7. Controllers Updated with `@Valid`

### `EmployeeController.java`

```java
@PostMapping
public ResponseEntity<Employee> addEmployee(
        @Valid @RequestBody EmployeeRequestDTO dto) {   // ← @Valid triggers validation
    Employee emp = employeeService.addEmployee(dto);
    return ResponseEntity.ok(emp);
}
```

`PUT /employees/{id}/{salary}` — does **not** use a DTO (salary passed as path variable), so `@Valid` is not applicable there.

---

### `AuthController.java`

```java
@PostMapping("/login")
public String login(
        @Valid @RequestBody LoginRequestDTO request) {  // ← @Valid triggers validation
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()));
    return "Login succesfull";
}
```

---

### `UserController.java`

```java
@PostMapping("/register")
public User registerUser(
        @Valid @RequestBody RegisterRequestDTO dto) {   // ← @Valid triggers validation
    return userService.registerUser(dto);
}
```

---

### `RoleController.java`

```java
@PostMapping
public Role createRole(
        @Valid @RequestBody Role role) {                // ← @Valid present
    return roleService.createRole(role);
}
```

> Note: `RoleController` has `@Valid` but validates the `Role` entity directly (no DTO). The `Role` entity has `@Column(unique=true)` on `roleName` but no Bean Validation annotations — so `@Valid` has no effect here in UC2. A `RoleRequestDTO` with proper constraints is the improvement point for a future UC.

---

### `DepartmentController.java`

```java
@PostMapping
public Department createDepartment(
        @RequestBody Department department) {           // ← No @Valid
    return departmentService.createDepartment(department);
}
```

> `DepartmentController` does **not** have `@Valid`. The `Department` entity has no Bean Validation annotations. No validation occurs on department creation in UC2. A `DepartmentRequestDTO` is the improvement point for a future UC.

---

## 8. ErrorResponse — Redesigned

**File:** `com.vishal.employeesystem.exception.ErrorResponse`

The `ErrorResponse` class was completely redesigned in UC2 to serve two distinct error scenarios with a single class, using static factory methods and `@JsonInclude(NON_NULL)` to keep the JSON clean.

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;               // Used for 404, 500
    private Map<String, String> errors;   // Used for 400 validation errors

    private ErrorResponse() {}            // Private — no direct instantiation

    public static ErrorResponse of(int status, String message) { ... }
    public static ErrorResponse ofValidation(int status, Map<String, String> errors) { ... }
}
```

**Design decisions explained:**

| Decision | Reason |
|---|---|
| Private constructor | Forces use of named factory methods — intent is explicit at the call site |
| `ErrorResponse.of()` | For single-message errors: sets `timestamp`, `status`, `message`. Leaves `errors` null. |
| `ErrorResponse.ofValidation()` | For field errors: sets `timestamp`, `status`, `errors` map. Leaves `message` null. |
| `@JsonInclude(NON_NULL)` | Null fields are excluded from JSON output — no `"message": null` or `"errors": null` clutter |
| `LocalDateTime timestamp` | Every error carries a timestamp for log correlation and debugging |
| `Map<String, String> errors` | Key = field name, Value = constraint message. Multiple field errors in one response. |
| `LinkedHashMap` in handler | Preserves field declaration order in the response — deterministic, testable |
| No Lombok on this class | Explicit getters only — no setters, no `@Data` — response body is effectively immutable |

---

## 9. GlobalExceptionHandler — Updated

**File:** `com.vishal.employeesystem.exception.GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handler 1: DTO validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error ->
              fieldErrors.put(error.getField(), error.getDefaultMessage())
          );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofValidation(400, fieldErrors));
    }

    // Handler 2: Entity not found in DB
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, ex.getMessage()));
    }

    // Handler 3: Safety net — all unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500,
                    "An unexpected error occurred. Please contact support."));
    }
}
```

**How the three handlers work:**

| Handler | Exception Type | HTTP Status | When Triggered | Response Fields |
|---|---|---|---|---|
| `handleValidationErrors` | `MethodArgumentNotValidException` | 400 | `@Valid` finds a constraint violation | `timestamp`, `status`, `errors` map |
| `handleResourceNotFound` | `ResourceNotFoundException` | 404 | Service throws `new ResourceNotFoundException(...)` | `timestamp`, `status`, `message` |
| `handleGenericException` | `Exception` (superclass) | 500 | Any unhandled runtime exception anywhere in app | `timestamp`, `status`, `message` (generic, safe) |

**`@RestControllerAdvice` explained:**
- Combines `@ControllerAdvice` + `@ResponseBody`
- Intercepts exceptions thrown from any `@RestController` in the application
- Spring matches the most specific exception type first — `MethodArgumentNotValidException` is matched before the generic `Exception` handler

**`ex.getBindingResult().getFieldErrors()`:**
- `getBindingResult()` — returns the full validation result object from Hibernate Validator
- `getFieldErrors()` — returns a list of all field-level constraint violations
- Each error has `.getField()` (the DTO field name) and `.getDefaultMessage()` (the message from the annotation)
- `LinkedHashMap` preserves insertion order → fields appear in the response in the same order they appear in the DTO class

---

## 10. How the Validation Pipeline Works End-to-End

### Scenario A — All fields valid

```
POST /employees  { "name": "Arjun", "email": "a@b.com", "salary": 50000, "departmentId": 1 }
        │
        ▼
  Jackson parses JSON → EmployeeRequestDTO populated
        │
        ▼
  @Valid triggers Hibernate Validator
  → name: passes @NotBlank and @Size ✓
  → email: passes @NotBlank and @Email ✓
  → salary: passes @Positive ✓
  → departmentId: passes @NotNull and @Positive ✓
        │
        ▼
  EmployeeController.addEmployee() executes
        │
        ▼
  EmployeeService.addEmployee(dto) executes
        │
        ▼
  Employee saved to DB
        │
        ▼
  ResponseEntity.ok(savedEmployee) → 200 OK
```

---

### Scenario B — Validation fails

```
POST /employees  { "name": "", "email": "bad", "salary": -100, "departmentId": null }
        │
        ▼
  Jackson parses JSON → EmployeeRequestDTO populated
        │
        ▼
  @Valid triggers Hibernate Validator
  → name: FAILS @NotBlank ✗
  → email: FAILS @Email ✗
  → salary: FAILS @Positive ✗
  → departmentId: FAILS @NotNull ✗
        │
        ▼
  MethodArgumentNotValidException thrown
  Controller method body NEVER executes
  Service NEVER called
  DB NEVER touched
        │
        ▼
  GlobalExceptionHandler.handleValidationErrors() catches it
        │
        ▼
  Builds LinkedHashMap: { name→msg, email→msg, salary→msg, departmentId→msg }
        │
        ▼
  Returns ErrorResponse.ofValidation(400, fieldErrors)
        │
        ▼
  400 Bad Request JSON with all field errors
```

---

## 11. Error Response Structure — All Scenarios

### 400 Bad Request — Validation failure

Triggered by: `@Valid` on DTO with constraint violation
Has: `timestamp`, `status`, `errors`
Missing: `message` (excluded by `@JsonInclude(NON_NULL)`)

```json
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

### 404 Not Found — Resource missing

Triggered by: `throw new ResourceNotFoundException("Employee not found with id: 99")`
Has: `timestamp`, `status`, `message`
Missing: `errors` (excluded by `@JsonInclude(NON_NULL)`)

```json
{
  "timestamp": "2026-03-17T10:47:01.104",
  "status": 404,
  "message": "Employee not found with id: 99"
}
```

---

### 500 Internal Server Error — Unexpected exception

Triggered by: Any unhandled `Exception` in the application
Has: `timestamp`, `status`, `message`
Missing: `errors` (excluded by `@JsonInclude(NON_NULL)`)

```json
{
  "timestamp": "2026-03-17T10:50:00.000",
  "status": 500,
  "message": "An unexpected error occurred. Please contact support."
}
```

> **Security note:** The 500 handler returns a safe generic message. The actual exception details (`ex.getMessage()`, stack trace) are never exposed to the client — this is essential for production security.

---

## 12. Sample API Requests & Responses

### POST /employees — name blank, email invalid

```http
POST /employees
Content-Type: application/json

{
  "name": "",
  "email": "notvalid",
  "salary": 50000.0,
  "departmentId": 1
}
```

```json
// 400 Bad Request
{
  "timestamp": "2026-03-17T10:45:22.318",
  "status": 400,
  "errors": {
    "name": "Name is required ",
    "email": "Email should be valid "
  }
}
```

---

### POST /employees — departmentId null

```http
POST /employees
Content-Type: application/json

{
  "name": "Arjun Sharma",
  "email": "arjun@company.com",
  "salary": 75000.0,
  "departmentId": null
}
```

```json
// 400 Bad Request
{
  "timestamp": "2026-03-17T10:45:30.100",
  "status": 400,
  "errors": {
    "departmentId": "Department ID is required "
  }
}
```

---

### POST /employees — all fields valid

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
  "id": 3,
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

### POST /users/register — password too short, bad email

```http
POST /users/register
Content-Type: application/json

{
  "username": "ab",
  "email": "notanemail",
  "password": "123"
}
```

```json
// 400 Bad Request
{
  "timestamp": "2026-03-17T10:46:00.200",
  "status": 400,
  "errors": {
    "username": "Username must be between 3 and 30 characters ",
    "email": "Email must be a valid address ",
    "password": "Password must be between 8 and 64 characters "
  }
}
```

---

### POST /auth/login — empty credentials

```http
POST /auth/login
Content-Type: application/json

{
  "username": "",
  "password": ""
}
```

```json
// 400 Bad Request
{
  "timestamp": "2026-03-17T10:46:15.300",
  "status": 400,
  "errors": {
    "username": "Username is required ",
    "password": "Password is required "
  }
}
```

---

### POST /auth/login — valid but wrong credentials

```http
POST /auth/login
Content-Type: application/json

{
  "username": "vishal",
  "password": "wrongpassword"
}
```

```
// Spring Security handles this — not Bean Validation
401 Unauthorized  (or 403 depending on Security config)
```

> Passes `@Valid` because both fields are non-blank. Fails at `AuthenticationManager.authenticate()` — Spring Security throws `BadCredentialsException`. This is authentication failure, not validation failure — handled differently.

---

### GET /employees/999 — not found

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

## 13. What Was NOT Changed

This is an important section. UC2 is a pure addition — it did not modify any of the following:

| Component | Status |
|---|---|
| All 4 entities (`Employee`, `Department`, `Role`, `User`) | Unchanged |
| All 4 repositories | Unchanged |
| All service classes | Unchanged |
| `application.properties` | Unchanged |
| `SecurityConfig.java` | Unchanged |
| `CustomUserDetailsService.java` | Unchanged |
| Database schema | Unchanged |
| Git `main` and `dev` branches | Unchanged |
| `ResourceNotFoundException.java` | Unchanged |

**Architecture contract maintained:**
- Zero business logic added to controllers
- Zero validation logic added to service layer
- Zero changes to entity classes
- The separation of concerns from UC1 is fully preserved

---

## 14. Architecture Principle — Validation Boundary

UC2 enforces a strict rule: **the DTO layer is the validation boundary.**

```
HTTP Request (untrusted input)
        │
        ▼
   [ DTO + @Valid ]  ◄─── VALIDATION BOUNDARY
        │                  All input is validated HERE
        │                  Invalid input is REJECTED HERE
        │                  Service layer never sees invalid data
        ▼
   Service Layer     ◄─── Receives only valid, clean data
        │
        ▼
   Repository        ◄─── Only executes with valid data
        │
        ▼
   Database          ◄─── Only stores valid data
```

**Why validation belongs in DTOs and not in entities:**

| Location | Should Validate? | Reason |
|---|---|---|
| DTO | ✅ Yes | First point of entry — catches errors before any processing |
| Entity | ❌ No (for API input) | Entity is a DB mapping — its constraints belong at the DB level, not API level |
| Service | ❌ No (for format rules) | Service handles business rules (e.g. email already exists), not format rules |
| Repository | ❌ No | DB layer — should never receive invalid data |

> The `Employee` entity still has `@NotNull`, `@Email`, `@Positive` annotations from UC1. These are redundant now that `EmployeeRequestDTO` validates the same fields. They are harmless but considered an improvement point — entities should be kept clean of Bean Validation annotations when DTOs are in use.

---

## 15. Git Workflow

```
main
  └── dev
        ├── feature/uc1-project-foundation  (merged ✅)
        └── feature/uc2-validation-layer    (completed ✅)
```

**Commands:**

```bash
# Branch off dev
git checkout dev
git pull origin dev
git checkout -b feature/uc2-validation-layer

# After all UC2 changes
git add .
git commit -m "feat(uc2): add Jakarta validation layer with structured error response"
git push origin feature/uc2-validation-layer

# PR → dev → review → merge
git checkout dev
git merge feature/uc2-validation-layer
git push origin dev
```

**Files committed in this branch:**

| File | Change Type |
|---|---|
| `pom.xml` | Modified — added validation dependency |
| `dto/EmployeeRequestDTO.java` | Modified — validation annotations confirmed/added |
| `dto/RegisterRequestDTO.java` | Modified — validation annotations confirmed/added |
| `dto/LoginRequestDTO.java` | Modified — validation annotations confirmed/added |
| `exception/ErrorResponse.java` | Rewritten — static factories + `@JsonInclude` |
| `exception/GlobalExceptionHandler.java` | Modified — added 2 new exception handlers |
| `controller/EmployeeController.java` | Modified — `@Valid` confirmed on POST |
| `controller/AuthController.java` | Modified — `@Valid` confirmed on login |
| `controller/UserController.java` | Modified — `@Valid` confirmed on register |

---

## 16. UC2 Completion Checklist

| # | Task | Status |
|---|---|---|
| 1 | `spring-boot-starter-validation` dependency added to `pom.xml` | ✅ Done |
| 2 | `EmployeeRequestDTO` — `@NotBlank`, `@Email`, `@Size`, `@NotNull`, `@Positive` applied | ✅ Done |
| 3 | `RegisterRequestDTO` — `@NotBlank`, `@Email`, `@Size` applied on all required fields | ✅ Done |
| 4 | `LoginRequestDTO` — `@NotBlank` applied on username and password | ✅ Done |
| 5 | `@Valid` placed on `EmployeeController.addEmployee()` parameter | ✅ Done |
| 6 | `@Valid` placed on `AuthController.login()` parameter | ✅ Done |
| 7 | `@Valid` placed on `UserController.registerUser()` parameter | ✅ Done |
| 8 | `ErrorResponse` redesigned with static factory methods | ✅ Done |
| 9 | `ErrorResponse` uses `@JsonInclude(NON_NULL)` — null fields excluded from JSON | ✅ Done |
| 10 | `GlobalExceptionHandler` handles `MethodArgumentNotValidException` → 400 | ✅ Done |
| 11 | `GlobalExceptionHandler` handles `ResourceNotFoundException` → 404 | ✅ Done |
| 12 | `GlobalExceptionHandler` handles generic `Exception` → 500 | ✅ Done |
| 13 | All field errors collected in `LinkedHashMap` — ordered, deterministic output | ✅ Done |
| 14 | Validation on DTOs only — entities and service layer untouched | ✅ Done |
| 15 | Architecture intact — no business logic added to controllers | ✅ Done |
| 16 | Tested all validation scenarios in Postman | ✅ Done |
| 17 | Feature branch `feature/uc2-validation-layer` created and merged to `dev` | ✅ Done |

---

## 17. Known Notes & Improvement Points for UC3

| # | Current State | Planned Improvement |
|---|---|---|
| 1 | `SecurityConfig` still has `anyRequest().permitAll()` | UC3: JWT filter + role-based authorization |
| 2 | Login returns plain string `"Login succesfull"` | UC3: Return a JWT token in the response body |
| 3 | `DepartmentController` has no `@Valid` and no DTO | Future UC: Introduce `DepartmentRequestDTO` |
| 4 | `RoleController` validates raw `Role` entity — no DTO | Future UC: Introduce `RoleRequestDTO` |
| 5 | `salary` in `EmployeeRequestDTO` has `@Positive` but no `@NotNull` | Decide if null salary should be accepted; add `@NotNull` if not |
| 6 | `Employee` entity still has `@NotNull`, `@Email`, `@Positive` | Remove — redundant since DTO validates; entities should be clean |
| 7 | `CustomUserDetailsService` hardcodes authority `"USER"` | UC3: Load actual roles from DB and map to `GrantedAuthority` |
| 8 | No response DTOs — `User` entity exposes BCrypt password in register response | Future: Add `UserResponseDTO`, `EmployeeResponseDTO` |
| 9 | `PUT /employees/{id}/{salary}` uses path variables instead of a DTO | Future: `EmployeeUpdateRequestDTO` for proper update contract |
| 10 | Error messages have trailing spaces (e.g., `"Name is required "`) | Minor: Clean up whitespace in message strings |

---

*README authored for UC2 — Smart Employee Management System*
*Completed: UC1 — Project Foundation | UC2 — Validation Layer*
*Next: UC3 — JWT Authentication & Role-Based Authorization*
