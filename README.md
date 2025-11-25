# HMCTS Task Management Backend

A Spring Boot REST API for managing tasks in the HMCTS case management system.

## Technology Stack

- Java 21
- Spring Boot 3.5.7
- Spring Data JPA for database persistence
- H2 Database (in-memory) for development
- Jakarta Bean Validation for request validation
- SpringDoc OpenAPI for API documentation
- Gradle 8.14.3 as build tool
- JUnit 5 for testing
- Mockito for mocking

## Getting Started

### Prerequisites

- Java 21
- Gradle (wrapper included)

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

The application starts on http://localhost:4000

### Run Tests

```bash
./gradlew test          # Unit tests
./gradlew integration   # Integration tests
./gradlew check         # All tests + code quality checks
```

## API Documentation

Interactive API documentation is available via Swagger UI at http://localhost:4000/swagger-ui.html

## API Endpoints

### Create Task

Creates a new task in the system.

**Endpoint:** `POST /tasks`

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**

```json
{
  "title": "Review case documents",
  "description": "Review all submitted documents for case #12345",
  "dueDateTime": "2025-12-31T17:00:00"
}
```

**Request Fields:**

- `title` (String, required): The title of the task. Must not be blank. Maximum 255 characters.
- `description` (String, optional): A detailed description of the task. Maximum 1000 characters.
- `dueDateTime` (ISO DateTime, required): When the task is due. Must be a future date/time.

**Success Response:**

- Status: `201 Created`

```json
{
  "id": 1,
  "title": "Review case documents",
  "description": "Review all submitted documents for case #12345",
  "status": "PENDING",
  "dueDateTime": "2025-12-31T17:00:00",
  "createdAt": "2025-11-25T10:30:00"
}
```

**Validation Error Response:**

- Status: `400 Bad Request`

```json
{
  "timestamp": "2025-11-25T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields have invalid values",
  "path": "/tasks",
  "fieldErrors": {
    "title": "Title is required",
    "dueDateTime": "Due date must be in the future"
  }
}
```

## Task Status Values

- `PENDING`: Task is awaiting action. This is the default status for new tasks.
- `IN_PROGRESS`: Task is currently being worked on.
- `COMPLETED`: Task has been finished.

## Project Structure

- `src/main/java/uk/gov/hmcts/reform/dev/Application.java` - Spring Boot entry point
- `src/main/java/uk/gov/hmcts/reform/dev/config/WebConfig.java` - CORS configuration
- `src/main/java/uk/gov/hmcts/reform/dev/controllers/TaskController.java` - Task management endpoint
- `src/main/java/uk/gov/hmcts/reform/dev/dto/CreateTaskRequest.java` - Request DTO with validation
- `src/main/java/uk/gov/hmcts/reform/dev/dto/ErrorResponse.java` - Error response format
- `src/main/java/uk/gov/hmcts/reform/dev/exceptions/GlobalExceptionHandler.java` - Centralized error handling
- `src/main/java/uk/gov/hmcts/reform/dev/models/Task.java` - Task entity with TaskStatus enum
- `src/main/java/uk/gov/hmcts/reform/dev/repositories/TaskRepository.java` - Task data access
- `src/main/java/uk/gov/hmcts/reform/dev/services/TaskService.java` - Task business logic

## Configuration

### Server

The application runs on port 4000 by default.

### H2 Database Console

The H2 database console is enabled for development and accessible at http://localhost:4000/h2-console

- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: (leave empty)

### CORS

The API allows cross-origin requests from `https://localhost:3100` and `http://localhost:3100` (HMCTS frontend Express server). Allowed methods are POST and OPTIONS.

## Tests

### Unit Tests

Located in `src/test/java/`

**TaskServiceTest** (`src/test/java/uk/gov/hmcts/reform/dev/services/TaskServiceTest.java`)

Tests for the service layer business logic:

- `createTask_WithValidRequest_ReturnsSavedTask`: Verifies that a valid request creates a task and returns it with an ID, status set to PENDING, and all fields correctly populated.
- `createTask_MapsRequestFieldsToEntity`: Uses ArgumentCaptor to verify that request fields (title, description, dueDateTime) are correctly mapped to the Task entity before saving.
- `createTask_WithNullDescription_CreatesTaskSuccessfully`: Verifies that tasks can be created without a description since it is an optional field.

**TaskControllerTest** (`src/test/java/uk/gov/hmcts/reform/dev/controllers/TaskControllerTest.java`)

Tests for the REST controller HTTP handling and validation:

- `createTask_WithValidRequest_Returns201`: Verifies that a valid POST request returns HTTP 201 Created with the task object containing id, title, description, and status.
- `createTask_WithMissingTitle_Returns400`: Verifies that omitting the title field returns HTTP 400 Bad Request with a field error message "Title is required".
- `createTask_WithBlankTitle_Returns400`: Verifies that providing a blank title (whitespace only) returns HTTP 400 Bad Request, testing the @NotBlank validation.
- `createTask_WithMissingDueDate_Returns400`: Verifies that omitting the dueDateTime field returns HTTP 400 Bad Request with a field error message "Due date is required".
- `createTask_WithPastDueDate_Returns400`: Verifies that providing a past date returns HTTP 400 Bad Request with a field error message "Due date must be in the future".
- `createTask_WithOnlyRequiredFields_Returns201`: Verifies that a task can be created with only the required fields (title and dueDateTime), with description being null.
- `createTask_WithTitleExceedingMaxLength_Returns400`: Verifies that a title exceeding 255 characters returns HTTP 400 Bad Request with a field error message "Title must not exceed 255 characters".
- `createTask_WithDescriptionExceedingMaxLength_Returns400`: Verifies that a description exceeding 1000 characters returns HTTP 400 Bad Request with a field error message "Description must not exceed 1000 characters".

### Integration Tests

Located in `src/integrationTest/java/`

**GetWelcomeTest** (`src/integrationTest/java/uk/gov/hmcts/reform/dev/controllers/GetWelcomeTest.java`)

- `welcomeRootEndpoint`: Verifies that the root endpoint returns HTTP 200 OK with a response starting with "Welcome".

## Architecture

The application follows a layered architecture with separation of concerns:

- **Controller Layer**: Handles HTTP requests and responses, performs input validation using Jakarta Bean Validation annotations, and returns appropriate HTTP status codes.
- **Service Layer**: Contains business logic, manages transactions, and orchestrates data operations. Marked with @Transactional for database consistency.
- **Repository Layer**: Provides data access using Spring Data JPA. Extends JpaRepository for standard CRUD operations.
- **Entity Layer**: JPA entities mapped to database tables. The Task entity uses @PrePersist to automatically set createdAt timestamp and default status.

## Design Decisions

- **Service layer included**: Separates business logic from HTTP concerns and enables easier unit testing with mocked dependencies.
- **Separate request DTO**: Decouples the API contract from the persistence model. Allows validation rules specific to the API without affecting the entity.
- **Nested TaskStatus enum**: Keeps the status values tightly coupled with the Task entity. Provides type safety and prevents invalid status values.
- **@PrePersist for timestamps**: Ensures the createdAt field is set by the system rather than accepting it from API consumers. Provides reliable audit data.
- **H2 in-memory database**: Requires zero configuration for development. The application can be started immediately without external database setup.
- **GlobalExceptionHandler**: Provides consistent error response format across all endpoints. Centralizes error handling logic in one place.
