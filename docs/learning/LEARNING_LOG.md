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
