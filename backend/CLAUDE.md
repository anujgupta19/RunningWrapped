# Backend: Java 21 + Spring Boot

Learning context: the developer knows Go/Node well and is learning Java/Spring. Explain *why* for Java-specific choices.

## Commands

```bash
./mvnw spring-boot:run          # run locally on :8081 (dev profile; starts Postgres from ../compose.yaml on :5433)
./mvnw test                     # all tests (Testcontainers starts its own Postgres, needs Docker)
./mvnw verify                   # full build incl. integration tests (needs Docker for Testcontainers)
./mvnw spotless:apply           # format (once Spotless is added)
```
Always use the Maven wrapper (`./mvnw`), not a global `mvn`. Maven isn't installed globally on this machine.

## Stack

Java 21 · Spring Boot (latest stable, pinned in `pom.xml`) · Spring Web MVC · Spring Data JPA/Hibernate · Spring Security
(stateless JWT) · PostgreSQL · Flyway · springdoc-openapi · JUnit 5 + AssertJ + Mockito · Testcontainers.
Before adding a dependency, check that it's compatible with the pinned Boot version and say why it's needed.

## Package structure: package by feature

```
com.runningwrapped
  RunningWrappedApplication.java
  auth/  users/  activities/  imports/  integrations/  analytics/  recaps/  ai/  notifications/
  common/   (cross-cutting only: error handling, pagination DTO, config, security config, time utils)
```
- Each feature package holds its own controller, service, repository, entity, DTOs, and exceptions. Keep it **flat**
  (no `controller/ service/ repository/` subfolders) until a package grows past ~15 files.
- **Default to package-private** (no modifier) for classes and methods. Only make `public` what other features need,
  usually a service's API and its DTOs. This is how we keep module boundaries in a monolith.
- Features talk to each other through the other feature's **public service**, never its repository or entity.
- `common` must not depend on feature packages.

## Idiomatic Java rules for this repo

- **Constructor injection only.** Use `private final` fields and a single constructor, with no `@Autowired` (it isn't needed when there's one constructor).
  No field injection.
- **Records** for DTOs (requests/responses), value objects, query projections, and `@ConfigurationProperties`.
  **Not** for JPA entities (Hibernate needs a no-arg constructor and mutable, proxyable classes).
- **No Lombok.** Records cover most of the boilerplate, and plain Java is easier to learn from.
- **Interfaces only when they earn it:** there are several real implementations (e.g. `ActivityFileParser` for FIT/GPX/TCX),
  or it's a boundary to an external system you want to fake in tests (e.g. `InsightGenerator` for the LLM).
  No `FooService` + `FooServiceImpl` pairs. Spring Data repositories are interfaces only because Spring generates the impl.
- `Optional` is only for return values that might be absent (e.g. repository finders). Never use it for fields or parameters. Don't return `null` from public methods.
- Time: `java.time` only (`Instant`, `ZoneId`, `LocalDate`). Never `Date`/`Calendar`. Inject a `Clock` bean where "now" matters, so tests are deterministic.
- Durations/distances: use `long`/`int` seconds and `double` meters in the domain. Format pace/units at the edges.
- Use `var` for locals when the type is obvious from the right-hand side.
- Prefer `switch` expressions, `sealed` types, and pattern matching where they make the code clearer. Don't use them to show off.
- Exceptions: unchecked domain exceptions (`ActivityNotFoundException extends RuntimeException`). Don't catch-and-rethrow without adding info.

## Layers

- **Controller** (`@RestController`): thin. HTTP mapping, `@Valid` request records, and it calls one service method and returns a DTO.
  No business logic and no repositories. Return the DTO directly. Use `ResponseEntity` only when status or headers vary.
- **Service** (`@Service`): business logic and transaction boundaries. Put `@Transactional` here (`readOnly = true` for queries),
  never on controllers. Services map entities to DTOs before returning.
- **Repository** (`interface … extends JpaRepository<E, UUID>`): derived queries for simple cases, `@Query` JPQL for joins, and
  native SQL for analytics aggregations (`date_trunc` with time zones etc.). Map results to record projections.
- **Entity** (`@Entity`): a plain class with a `protected` no-arg constructor, a meaningful public constructor, getters, and
  intention-revealing methods instead of blanket setters. `@ManyToOne(fetch = LAZY)`. Avoid bidirectional associations unless needed.

## Persistence

- **Flyway owns the schema.** Migrations are `src/main/resources/db/migration/V{n}__{description}.sql`. Never edit a migration that has
  been applied anywhere. Add a new one. `spring.jpa.hibernate.ddl-auto=validate`.
- `spring.jpa.open-in-view=false`. Load what you need inside the service transaction.
- Primary keys: `UUID`. Timestamps: `timestamptz` ↔ `Instant`. Flexible snapshots (recap stats): `jsonb`.
- Watch for N+1 queries. Use fetch joins or projections. Enable SQL logging in dev when investigating.
- **Every query on user data is scoped by the authenticated user's id** (`findByIdAndUserId`), to prevent IDOR.

## API

- `/api/v1/...`, plural nouns, standard verbs/status codes (201 + `Location` on create, 204 on empty success).
- Pagination: `page`/`size` query params with a capped `size`. Return our own `PageResponse<T>` record, not a serialized Spring `Page`.
- Errors: a single `@RestControllerAdvice` in `common` returning `ProblemDetail` (RFC 9457) for validation, not-found,
  conflict, auth and unexpected errors. Never leak stack traces or internal messages.
- Annotate controllers enough for springdoc to produce a useful OpenAPI spec. Mobile generates types from it.

## Config & security

- `application.yaml` + profiles `dev`, `test`, `prod`. Secrets come from env vars only (`${DB_PASSWORD}`, `${JWT_SECRET}`, `${OPENAI_API_KEY}`).
- Bind config to `@ConfigurationProperties` records instead of scattering `@Value`.
- Stateless security with short-lived JWT access tokens and rotating refresh tokens (hashed in the DB). Passwords use
  `PasswordEncoderFactories.createDelegatingPasswordEncoder()`.
- Logging: SLF4J with parameterized messages (`log.info("Imported {} activities", n)`). Use structured JSON logs in prod.
  **Never log** tokens, passwords, emails, or workout/health values.

## Testing

- **Unit tests** (plain JUnit + AssertJ) for pure logic such as parsers, best-effort calculation, and analytics math. These are the most valuable tests.
- **Service tests** with Mockito only for awkward collaborators (external APIs, clock). Prefer real objects and fakes over deep mocking.
- **`@WebMvcTest`** for controllers (status codes, validation, error shape).
- **`@DataJpaTest` + Testcontainers Postgres** for repositories and native queries. Don't use H2, since we rely on Postgres features.
- **`@SpringBootTest`** sparingly, for a few end-to-end happy paths per feature.
- Test names describe behavior: `importsGpxFileAndSkipsDuplicate()`. Put test fixtures (sample .gpx/.tcx/.fit) in `src/test/resources/fixtures/`.
