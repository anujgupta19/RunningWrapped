# Java & Spring for a Go / TypeScript developer

A quick translation table to read alongside the code. Concepts get a deeper write-up in `LEARNING_LOG.md` as we hit them.

## Language

| You know | Java equivalent | Notes |
|---|---|---|
| Go struct / TS `type` for data | `record UserResponse(UUID id, String email) {}` | Immutable, auto `equals/hashCode/toString`. Use for DTOs and value objects |
| Go exported vs unexported (`Foo`/`foo`) | `public` vs *package-private* (no modifier) | We default to package-private to enforce module boundaries |
| Go interface (implicit) | `interface` (explicit `implements`) | Only create one when there are multiple impls or an external boundary |
| `error` return values | unchecked exceptions (`RuntimeException`) | Spring maps them to HTTP responses centrally. Checked exceptions are rarely used in Spring apps |
| `T \| undefined`, `nil` | `Optional<T>` (return values only) | Fields/params can be null but we avoid it |
| TS union / Go sum-type hacks | `sealed interface` + `record`s + `switch` pattern matching | Exhaustive switches like TS discriminated unions |
| `const` | `final` | `final` fields + constructor = immutable object |
| `time.Time` / `Date` | `Instant` (UTC point), `LocalDate`, `ZonedDateTime`, `ZoneId` | Never `java.util.Date` |
| goroutines | virtual threads (Java 21) | Spring Boot can run request handling on virtual threads with one property |
| `go.mod` / `package.json` | `pom.xml` (Maven) | `./mvnw` = pinned Maven, like a lockfile + `npx` |
| `go test` / Jest | JUnit 5 + AssertJ (`assertThat(x).isEqualTo(y)`) | Mockito ≈ jest.fn() mocks |

## Spring

| You know | Spring equivalent | Notes |
|---|---|---|
| Wiring deps by hand in `main.go` | **Bean container / dependency injection** | Spring constructs `@Service`, `@Repository`, `@RestController` classes and passes deps into constructors |
| Express/chi router + handlers | `@RestController` + `@GetMapping` | Return a record → Jackson serializes to JSON |
| Middleware | Servlet filters / Spring Security filter chain | Auth happens in filters before controllers |
| zod / validator | Bean Validation (`@NotBlank`, `@Email`) + `@Valid` | Failures become 400 ProblemDetail via our advice |
| Express error middleware | `@RestControllerAdvice` + `@ExceptionHandler` | |
| `pgx`/`sqlc` / Prisma | Spring Data JPA (Hibernate ORM) | Repositories are interfaces. Spring generates the implementation from method names |
| DB transaction `tx := db.Begin()` | `@Transactional` on a service method | Proxy opens/commits/rolls back around the call. Only works on calls from *outside* the bean |
| golang-migrate / Prisma migrate | Flyway | Runs automatically at app startup |
| `.env` + `process.env` | `application.yaml` + profiles + env vars | Env vars override yml (`DB_URL` → `spring.datasource.url` via `${}`) |
| cron | `@Scheduled` | |

## Gotchas that bite newcomers
- **`@Transactional` self-invocation**: calling a `@Transactional` method from another method *in the same class* skips the proxy, so no transaction.
- **LazyInitializationException**: touching a lazy relation after the transaction closed. Load what you need in the service.
- **Dirty checking**: modifying a managed entity inside a transaction is saved automatically at commit, with no `save()` call needed.
- **Entities are not DTOs**: never return an entity from a controller (lazy proxies, leaking fields, coupling API to schema).
- `equals/hashCode` on entities: don't use Lombok `@Data`. Base it on id carefully or leave it as the default identity.
