# Learning log

One entry per completed milestone: the concepts it introduced, in a few bullets, plus links to the code that shows them.
Written so you can reread it in six months and remember *why*.

<!-- Template:
## 1.x: Title (YYYY-MM-DD)
**Concepts:** ...
**Where to look:** `backend/src/main/java/...`
**Gotcha / aha:** ...
**Further reading:** ...
-->

## 1.1: Spring Boot skeleton (2026-10-31)
**Concepts:**
- **Maven `pom.xml`** ≈ `go.mod` + `package.json` scripts. The `spring-boot-starter-parent` parent pins compatible versions of
  ~hundreds of libraries (a "BOM"), so our `<dependency>` entries have no `<version>`.
- **Starters** (`spring-boot-starter-webmvc`, `-actuator`) are curated dependency bundles. Adding one pulls in the libs *and*
  triggers matching **auto-configuration** (e.g. "Tomcat is on the classpath → start an embedded web server on 8080").
- **`@SpringBootApplication`** = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. Component scanning only looks in
  `com.runningwrapped` and its sub-packages, which is why the main class sits at the root package.
- **`SpringApplication.run`** builds the *application context* (the bean container), roughly the `main.go` where you'd construct
  and wire everything by hand, except Spring does it by reflection.
- **Maven wrapper** (`./mvnw` + `.mvn/wrapper/maven-wrapper.properties`) pins the Maven version per repo, like `corepack`/`.nvmrc` but enforced.
- **Actuator** gives production endpoints for free. `/actuator/health` is what Railway/Docker health checks will hit later.

**Where to look:** `backend/pom.xml`, `backend/src/main/java/com/runningwrapped/RunningWrappedApplication.java`

**Gotcha / aha:**
- Boot 4 splits starters by technology (`spring-boot-starter-webmvc` rather than the old `-web`) and has matching test starters
  (`-webmvc-test`). Older tutorials will show Boot 3 names.
- Port 8080 was taken by a local Docker container (traefik). `--server.port=8081` on the command line overrode the default
  with no code change. That's Spring's externalized config precedence at work (more in 1.2).

**Further reading:** https://docs.spring.io/spring-boot/reference/using/auto-configuration.html

## 1.2: Postgres + config profiles (2026-10-31)
**Concepts:**
- **Externalized config.** One app binary, config from layered sources. Higher in this list wins:
  command-line args > env vars (`SPRING_DATASOURCE_URL`, via relaxed binding) > `application-{profile}.yaml` > `application.yaml`.
  It's like viper/dotenv layering in Go, but built in.
- **Profiles.** `application-dev.yaml` and `application-prod.yaml` only load when that profile is active. Activate with
  `--spring.profiles.active=prod` or `SPRING_PROFILES_ACTIVE=prod`. The Maven plugin sets `dev` for `spring-boot:run`.
- **`${DB_URL}` placeholders** in yaml are resolved from env vars. Without a default (`${PORT:8080}` has one), startup fails fast.
- **ConnectionDetails.** Spring Boot's docker-compose support (dev) and Testcontainers `@ServiceConnection` (tests) both
  produce a `JdbcConnectionDetails` bean that *beats* `spring.datasource.*`. That's why dev and test configs have no URL/password.
- **Dependency scopes:** `runtime` (needed to run, not to compile, e.g. the JDBC driver, since we code against `javax.sql.DataSource`),
  `test` (test classpath only), `optional` (not passed on, and the Boot plugin leaves docker-compose out of the prod jar).
- **`@TestConfiguration` + `@Import`** adds test-only beans (the Postgres container) to the test's application context.

**Where to look:** `compose.yaml`, `backend/src/main/resources/application*.yaml`, `backend/src/test/java/com/runningwrapped/TestcontainersConfiguration.java`

**Gotcha / aha:** Adding `spring-boot-starter-jdbc` alone breaks `contextLoads()`, because auto-config now *requires* a DataSource.
Classpath presence turns features on, so tests needed a database too. Ports 8080 and 5432 were taken locally, so we use 8081 and 5433.

**Further reading:** https://docs.spring.io/spring-boot/reference/features/external-config.html

## 1.3: Flyway + first migration (2026-10-31)
**Concepts:**
- **Flyway runs at app startup**, before the app accepts traffic. Auto-config sees Flyway + a DataSource on the classpath,
  scans `classpath:db/migration`, and applies pending `V{n}__{description}.sql` files in version order. Like golang-migrate's
  `m.Up()` in `main()`, except it's automatic.
- **`flyway_schema_history`** records each applied version + checksum. **Editing an applied migration changes its checksum
  and startup fails** (`validate` step). Fix forward with a new `V{n+1}` migration.
- **Tests run the real migrations** against Testcontainers Postgres, so the schema under test is exactly what prod gets.
- **`@Transactional` on a test class** rolls back each test method's changes. (In app code it commits. Tests flip the default.)
- **Spring context caching:** test classes with the same config share one application context (and one container),
  which is why the second test class started in ~6 ms.
- **`DataIntegrityViolationException`:** Spring translates driver-specific SQL errors (SQLState 23505 etc.) into a
  portable exception hierarchy. We'll map it to a 409 Conflict later.

**Where to look:** `backend/src/main/resources/db/migration/V1__create_app_user.sql`, `backend/src/test/java/com/runningwrapped/DatabaseMigrationTests.java`

**Gotcha / aha:** Field injection (`@Autowired JdbcTemplate jdbc`) is fine *in tests*, since JUnit creates test instances, not Spring.
App code still uses constructor injection only. `user` is reserved in Postgres, hence `app_user`.
Inspect the dev DB with `docker compose exec postgres psql -U runningwrapped`.

**Further reading:** https://documentation.red-gate.com/flyway/flyway-concepts/migrations

## 1.4: AppUser entity + UserRepository (2026-10-31)
**Concepts:**
- **Entity anatomy:** `@Entity` class, `protected` no-arg constructor (for Hibernate's reflection), a real constructor for
  required fields, getters, no blanket setters. Field names map to snake_case columns automatically.
- **`ddl-auto: validate`:** Hibernate checks every entity against the Flyway-created schema at startup and refuses to boot on mismatch.
- **Java defaults must mirror SQL defaults:** Hibernate's INSERT lists every column, so `default 'MILES'` in SQL never fires.
- **`@Enumerated(EnumType.STRING)`** always. The default (ORDINAL) stores 0/1 and silently corrupts data if you reorder the enum.
- **Spring Data repositories:** an interface → Spring generates the implementation at startup (`save`, `findById`, ...).
  Derived queries come from method names. `@Query` (JPQL: class/field names, not tables) when you need control.
- **Persistence context (first-level cache):** within a transaction Hibernate tracks loaded entities and **delays writes until flush**.
  `save()` queues an INSERT. `saveAndFlush()`/commit sends it. `entityManager.clear()` forces the next read to hit the DB.
- **`@DataJpaTest`** = a "slice": only JPA + Flyway + DataSource. Fast, each test rolled back.
- **Package-private entity + repository** keep `users` internals invisible to other features (enforced by the compiler).

**Where to look:** `backend/src/main/java/com/runningwrapped/users/`, `backend/src/test/java/com/runningwrapped/users/UserRepositoryTests.java`

**Gotcha / aha:**
- Swapping `saveAndFlush` for `save` in `rejectsDuplicateEmailDifferingOnlyInCase` makes it fail ("Expecting code to raise a
  throwable"): the duplicate INSERT never reached Postgres because the rolled-back test never flushed.
- Derived `findByEmailIgnoreCase` generates `upper(email) = upper(?)`, which can't use our `lower(email)` index, hence the hand-written `@Query`.

**Further reading:** https://vladmihalcea.com/a-beginners-guide-to-jpa-hibernate-flush-strategies/

## 1.5: First endpoint, GET /api/v1/users/me (2026-10-31)
**Concepts:**
- **Constructor injection:** `UserController(UserService)` → `UserService(UserRepository)`. Spring builds the graph at startup by
  reading constructors, the same thing you'd write by hand in `main.go`. One constructor means no `@Autowired` needed. `final` fields.
- **Stereotypes:** `@RestController`, `@Service`, `@Component` mark classes for component scanning → they become singleton *beans*.
- **Layers:** controller (HTTP only) → service (transaction + mapping) → repository. The entity never leaves the service. The
  controller gets a `UserResponse` record.
- **Records as DTOs** + static `from(entity)` factory. Jackson serializes record components directly. Boot 4 uses Jackson 3,
  which writes `Instant` as ISO-8601 strings by default.
- **`@Transactional(readOnly = true)`:** Spring wraps the bean in a proxy that opens/commits the transaction around the call.
  Verified via `JpaTransactionManager` DEBUG logs that it applies to our *package-private* method (Spring 6+ supports this for class proxies).
- **`@ResponseStatus` on an exception** maps it to an HTTP status (temporary, replaced by central handling in 1.6).
- **`@Profile("dev")` + `ApplicationRunner`:** a bean that exists only in dev and runs once after startup (the dev-user seeder).
- **`@WebMvcTest` + `@MockitoBean` + `MockMvcTester`:** web slice with no DB. The service is replaced by a Mockito mock
  (`given(...).willReturn(...)` ≈ `jest.fn().mockReturnValue`), and the AssertJ-style MockMvc asserts on status + JSON paths.

**Where to look:** `backend/src/main/java/com/runningwrapped/users/{UserController,UserService,UserResponse,DevUserSeeder}.java`,
`backend/src/test/java/com/runningwrapped/users/UserControllerTests.java`

**Gotcha / aha:**
- Missing or malformed `X-User-Id` → 400 automatically: Spring converts header strings to `UUID` and rejects bad input before our code runs.
- Error bodies are still Boot's default `{timestamp,status,error,path}` shape. Step 1.6 switches to RFC 9457 ProblemDetail.
- Mockito now loads as a `-javaagent` via Surefire (pom.xml), because self-attaching agents are being phased out of the JDK.

**Further reading:** https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
