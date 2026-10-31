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
