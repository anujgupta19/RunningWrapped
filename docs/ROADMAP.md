# Roadmap

Each step is small (roughly one sitting), ends in something runnable or tested, and has a **learning goal**.
Run `/milestone` to pick up the next unchecked step. Later phases are deliberately coarser and get split up when we reach them.

Legend: 🧑‍💻 = good "your turn" candidate (you write it, Claude reviews)

## Phase 0: Repo setup
- [x] **0.1** CLAUDE.md files, skills, architecture, data model, roadmap

## Phase 1: Foundation

### Backend
- [x] **1.1 Spring Boot skeleton.** Generate via Spring Initializr (Maven, Java 21, Web, Actuator), Maven wrapper, run it, hit `/actuator/health`.
  *Learn:* `pom.xml` vs `go.mod`/`package.json`, starters, auto-configuration, `@SpringBootApplication`, the bean container.
- [x] **1.2 Postgres + config.** `compose.yaml`, `dev`/`prod` profiles, Spring Boot docker-compose support, Testcontainers for tests.
  *Learn:* externalized config, profiles, property precedence, ConnectionDetails. (`@ConfigurationProperties` records moved to 1.12, when we have our own config: JWT.)
- [ ] **1.3 Flyway + first migration.** `V1__create_app_user.sql`, `ddl-auto=validate`.
  *Learn:* how Flyway runs at startup, migration discipline.
- [ ] **1.4 User entity + repository + first test.** `AppUser` entity, `UserRepository`, `@DataJpaTest` with Testcontainers. 🧑‍💻 (repository test)
  *Learn:* JPA entities, persistence context / dirty checking, Spring Data derived queries, Testcontainers `@ServiceConnection`.
- [ ] **1.5 First endpoint.** `GET /api/v1/users/me` (temporary dev-user resolver), `UserResponse` record, `UserService`, controller, `@WebMvcTest`. 🧑‍💻 (DTO + mapping)
  *Learn:* DI with constructors, layer responsibilities, records, Jackson serialization, `@Transactional(readOnly = true)`.
- [ ] **1.6 Validation + error handling.** `PATCH /api/v1/users/me` with `@Valid` request record; global `@RestControllerAdvice` → `ProblemDetail`.
  *Learn:* Bean Validation, exception hierarchy, RFC 9457, why checked vs unchecked exceptions.
- [ ] **1.7 Activity + import schema.** Migrations + entities for `activity_import`, `activity`, `activity_best_effort`, with indexes. 🧑‍💻 (one migration)
  *Learn:* `@ManyToOne(LAZY)`, enums with `@Enumerated(STRING)`, partial unique indexes, why we avoid bidirectional mappings.
- [ ] **1.8 OpenAPI + logging + Clock.** springdoc, Swagger UI in dev, structured logging, `Clock` bean.
  *Learn:* how annotations become docs, logging config, testability via injected time.

### Mobile
- [ ] **1.9 Expo app skeleton.** `create-expo-app` (TS), strict tsconfig, `@/` alias, ESLint/Prettier, 5-tab Expo Router layout with placeholder screens.
  *Learn:* Expo Router file conventions, Expo Go vs dev builds.
- [ ] **1.10 Design system base.** NativeWind setup, dark-first tokens (colors, gradients, spacing, type), `Screen`, `Text`, `Card`, `StatTile` primitives.
- [ ] **1.11 Talk to the backend.** `openapi-typescript` codegen, typed fetch client, TanStack Query, Profile screen shows `/users/me`.
  *Learn:* simulator ↔ localhost networking, `EXPO_PUBLIC_*` env.

### Auth + CI
- [ ] **1.12 Backend auth.** Spring Security filter chain, register/login, BCrypt, JWT access tokens, refresh token rotation; replace dev-user resolver.
  *Learn:* the filter chain, `SecurityContext`, `@AuthenticationPrincipal`, resource-server JWT validation, testing secured endpoints.
- [ ] **1.13 Mobile auth.** Sign-in/up screens, secure-store, auth-gated routes, refresh-on-401.
- [ ] **1.14 CI.** GitHub Actions: `./mvnw verify` (with Testcontainers) + mobile typecheck/lint/test.

## Phase 2: Running data
- [ ] **2.1** Import endpoint (multipart) + `activity_import` lifecycle + `ActivityFileParser` interface. *Learn:* multipart, interface + `List<>` injection.
- [ ] **2.2** GPX parser → `ParsedActivity` (StAX/Jackson XML) + fixtures + tests 🧑‍💻
- [ ] **2.3** TCX parser 🧑‍💻
- [ ] **2.4** FIT parser (Garmin FIT SDK)
- [ ] **2.5** Duplicate detection (exact + fuzzy) with tests
- [ ] **2.6** Best-effort calculator (sliding window over time series) 🧑‍💻, which is a great pure-Java algorithm exercise
- [ ] **2.7** `GET /activities` (paginated) + `GET /activities/{id}`, user-scoped
- [ ] **2.8** Mobile: file picker import flow + import history in Profile
- [ ] **2.9** Mobile: Activities list (infinite scroll) + detail screen
- [ ] **2.10** Weekly/monthly analytics endpoints (native SQL, timezone-aware bucketing). *Learn:* native queries → record projections
- [ ] **2.11** Mobile: Home dashboard (stat tiles, week-over-week, weekly distance chart, W/M/Y toggle)

## Phase 3: Running progress
- [ ] **3.1** Personal records service + endpoint (best efforts, longest run, top week/month)
- [ ] **3.2** `/analytics/progress`: pace series, volume, consistency
- [ ] **3.3** Pace-at-HR comparison (HR bands × distance buckets, with sample sizes + caveats)
- [ ] **3.4** Yearly analytics
- [ ] **3.5** Mobile: Progress screen charts (Victory Native), accessible chart summaries

## Phase 4: Wrapped experience
- [ ] **4.1** Recap generation service (snapshot stats, idempotent upsert) + endpoints
- [ ] **4.2** "Running personality" + card-content builder (pure, well-tested)
- [ ] **4.3** Mobile: full-screen story player (swipe, progress bars, Reanimated transitions)
- [ ] **4.4** Mobile: Weekly / Monthly / Yearly card designs
- [ ] **4.5** Export card as image (view-shot, 9:16) + share
- [ ] **4.6** AI consent UI + `PATCH /users/me` consent fields
- [ ] **4.7** `InsightGenerator` (template impl + OpenAI impl), prompt, numeric guardrail, tests

## Phase 5: Production readiness
- [ ] **5.1** Scheduled recap jobs (`@Scheduled`, per-user timezone)
- [ ] **5.2** Push notifications (Expo push tokens, sending, anti-spam rules)
- [ ] **5.3** Hardening: rate limiting on auth/import, upload size limits, error-path tests
- [ ] **5.4** Dockerfile + Railway deploy (env config, Flyway on deploy, health checks)
- [ ] **5.5** Monitoring: Actuator metrics, Sentry backend + mobile
- [ ] **5.6** EAS build → TestFlight
