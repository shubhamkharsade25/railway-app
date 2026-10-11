# Railway Management System — Agent Rules

## Stack
- Java 21 (project targets 21 compatibility, running on JDK 25), Spring Boot 3.x, Maven
- Package root: `com.railway`
- Database: MySQL 8 via `JdbcTemplate` / `NamedParameterJdbcTemplate` with hand-written SQL in repository classes
- Frontend: React + Vite

## Hard Rules
- Database access ONLY through `JdbcTemplate` / `NamedParameterJdbcTemplate` with hand-written SQL
- NEVER add `spring-boot-starter-data-jpa`, Hibernate, or MyBatis
- Every SQL statement uses `?` / named parameters — NEVER concatenate user input into SQL
- Layering: Controller → Service → Repository. No SQL in controllers
- `@Transactional` annotations ONLY in Service classes
- Use DTOs (Java `record`s) for requests/responses
- Validate with Bean Validation (`@Valid`, `@NotBlank`, `@Size`, etc.)

## Error Handling
- All errors go through one `@RestControllerAdvice` returning JSON: `{timestamp, status, error, message, path}`

## Security
- DB credentials come from environment variables `DB_USERNAME`, `DB_PASSWORD` — never hard-code or commit them
- No authentication/authorization
- Payments are SIMULATED (`SUCCESS/FAILED/PENDING`); never claim real payment processing

## Data
- Initial data is SAMPLE DATA and must be labelled as such in comments and README
- `spring.sql.init.mode=never` — SQL scripts are run manually, never on startup

## Testing
- Every new endpoint gets a test (JUnit/MockMvc or documented Postman request)
- Use `MockMvc` with `@WebMvcTest` or `@SpringBootTest`

## Frontend
- Origin: `http://localhost:5173`
- CORS configured for `/api/**`
