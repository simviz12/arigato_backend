# Restaurant Inventory System

This is a backend application built with Spring Boot 3 and Java 21, enforcing **Clean Architecture** principles.

## Module Responsibilities

1. **`domain`**: The core of the application. Contains domain entities, value objects, and repository interfaces. **It has ZERO dependencies on Spring or any other framework.**
2. **`application`**: Contains the business logic (Use Cases / Interactors). It orchestrates the domain layer and depends *only* on the `domain` module.
3. **`infrastructure`**: Implementation details. Contains JPA repositories (implementing `domain` interfaces), database configurations (PostgreSQL, Flyway), and security configurations. Depends on `domain` and `application`.
4. **`api`**: The presentation layer. Contains REST controllers, DTOs, exception handlers, and MapStruct mappers. Depends on `application` (compile-time) and `infrastructure` (runtime for dependency injection).

## How to run locally

### Prerequisites
- Java 21
- Maven
- PostgreSQL instance running

### Environment Variables
You need to provide the following environment variables (or configure them in your IDE):
- `DB_HOST`: Database host (e.g., localhost)
- `DB_PORT`: Database port (e.g., 5432)
- `DB_NAME`: Database name (e.g., restaurant_db)
- `DB_USER`: Database user
- `DB_PASSWORD`: Database password

### Running the App
1. Build the project:
   ```bash
   mvn clean install
   ```
2. Run the Spring Boot application (from the `api` module):
   ```bash
   mvn spring-boot:run -pl api
   ```

### Health Check
Once running, you can verify the application is up by hitting the health check endpoint:
```
GET http://localhost:8080/api/health
```
